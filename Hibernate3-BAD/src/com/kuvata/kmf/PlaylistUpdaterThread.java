/*
 * Created on Jan 18, 2005
 */
package com.kuvata.kmf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;

import parkmedia.usertype.PlaylistOrderType;

import com.kuvata.kmf.permissions.ActionType;
import com.kuvata.kmf.permissions.FilterManager;
import com.kuvata.kmf.usertype.PresentationStyle;
import com.kuvata.kmm.actions.DynamicQueryAction;

/**
 * This class is intended to be used to update playlist assets
 * in it's own thread.
 * 
 * @author jrandesi
 */
public class PlaylistUpdaterThread extends Thread 
{
	private String schemaName;
	private Long playlistId;
	private String playlistType;
	private String query;
	private Integer maxResults;
	private String presentationStyleMethod;
	private Date dateToUse;
	
	public static boolean childrenUpdated = false;
	
	/**
	 * 
	 * @param schemaName
	 * @param playlistId
	 * @param playlistType
	 * @param query
	 * @param maxResults
	 */
	public PlaylistUpdaterThread(String schemaName, Long playlistId, String playlistType, String query, Integer maxResults)
	{
		super("PlaylistUpdaterThread");
		this.schemaName = schemaName;
		this.playlistId = playlistId;
		this.query = query;
		this.maxResults = maxResults;
		this.playlistType = playlistType;
	}
	/**
	 * 
	 * @param schemaName
	 * @param playlistId
	 * @param presentationStyleMethod
	 */
	public PlaylistUpdaterThread(String schemaName, Long playlistId, String presentationStyleMethod)
	{
		super("PlaylistUpdaterThread");
		this.schemaName = schemaName;
		this.playlistId = playlistId;
		this.presentationStyleMethod = presentationStyleMethod;
	}
	
	/**
	 * 
	 * @param schemaName
	 * @param playlistId
	 * @param playlistType
	 * @param ad
	 * @param attrValue
	 * @param maxResults
	 */
	public PlaylistUpdaterThread(String schemaName, Long playlistId, String playlistType, Integer maxResults, Date dateToUse)
	{
		super("PlaylistUpdaterThread");
		this.schemaName = schemaName;
		this.playlistId = playlistId;
		this.maxResults = maxResults;
		this.playlistType = playlistType;			
	}
	
