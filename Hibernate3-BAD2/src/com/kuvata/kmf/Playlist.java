package com.kuvata.kmf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.engine.SessionFactoryImplementor;

import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.ContentSchedulerHookType;
import com.kuvata.kmf.usertype.DirtyType;
import com.kuvata.kmf.usertype.PlaylistImportStatus;
import com.kuvata.kmf.usertype.PlaylistOrderType;
import com.kuvata.kmf.usertype.PlaylistSearchType;
import com.kuvata.kmf.usertype.ScreenTransitionType;
import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableLinkedList;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.util.Reformat;

public class Playlist extends Entity implements TreeViewable, Historizable {

	private static Logger logger = Logger.getLogger(Playlist.class);
	
	private Long playlistId;
	private String playlistName;
	private Float length;
	private String hql;
	private String type;
	private String dynamicContentType;
	private String customMethod;
	private PresentationStyle presentationStyle;
	private Integer runFromContentScheduler;
	private Integer maxResults;
	private PlaylistOrderType playlistOrder;
	private Date csvImportDate;
	private PlaylistImportStatus csvImportStatus;
	private Clob csvImportDetail;
	private String contentRotationImportDetail;
	private ContentSchedulerHookType contentSchedulerHook;
	private ScreenTransitionType transition;
	private Set<PlaylistDisplayarea> playlistDisplayareas = new HashSet<PlaylistDisplayarea>();
	private Set<PlaylistContentRotation> playlistContentRotations = new HistorizableSet<PlaylistContentRotation>();
	private Set<PlaylistSegmentPart> playlistSegmentParts = new HashSet<PlaylistSegmentPart>();
	private Set<PlaylistGrpMember> playlistGrpMembers = new HashSet<PlaylistGrpMember>();
	private Set<PlaylistImport> playlistImports = new HistorizableSet<PlaylistImport>();
	private List<PlaylistAsset> playlistAssets = new HistorizableLinkedList<PlaylistAsset>();
	private List<DynamicContentPart> dynamicContentParts = new HistorizableLinkedList<DynamicContentPart>();
	private Boolean useRoles;
	private Integer numAssets;
	private String dynamicContentDisplay;
	private Layout dynamicContentLayout;
	private Displayarea dynamicContentDisplayarea;
	private Date lastDynamicUpdateDt;
	private Integer avgLoopLength;
	
	/*
	 * Used by content scheduler for non-persistent copies of playlistAssets and Length
	 * (used for asset exclusion and randomization)
	 */
	private List myPlaylistAssets = null;
	private float myLength;
	private Asset lastAsset = null;
	private HashMap myPlaylistAssetLengthsHash = new HashMap();
	private long myDeviceId = -1;
	
	/**
	 * Constructor
	 */	
	public Playlist()
	{		
	}
	
	/**
	 * Returns a Playlist with the given playlistId
	 * 
	 * @param playlistId
	 * @return
	 * @throws HibernateException
	 */
	public static Playlist getPlaylist(Long playlistId) throws HibernateException
	{
		return (Playlist)Entity.load(Playlist.class, playlistId);		
	}
	
	/**
	 * Returns a Playlist with the given name, or null if one does not exist
	 * 
	 * @param playlistName
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylists(String playlistName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		List l = session.createCriteria(Playlist.class)
				.add( Expression.eq("playlistName", playlistName).ignoreCase() )
				.list();		
		return l;
	}
	
	/**
	 * Returns a Playlist with the given playlistId
	 * 
	 * @param playlistId
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylists(List playlistIds) throws HibernateException
	{
		return Entity.load(Playlist.class, playlistIds);		
	}
	
	/**
	 * Returns a list of all playlists, ordered by playlist name
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getPlaylists() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List l = session.createQuery(				
				"SELECT p "
				+ "FROM Playlist as p "				
				+ "ORDER BY UPPER(p.playlistName)"						
				).list();			
		return l;
	}
	
	public static Playlist create(String playlistName)
	{
		Playlist p = new Playlist();
		p.setPlaylistName( playlistName );	
		p.setLength( new Float(0) );
		p.setPlaylistOrder( PlaylistOrderType.SEQUENTIAL );
		p.setTransition( ScreenTransitionType.OFF );
		p.setType(Constants.STATIC);
		p.save();
		return p;
	}
	
	public void update(String playlistName)
	{									
		this.setPlaylistName( playlistName );				
		this.update();	
	}

	
	/**
	 * 
	 * @param hql
	 * @param maxResults
	 * @throws HibernateException
	 * @throws SQLException
	 */
	public void addAssetsToPlaylist(Query q, Integer maxResults) throws HibernateException, SQLException
	{				
		Session session = HibernateSession.currentSession();
		List l = null;
		List assetsToExclude = null;
		int iMaxResults = 0;
		if( maxResults != null ) iMaxResults = maxResults.intValue();
		int iOrigMaxResults = iMaxResults;
				
		// If we are limiting the number of results to return
		if( iMaxResults > 0 )
		{
			/*
			 * We need to account for the assets that may be excluded from this query
			 * before limiting the number of results that this query may return.
			 */			
			String playlistsToExclude = "";			
			List excludedPlaylists = ExcludedPlaylist.getExcludedPlaylists( this );
			if( excludedPlaylists.size() > 0 )
			{
				// First, build the list of playlists to exclude, if any
				for(Iterator i = excludedPlaylists.iterator(); i.hasNext(); )
				{
					ExcludedPlaylist ep = (ExcludedPlaylist)i.next();
					if( playlistsToExclude.length() > 0 )
					{
						playlistsToExclude += ", ";
					}
					playlistsToExclude += ep.getPlaylist().getPlaylistId();			
				}
			
				// Second, select the assets that are in the playlists to exclude
				String excludeHQL = "SELECT pa.asset "
					+" FROM PlaylistAsset as pa "
					+" WHERE pa.playlist.playlistId IN (" + playlistsToExclude + ")";
				assetsToExclude = session.createQuery( excludeHQL ).list();
				
				// Third, add the total number of assets to exclude to the max number of results
				// This will allow us to exclude the necessary assets later, without affecting 
				// the number of results to return
				iMaxResults += assetsToExclude.size();
			}			
			l = q.setMaxResults( iMaxResults ).list();
		}
		else
		{
			l = q.list();	
		}
					
		// Start bulk mode
		HibernateSession.startBulkmode();
		
		int flushCount = 0;
		int assetsCounter = 0;
		boolean usePlaylistDisplayProperties = this.getDynamicContentDisplay() != null && this.getDynamicContentDisplay().equals("default") == false;
		for( Iterator i = l.iterator(); i.hasNext(); ) 
		{			
			Asset a = (Asset)i.next();
			
			// Only add this asset to this playlist if it is not one of the assets we are excluding
			boolean includeAsset = true;
			if( assetsToExclude != null && assetsToExclude.contains( a ) ) {			
				includeAsset = false;
			}
			
			if( includeAsset ) 
			{
				/*
				 * Make sure we don't exceed our original maximum number of results.
				 * This could happen if the results of our hql query, (by limiting 
				 * the max number or results), does not include one or more of our assets to exclude
				 */
				if( iOrigMaxResults == 0 || (iOrigMaxResults > 0 && assetsCounter++ < iOrigMaxResults) ) {		
					Layout layout = usePlaylistDisplayProperties ? this.getDynamicContentLayout() : a.getAssetPresentation().getLayout();
					Displayarea da = usePlaylistDisplayProperties ? this.getDynamicContentDisplayarea() : a.getAssetPresentation().getDisplayarea();
					PlaylistAsset.create( this, a, a.getAssetPresentation().getLength().toString(), layout, da, a.getAssetPresentation().getVariableLength(), false, true, false, false);
				}
			}
						
			if (flushCount == 150)
			{
				
				// Restart bulk mode
				HibernateSession.stopBulkmode();
				HibernateSession.startBulkmode();
				
				flushCount = 0;
			}
			flushCount++;
		}	
		
		// Create PlaylistDisplayarea objects for secondary displayareas if necessary
		this.update();
		this.resetPlaylistDisplayareas();
		
		// Stop bulk mode
		HibernateSession.stopBulkmode();
		
		// Exclude from this playlist any assets that are in the excludedPlaylists objects associated with this playlist
		this.excludePlaylists();
	}
	
	/**
	 * Test the given hql. If it throws a HibernateException, 
	 * it is invalid -- return -1. Else, if it is valid hql, 
	 * return the number of records that the hql returned.
	 * 
	 * @return 
	 */
	public static int testDynamicHqlQuery(String hql) 
	{
		
		int result = -1;		
		Iterator<Long> i = null;
		try
		{			
			// First make sure that the entire hql is valid
			Session session = HibernateSession.currentSession();
			List l = session.createQuery(hql).setMaxResults(1).list();
			result = l.size();			
			if(l.size() > 0){
				// Make sure that the return type of the hql is of type 'IAsset'
				// A classCast exception will be thrown if this test fails.
				IAsset firstResult = (IAsset)l.get(0);

				// Next, strip out the order by clause, if any 
				if( hql.toLowerCase().indexOf("order by") > 0 ) {
					hql = hql.substring( 0, hql.toLowerCase().indexOf("order by") );
				}

				// Get the number of records returned by the hql
				i = session.createQuery("SELECT COUNT(*) FROM Asset a where a.assetId in ("+ hql +") ").iterate();
				result = i.next().intValue();
				Hibernate.close( i );
			}
		}
		catch(Exception e)
		{
			// Invalid hql
			result = -1;
		}finally{
			if( i != null ){
				Hibernate.close( i );
			}
		}
		return result;
	}
	
