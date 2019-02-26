package com.kuvata.kmf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import parkmedia.KMFLogger;
import parkmedia.usertype.AssetType;
import parkmedia.usertype.ContentUpdateType;
import parkmedia.usertype.DirtyType;
import parkmedia.usertype.DownloadPriorityType;


public class Dirty extends PersistentEntity
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Dirty.class );
	private Long dirtyId;
	private Long dirtyEntityId;
	private DirtyType dirtyType;
	private String status;
	private AppUser appUser;
	private Date createDt;
	
	/**
	 * 
	 *
	 */
	public Dirty()
	{		
	}
	
	/**
	 * 
	 * @param dirtyEntityId
	 * @return
	 * @throws HibernateException
	 */
	public static Dirty getDirty(Long dirtyEntityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		Iterator iter = session.createQuery("from Dirty d where d.dirtyEntityId=?")
					.setParameter(0, dirtyEntityId)
					.iterate(); 
		Dirty result = iter.hasNext() ? (Dirty) iter.next() : null;
		Hibernate.close( iter );
		return result;
	}
	
	/**
	 * 
	 * @param dirtyId
	 * @return
	 * @throws HibernateException
	 */
	public static List getDirtyInstances(List dirtyIds) throws HibernateException
	{
		return Entity.load(Dirty.class, dirtyIds);		
	}
	
	/**
	 * 
	 * @param dirtyId
	 * @return
	 * @throws HibernateException
	 */
	public static Dirty getDirtyInstance(Long dirtyId) throws HibernateException
	{
		return (Dirty)PersistentEntity.load(Dirty.class, dirtyId);		
	}
	
	/**
	 * 
	 * @param dirtyType
	 * @return
	 * @throws HibernateException
	 */
	public static List<Dirty> getDirtyEntities(DirtyType dirtyType) throws HibernateException
	{
		return getDirtyEntities( new DirtyType[]{ dirtyType} );
	}
	
	/**
	 * Returns all dirty objects of the given dirtyType with the given dirtyEntityId.
	 * 
	 * @param dirtyType
	 * @return
	 * @throws HibernateException
	 */
	public static List getDirtyEntities(DirtyType dirtyType, Long dirtyEntityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l= session.createQuery("from Dirty d where d.dirtyType=? and d.dirtyEntityId=?")
					.setParameter(0, dirtyType)
					.setParameter(1, dirtyEntityId)					
					.list(); 
		return l; 		
	}	
	
	/**
	 * Returns all dirty objects of the given dirtyType in the given dirtyEntityIds list.
	 * 
	 * @param dirtyType
	 * @return
	 * @throws HibernateException
	 */
	public static List getDirtyEntities(DirtyType dirtyType, List dirtyEntityIds) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List result = new ArrayList();
		if(dirtyEntityIds != null && dirtyEntityIds.size() > 0)
		{
			// Make a copy of the entityIds so that manipulating it does not affect other processing
			List copyOfEntityIds = new ArrayList(dirtyEntityIds);
			int toIndex = 0;
			while( copyOfEntityIds.size() > 0 )
			{						
				try 
				{
					/*
					 * Limit the number of entities we select at a time (to avoid max sql length error)
					 */
					if( copyOfEntityIds.size() > Constants.MAX_NUMBER_EXPRESSIONS_IN_LIST ){
						toIndex = Constants.MAX_NUMBER_EXPRESSIONS_IN_LIST;				
					}else{
						toIndex = copyOfEntityIds.size();
					}					
					List l= session.createQuery("from Dirty d where d.dirtyType=? and d.dirtyEntityId in (:dirtyEntityIds)")
						.setParameter(0, dirtyType)
						.setParameterList("dirtyEntityIds", copyOfEntityIds.subList( 0, toIndex ))					
						.list();					
					result.addAll( l );					
				} 
				catch (HibernateException e) {
					logger.error( e );
				} 
				finally 
				{
					// Make sure we're always removing from the list -- even if the hql statement fails 
					copyOfEntityIds.subList( 0, toIndex ).clear();
				}
			}				
		}
		return result; 		
	}
	
	/**
	 * Returns all dirty objects with the given dirtyEntityId.
	 * 
	 * @param dirtyType
	 * @return
	 * @throws HibernateException
	 */
	public static List getDirtyEntities(Long dirtyEntityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l= session.createQuery("from Dirty d where d.dirtyEntityId=?")					
					.setParameter(0, dirtyEntityId)					
					.list(); 
		return l; 		
	}
	
	/**
	 * Returns all dirty ids with the given dirtyEntityId.
	 * 
	 * @param dirtyType
	 * @return
	 * @throws HibernateException
	 */
	public static List getDirtyEntityIds(Long dirtyEntityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l= session.createQuery("Select d.dirtyId from Dirty d where d.dirtyEntityId=?")
					.setParameter(0, dirtyEntityId)					
					.list(); 
		return l; 		
	}
	
	/**
	 * Returns a list of dirty objects that have a dirty type in the given array of dirtyTypes. 
	 * 
	 * @param dirtyType
	 * @return
	 * @throws HibernateException
	 */
	public static List<Dirty> getDirtyEntities(DirtyType[] dirtyTypes) throws HibernateException
	{
		String strDirtyTypes = "";
		for( int i=0; i<dirtyTypes.length; i++ )
		{
			if( strDirtyTypes.length() > 0 ){
				strDirtyTypes += ", ";
			}
			strDirtyTypes += "'"+ dirtyTypes[i].getPersistentValue() +"'";
		}		
		String hql = "SELECT d from Dirty d "
			+ "WHERE d.dirtyType "
			+ "IN ("+ strDirtyTypes +")";		
		Session session = HibernateSession.currentSession();			
		return session.createQuery( hql ).list();
	}	
	
	/**
	 * Returns a list of all dirty objects.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getDirtyEntities() throws HibernateException
	{
		String hql = "SELECT d from Dirty d";
		Session session = HibernateSession.currentSession();			
		return session.createQuery( hql ).list();
	}	
	
	/**
	 * Returns all list of entityIds associated with the dirty objects of the given dirtyType
	 * 
	 * @param dirtyType
	 * @return
	 * @throws HibernateException
	 */
	public static List<Long> getDirtyEntityIds(DirtyType dirtyType) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		return session.createQuery("SELECT d.dirtyEntityId FROM Dirty d where d.dirtyType=?")
						.setParameter(0, dirtyType)					
						.list(); 		
	}	
	
	/**
	 * Returns a list of all dirty objects of type ASSET_LENGTH_CHANGED, ASSET_LENGTH_UNCHANGED or ASSET_EXPIRATION_CHANGED
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getDirtyAssets() throws HibernateException
	{
		return Dirty.getDirtyEntities( new DirtyType[]{ DirtyType.ASSET_LENGTH_CHANGED, DirtyType.ASSET_LENGTH_UNCHANGED, DirtyType.ASSET_EXPIRATION_CHANGED } );
	}		
		
	/**
	 * If there are any dirty objects with the given dirty types
	 * return true, else return false
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static boolean isDirty(DirtyType[] dirtyTypes) throws HibernateException
	{		
		List l = Dirty.getDirtyEntities( dirtyTypes ); 
		return l.iterator().hasNext() ? true : false; 		
	}
	
	/**
	 * Clear the dirty status for all dirty objects that have the given entityId
	 * 
	 * @throws HibernateException
	 */
	public static void makeNotDirty(Entity e) throws HibernateException
	{		
		List l = Dirty.getDirtyEntities( e.getEntityId() );
		for( Iterator i=l.iterator(); i.hasNext(); ){
			Dirty d = (Dirty)i.next();
			d.delete();
		}		
	}
	
	/**
	 * Determines if any dirty devices can be deleted and deletes them.
	 *
	 */
	public static synchronized List clearDirtyDevices(String csParam)
	{
		// Stores a list of devices that were published.
		List result = new ArrayList();
		
		// List that holds device Id's for devices that need to be published
		ArrayList dirtyIdsToPublish = new ArrayList();
		
		// Temporary dirty list that stores dirty entities/Id's
		ArrayList dirtyEntities = new ArrayList();
		
		// Stores entity id's mapped to their dirty id's
		HashMap dirtyIdMap = new HashMap();
		
		// Get all dirty objects of type DEVICE
		List dirtyDevices = Dirty.getDirtyEntities( DirtyType.DEVICE );
		
		/*
		 * Create a map of deviceIds and their respective dirtyIds
		 * This way we can query the device table just once
		 */
		for( Iterator i=dirtyDevices.iterator(); i.hasNext(); )
		{
			Dirty dirty = (Dirty)i.next();
			
			// Add the entity id and dirty id to the map
			dirtyIdMap.put(dirty.getDirtyEntityId(), dirty.getDirtyId());
			
			// Add the entity id to the dirty list to get the list of devices associated with it
			dirtyEntities.add(dirty.getDirtyEntityId());
		}
		
		// If we've determined that there are any dirty devices -- get them in one query		
		dirtyEntities = (ArrayList)Device.getDevices( dirtyEntities );
		
		// If there are any dirty segments, content rotations, playlists or assets, we need to check if we can clear the dirty flag for each device
		boolean otherDirtyObjectsExist = false;
		if( Dirty.isDirty( new DirtyType[]{ DirtyType.ASSET_LENGTH_CHANGED, DirtyType.ASSET_LENGTH_UNCHANGED, DirtyType.ASSET_EXPIRATION_CHANGED,
											DirtyType.PLAYLIST, DirtyType.CONTENT_ROTATION, DirtyType.SEGMENT })  )
		{
			otherDirtyObjectsExist = true;
		}
		
		
		// For each dirty device
		for(Iterator i=dirtyEntities.iterator();i.hasNext();)
		{
			Device device = (Device)i.next();
			
			if(csParam.equals("Run Content Scheduler for Network Devices")){
				if(device.getContentUpdateType().equals(ContentUpdateType.DVD_USB.getPersistentValue())){
					continue;
				}
			}else if(csParam.equals("Run Content Scheduler for DVD/USB Devices")){
				if(device.getContentUpdateType().equals(ContentUpdateType.NETWORK.getPersistentValue())){
					continue;
				}
			}
			
			// If there are not any other dirty objects -- we can clear the dirty flag for each device
			if( otherDirtyObjectsExist == false )
			{
				// Add to the list that will be published
				result.add(device);
				dirtyIdsToPublish.add((Long)dirtyIdMap.get( device.getDeviceId() ));				
			}
			else
			{
				// If any of the segments, playlists, or assets that are scheduled to this device are dirty -- the device must stay dirty 
				if( device.hasDirtySegments() ) {
					continue;
				} else if( device.hasDirtyPlaylists() ) {
					continue;
				} else if( device.hasDirtyAssets() ) {
					continue;
				} else {
					// Add to the list that will be published
					result.add(device);
					dirtyIdsToPublish.add((Long)dirtyIdMap.get( device.getDeviceId() ));
				}				
			}
		}
		
		
		// If the currently logged in user doesn't have access to this device
		// remove it from the return list of published devices
		for(Iterator i=result.iterator();i.hasNext();){
			Device device = (Device)i.next();
			if( !device.allowReadAccess() ){
				i.remove();
			}
		}

    	// Get a list of deviceIds associated with the ContentSchedulerCouldNotRun dirty objects before performing the publish
    	List<Long> dirtyContentSchedulerCouldNotRun = Dirty.getDirtyEntityIds( DirtyType.CONTENT_SCHEDULER_COULD_NOT_RUN );
    	
		// Remove any devices that had a dirty object of type ContentSchedulerCouldNotRun
		// from the return list of published devices
		for(Iterator<Device> i=result.iterator();i.hasNext();){
			if( dirtyContentSchedulerCouldNotRun.contains( i.next().getDeviceId() ) ){
				i.remove();
			}
		}
		
		// Clear the dirty flag for these devices
		if(dirtyIdsToPublish.size()>0){
	    	Dirty.publish(dirtyIdsToPublish);
		}		
		return result;
	}
	
	/**
	 * Queries the dirty table for each entityToPublish,
	 * builds the list of dirtyIds and calls the bulk publish method.
	 * @param list
	 * @return
	 */
    public static String publishEntities(List entitiesToPublish){
    	
    	boolean foundEntityToPublish = false;
    	for ( Iterator i = entitiesToPublish.iterator(); i.hasNext(); ){
    		Entity e = (Entity) i.next();
    		List<Dirty> dirtyList = Dirty.getDirtyEntities(e.getEntityId());
    		
	    	// Convert the list of dirty object to a list of dirtyIds
			ArrayList<Long> dirtyIds = new ArrayList<Long>();	    	
    		for( Iterator<Dirty> j = dirtyList.iterator(); j.hasNext(); ){
    			dirtyIds.add( j.next().getDirtyId() );    			
    		}
    		
    		// If we found one or more dirty entities to publish
    		if( dirtyList.size() > 0 ){
    			Dirty.publish( dirtyIds );
    			foundEntityToPublish = true;	
    		}    		 
    	}
		if( foundEntityToPublish  )
			return Constants.SUCCESS;
		else
			return Constants.FAILURE;
    }
    
    public static synchronized void publish(List dirtyIds)
	{	    	    	
    	ArrayList dirtyAssetsLengthUnchanged = new ArrayList();
    	ArrayList dirtyPlaylists = new ArrayList();
    	ArrayList dirtySegments = new ArrayList();
    	ArrayList dirtyDevices = new ArrayList();

    	// Get the dirty instances for the selected items
    	List dirtyInstances = Dirty.getDirtyInstances(dirtyIds);

    	// Iterate through the collection of dirty instances and
    	// create separate lists of dirty entities by their dirty type
    	for( Iterator i = dirtyInstances.iterator(); i.hasNext(); )
		{
			Dirty d = (Dirty)i.next();			
			if( d != null )
			{				
				// If this is a dirty object of type "Asset: length unchanged"
				if( d.getDirtyType().equals( DirtyType.ASSET_LENGTH_UNCHANGED ) )
					dirtyAssetsLengthUnchanged.add(d.dirtyEntityId);
				
				// If this is a dirty object of type "Playlist"		
				else if( d.getDirtyType().equals( DirtyType.PLAYLIST ) )
					dirtyPlaylists.add(d.dirtyEntityId);
				
				// If this is a dirty object of type "Segment"		
				else if( d.getDirtyType().equals( DirtyType.SEGMENT ) )
					dirtySegments.add(d.dirtyEntityId);
				
				// If this is dirty object of type "Device"
				else if( d.getDirtyType().equals( DirtyType.DEVICE ) ){
					dirtyDevices.add(d.dirtyEntityId);
				}
			}
		}
    	
    	// If there are dirty devices to be published
    	if(dirtyDevices.size() > 0){
    		// Get a list of dirty objects of type "Content Scheduler Could Not Run" for all dirty devices
	    	List contentSchedulerCouldNotRun = Dirty.getDirtyEntities(DirtyType.CONTENT_SCHEDULER_COULD_NOT_RUN, dirtyDevices);
	    	
	    	// For each dirty object of type "Content Scheduler Could Not Run"
	    	for(Iterator<Dirty> i = contentSchedulerCouldNotRun.iterator();i.hasNext();){
	    		Dirty dirty = i.next();
	        	// Add the contentSchedulerCouldNotRun row to dirtyIds 
	    		dirtyIds.add(dirty.getDirtyId());	    		
	    	}
    	}
    	
    	// Instantiate the publish thread
    	Dirty dirty = new Dirty();
		PublishThread pt = dirty.new PublishThread();
		
		// Set the schema name, dirtyIds, dirtyAssetsLengthUnchanged, dirtyPlaylists and dirtySegments
		pt.setSchemaName(SchemaDirectory.getSchemaName());
		pt.setDirtyAssetsLengthUnchanged(dirtyAssetsLengthUnchanged);
		pt.setDirtyPlaylists(dirtyPlaylists);
		pt.setDirtySegments(dirtySegments);
		
		// Start the background thread to update playlists, segments
    	// and devices & to do the pre-delete process
		pt.start();
		
		if(dirtyIds.size() > 0){
			Session sess = HibernateSession.currentSession();
			HibernateSession.beginTransaction();
			try {
		
				// Remove the selected items from the dirty table
				String hql = "DELETE FROM Dirty WHERE dirty_id IN (:ids)";
				HibernateSession.executeBulkUpdate( hql, dirtyIds );
				HibernateSession.commitTransaction();
			} catch (Exception e) {
				HibernateSession.rollbackTransaction();
				logger.error(e);
			}			
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public Long save() throws HibernateException
	{		
		// Get the appUserId out of the KmfSession
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null ){
			if(kmfSession.getAppUserId() != null){
				// Set the appUser property before updating
				AppUser appUser = AppUser.getCurrentUser( kmfSession.getAppUserId() );
				this.setAppUser( appUser );
			}else if(kmfSession.getAppUsername() != null){
				// Set the appUser property before updating
				AppUser appUser = AppUser.getAppUser(kmfSession.getAppUsername());
				this.setAppUser( appUser );
			}
			
		}
		this.setCreateDt(new Date());
		return super.save();
	}
	
	public void saveAndBubbleExceptions() throws HibernateException{
		
		HibernateException exceptionToThrow = null;
		
		// Get the appUserId out of the KmfSession
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null && kmfSession.getAppUserId() != null){
			// Set the appUser property before updating
			AppUser appUser = AppUser.getCurrentUser( kmfSession.getAppUserId() );
			this.setAppUser( appUser );
		}
		this.setCreateDt(new Date());
		
		Session session = HibernateSession.currentSession();
		if( HibernateSession.getBulkmode() == false ){	
			HibernateSession.beginTransaction();
		}
		
		try {			
			session.save( this );			
			if( HibernateSession.getBulkmode() == false ){
					
					Transaction tx = HibernateSession.getTx();
					Integer txCount = HibernateSession.getTxCount();
					
					// If the transaction count is 1
					if(txCount != null && txCount == 1){
						// Commit
						if(tx != null){
							try {
								tx.commit();
							} catch (HibernateException e) {
								exceptionToThrow = e;
								HibernateSession.rollbackTransaction();
							}
						}
						HibernateSession.setTxCount(new Integer(0));
					}else{
						// Decrement transaction count
						HibernateSession.setTxCount( txCount - 1);
					}
			}
		} catch (HibernateException e) {
			// Rollback the transaction if an error occurred
			if( HibernateSession.getBulkmode() == true ){
				HibernateSession.rollbackBulkmode();
			}else{
				HibernateSession.rollbackTransaction();
			}
			exceptionToThrow = e;
		}
		
		if(exceptionToThrow != null){
			throw exceptionToThrow;
		}
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void update() throws HibernateException
	{
		// Set the appUser property before updating
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null && kmfSession.getAppUserId() != null){
			// Set the appUser property before updating
			AppUser appUser = AppUser.getCurrentUser( kmfSession.getAppUserId() );
			this.setAppUser( appUser );
		}	
		super.update();	
	}	
		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getDirtyId();
	}
	
	/**
	 * @return Returns the dirtyId.
	 */
	public Long getDirtyId() {
		return dirtyId;
	}
	/**
	 * @param dirtyId The dirtyId to set.
	 */
	public void setDirtyId(Long dirtyId) {
		this.dirtyId = dirtyId;
	}
	/**
	 * @return Returns the dirtyType.
	 */
	public DirtyType getDirtyType() {
		return dirtyType;
	}
	/**
	 * @param dirtyType The dirtyType to set.
	 */
	public void setDirtyType(DirtyType dirtyType) {
		this.dirtyType = dirtyType;
	}

	/**
	 * @return Returns the dirtyEntityId.
	 */
	public Long getDirtyEntityId() {
		return dirtyEntityId;
	}
	/**
	 * @param dirtyEntityId The dirtyEntityId to set.
	 */
	public void setDirtyEntityId(Long dirtyEntityId) {
		this.dirtyEntityId = dirtyEntityId;
	}
	/**
	 * @return Returns the appUser.
	 */
	public AppUser getAppUser() {
		return appUser;
	}
	
	/**
	 * @param appUser The appUser to set.
	 */
	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}
	
	public class PublishThread extends Thread{
	
		List dirtyAssetsLengthUnchanged, dirtyPlaylists, dirtySegments;
		String schemaName;
		
		public void run(){
			
			try {
				SchemaDirectory.initialize( this.getSchemaName(), "PublishThread", "Publish", false, false );
				HibernateSession.currentSession();
				
				// Iterate through the list of type "Asset: length unchanged"
				if(dirtyAssetsLengthUnchanged.size() > 0){
					
					// Get asset instances from their Id's
					dirtyAssetsLengthUnchanged = Asset.getAssets(dirtyAssetsLengthUnchanged);
					
					for( Iterator i=dirtyAssetsLengthUnchanged.iterator();i.hasNext();)
					{					
						Asset a = (Asset)i.next();
						
						/*
						 * We can't add getPresentation commands for targeted assets since there is no way
						 * to determine the underlying asset for this TA. That code resides in the CS and
						 * this issue will be resolved in IT #3162.
						 */
						if(a.getAssetType().equals(AssetType.TARGETED_ASSET) == false && a.getAssetType().equals(AssetType.AD_SERVER) == false){
							
							// For each device that contains a segment or playlist that references this asset,
							// insert a getPresentation() device command for the asset for this device
							List devices = Device.getDevices( a );
							for( Iterator j=devices.iterator(); j.hasNext(); )
							{
								// Don't add this command to devices that exclude this asset
								boolean addDeviceCommand = true;
								Device device = (Device)j.next();
								for(AssetExclusion ae : device.getAssetExclusions()){
									if(ae.getAsset().getAssetId().equals(a.getAssetId())){
										addDeviceCommand = false;
									}
								}
								try {
									if(addDeviceCommand){
										// Insert a getPresentation() device command for the asset for this device							
										device.addGetPresentationDeviceCommand( a, false, DownloadPriorityType.PUBLISHED_CONTENT );
									}
								} catch(Exception e) {
									logger.error("Unexpected error while adding getPresentation() device command: "+ device.getDeviceName(), e);
								}				
							}
						}
					}
				}
				
				List updatedSegments = new ArrayList();
				
				// Iterate through the list of type "Playlist"
				if(dirtyPlaylists.size() > 0){
					
					// Get playlists from their Id's
					dirtyPlaylists = Playlist.getPlaylists(dirtyPlaylists);
					
					// We need to update the lenghts of all playlists which
					// includes calculating the new lengths based on
					// modifications done to the playlist
					updatedSegments = Playlist.updateLengths(dirtyPlaylists);
					
				}
						
				// Iterate through the list of type "Segment"	
				if(dirtySegments.size() > 0){
					
					//Get segments from their Id's
					dirtySegments = Segment.getSegments(dirtySegments);
					
					for( Iterator i=dirtySegments.iterator();i.hasNext();)
					{
						// Update their length
						Segment s = (Segment)i.next() ;
						if(!updatedSegments.contains(s))
							s.updateLength();
					}
				}
			} catch (Exception e) {
				logger.error(e);
			} finally{
				HibernateSession.closeSession();
			}
		}
		public void setDirtyAssetsLengthUnchanged(List dirtyAssetsLengthUnchanged) {
			this.dirtyAssetsLengthUnchanged = dirtyAssetsLengthUnchanged;
		}
		public void setDirtyPlaylists(List dirtyPlaylists) {
			this.dirtyPlaylists = dirtyPlaylists;
		}
		public void setDirtySegments(List dirtySegments) {
			this.dirtySegments = dirtySegments;
		}
		public String getSchemaName() {
			return schemaName;
		}
		public void setSchemaName(String schemaName) {
			this.schemaName = schemaName;
		}
	}

	public Date getCreateDt() {
		return createDt;
	}
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