	/**
	 * 
	 */
	public void run()
	{		
		// Pass in a dummy appUserId so that a KmfSession is created (which will be used to filter queries)
		SchemaDirectory.initialize( this.schemaName, "PlaylistUpdaterThread", "Content Scheduler", true, false );
		try
		{			
			Playlist playlist = Playlist.getPlaylist( playlistId );
			
			// Add each roles associated with this playlist to the collection of appUserViewableRoleIds
			// Now the subsequent queries should reflect the permissions associated with the playlist 
			// (as opposed to the permissions associated with the currently logged in user)
			KmfSession kmfSession = KmfSession.getKmfSession();
			kmfSession.setAdmin( true );
			List<Role> roles = playlist.getRoles(false);
			for( Role role : roles ){
				kmfSession.getAppUserViewableRoleIds().add( role.getRoleId() );
			}
			
			// If we need to apply permissions
			if(playlist.getUseRoles() == Boolean.TRUE){
				FilterManager.enableFilters( ActionType.DYNAMIC_QUERY );
			}
			
			// If a playlistType was passed in
			if( playlistType != null )
			{
				if( playlistType.equals("HQL") )
				{
					updatePlaylistWithHQL( playlist );
				}
				else if( playlistType.equals("CustomMethod") )
				{
					updatePlaylistWithCustomMethod( playlist );
				}
				else if( playlistType.equals("Metadata") )
				{
					updatePlaylistWithMetadata( playlist );
				}
				else if( playlistType.equals("PlayX") )
				{
					updatePlaylistWithContentRotations( playlist, dateToUse );
				}
			}
			else
			{
				updatePlaylistWithPresentationStyle( playlist );
			}			
			
			// Disable the filter so to not unintentionally affect subsequent processing
			if(playlist.getUseRoles() == Boolean.TRUE){
				FilterManager.disableFilters( ActionType.DYNAMIC_QUERY );
			}
		}
		catch(Exception e)
		{			
			e.printStackTrace();
		}
		finally
		{
			try
			{
				HibernateSession.closeSession();
			}
			catch(HibernateException he)
			{
				he.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @throws Exception
	 */
	private void updatePlaylistWithHQL(Playlist p) throws Exception
	{
		p.deletePlaylistAssets();
		
		// Add all assets returned by the hql to this playlist					
		p.setCustomMethod("");
		p.setHql( query );			
		p.update();		
		
		Session session = HibernateSession.currentSession();
		Query q = session.createQuery(DynamicQueryAction.getHqlForCurrentAssets(query));
		p.addAssetsToPlaylist( q, maxResults );
		
		q = session.createQuery( DynamicQueryAction.getHqlForFutureAssets(query));
		p.addAssetsToPlaylist( q, null );
		
		// Re-associate this playlist object with the current session since we potentially have a new session 
		// after calling addAssetsToPlaylist() (for performance reasons we are flushing and clearing the session)
		p = Playlist.getPlaylist( playlistId );
		p.update();
				
		// Update this playlist's length
		p.updateLength();
	}
	
	private void updatePlaylistWithContentRotations(Playlist p, Date dateToUse) throws Exception
	{
		p.deletePlaylistAssets();
		
		// If we are meant to update the children
		if(childrenUpdated == false){
			// Get all underlying content rotations first
			HashSet<ContentRotation> contentRotationsToUpdate = new HashSet<ContentRotation>();
			for(ContentRotation cr : DynamicContentPart.getRecursiveContentRotationsToUpdate(p)){
				contentRotationsToUpdate.add(cr);
			}
			
			// Update all underlying content rotations
			for(ContentRotation cr : contentRotationsToUpdate){
				// Since each update applies its own permissions and then the cache is cleared, we need to lock this to the current session.
				HibernateSession.currentSession().lock(cr, LockMode.NONE);
				cr.updateContentRotation(null, dateToUse, false);
			}
			
			// Re-attach this playlist to this session
			HibernateSession.clearCache();
			HibernateSession.currentSession().lock(p, LockMode.NONE);
		}
		
		// Add assets from content rotation parts
		HashMap<Long, Iterator> iterators = new HashMap<Long, Iterator>();
		
		// Do this in bulk mode since we might be creating hundreds of playlist assets.
		HibernateSession.startBulkmode();
		
		int numResults = 0;
		float length = 0f;
		
		// Get the list of dynamic content parts
		List<DynamicContentPart> dcps = DynamicContentPart.getDynamicContentParts(p);
		
		// Generate content for a whole day
		Date now = new Date();
		boolean usePlaylistDisplayProperties = p.getDynamicContentDisplay() != null && p.getDynamicContentDisplay().equals("default") == false;
		while(length < Constants.MILLISECONDS_IN_A_DAY && (this.maxResults == null || numResults < this.maxResults)){
			
			// For each content rotation
			for(DynamicContentPart dcp : dcps){
				ContentRotation cr = dcp.getContentRotation();
				
				// Make sure that this content rotation has assets
				if(cr.getContentRotationAssets().size() > 0 && (this.maxResults == null || numResults < this.maxResults)){
					Iterator i;
					if(iterators.containsKey(cr.getContentRotationId())){
						i =  iterators.get(cr.getContentRotationId());
					}else{
						// Randomize if needed
						List crAssets = new LinkedList(cr.getContentRotationAssets());
						if(cr.getContentRotationOrder() != null && cr.getContentRotationOrder().equals(PlaylistOrderType.RANDOM)){
							Collections.shuffle(crAssets);
						}
						cr.setMyContentRotationAssets(crAssets);
						i = cr.getMyContentRotationAssets().iterator();
					}
					
					// Get numX assets
					int numAssetsSkipped = 0;
					for(int j=0; j<cr.getMyContentRotationAssets().size() && j-numAssetsSkipped<p.getNumAssets() && length < Constants.MILLISECONDS_IN_A_DAY && (this.maxResults == null || numResults < this.maxResults); j++){
						// Get next asset
						if(i.hasNext() == false){
							if(numAssetsSkipped == cr.getContentRotationAssets().size()){
								break;
							}else{
								i = cr.getMyContentRotationAssets().iterator();
								numAssetsSkipped = 0;
							}
						}
						ContentRotationAsset cra = (ContentRotationAsset)i.next();
						Asset a = Asset.convert(cra.getAsset());
						
						// Check to see if this is a current asset
						if( (a.getStartDate() == null || a.getStartDate().before(now)) && (a.getEndDate() == null || a.getEndDate().after(now)) ){
							
							Layout layout = usePlaylistDisplayProperties ? (p.getDynamicContentDisplay().equals("selected") ? p.getDynamicContentLayout() : (dcp.getLayout() != null ? dcp.getLayout() : a.getAssetPresentation().getLayout())) : a.getAssetPresentation().getLayout();
							Displayarea da = usePlaylistDisplayProperties ? (p.getDynamicContentDisplay().equals("selected") ? p.getDynamicContentDisplayarea() : (dcp.getDisplayarea() != null ? dcp.getDisplayarea() : a.getAssetPresentation().getDisplayarea())) : a.getAssetPresentation().getDisplayarea();
							PlaylistAsset pa = PlaylistAsset.create( p, a, cra.getLength().toString(), layout, da , cra.getVariableLength(), false, false, false, false);
							
							// Create pairings if needed
							for( Iterator k=cra.getAsset().getAssetPresentation().getPairedDisplayareas().iterator(); k.hasNext(); )
							{
								PairedDisplayarea pda = (PairedDisplayarea)k.next();
								if( pa.getAssetPresentation().getLayout().containsDisplayarea( pda.getDisplayarea() ) ){
									PairedDisplayarea newPda = PairedDisplayarea.create(pa.getAssetPresentation(), pda.getDisplayarea());
									
									for(PairedAsset pairedAsset : pda.getOrderedPairedAssets()){
										PairedAsset.create(newPda, Asset.convert(pairedAsset.getAsset()), pairedAsset.getLength().toString());
									}
								}
							}
							
							numResults++;
							length += cra.getLength() * 1000f;
						}else{
							numAssetsSkipped++;
						}
					}
					
					// Update iterator map
					iterators.put(cr.getContentRotationId(), i);
				}
			}
			
			// Break out of the loop if there are no results after an iteration thru all sub parts.
			if(numResults == 0){
				break;
			}
		}
		
		// Pull in future assets
		// For each content rotation
		for(DynamicContentPart dcp : dcps){
			ContentRotation cr = dcp.getContentRotation();
			
			// Make sure that this content rotation has assets
			if(cr.getContentRotationAssets().size() > 0){
				for(Iterator i = cr.getMyContentRotationAssets().iterator(); i.hasNext();){
					ContentRotationAsset cra = (ContentRotationAsset)i.next();
					Asset a = Asset.convert(cra.getAsset());
					
					// Check to see if this is a future asset
					if((a.getStartDate() != null && a.getStartDate().after(now))){
						Layout layout = usePlaylistDisplayProperties ? (p.getDynamicContentDisplay().equals("selected") ? p.getDynamicContentLayout() : (dcp.getLayout() != null ? dcp.getLayout() : a.getAssetPresentation().getLayout())) : a.getAssetPresentation().getLayout();
						Displayarea da = usePlaylistDisplayProperties ? (p.getDynamicContentDisplay().equals("selected") ? p.getDynamicContentDisplayarea() : (dcp.getDisplayarea() != null ? dcp.getDisplayarea() : a.getAssetPresentation().getDisplayarea())) : a.getAssetPresentation().getDisplayarea();
						PlaylistAsset pa = PlaylistAsset.create( p, a, cra.getLength().toString(), layout, da , cra.getVariableLength(), false, false, false, false);
						p.getPlaylistAssets().add(pa);
						length += cra.getLength() * 1000f;
					}
				}
			}
		}
		
		// Stop bulk mode
		HibernateSession.stopBulkmode();
		
		// Re-associate this playlist object with the current session since we potentially have a new session 
		// after calling addAssetsToPlaylist() (for performance reasons we are flushing and clearing the session)
		p = Playlist.getPlaylist( playlistId );
		p.setAvgLoopLength(DynamicContentPart.calculateAvgLoopLength(dcps, p.getNumAssets()).intValue());
		p.update();
		
		// Reset playlist display areas
		p.resetPlaylistDisplayareas();
				
		// Update this playlist's length
		p.updateLength();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void updatePlaylistWithCustomMethod(Playlist p) throws Exception
	{
		p.deletePlaylistAssets();
		
		// Parse the class name from the customMethod
		String className = query.substring(0, query.lastIndexOf("."));
		String methodName = query.substring(query.lastIndexOf(".") + 1);		
		try
		{
			// Invoke the static method on the given class
			Class c = Class.forName( className );
			Class[] params = { Playlist.class, Integer.class };		
			Method m = c.getDeclaredMethod( methodName, params );
			Object[] methodParams = { p, maxResults };
			m.invoke( null, methodParams );
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		p.setHql("");
		p.setCustomMethod(query);		
		p.update();		
		
		// Update this playlist's length
		p.updateLength();
	}
	/**
	 * 
	 * @param p
	 * @throws Exception
	 */
	private void updatePlaylistWithMetadata(Playlist p) throws Exception
	{
		// Delete all playlist assets that are currently associated with this playlist
		p.deletePlaylistAssets();
		
		Session session = HibernateSession.currentSession();		
		Query q = session.createQuery( DynamicQueryAction.getHqlForCurrentAssets(p.getHql()));
		p.addAssetsToPlaylist( q, maxResults );
		
		q = session.createQuery( DynamicQueryAction.getHqlForFutureAssets(p.getHql()));
		p.addAssetsToPlaylist( q, null );
		
		// Re-associate this playlist object with the current session since we potentially have a new session 
		// after calling addAssetsToPlaylist() (for performance reasons we are flushing and clearing the session)
		p = Playlist.getPlaylist( playlistId );
		p.update();
				
		// Update this playlist's length
		p.updateLength();
	}	
	
	/**
	 * 
	 * @throws Exception
	 */
	private void updatePlaylistWithPresentationStyle(Playlist p) throws Exception
	{		
		// Parse the class name from the presentationStyleMethod
		String className = presentationStyleMethod.substring(0, presentationStyleMethod.lastIndexOf("."));
		String methodName = presentationStyleMethod.substring(presentationStyleMethod.lastIndexOf(".") + 1);
		
		Date now = new Date();
		List<Asset> futureAssets = new ArrayList<Asset>();
		List<Long> playlistAssetsToDelete = new ArrayList<Long>();
		for(PlaylistAsset pa : p.getPlaylistAssets()){
			Asset a = Asset.convert(pa.getAsset());
			
			// If this is a future asset
			if(a.getStartDate() != null && a.getStartDate().after(now)){
				futureAssets.add(a);
				playlistAssetsToDelete.add(pa.getPlaylistAssetId());
			}
		}
		
		if(playlistAssetsToDelete.size() > 0){
			for(Long playlistAssetId : playlistAssetsToDelete){
				PlaylistAsset pa = PlaylistAsset.getPlaylistAsset(playlistAssetId);
				pa.delete();
			}
			p.update();
		}
		
		try{
			// Invoke the static method on the given class
			Class c = Class.forName( className );
			Class[] params = { Playlist.class };		
			Method m = c.getDeclaredMethod( methodName, params );
			Object[] methodParams = { p };
			m.invoke( null, methodParams );
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(Asset a : futureAssets){
			Layout layout = p.getDynamicContentLayout() != null ? p.getDynamicContentLayout() : a.getAssetPresentation().getLayout();
			Displayarea da = p.getDynamicContentDisplayarea() != null ? p.getDynamicContentDisplayarea() : a.getAssetPresentation().getDisplayarea();
			PlaylistAsset.create( p, a, a.getAssetPresentation().getLength().toString(), layout, da, a.getAssetPresentation().getVariableLength(), false, true, false, false);
		}
		
		p.setPresentationStyle( PresentationStyle.getPresentationStyleByPersistentValue( presentationStyleMethod ) );
		p.update();	

		// Update this playlist's length
		p.updateLength();
		
	}
	public Date getDateToUse() {
		return dateToUse;
	}
	public void setDateToUse(Date dateToUse) {
		this.dateToUse = dateToUse;
	}
}