	/**
	 * @return Returns the number of assets in the given playlist
	 */
	public int getPlaylistAssetsCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		Iterator i = session.createQuery(
				"SELECT COUNT(elements (p.playlistAssets)) "
				+ "FROM Playlist as p "				
				+ "WHERE p.playlistId= "+ this.getPlaylistId().toString()				
				).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}
	
	/**
	 * This method reorders all assets in the given playlist. Is called
	 * after removing assets from playlist so there is not a gap in the
	 * sequence numbers.
	 * 
	 * @throws HibernateException
	 */
	public void orderAssets() throws HibernateException
	{		
		int seqCounter = 0;
		
		// Re-order all assets in playlist
		List l = this.getPlaylistAssets();				
		for( Iterator i = l.iterator(); i.hasNext(); )
		{
			PlaylistAsset pa = (PlaylistAsset)i.next();
			pa.setSeqNum( new Integer(seqCounter) );
			pa.update(false);
			seqCounter++;
		}		
		this.update();
	}
	
	/**
	 * Returns true if a playlist with the given name already exists in the database
	 * 
	 * @param playlistName
	 * @return
	 */
	public static boolean playlistExists(String playlistName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(Playlist.class)
					.add( Expression.eq("playlistName", playlistName).ignoreCase() )
					.list();
		return l.size() > 0 ? true : false;
	}	

	/**
	 * Retrieve a list of unique displayareas and their respective layouts
	 * for all assets in this playlist.
	 *  
	 * @return	List of Object[] (layout|displayarea) objects
	 */
	private List getLayoutsAndDisplayareas() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		
		// First get the displayareas from assets in this playlist that
		// do not have a playlist_asset specific asset presentation object
		String hql = "select distinct layout, da "
				+ "FROM Playlist as p "
				+ "JOIN p.playlistAssets as pa "
				+ "JOIN pa.asset.assetPresentation.layout.layoutDisplayareas as lda "	
				+ "JOIN lda.displayarea as da "
				+ "JOIN lda.layout as layout "
				+ "WHERE p.playlistId= "+ this.getPlaylistId().toString() +" "
				+ "AND pa.assetPresentation IS NULL "
				+ "ORDER BY lda.displayarea.displayareaName";		
		List l1 = session.createQuery( hql ).list();	
		
		// Next get the displayareas from assets in this playlist that
		// do have a playlist_asset specific asset presentation object
		hql = "select distinct layout, da "
				+ "FROM Playlist as p "
				+ "JOIN p.playlistAssets as pa "
				+ "JOIN pa.assetPresentation.layout.layoutDisplayareas as lda "	
				+ "JOIN lda.displayarea as da "
				+ "JOIN lda.layout as layout "
				+ "WHERE p.playlistId= "+ this.getPlaylistId().toString() +" "
				+ "AND pa.assetPresentation IS NOT NULL "
				+ "ORDER BY lda.displayarea.displayareaName";		
		List l2 = session.createQuery( hql ).list();
		l1.addAll( l2 );
		
	    // Eliminate duplicate elements if any
	    HashSet result = new HashSet(l1);
	    List list = new LinkedList(result);
		return list;
	}	
	
	/**
	 * Retrieves a list of unique displayareas and their respective layouts for each
	 * playlist asset's asset presentation. 
	 *  
	 * @return	List of Object[] (layout|displayarea) objects
	 */
	private List getPrimaryLayoutsAndDisplayareas() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		
		// First get the displayareas from assets in this playlist that
		// do not have a playlist_asset specific asset presentation object
		String hql = "select distinct layout, da "
				+ "FROM Playlist as p "
				+ "JOIN p.playlistAssets as pa "
				+ "JOIN pa.asset.assetPresentation as ap "	
				+ "JOIN ap.displayarea as da "
				+ "JOIN ap.layout as layout "
				+ "WHERE p.playlistId= "+ this.getPlaylistId().toString() +" "
				+ "AND pa.assetPresentation IS NULL "
				+ "ORDER BY da.displayareaName";		
		List l1 = session.createQuery( hql ).list();	
		
		// Next get the displayareas from assets in this playlist that
		// do have a playlist_asset specific asset presentation object
		hql = "select distinct layout, da "
				+ "FROM Playlist as p "
				+ "JOIN p.playlistAssets as pa "
				+ "JOIN pa.assetPresentation as ap "	
				+ "JOIN ap.displayarea as da "
				+ "JOIN ap.layout as layout "
				+ "WHERE p.playlistId= "+ this.getPlaylistId().toString() +" "
				+ "AND pa.assetPresentation IS NOT NULL "
				+ "ORDER BY da.displayareaName";		
		List l2 = session.createQuery( hql ).list();		
		l1.addAll( l2 );
		
	    // Eliminate duplicate elements if any
	    HashSet result = new HashSet(l1);
	    List list = new LinkedList(result);		
		return list;
	}		
	
	public void makeDirty() throws HibernateException
	{
		makeDirty( true );
	}
	
	/**
	 * 
	 *
	 */
	public void makeDirty(boolean makeDirtyDevices) throws HibernateException
	{		
		// If there is not a dirty object for this object		
		Dirty d = Dirty.getDirty( this.getEntityId() );		
		if(d == null)
		{		
			// Create a new dirty object
			d = new Dirty();			
			d.setDirtyEntityId( this.getEntityId() );
			d.setDirtyType( DirtyType.PLAYLIST );
			d.save();
		}else{
			d.setDirtyType( DirtyType.PLAYLIST );
			d.update();
		}
		
		// Make dirty any devices associated with this playlist
		if( makeDirtyDevices ){
			Device.makeDirty( this );
		}
	}
	
	/**
	 * Make dirty any playlists that contain the given asset.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static void makeDirty(Asset a) throws HibernateException
	{					
		// NOTE: Cannot select DISTINCT from playlist because the playlist object contains a property of type CLOB		
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT playlist FROM Playlist playlist "
				+ "WHERE playlist.playlistId IN "
				+ "(SELECT DISTINCT p.playlistId "
				+ "FROM Playlist as p "				
				+ "WHERE p.playlistId IN "
				+ "	(SELECT pa.playlist.playlistId "
				+ "	FROM PlaylistAsset as pa "
				+ "	WHERE pa.asset.assetId = "+ a.getAssetId().toString() +"))";
		List<Playlist> l = session.createQuery( hql ).list();
		for(Iterator<Playlist> i = l.iterator(); i.hasNext(); ) {
			Playlist p = i.next();
			p.makeDirty();
		}		
	}
	
	/**
	 * If this playlist is dirty, return true, else return false
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public boolean isDirty() throws HibernateException
	{
		String hql = "SELECT d from Dirty d "
			+ "WHERE d.dirtyType = '"+ DirtyType.PLAYLIST +"' " 
			+ "AND d.dirtyEntityId = "+ this.getPlaylistId();		
		Session session = HibernateSession.currentSession();			
		Iterator iter = session.createQuery( hql ).iterate(); 
		boolean result = iter.hasNext() ? true : false;
		Hibernate.close( iter );
		return result;
	}
	
	/**
	 * Returns all playlists that have a dirty status
	 * @return 
	 */
	public static List getDirtyPlaylists() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT p "
					+ "FROM Playlist p, Dirty d "
					+ "WHERE p.playlistId = d.dirtyEntityId "
					+ "AND d.dirtyType = '"+ DirtyType.PLAYLIST.getPersistentValue() + "'";		
		List l = session.createQuery( hql ).list(); 
		return l; 	
	}	
	/**
	 * 
	 * @throws HibernateException
	 * @throws SQLException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public void updatePlaylist(Date dateToUse) throws HibernateException, SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		// If this is a dynamic playlist
		if( this.getType().equalsIgnoreCase( Constants.DYNAMIC ) )
		{
			// Update the playlist assets
			if( this.getDynamicContentType() != null && this.getDynamicContentType().equals("metadata") )
			{
				this.updatePlaylistAssetsWithMetadata( this.getMaxResults(), dateToUse );
			}
			else if( this.getHql() != null && this.getHql().length() > 0 )
			{							
				this.updatePlaylistAssetsWithHQL( this.getHql(), this.getMaxResults() );
			}
			else if( this.getCustomMethod() != null && this.getCustomMethod().length() > 0 )
			{
				HibernateSession.startBulkmode();
				this.updatePlaylistAssetsWithCustomMethod( this.getCustomMethod(), this.getMaxResults() );
				HibernateSession.stopBulkmode();
			}	
			else if( this.getNumAssets() != null ){
				this.updatePlaylistAssetsWithContentRotations( this.getMaxResults(), dateToUse );
				this.setAvgLoopLength(DynamicContentPart.calculateAvgLoopLength(DynamicContentPart.getDynamicContentParts(this), this.getNumAssets()).intValue());
			}
			
			// Apply the presentation style if any
			if( this.getPresentationStyle() != null && this.getPresentationStyle().getPersistentValue().length() > 0 )
			{
				HibernateSession.startBulkmode();
				this.updatePlaylistAssetsWithPresentationStyle( this.getPresentationStyle().getPersistentValue() );
				HibernateSession.stopBulkmode();
			}	
			
			// Make the playlist dynamic
			this.setType(Constants.DYNAMIC);
		}
		this.updateLength();
	}
		
	/**
	 * This method is called from the updatePlaylist method. It will
	 * take the updated lengths for each asset in this playlist
	 * and update the total length of the playlist.	 
	 */
	public void updateLength() throws HibernateException
	{
		// Update the length of this playlist
		this.setLength( this.calculatePlaylistLength() );
		this.update();
		
		// Update the length of all segments associated with this playlist
		Segment.updateLengths( this );
	}
	
	/**
	 * This method is called from the updatePlaylist method. It will
	 * take the updated lengths for each asset in this playlist
	 * and update the total length of the playlist.	 
	 */
	public static List updateLengths(List playlists) throws HibernateException
	{
		ArrayList playlistIds = new ArrayList();
		for(Iterator i=playlists.iterator();i.hasNext();){
			Playlist p = (Playlist)i.next();
			p.setLength(p.calculatePlaylistLength());
			p.update();
			playlistIds.add(p.getPlaylistId());
		}
		
		// Update the length of all segments associated with this playlist
		return Segment.updateLengths( playlistIds );
	}
	
	/**
	 * 
	 * @param playlistGrpMember
	 */
	public void addPlaylistGrpMember(PlaylistGrpMember playlistGrpMember) 
	{
		if (playlistGrpMember == null)
			throw new IllegalArgumentException("Null playlistGrpMember!");
				
		playlistGrpMembers.add(playlistGrpMember);
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removePlaylistGrpMembers() throws HibernateException
	{
		this.playlistGrpMembers.clear();
		this.update();
	}	
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPlaylistId();
	}
	
	/**
	 * 
	 * @param getAllDevices
	 * @param includeLeaves
	 * @param formatter
	 * @return
	 * @throws HibernateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static String treeViewFormat(boolean getAllPlaylists, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, Method formatter) 
		throws HibernateException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException
	{		
		Method allBranchMethod = null;
		StringBuffer result = new StringBuffer();	
		
		if(getAllPlaylists == true)
		{	
			// Declare the method that will be used to build the "All Playlists" branch of the tree
			Class[] methodParamTypes = { boolean.class, boolean.class, boolean.class, boolean.class };						
			allBranchMethod = Class.forName(Playlist.class.getName()).getDeclaredMethod("getAllPlaylistsBranch", methodParamTypes);
		}					
		result.append( Grp.getTree(Constants.PLAYLIST_GROUPS, includeLeaves, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, Constants.TYPE_PLAYLIST_GROUP, formatter, allBranchMethod) );
		return result.toString();
	}		
	/**
	 * 
	 */
	public String treeViewFormat(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();		
		if(includeLeaves == true)
		{				
			String onClick = "null";	
			String onDoubleClick = "";
			if( includeHref )
			{
				onClick = "\'javascript:top.playlistOnClick("+ this.getPlaylistId() +")\'";	
			}		
			if( includeDoubleClick )
			{
				onDoubleClick = " onDblClick=\\\"javascript:add('treeNodePlaylist', '_playlist')\\\"";
			}
			result.append("[");
			result.append("{id:"+ this.getPlaylistId() +"}, \"<span class=\\\"treeNodePlaylist\\\""+ onDoubleClick +">"+ Reformat.jspEscape(this.getPlaylistName()) + "</span>\", "+ onClick +", null, type_playlist,");					
			result.append("],\n");
		}
		return result.toString();
	}
	/**
	 * 
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @return
	 * @throws HibernateException
	 */
	public static String getAllPlaylistsBranch(boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, boolean includeAllLeaves) throws HibernateException
	{				
		StringBuffer result = new StringBuffer();
		String onClick = "";	
		String onDoubleClick = "";
		if( doubleClickLeavesOnly == false )
		{		
			// Set the flag to retrieve the remaining child nodes
			// GetChildNodesAction needs this "allPlaylists" identifying flag
			onClick = "\'javascript:grpOnClick(\\\'allPlaylists\\\', true, "+ String.valueOf(includeHref) +", "+ String.valueOf(includeDoubleClick) +", "+ String.valueOf(doubleClickLeavesOnly) +")\'";
		}		
		if(( includeDoubleClick ) && (doubleClickLeavesOnly == false) )
		{
			// Pass a -1 as device group id so we know it's the "All Devices" device group
			onDoubleClick = " onDblClick=\\\"grpOnDoubleClick('-1', '"+ Constants.PLAYLIST_GROUP_SUFFIX +"')\\\"";	
		}			
		result.append("[");
		result.append("{id:-1}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">All Playlists</span>\", "+ onClick +", null, type_playlist_group,\n");
		
		// If we are include all leaves
		if( includeAllLeaves )
		{
			// Get a list of all playlists
			List l = Playlist.getPlaylists();		
			for(int i=0; i<l.size(); i++)
			{
				Playlist p = (Playlist)l.get(i);										   	
				result.append( p.treeViewFormat(1, true, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, null, null) );
				
				// Limit the number of child nodes
				if( i >= Constants.MAX_CHILD_NODES - 1 ){
					
					// Append a "more" child node and break
					result.append("[");					
					result.append("{id:0}, \"<span class=\\\"treeNode\\\">...more</span>\", null, null, type_playlist,");
					result.append("],\n");
					break;
				}					
			}		
		}
		result.append("],\n");
		return result.toString();
	}	
	
	/**
	 * 
	 * @throws HibernateException
	 * @throws SQLException
	 */
	public void deletePlaylistAssets() throws HibernateException, SQLException
	{
		// Executing following in JDBC because hibernate isn't efficient enough on large playlists
		SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
		Connection conn = sessionImplementor.getConnectionProvider().getConnection();
		
		String pairedAssetSql = "(select paired_asset_id from paired_asset where paired_displayarea_id in (select paired_displayarea_id from paired_displayarea where asset_presentation_id in (select asset_presentation_id from playlist_asset where playlist_id = " + this.playlistId.toString() + ") ) )";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("delete from entity_instance where entity_id in " + pairedAssetSql);
		stmt.close();
		stmt = conn.createStatement();
		stmt.executeUpdate("delete from paired_asset where paired_asset_id in " + pairedAssetSql);
		stmt.close();
		
		String pairedDaSql = "(select paired_displayarea_id from paired_displayarea where asset_presentation_id in (select asset_presentation_id from playlist_asset where playlist_id = " + this.playlistId.toString() + ") )";
		stmt = conn.createStatement();
		stmt.executeUpdate("delete from entity_instance where entity_id in " + pairedDaSql);
		stmt.close();
		stmt = conn.createStatement();
		stmt.executeUpdate("delete from paired_displayarea where paired_displayarea_id in " + pairedDaSql);
		stmt.close();
		
		String apSql = "(select asset_presentation_id from playlist_asset where playlist_id = " + this.playlistId.toString() + ")";
		stmt = conn.createStatement();
		stmt.executeUpdate("delete from asset_presentation where asset_presentation_id in " + apSql);
		stmt.close();
		
		String paSql = "(select playlist_asset_id from playlist_asset where playlist_id = " + this.playlistId.toString() + ")";
		stmt = conn.createStatement();
		stmt.executeUpdate("delete from playlist_asset where playlist_asset_id in " + paSql);
		stmt.close();
		
		stmt = conn.createStatement();
		stmt.executeUpdate("commit");
		stmt.close();
		
		conn.close();
	}
	/**
	 * 
	 * @param hql
	 * @param maxResults
	 * @throws HibernateException
	 */
	public void updatePlaylistAssetsWithHQL(String hql, Integer maxResults) throws HibernateException
	{
		try
		{
			PlaylistUpdaterThread t = new PlaylistUpdaterThread(SchemaDirectory.getSchema().getSchemaName(), this.getPlaylistId(), "HQL", hql, maxResults);
			t.start();
			t.join();	
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}		
	}
	/**
	 * 
	 * @param customMethod
	 * @param maxResults
	 * @throws HibernateException
	 */
	public void updatePlaylistAssetsWithCustomMethod(String customMethod, Integer maxResults) throws HibernateException
	{				
		try
		{
			PlaylistUpdaterThread t = new PlaylistUpdaterThread(SchemaDirectory.getSchema().getSchemaName(), this.getPlaylistId(), "CustomMethod", customMethod, maxResults);
			t.start();
			t.join();	
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param customMethod
	 * @param maxResults
	 * @throws HibernateException
	 */
	public void updatePlaylistAssetsWithMetadata(Integer maxResults, Date dateToUse) throws HibernateException
	{				
		try
		{
			// Generate the hql in the main thread to avoid re-loading of the object
			this.generateHql(dateToUse);
			this.update();
			
			// If we generated a valid hql
			if(this.getHql() != null && this.getHql().length() > 0){
				PlaylistUpdaterThread t = new PlaylistUpdaterThread(SchemaDirectory.getSchema().getSchemaName(), this.getPlaylistId(), "Metadata", maxResults, dateToUse);
				t.start();
				t.join();
			}
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}		
	}
	
	public void updatePlaylistAssetsWithContentRotations(Integer maxResults, Date dateToUse) throws HibernateException
	{				
		try
		{
			PlaylistUpdaterThread t = new PlaylistUpdaterThread(SchemaDirectory.getSchema().getSchemaName(), this.getPlaylistId(), "PlayX", maxResults, dateToUse);
			t.start();
			t.join();	
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param presentationStyleMethod
	 * @throws HibernateException
	 */
	public void updatePlaylistAssetsWithPresentationStyle( String presentationStyleMethod ) throws HibernateException
	{							
		try
		{
			PlaylistUpdaterThread t = new PlaylistUpdaterThread(SchemaDirectory.getSchema().getSchemaName(), this.getPlaylistId(), presentationStyleMethod);
			t.start();
			t.join();	
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}	
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public Float calculatePlaylistLength() throws HibernateException
	{		
		Session session = HibernateSession.currentSession();
		float offset1 = 0;
		float offset2 = 0;
		
		// First get the lengths of all the assets that do not have 
		// playlist_asset specific asset presentations
		String hql = "SELECT SUM(pa.asset.assetPresentation.length) "
			+ "FROM PlaylistAsset as pa " 
			+ "WHERE pa.playlist.playlistId = "+ this.getPlaylistId() + " "
			+ "AND pa.assetPresentation is null";				
		Iterator i = session.createQuery( hql ).iterate();
		if(i.hasNext())
		{
			Double temp1 = (Double)i.next();
			if(temp1 != null)
			{
				offset1 = temp1.floatValue();	
			}			
		}		
		Hibernate.close( i );
		
		// Second get the lengths of all the assets that do have 
		// playlist_asset specific asset presentations
		String hql2 = "SELECT SUM(pa.assetPresentation.length) "
			+ "FROM PlaylistAsset as pa " 
			+ "WHERE pa.playlist.playlistId = "+ this.getPlaylistId() + " "
			+ "AND pa.assetPresentation is not null ";			
		Iterator j = session.createQuery( hql2 ).iterate();
		if(j.hasNext())
		{
			Double temp2 = (Double)j.next();
			if(temp2 != null)
			{
				offset2 = temp2.floatValue();	
			}			
		}		
		Hibernate.close( j );
		return new Float(offset1 + offset2);
	}	
	
	/**
	 * Copies this playlist and assigns the given new playlist name.
	 * 
	 * @param newPlaylistName
	 * @return
	 */
	public Long copy(String newPlaylistName) throws Exception
	{		
		Session session = HibernateSession.currentSession();
		session.lock( this, LockMode.READ );
		
		// First create a new playlist object
		Playlist newPlaylist = new Playlist();
		newPlaylist.setPlaylistName( newPlaylistName );
		newPlaylist.setLength( this.getLength() );
		newPlaylist.setType(this.getType());
		newPlaylist.setPlaylistOrder( this.getPlaylistOrder() );
		newPlaylist.setContentSchedulerHook( this.getContentSchedulerHook() );
		newPlaylist.setTransition( this.getTransition() );
		
		// Save the playlist but do not create permission entries since we are going to copy them
		Long newPlaylistId = newPlaylist.save( false );
		newPlaylist.copyPermissionEntries( this );
		
		// Copy all content rotations associated with this playlist		
		for( Iterator i = this.getPlaylistContentRotations().iterator(); i.hasNext(); )
		{
			PlaylistContentRotation pcr = (PlaylistContentRotation)i.next();
			PlaylistContentRotation.copy( pcr, newPlaylist, pcr.getContentRotation() );						
		}
		
		// Copy all playlist displayareas associated with this playlist		
		for( Iterator i = this.getPlaylistDisplayareas().iterator(); i.hasNext(); )
		{
			PlaylistDisplayarea pd = (PlaylistDisplayarea)i.next();
			PlaylistDisplayarea.copy( pd, newPlaylist );						
		}		
				
		// Set type and dynamic content if dynamic playlist
		if(this.getType().equals("dynamic")){
			if(this.getDynamicContentType() != null && this.getDynamicContentType().equals("metadata")){
				for(DynamicQueryPart dqp : DynamicQueryPart.getDynamicQueryParts(this)){
					DynamicQueryPart.create(newPlaylist, null, dqp.getAttrDefinition(), dqp.getOperator(), dqp.getValue(), dqp.getSelectedDate(), dqp.getNumDaysAgo(), dqp.getIncludeNull(), dqp.getSeqNum().intValue());
				}
			}else if(this.getNumAssets() != null){
				newPlaylist.setNumAssets(this.getNumAssets());
				for(DynamicContentPart dcp : DynamicContentPart.getDynamicContentParts(this)){
					DynamicContentPart.create(newPlaylist, null, dcp.getContentRotation(), dcp.getLayout(), dcp.getDisplayarea(), dcp.getSeqNum());
				}
			}else if(this.getHql() != null){
				newPlaylist.setHql(this.getHql());
			}else if(this.getCustomMethod() != null){
				newPlaylist.setCustomMethod(this.getCustomMethod());
			}
			
			newPlaylist.setDynamicContentDisplay(this.getDynamicContentDisplay());
			newPlaylist.setDynamicContentLayout(this.getDynamicContentLayout());
			newPlaylist.setDynamicContentDisplayarea(this.getDynamicContentDisplayarea());
			newPlaylist.setMaxResults(this.getMaxResults());
			newPlaylist.setUseRoles(this.getUseRoles());
			newPlaylist.setRunFromContentScheduler( this.getRunFromContentScheduler() );
			newPlaylist.setPresentationStyle( this.getPresentationStyle() );
			newPlaylist.setDynamicContentType(this.getDynamicContentType());
		}
		
		// Copy all the playlist group members		
		for( Iterator i = this.getPlaylistGrpMembers().iterator(); i.hasNext(); )
		{
			PlaylistGrpMember pgm = (PlaylistGrpMember)i.next();
			PlaylistGrpMember newPlaylistGrpMember = new PlaylistGrpMember();
			newPlaylistGrpMember.setGrp( pgm.getGrp() );
			newPlaylistGrpMember.setPlaylist( newPlaylist );
			newPlaylistGrpMember.save();			
		}
		
		// Copy all playlist assets
		if(this.getType().equals("dynamic")){
			newPlaylist.updatePlaylist(null);
		}else{
			for( Iterator i = this.getPlaylistAssets().iterator(); i.hasNext(); )
			{
				PlaylistAsset pa = (PlaylistAsset)i.next();		
				AssetPresentation assetPresentation = pa.getAssetPresentation();
				AssetPresentation newAssetPresentation = null;
				if( assetPresentation != null )
				{
					newAssetPresentation = assetPresentation.copy();
				}
				PlaylistAsset newPlaylistAsset = new PlaylistAsset();
				newPlaylistAsset.setAsset( pa.getAsset() );
				newPlaylistAsset.setAssetPresentation( newAssetPresentation );
				newPlaylistAsset.setPlaylist( newPlaylist );
				newPlaylistAsset.setSeqNum( pa.getSeqNum() );
				newPlaylistAsset.save();			
			}
		}
		
		this.copyMetadata( newPlaylist.getPlaylistId() );
		return newPlaylistId;
	}
	
	/**
	 * Append or replace the content rotation assets to the list of playlist assets for this playlist.
	 * If there are one or more existing playlist assets, use the presentation properties of the
	 * last playlist asset. Otherwise, use the default presentation properties of each content rotation asset.
	 * @param contentRotationId
	 */
	public void importContentRotation(ContentRotation contentRotation, Layout layout, Displayarea displayarea, boolean appendAssetsToPlaylist, boolean makeDirty) throws SQLException
	{
		// Start a new thread
		ImportContentRotationThread t = new ImportContentRotationThread(){
			public void run(){
				
				// Initialize hibernate session now
				SchemaDirectory.initialize(schemaName, "ImportContentRotationThread", appUsername, true, false);
				
				// If something other than the "Default" layout was passed in, attempt to locate the layout		
				String appendMessage = appendAssetsToPlaylist ? "was appended to" : "replaced";	
						
				// Replace the playlist assets if specified
				if( appendAssetsToPlaylist == false ){
					try {
						Playlist playlist = Playlist.getPlaylist(playlistId);
						playlist.deletePlaylistAssets();
						
						// Re-set session after JDBC deletes
						HibernateSession.closeSession();
						HibernateSession.currentSession();
					} catch (Exception e) {
						logger.error(e);
					}
				}
				
				// Load objects in this session
				Playlist playlist = Playlist.getPlaylist(playlistId);
				ContentRotation contentRotation = ContentRotation.getContentRotation(contentRotationId);
				Layout layout = layoutId != null ? Layout.getLayout(layoutId) : null;
				Displayarea displayarea = displayareaId != null ? Displayarea.getDisplayarea(displayareaId) : null;
				
				// Start bulk mode
				HibernateSession.startBulkmode();
				
				if( contentRotation != null && contentRotation.getContentRotationAssets().size() > 0 )
				{
					boolean useAssetPresentationLayout = layout == null ? true : false;
					boolean useAssetPresentationDisplayarea = displayarea == null ? true : false;
					
					// Append each content rotation asset to this playlist
					for( Iterator i=contentRotation.getContentRotationAssets().iterator(); i.hasNext(); ){
						// If we did not find an existing playlist asset in this playlist,
						// use the default presentation properties of this content rotation asset
						ContentRotationAsset cra = (ContentRotationAsset)i.next();
						
						if( useAssetPresentationLayout ){
							layout = cra.getAsset().getAssetPresentation().getLayout();
						}
						if( useAssetPresentationDisplayarea ){
							displayarea = cra.getAsset().getAssetPresentation().getDisplayarea();
						} 
										
						// Create a new PlaylistAsset for this playlist
						PlaylistAsset.create( playlist, Asset.convert(cra.getAsset()), cra.getLength().toString(), layout, displayarea, cra.getVariableLength(), false, true, false, true );
					}
					
					// Create PlaylistDisplayarea objects for secondary displayareas if necessary
					playlist.resetPlaylistDisplayareas();
				}
				
				// Set the contentRotationImportDetail message to be displayed in the interface		
				SimpleDateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				String contentRotationImportDetail = "Successful import of Content Rotation: "+ contentRotation.getContentRotationName() 
					+" "+ appendMessage +" the assets in this Playlist on "+dateTimeFormat.format( new Date() ) +".";
				playlist.setContentRotationImportDetail( contentRotationImportDetail );
				
				// Set the playlist to be static
				playlist.setType(Constants.STATIC);
				
				// Update the length of this playlist
				playlist.updateLength();
				
				if( makeDirty ){
					playlist.makeDirty();
				}
				
				// Stop bulk mode
				HibernateSession.stopBulkmode();
			}
		};
		t.playlistId = this.playlistId;
		t.contentRotationId = contentRotation.getContentRotationId();
		t.layoutId = layout != null ? layout.getLayoutId() : null;
		t.displayareaId = displayarea != null ? displayarea.getDisplayareaId() : null;
		t.appendAssetsToPlaylist = appendAssetsToPlaylist;
		t.makeDirty = makeDirty;
		t.appUsername = KmfSession.getKmfSession() != null && KmfSession.getKmfSession().getCurrentAppUser() != null ? KmfSession.getKmfSession().getCurrentAppUser().getName() : "Auto";
		
		// We need to init the kmf schema to get the schema name
		SchemaDirectory.initialize("kmf", "", t.appUsername, true, false);
		t.schemaName = KmfSession.getKmfSession() != null && KmfSession.getKmfSession().getCurrentAppUser() != null ? KmfSession.getKmfSession().getCurrentAppUser().getSchema().getSchemaName() : SchemaDirectory.KUVATA_SCHEMA;
		
		// Switch back to the kuvata schema
		SchemaDirectory.initialize(SchemaDirectory.KUVATA_SCHEMA, "", t.appUsername, true, false);
		
		// Wait for all the updates to finish
		try {
			t.start();
			t.join();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * Exclude from this playlist any assets that are in the excludedPlaylists objects associated with this playlist
	 * @throws HibernateException
	 * @throws SQLException
	 */
	private void excludePlaylists() throws HibernateException, SQLException
	{
		// Delete from playlist_assets those which are in the exclude	
		String playlistsToExclude = "";			
		List excludedPlaylists = ExcludedPlaylist.getExcludedPlaylists( this );
		if( excludedPlaylists.size() > 0 )
		{
			// First, build the list of playlists to exclude, if any
			for(Iterator i = excludedPlaylists.iterator(); i.hasNext(); )
			{
				ExcludedPlaylist ep = (ExcludedPlaylist)i.next();
				if( playlistsToExclude.length() > 0 )
				{
					playlistsToExclude += ", ";
				}
				playlistsToExclude += ep.getPlaylist().getPlaylistId();			
			}
			
			SessionFactoryImplementor sessionImplementor = (SessionFactoryImplementor)SchemaDirectory.getSchema().getSessionFactory();
			Connection conn = sessionImplementor.getConnectionProvider().getConnection();		
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("delete from playlist_asset where playlist_id = " + this.getPlaylistId() + " " +
							   "and asset_id in (select asset_id from playlist_asset where playlist_id in (" + playlistsToExclude + "))");
			stmt.close();
			stmt = conn.createStatement();
			stmt.executeUpdate("commit");
			stmt.close();
			conn.close();
			
			this.orderAssetsInPlaylist();
		}	
	}
	
	/**
	 * Re-order all assets in this playlist
	 * @throws HibernateException
	 */
	private void orderAssetsInPlaylist() throws HibernateException
	{
		int seqNum = 0;
		String hql = "SELECT pa from PlaylistAsset pa "
			+"WHERE pa.playlist.playlistId = "+ this.getPlaylistId() +" "
			+"ORDER BY pa.seqNum";
		Session session = HibernateSession.currentSession();		
		List<PlaylistAsset> l = session.createQuery( hql ).list();
		for( Iterator<PlaylistAsset> i = l.iterator(); i.hasNext(); )
		{
			PlaylistAsset playlistAsset = i.next();
			playlistAsset.setSeqNum( new Integer(seqNum) );
			playlistAsset.update(false);
			seqNum++;
		}		
	}
	
	/**
	 * @return Returns the number of playlists
	 */
	public static int getPlaylistsCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		Iterator i = session.createQuery(
				"SELECT COUNT(p) "
				+ "FROM Playlist as p"							
				).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}	

	/**
	 * Returns the length in seconds of all paired assets for the given displayarea.
	 * @return Returns the number of playlists
	 */
	public float getPairedAssetsLength(Displayarea da) throws HibernateException 
	{
		float result = 0;
		Session session = HibernateSession.currentSession();	
				
		// First get the paired assets from assets in this playlist that
		// have a playlist_asset specific asset presentation object
		String hql = "SELECT SUM(pairedAsset.length) "
				+ "FROM Playlist as p "
				+ "JOIN p.playlistAssets as pa "
				+ "JOIN pa.assetPresentation as ap "
				+ "JOIN ap.pairedDisplayareas as pairedDisplayarea "
				+ "JOIN pairedDisplayarea.displayarea as da "
				+ "JOIN pairedDisplayarea.pairedAssets as pairedAsset "
				+ "WHERE p.playlistId= "+ this.getPlaylistId().toString() +" "
				+ "AND da.displayareaId= "+ da.getDisplayareaId().toString() +" "
				+ "AND pa.assetPresentation IS NOT NULL";
		Iterator i = session.createQuery( hql ).iterate();
		Double d = (Double) i.next();
		Hibernate.close( i );
		result = d != null ? d.floatValue() : result;	
		
		// If we've already found a paired asset for this displayarea, no need to go on 
		if( result <= 0 )
		{
			// Next get the paired assets from assets in this playlist that
			// do not have a playlist_asset specific asset presentation object
			hql = "SELECT SUM(pairedAsset.length) "
				+ "FROM Playlist as p "
				+ "JOIN p.playlistAssets as pa "
				+ "JOIN pa.asset as asset "
				+ "JOIN asset.assetPresentation as ap "
				+ "JOIN ap.pairedDisplayareas as pairedDisplayarea "
				+ "JOIN pairedDisplayarea.displayarea as da "
				+ "JOIN pairedDisplayarea.pairedAssets as pairedAsset "
				+ "WHERE p.playlistId= "+ this.getPlaylistId().toString() +" "
				+ "AND da.displayareaId= "+ da.getDisplayareaId().toString() +" "
				+ "AND pa.assetPresentation IS NULL";
			Iterator i2 = session.createQuery( hql ).iterate();
			Double d2 = (Double) i2.next();
			Hibernate.close( i2 );
			result = d2 != null ? d2.floatValue() : result;	
		}		
		return result;
	}	
	
	/**
	 * Returns a list of GrpGrpMember objects of the "Playlist Groups" group
	 * @return
	 */
	public static List getAllPlaylistGroups()
	{
		List result = null;
		Grp playlistGroups = Grp.getUniqueGrp( Constants.PLAYLIST_GROUPS );
		if( playlistGroups != null )
		{			
			result = new ArrayList( playlistGroups.getGrpMembers() );
			BeanPropertyComparator comparator1 = new BeanPropertyComparator( "grpName" );
			BeanPropertyComparator comparator2 = new BeanPropertyComparator( "childGrp", comparator1 );				
			Collections.sort( result, comparator2 );
			
			// Initialize the child groups within the list
			for( Iterator i=result.iterator(); i.hasNext(); )
			{
				GrpGrpMember ggm = (GrpGrpMember)i.next();
				Hibernate.initialize( ggm.getChildGrp() );
			}
		}
		return result;
	}
	
	/**
	 * Gets the count of the assets according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @return
	 * @throws ParseException
	 */
	public static int searchPlaylistsCount(PlaylistSearchType playlistSearchType, AttrDefinition attrDefinition, String playlistNameSearchString, String selectedDisplayarea, String selectedPlaylistGroup, String selectedSearchOption, String searchString, String[] selectedSearchOptions, 
			String minDate, String maxDate, String minNumber, String maxNumber, int startingRecord, boolean excludeDynamicPlaylists) throws ParseException
	{
		int result = 0;
		String hql = buildSearchHql( playlistSearchType, playlistNameSearchString, selectedDisplayarea, selectedPlaylistGroup, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, null, excludeDynamicPlaylists, true, false );		
		
		// If an attrDefinition object was not passed in, we must be searching by device group		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = KMMServlet.getRecordCount( hql, selectedSearchOptions, "attrValues" );					
			}else{
				result = KMMServlet.getRecordCount( hql );
			}
		}		
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) };
				result = KMMServlet.getRecordCount( hql, params );
			}		
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}		
		}
		return result;
	}
	
	/**
	 * Returns a page of playlists according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @param selectedItemsPerPage
	 * @return
	 * @throws ParseException
	 */
	public static Page searchPlaylists(PlaylistSearchType playlistSearchType, AttrDefinition attrDefinition, String playlistNameSearchString, String selectedDisplayareaId, String selectedPlaylistGroupId, 
			String selectedSearchOption, String searchString, String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, 
			String maxNumber, String orderBy, boolean excludeDynamicPlaylists, int startingRecord, int selectedItemsPerPage, boolean isEntityOverviewPage) throws ParseException
	{
		Page result = null;
		int pageNum = startingRecord / selectedItemsPerPage;
		String hql = buildSearchHql( playlistSearchType, playlistNameSearchString, selectedDisplayareaId, selectedPlaylistGroupId, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, orderBy, excludeDynamicPlaylists, false, isEntityOverviewPage );
		
		// If an attrDefinition object was not passed in, we must be searching by asset type		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = Playlist.getPlaylists( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = Playlist.getPlaylists( hql, pageNum, selectedItemsPerPage, selectedSearchOptions, "attrValues", isEntityOverviewPage );
			}else{
				result = Playlist.getPlaylists( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );
			}
		}
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = Playlist.getPlaylists( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );	
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = Playlist.getPlaylists( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );					
			}			
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = Playlist.getPlaylists( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );	
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = Playlist.getPlaylists( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );						
			}			
		}		
		return result;
	}	
	
	/**
	 * Builds the hql to retrieve assets according to the given search criteria.
	 *  
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param getCount
	 * @return
	 */
	private static String buildSearchHql(PlaylistSearchType playlistSearchType, String playlistSearchString, String selectedDisplayareaId, String selectedPlaylistGroupId, String selectedSearchOption, String searchString, 
			String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, String maxNumber, String orderBy, boolean excludeDynamicPlaylists, boolean getCount, boolean isEntityOverviewPage)
	{
		String hql = "";
		
		// Trim input boxes
		playlistSearchString = playlistSearchString != null && playlistSearchString.length() > 0 ? playlistSearchString.trim() : playlistSearchString;
		searchString = searchString != null && searchString.length() > 0 ? searchString.trim() : searchString;
		
		// If the playlistName search string was left blank, use wildcard
		if( playlistSearchString == null || playlistSearchString.trim().length() == 0 ){
			playlistSearchString = "%";
		}
		
		// If this is a list of playlist names
		String playlistNamesList = "";
		if(playlistSearchString.contains("~")){
			for(String s : playlistSearchString.split("~")){
				playlistNamesList += playlistNamesList.length() > 0 ? ",'" + s.trim() + "'" : "'" + s.trim() + "'";
			}
		}
		// Imply *
		else{
			if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_ID) == false){
				if(playlistSearchString.startsWith("*") == false){
					playlistSearchString = "*" + playlistSearchString;
				}
				if(playlistSearchString.endsWith("*") == false){
					playlistSearchString = playlistSearchString + "*";
				}
			}
			
			if(searchString != null && searchString.length() > 0){
				if(searchString.startsWith("*") == false){
					searchString = "*" + searchString;
				}
				if(searchString.endsWith("*") == false){
					searchString = searchString + "*";
				}
			}
		}
		
		// Convert any "*" to "%" for wildcard searches
		playlistSearchString = playlistSearchString.replaceAll("\\*", "\\%");	
		playlistSearchString = Reformat.oraesc(playlistSearchString);
		
		// Default
		if( orderBy == null || orderBy.length() == 0 ){
			orderBy = "UPPER(playlist.playlistName)";
		}
				
		// If we are counting the number of records
		if( getCount == true) {
			hql = "SELECT COUNT(playlist) ";
		} else {
			if( isEntityOverviewPage ){
				hql = "SELECT playlist, ei.lastModified ";
			}else{
				hql = "SELECT playlist ";
			}
		}

		/*
		 * If the "No Metadata", or "Date Modified" option was selected, exclude the metadata from the search criteria 
		 */
		boolean excludeMetadataCriteria = false;
		if( selectedSearchOption.equalsIgnoreCase( Constants.NO_METADATA ) || selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ))
		{
			excludeMetadataCriteria = true;
		}
		else
		{
			/*
			 * Search by this attr definition
			 */
			AttrDefinition ad = AttrDefinition.getAttrDefinition( new Long( selectedSearchOption ) );
			if( ad != null)
			{				
				// If this is a String attr
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If this is a multi-select
					if( ad.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) )
					{
						// If no items were selected
						if( selectedSearchOptions.length == 0 )
						{
							// Exclude the attrDefinition criteria							
							excludeMetadataCriteria = true;
						}
						else
						{
							// Get all playlists that have a StringAttr with the given criteria
							hql += "FROM Playlist as playlist ";
							if( isEntityOverviewPage ){
								hql += ", EntityInstance as ei "+
									"WHERE playlist.playlistId = ei.entityId "+
									"AND ";
							}else{
								hql += " WHERE ";
							}
							hql	+= " playlist.playlistId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND attr.value IN (:attrValues) ) ";
														
							// If the "All Playlists" group id ("-1") was not passed in -- limit the search to the given playlistGroupId
							if( selectedPlaylistGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND playlist.playlistId IN "
								+ 	" (SELECT playlist.playlistId "
								+ 	"	FROM PlaylistGrpMember as dgm "
								+ 	"	JOIN dgm.playlist as playlist "
								+ 	"	WHERE dgm.grp.grpId = '"+ selectedPlaylistGroupId +"') ";																		
							}							
							// If we're filtering by displayarea
							if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
							{
								// Get all playlists that have this displayarea as a secondary displayarea
								hql += "  AND "+ selectedDisplayareaId +" IN "
								+	"	(SELECT pda.displayarea.displayareaId "
								+	" 	FROM PlaylistDisplayarea as pda "
								+	" 	WHERE pda.playlist.playlistId = playlist.playlistId) ";	
							}
							
							if(excludeDynamicPlaylists){
								hql += "AND playlist.type = 'static'";
							}
							
							if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_NAME)){
								if(playlistNamesList != null && playlistNamesList.length() > 0){
									hql += "AND playlist.playlistName IN (" + playlistNamesList + ") ";
								}else{
									hql += "AND UPPER(playlist.playlistName) LIKE UPPER('"+ playlistSearchString +"') ";
								}
							}else if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_ID)){
								hql += "AND playlist.playlistId = " + playlistSearchString + " ";
							}
							hql	+= "ORDER BY " + orderBy;									
						}																		
					}
					else
					{						
						// If the searchString was left blank 
						if( searchString == null || searchString.trim().length() == 0 )
						{
							// Exclude the attrDefinition criteria					
							excludeMetadataCriteria = true;						
						}
						else
						{
							// Convert any "*" to "%" for wildcard searches		
							searchString = searchString.replaceAll("\\*", "\\%");	
							searchString = Reformat.oraesc(searchString);											
							
							// Get all playlists that have a StringAttr with the given criteria
							hql += "FROM Playlist as playlist ";
							if( isEntityOverviewPage ){
								hql += ", EntityInstance as ei "+
									"WHERE playlist.playlistId = ei.entityId "+
									"AND ";
							}else{
								hql += " WHERE ";
							}
							hql	+= " playlist.playlistId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND UPPER(attr.value) LIKE UPPER ('"+ searchString +"') ) ";
							// If the "All Playlists" group id ("-1") was not passed in -- limit the search to the given playlistGroupId
							if( selectedPlaylistGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND playlist.playlistId IN "
								+ 	" (SELECT playlist.playlistId "
								+ 	"	FROM PlaylistGrpMember as dgm "
								+ 	"	JOIN dgm.playlist as playlist "
								+ 	"	WHERE dgm.grp.grpId = '"+ selectedPlaylistGroupId +"') ";																		
							}	
							// If we're filtering by displayarea
							if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
							{
								hql += "  AND "+ selectedDisplayareaId +" IN "
								+	"	(SELECT pda.displayarea.displayareaId "
								+	" 	FROM PlaylistDisplayarea as pda "
								+	" 	WHERE pda.playlist.playlistId = playlist.playlistId) ";	
							}
							
							if(excludeDynamicPlaylists){
								hql += "AND playlist.type = 'static'";
							}
							
							if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_NAME)){
								if(playlistNamesList != null && playlistNamesList.length() > 0){
									hql += "AND playlist.playlistName IN (" + playlistNamesList + ") ";
								}else{
									hql += "AND UPPER(playlist.playlistName) LIKE UPPER('"+ playlistSearchString +"') ";
								}
							}else if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_ID)){
								hql += "AND playlist.playlistId = " + playlistSearchString + " ";
							}
							hql	+= "ORDER BY " + orderBy;
						}										
					}
				}
				// If this is a Date attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					// If both the min and max dates were left blank
					if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a DateAttr.value between the two dates
						hql += "FROM Playlist as playlist ";
						if( isEntityOverviewPage ){
							hql += ", EntityInstance as ei "+
								"WHERE playlist.playlistId = ei.entityId "+
								"AND ";
						}else{
							hql += " WHERE ";
						}
						hql	+= " playlist.playlistId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM DateAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						
							// If the "All Playlists" group id ("-1") was not passed in -- limit the search to the given playlistGroupId
							if( selectedPlaylistGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND playlist.playlistId IN "
								+ 	" (SELECT playlist.playlistId "
								+ 	"	FROM PlaylistGrpMember as dgm "
								+ 	"	JOIN dgm.playlist as playlist "
								+ 	"	WHERE dgm.grp.grpId = '"+ selectedPlaylistGroupId +"') ";																		
							}										
							// If we're filtering by displayarea
							if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
							{
								hql += "  AND "+ selectedDisplayareaId +" IN "
								+	"	(SELECT pda.displayarea.displayareaId "
								+	" 	FROM PlaylistDisplayarea as pda "
								+	" 	WHERE pda.playlist.playlistId = playlist.playlistId) ";	
							}
							
							if(excludeDynamicPlaylists){
								hql += "AND playlist.type = 'static'";
							}
							
							if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_NAME)){
								if(playlistNamesList != null && playlistNamesList.length() > 0){
									hql += "AND playlist.playlistName IN (" + playlistNamesList + ") ";
								}else{
									hql += "AND UPPER(playlist.playlistName) LIKE UPPER('"+ playlistSearchString +"') ";
								}
							}else if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_ID)){
								hql += "AND playlist.playlistId = " + playlistSearchString + " ";
							}
							hql	+= "ORDER BY " + orderBy;
					}
				}
				// If this is a Number attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// If both the min and max numbers were left blank
					if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a NumberAttr.value between the two dates
						hql += "FROM Playlist as playlist ";
						if( isEntityOverviewPage ){
							hql += ", EntityInstance as ei "+
								"WHERE playlist.playlistId = ei.entityId "+
								"AND ";
						}else{
							hql += " WHERE ";
						}
						hql	+= " playlist.playlistId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM NumberAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						// If the "All Playlists" group id ("-1") was not passed in -- limit the search to the given playlistGroupId
						if( selectedPlaylistGroupId.equalsIgnoreCase("-1") == false )
						{					
							hql += "AND playlist.playlistId IN "
							+ 	" (SELECT playlist.playlistId "
							+ 	"	FROM PlaylistGrpMember as dgm "
							+ 	"	JOIN dgm.playlist as playlist "
							+ 	"	WHERE dgm.grp.grpId = '"+ selectedPlaylistGroupId +"') ";																		
						}								
						// If we're filtering by displayarea
						if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
						{
							hql += "  AND "+ selectedDisplayareaId +" IN "
							+	"	(SELECT pda.displayarea.displayareaId "
							+	" 	FROM PlaylistDisplayarea as ppda "
							+	" 	WHERE pda.playlist.playlistId = playlist.playlistId) ";	
						}
						
						if(excludeDynamicPlaylists){
							hql += "AND playlist.type = 'static'";
						}
						
						if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_NAME)){
							if(playlistNamesList != null && playlistNamesList.length() > 0){
								hql += "AND playlist.playlistName IN (" + playlistNamesList + ") ";
							}else{
								hql += "AND UPPER(playlist.playlistName) LIKE UPPER('"+ playlistSearchString +"') ";
							}
						}else if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_ID)){
							hql += "AND playlist.playlistId = " + playlistSearchString + " ";
						}
						hql	+= "ORDER BY " + orderBy;
					}
				}				
			}
		}
		
		// If we're excluding metadata in the search criteris
		if( excludeMetadataCriteria )
		{
			hql += "FROM Playlist as playlist ";
			if( isEntityOverviewPage ){
				hql += ", EntityInstance as ei "+
					"WHERE playlist.playlistId = ei.entityId "+
					"AND ";
			}else{
				hql += " WHERE ";
			}
			
			if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_NAME)){
				if(playlistNamesList != null && playlistNamesList.length() > 0){
					hql += " playlist.playlistName IN (" + playlistNamesList + ") ";
				}else{
					hql += " UPPER(playlist.playlistName) LIKE UPPER('"+ playlistSearchString +"') ";
				}
			}else if(playlistSearchType.equals(PlaylistSearchType.PLAYLIST_ID)){
				hql += " playlist.playlistId = " + playlistSearchString + " ";
			}

				// If the "All Playlists" group id ("-1") was not passed in -- limit the search to the given playlistGroupId
				if( selectedPlaylistGroupId.equalsIgnoreCase("-1") == false )
				{					
					hql += " AND playlist.playlistId IN "
					+ 	" (SELECT playlist.playlistId "
					+ 	"	FROM PlaylistGrpMember as dgm "
					+ 	"	JOIN dgm.playlist as playlist "
					+ 	"	WHERE dgm.grp.grpId = '"+ selectedPlaylistGroupId +"') ";									
				}				
				// If we're filtering by displayarea
				if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
				{				
					hql +=   " AND "+ selectedDisplayareaId +" IN "
					+	"(SELECT pda.displayarea.displayareaId "
					+	" FROM PlaylistDisplayarea as pda "
					+	" WHERE pda.playlist.playlistId = playlist.playlistId) ";	
				}								
				// If we're filtering by last modified date
				if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) )
				{
					hql +=   " AND playlist.playlistId IN "
						+	"(SELECT ei.entityId "
						+	" FROM EntityInstance as ei "	
						+ 	" WHERE ei.entityClass.className = '"+ Playlist.class.getName() +"' "
						+	" AND ei.lastModified >= ? "
						+	" AND ei.lastModified <= ?) ";					
				}
				
				if(excludeDynamicPlaylists){
					hql += "AND playlist.type = 'static'";
				}
				
				hql += "ORDER BY " + orderBy;		
		}
		return hql;
	}	
	
	/**
	 * @return Returns a page of playlists
	 */
	public static Page getPlaylists(String hql, int pageNum, int iSelectedItemsPerPage, Object[] params, String namedParameter, boolean isEntityOverviewPage) throws HibernateException 
	{
		Session session = HibernateSession.currentSession();		
		Query q = session.createQuery( hql );

		// If the params parameter was passed in, use them in the query
		if( params != null )
		{
			// If a namedParameter was passed in -- use it
			// This is required because .setParameterList(int, Object[]) is not supported
			if( namedParameter != null ){
				q.setParameterList( namedParameter, params );
			}
			else{
				for( int i=0; i<params.length; i++ ){
					q.setParameter( i, params[i] );
				}
			}
		}
		
		if( isEntityOverviewPage ){
			return new EntityOverviewPage( q, pageNum, iSelectedItemsPerPage );
		}else{
			return new PlaylistResultsPage( q, pageNum, iSelectedItemsPerPage );	
		}
	}	
	
	/**
	 * Updates the set of PlaylistDisplayareas associated with this playlist.
	 *
	 */
	public void resetPlaylistDisplayareas()
	{
		HibernateSession.startBulkmode();
		
		// First, get existing values
		Map<Long, PlaylistDisplayarea> pdas = new HashMap<Long, PlaylistDisplayarea>();
		for(PlaylistDisplayarea pda : this.playlistDisplayareas){
			pdas.put(pda.getDisplayarea().getDisplayareaId(), pda);
		}
		
		// For each secondary displayarea
		for( Iterator i=this.getSecondaryContentRotations().iterator(); i.hasNext(); ){
			// Create a new PlaylistDisplayarea object
			Displayarea da = (Displayarea)i.next();
			if(pdas.keySet().contains(da.getDisplayareaId()) == false){
				PlaylistDisplayarea pda = PlaylistDisplayarea.create( this, da, false );
				this.playlistDisplayareas.add( pda );
			}else{
				pdas.remove(da.getDisplayareaId());
			}
		}
		
		// For each playlist displayarea being removed
		for(PlaylistDisplayarea pda : pdas.values()){
			for( Iterator i=PlaylistContentRotation.getPlaylistContentRotations(this, pda.getDisplayarea()).iterator(); i.hasNext();){
				
				PlaylistContentRotation pcr = (PlaylistContentRotation)i.next();
				
				// Delete all playlist content rotation targets
				for(Iterator j=ContentRotationTarget.getContentRotationTargets(pcr.getContentRotation(), this, pda.getDisplayarea()).iterator();j.hasNext();){
					ContentRotationTarget crt = (ContentRotationTarget)j.next();
					crt.delete();
				}
			}
			
			// Delete the playlist display area
			this.playlistDisplayareas.remove(pda);
			pda.delete();
		}
		
		this.update();
		
		HibernateSession.stopBulkmode();
	}
	
	/**
	 * Returns a list of unique displayareas and their respective layouts for this playlist.
	 * @return
	 */
	private List getSecondaryContentRotations()
	{
		// Get all unique displayareas and their respective layouts for this playlist
		List allLayoutsAndDisplayareas = this.getLayoutsAndDisplayareas();
		
		// Get the list of unique "primary" displayareas and their respective layouts
		List primaryLayoutsAndDisplayareas = this.getPrimaryLayoutsAndDisplayareas();
		
		// For each "primary" displayarea
		for( Iterator i=primaryLayoutsAndDisplayareas.iterator(); i.hasNext(); )
		{			
			Object[] primaryLayoutAndDisplayarea = (Object[])i.next();
			Layout l = (Layout)primaryLayoutAndDisplayarea[0];
			Displayarea da = (Displayarea)primaryLayoutAndDisplayarea[1];
						
			// For each displayarea in this displayarea's layout,
			// determine if there is a matching displayarea in the primary list
			boolean removeFromContentRotations = true;
			for( Iterator j=l.getLayoutDisplayareas().iterator(); j.hasNext(); )
			{				
				LayoutDisplayarea lda = (LayoutDisplayarea)j.next();
				Displayarea secondaryDisplayarea = lda.getDisplayarea();
			
				// Ignore the "primary" displayarea in this layout
				if( secondaryDisplayarea.getDisplayareaId() != da.getDisplayareaId() )
				{
					// If there is a matching "primary" displayarea
					if( layoutDisplayareaExistsInPrimaryList( primaryLayoutsAndDisplayareas, l, secondaryDisplayarea ) )
					{
						// Set the flag so we don't remove it from the list of content rotations 
						removeFromContentRotations = false;
						break;
					}		
				}		
			}
			
			// If we did not find a matching secondary displayarea for this layout in our list of primaries
			if( removeFromContentRotations )
			{
				// Remove this displayarea/layout from the list of secondary content rotations
				for( Iterator j=allLayoutsAndDisplayareas.iterator(); j.hasNext(); )
				{
					Object[] layoutAndDisplayarea = (Object[])j.next();
					Layout testLayout = (Layout)layoutAndDisplayarea[0];
					Displayarea testDisplayarea = (Displayarea)layoutAndDisplayarea[1];
					if( testLayout.getLayoutId() == l.getLayoutId() && testDisplayarea.getDisplayareaId() == da.getDisplayareaId() )
					{
						j.remove();
						break;
					}
				}
			}			
		}
		
		// Unique the list of displayareas
		HashSet uniqueDisplayareas = new HashSet();
		for( Iterator j=allLayoutsAndDisplayareas.iterator(); j.hasNext(); )
		{
			Object[] layoutAndDisplayarea = (Object[])j.next();			
			Displayarea displayarea = (Displayarea)layoutAndDisplayarea[1];
			uniqueDisplayareas.add( displayarea );
		}
		
	    List secondaryDisplayareas = new LinkedList(uniqueDisplayareas);
		BeanPropertyComparator comparator = new BeanPropertyComparator("displayareaName");		
		Collections.sort(secondaryDisplayareas, comparator);	
		return secondaryDisplayareas;
	}	
	
	private boolean layoutDisplayareaExistsInPrimaryList(List primaryList, Layout layout, Displayarea displayarea)
	{
		boolean result = false;
		
		// Attempt to find the given layout and displayarea in the list of primary secondary content rotations
		for( Iterator j=primaryList.iterator(); j.hasNext(); )
		{
			Object[] layoutAndDisplayarea = (Object[])j.next();
			Layout testLayout = (Layout)layoutAndDisplayarea[0];
			Displayarea testDisplayarea = (Displayarea)layoutAndDisplayarea[1];
			if( testLayout.getLayoutId() == layout.getLayoutId() && testDisplayarea.getDisplayareaId() == displayarea.getDisplayareaId() )
			{
				result = true;
				break;
			}
		}
		
		return result;
	}	
	
	/**
	 * This method ensures that we have PlaylistDisplayarea objects for 
	 * all secondary displayareas of a playlist. This is important because 
	 * our search algorithms (for playlists and content rotations) rely
	 * on those rows being there.
	 *  
	 * NOTE:  We could consider doing this for each asset in the playlist,
	 * but for performance reasons, we'll just do it for the single given playlist asset.
	 * Called when either creating or modifying a playlist asset.
	 * @param pa
	 */
	public void createPlaylistDisplayareas(PlaylistAsset pa)
	{
		// Get the layout for this pa
		Layout layout = pa.loadAssetPresentation().getLayout();
		Displayarea playlistAssetDisplayarea = pa.loadAssetPresentation().getDisplayarea();
		
		// For each displayarea in the layout that is not the playlist asset's displayarea (all secondary displayareas)
		for( Iterator i=layout.getLayoutDisplayareas().iterator(); i.hasNext(); )
		{
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			if( lda.getDisplayarea().getDisplayareaId() != playlistAssetDisplayarea.getDisplayareaId() )
			{
				// If a PlaylistDisplayarea does not yet exist for this secondary content rotation
				PlaylistDisplayarea pd = PlaylistDisplayarea.getPlaylistDisplayarea( this, lda.getDisplayarea() );
				if( pd == null )
				{
					// Create a new one
					PlaylistDisplayarea.create( this, lda.getDisplayarea(), false );					
				}
			}
		}
	}
	
	/**
	 * Returns a list of segments that contain this playlist
	 * as one of it's PlaylistSegmentParts, sorted by segment name.
	 * @return
	 */
	public List<Segment> getSegments()
	{
		// Get all segments that have this playlist as one of the PlaylistSegmentParts
		HashSet<Segment> uniqueSegments = new HashSet<Segment>();
		for( Iterator<PlaylistSegmentPart> i=this.getPlaylistSegmentParts().iterator(); i.hasNext(); )
		{
			PlaylistSegmentPart psp = i.next();
			uniqueSegments.add( psp.getSegment() );
		}
			
		// Get any segments that have this playlist as its assetIntervalPlaylist
		String hql = "SELECT s FROM Segment s "
			+ "	WHERE s.assetIntervalPlaylist.playlistId = :playlistId";
			Session session = HibernateSession.currentSession();		
		List<Segment> l = session.createQuery( hql ).setParameter("playlistId", this.getPlaylistId()).list();
		uniqueSegments.addAll( l );
		
		LinkedList<Segment> result = new LinkedList<Segment>();
		result.addAll( uniqueSegments );
		BeanComparator comparator = new BeanComparator("segmentName");
		Collections.sort( result, comparator );		
		return result;
	}	
	
	public PlaylistContentRotation getPlaylistContentRotation(long displayAreaId) {
		PlaylistContentRotation result = null;
		
		for (PlaylistContentRotation element : this.getPlaylistContentRotations()) {
			if (element.getDisplayarea().getDisplayareaId() == displayAreaId)
			{
				result = element;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * If this playlist is an assetIntervalPlaylist for any segment,
	 * remove this playlist from that segment before deleting. 
	 * @throws HibernateException
	 */
	public void delete() throws HibernateException
	{		
		// If this playlist is an assetIntervalPlaylist for any segment
		List l = Segment.getSegmentsWithAssetIntervalPlaylist( this );
		for( Iterator i=l.iterator(); i.hasNext(); )
		{
			// Remove the asset interval properties for this segment
			Segment segment = (Segment)i.next();
			segment.setAssetIntervalFrequency( null );
			segment.setAssetIntervalNumAssets( null );
			segment.setAssetIntervalPlaylist( null );
			segment.setAssetIntervalUnits( null );
			segment.setUseAssetIntervalPlaylist( Boolean.FALSE );
			segment.update();
		}
				
		// Save off the assetPresentationIds that would be left behind folowing the deletion of this playlist
		// We cannot delete the assetPresentations prior to deleting the playlist because it would cause a 
		// child record integrity constraint violation.  Further, if there are more than 1000 assetPresenations,
		// we must split them into separate SQL statements
		String hql = "SELECT pa.assetPresentation.assetPresentationId FROM PlaylistAsset pa "
		+ "	WHERE pa.playlist.playlistId = "+ this.getPlaylistId().toString() +")";
		Session session = HibernateSession.currentSession();		
		List assetPresentations = session.createQuery( hql ).list();
		String[] assetPresentationIdsArray = new String[ (assetPresentations.size()/1000) + 1 ];
		Iterator i=assetPresentations.iterator();
		for( int count = 0; count<assetPresentations.size(); count++ )
		{
			int j = count/1000;
			if( count%1000 == 0 ) assetPresentationIdsArray[j] = "";
			Long assetPresentationId = (Long)i.next();
			if( assetPresentationId != null ) {
				if( assetPresentationIdsArray[j].length() > 0 ){
					assetPresentationIdsArray[j] += ",";
				}
				assetPresentationIdsArray[j] += assetPresentationId.toString();				
			}
			if( i.hasNext() == false ) {
				break;
			}
			i.next();
			count++;
		}
		
		// Delete all dynamic content parts
		for(DynamicContentPart dcp : DynamicContentPart.getDynamicContentParts(this)){
			dcp.delete();
		}
		
		// Delete all dynamic query parts
		for(DynamicQueryPart dqp : DynamicQueryPart.getDynamicQueryParts(this)){
			dqp.delete();
		}
		
		// Delete the playlist
		super.delete();
		
		// Remove any assetPresentations associated with this playlist.
		// We cannot rely on cascade delete in the db because the assetPresentation
		// record could be associated with either a playlistAsset or an Asset
		if( assetPresentations.size() > 0 ){
			HibernateSession.beginTransaction();
			for( int j=0; j<assetPresentationIdsArray.length; j++ ) {
				if( assetPresentationIdsArray[j].length() > 0 ) {
					hql = "DELETE FROM AssetPresentation WHERE asset_presentation_id IN (" + assetPresentationIdsArray[j] + ")";
					session.createQuery( hql ).executeUpdate();
				}
			}
			HibernateSession.commitTransaction();
		}
	}
	
	public void generateHql(Date dateToUse){
		String hql = DynamicQueryPart.generateHql(DynamicQueryPart.getDynamicQueryParts(this), dateToUse);
		this.setHql(hql);
		this.update();
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof Playlist) ) result = false;
		
		Playlist c = (Playlist) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "Playlist".hashCode();
		result = Reformat.getSafeHash( this.getPlaylistId().toString(), result, 3 );
		result = Reformat.getSafeHash( this.getPlaylistName(), result, 11 );		
		return result < 0 ? -result : result;
	}		
	
	/**
	 * @return Returns the playlistId.
	 */
	public Long getPlaylistId() {
		return playlistId;
	}

	/**
	 * @param playlistId The playlistId to set.
	 */
	public void setPlaylistId(Long playlistId) {
		this.playlistId = playlistId;
	}

	/**
	 * @return Returns the playlistName.
	 */
	public String getPlaylistName() {
		return playlistName;
	}

	/**
	 * @param playlistName The playlistName to set.
	 */
	public void setPlaylistName(String playlistName) {
		this.playlistName = playlistName;
	}

	/**
	 * @return Returns the contentRotations.
	 */
	public Set<PlaylistContentRotation> getPlaylistContentRotations() {
		return playlistContentRotations;
	}

	/**
	 * @param contentRotations The contentRotations to set.
	 */
	public void setPlaylistContentRotations(Set contentRotations) {
		this.playlistContentRotations = contentRotations;
	}

	/**
	 * @return Returns the playlistGrpMembers.
	 */
	public Set<PlaylistGrpMember> getPlaylistGrpMembers() {
		return playlistGrpMembers;
	}

	/**
	 * @param playlistGrpMembers The playlistGrpMembers to set.
	 */
	public void setPlaylistGrpMembers(Set<PlaylistGrpMember> playlistGrpMembers) {
		this.playlistGrpMembers = playlistGrpMembers;
	}

	/**
	 * @return Returns the playlistSegmentParts.
	 */
	public Set getPlaylistSegmentParts() {
		return playlistSegmentParts;
	}

	/**
	 * @param playlistSegmentParts The playlistSegmentParts to set.
	 */
	public void setPlaylistSegmentParts(Set playlistSegmentParts) {
		this.playlistSegmentParts = playlistSegmentParts;
	}

	/**
	 * @return Returns the length.
	 */
	public Float getLength() {
		return length;
	}

	/**
	 * @param length The length to set.
	 */
	public void setLength(Float length) {
		this.length = length;
	}

	/**
	 * @return the playlistAssets
	 */
	public List<PlaylistAsset> getPlaylistAssets() {
		return playlistAssets;
	}

	/**
	 * @param playlistAssets the playlistAssets to set
	 */
	public void setPlaylistAssets(List<PlaylistAsset> playlistAssets) {
		this.playlistAssets = playlistAssets;
	}

	/**
	 * @return Returns the hql.
	 */
	public String getHql() {
		return hql;
	}

	/**
	 * @param hql The hql to set.
	 */
	public void setHql(String hql) {
		this.hql = hql;
	}
	
	/**
	 * @return Returns the customMethod.
	 */
	public String getCustomMethod() {
		return customMethod;
	}
	
	/**
	 * @param customMethod The customMethod to set.
	 */
	public void setCustomMethod(String customMethod) {
		this.customMethod = customMethod;
	}

	/**
	 * @return Returns the runFromContentScheduler.
	 */
	public Integer getRunFromContentScheduler() {
		return runFromContentScheduler;
	}
	
	/**
	 * @param runFromContentScheduler The runFromContentScheduler to set.
	 */
	public void setRunFromContentScheduler(Integer runFromContentScheduler) {
		this.runFromContentScheduler = runFromContentScheduler;
	}
	
	/**
	 * @return Returns the presentationStyle.
	 */
	public PresentationStyle getPresentationStyle() {
		return presentationStyle;
	}
	/**
	 * @param presentationStyle The presentationStyle to set.
	 */
	public void setPresentationStyle(PresentationStyle presentationStyle) {
		this.presentationStyle = presentationStyle;
	}
	/**
	 * @return Returns the maxResults.
	 */
	public Integer getMaxResults() {
		return maxResults;
	}
	/**
	 * @param maxResults The maxResults to set.
	 */
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * @return Returns the csvImportDate.
	 */
	public Date getCsvImportDate() {
		return csvImportDate;
	}
	

	/**
	 * @param csvImportDate The csvImportDate to set.
	 */
	public void setCsvImportDate(Date csvImportDate) {
		this.csvImportDate = csvImportDate;
	}	

	/**
	 * @return the csvImportDetail
	 */
	public Clob getCsvImportDetail() {
		return csvImportDetail;
	}

	/**
	 * @param csvImportDetail the csvImportDetail to set
	 */
	public void setCsvImportDetail(Clob csvImportDetail) {
		this.csvImportDetail = csvImportDetail;
	}

	/**
	 * @return Returns the csvImportStatus.
	 */
	public PlaylistImportStatus getCsvImportStatus() {
		return csvImportStatus;
	}
	

	/**
	 * @param csvImportStatus The csvImportStatus to set.
	 */
	public void setCsvImportStatus(PlaylistImportStatus csvImportStatus) {
		this.csvImportStatus = csvImportStatus;
	}

	/**
	 * @return Returns the playlistOrder.
	 */
	public PlaylistOrderType getPlaylistOrder() {
		return playlistOrder;
	}
	

	/**
	 * @param playlistOrder The playlistOrder to set.
	 */
	public void setPlaylistOrder(PlaylistOrderType playlistOrder) {
		this.playlistOrder = playlistOrder;
	}

	/*
	 * get non-persistent copy of playlistAssets
	 */
	public List getMyPlaylistAssets() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
	{
		/*
		 * If the current list is null, create a copy and return it
		 * Otherwise return a new copy
		 */
		if( myPlaylistAssets == null )
		{
			resetMyPlaylistAssets();
		}
		
		return myPlaylistAssets;
	}

	/*
	 * reset non-persistent copy of playlistAssets to current playlistAssets List
	 */
	public void resetMyPlaylistAssets() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
	{		
		this.myPlaylistAssets = new LinkedList();
		for( Iterator i = this.getPlaylistAssets().iterator(); i.hasNext(); )
		{
			PlaylistAsset pa = (PlaylistAsset) (i.next());
			PlaylistAsset mypa = new PlaylistAsset();
			mypa.setPlaylist( pa.getPlaylist() );
			mypa.setAsset( pa.getAsset() );
			
			// Copy the appropriate asset presentation so we don't accidentally save it later
			AssetPresentation ap = new AssetPresentation();
			PropertyUtils.copyProperties(ap, pa.loadAssetPresentation());			
			mypa.setAssetPresentation( ap );
			mypa.setSeqNum( pa.getSeqNum() );
			myPlaylistAssets.add( mypa );
		}
		this.myLength = this.getLength().floatValue();
	}

	/*
	 * change the non-persistent copy of playlistAssets (used for asset exclusion and randomization)
	 */
	public void setMyPlaylistAssets(List myPlaylistAssets)
	{
		this.myPlaylistAssets = myPlaylistAssets;
		this.myLength = 0;
		for( Iterator i = this.myPlaylistAssets.iterator(); i.hasNext(); )
		{
			PlaylistAsset pa = (PlaylistAsset) (i.next());
			myLength += pa.loadAssetPresentation().getLength();
		}
	}
	
	private class ImportContentRotationThread extends Thread{
		Long playlistId, contentRotationId, layoutId, displayareaId;
		boolean appendAssetsToPlaylist;
		boolean makeDirty;
		String schemaName;
		String appUsername;
	}

	/*
	 * get length associated with non-persistent copy of playlistAssets
	 */
	public float getMyLength()
	{
		return this.myLength;
	}

	/**
	 * @return Returns the contentSchedulerHook.
	 */
	public ContentSchedulerHookType getContentSchedulerHook() {
		return contentSchedulerHook;
	}
	

	/**
	 * @param contentSchedulerHook The contentSchedulerHook to set.
	 */
	public void setContentSchedulerHook(ContentSchedulerHookType contentSchedulerHook) {
		this.contentSchedulerHook = contentSchedulerHook;
	}

	/**
	 * @return Returns the playlistDisplayareas.
	 */
	public Set getPlaylistDisplayareas() {
		return playlistDisplayareas;
	}
	
	/**
	 * @param playlistDisplayareas The playlistDisplayareas to set.
	 */
	public void setPlaylistDisplayareas(Set playlistDisplayareas) {
		this.playlistDisplayareas = playlistDisplayareas;
	}

	public Asset getLastAsset() {
		return lastAsset;
	}

	public void setLastAsset(Asset lastAsset) {
		this.lastAsset = lastAsset;
	}

	/**
	 * @return Returns the contentRotationImportDetail.
	 */
	public String getContentRotationImportDetail() {
		return contentRotationImportDetail;
	}
	
	/**
	 * @param contentRotationImportDetail The contentRotationImportDetail to set.
	 */
	public void setContentRotationImportDetail(String contentRotationImportDetail) {
		this.contentRotationImportDetail = contentRotationImportDetail;
	}

	/**
	 * @return Returns the playlistImports.
	 */
	public Set getPlaylistImports() {
		return playlistImports;
	}
	
	/**
	 * @param playlistImports The playlistImports to set.
	 */
	public void setPlaylistImports(Set playlistImports) {
		this.playlistImports = playlistImports;
	}
	
	/**
	 * @return Returns the playlistImports, sorted by importDate
	 */
	public LinkedList getPlaylistImportsSorted() {
		LinkedList result = new LinkedList();
		result.addAll( this.getPlaylistImports() );
		BeanComparator comparator = new BeanComparator("importDate");
		Collections.sort( result, comparator );	
		return result;
	}

	/**
	 * @return Returns the transition.
	 */
	public ScreenTransitionType getTransition() {
		return transition;
	}
	
	/**
	 * @param transition The transition to set.
	 */
	public void setTransition(ScreenTransitionType transition) {
		this.transition = transition;
	}

	/**
	 * @return the myPlaylistAssetLengths
	 * @param uniqueid if the playlist is being tracked as an segment part, then the uniqueId is the deviceId-segmentId.  If it's being tracked as additional content, then it's deviceId-segmentId-additional
	 */	
	public List getMyPlaylistAssetLengths(String uniqueId) 
	{
		List myPlaylistAssetLengths = (List) myPlaylistAssetLengthsHash.get( uniqueId );
		if( myPlaylistAssetLengths == null )
		{
			myPlaylistAssetLengths = new LinkedList();
			myPlaylistAssetLengthsHash.put(uniqueId, myPlaylistAssetLengths);
		}
		return myPlaylistAssetLengths;
	}

	/**
	 * @param myPlaylistAssetLengths the myPlaylistAssetLengths to set
	 */
	public void setMyPlaylistAssetLengths(String uniqueId, List myPlaylistAssetLengths) {
		myPlaylistAssetLengthsHash.put(uniqueId, myPlaylistAssetLengths);
	}
	
	public void deleteAllPlaylistAssets(){
		try {
			HibernateSession.startBulkmode();
			List<PlaylistAsset> plAssets = this.getPlaylistAssets();
			logger.info("VAMOS BIEN: " + plAssets.size());
			for (PlaylistAsset pla : plAssets)
				pla.delete();					
			plAssets.clear();
		} catch (Exception e) {
			HibernateSession.rollbackBulkmode();
		} finally {
			HibernateSession.stopBulkmode();
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getUseRoles() {
		return useRoles;
	}

	public void setUseRoles(Boolean useRoles) {
		this.useRoles = useRoles;
	}

	public Integer getNumAssets() {
		return numAssets;
	}

	public void setNumAssets(Integer numAssets) {
		this.numAssets = numAssets;
	}

	public String getDynamicContentDisplay() {
		return dynamicContentDisplay;
	}

	public void setDynamicContentDisplay(String dynamicContentDisplay) {
		this.dynamicContentDisplay = dynamicContentDisplay;
	}

	public Layout getDynamicContentLayout() {
		return dynamicContentLayout;
	}

	public void setDynamicContentLayout(Layout dynamicContentLayout) {
		this.dynamicContentLayout = dynamicContentLayout;
	}

	public Displayarea getDynamicContentDisplayarea() {
		return dynamicContentDisplayarea;
	}

	public void setDynamicContentDisplayarea(Displayarea dynamicContentDisplayarea) {
		this.dynamicContentDisplayarea = dynamicContentDisplayarea;
	}

	public List<DynamicContentPart> getDynamicContentParts() {
		return dynamicContentParts;
	}

	public void setDynamicContentParts(List<DynamicContentPart> dynamicContentParts) {
		this.dynamicContentParts = dynamicContentParts;
	}

	public Date getLastDynamicUpdateDt() {
		return lastDynamicUpdateDt;
	}

	public void setLastDynamicUpdateDt(Date lastDynamicUpdateDt) {
		this.lastDynamicUpdateDt = lastDynamicUpdateDt;
	}

	public String getDynamicContentType() {
		return dynamicContentType;
	}

	public void setDynamicContentType(String dynamicContentType) {
		this.dynamicContentType = dynamicContentType;
	}

	public Integer getAvgLoopLength() {
		return avgLoopLength;
	}

	public void setAvgLoopLength(Integer avgLoopLength) {
		this.avgLoopLength = avgLoopLength;
	}
}