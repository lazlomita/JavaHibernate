package com.kuvata.kmf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.struts.util.LabelValueBean;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.usertype.AttrType;
import parkmedia.usertype.SearchInterfaceType;

import com.kuvata.kmf.attr.Attr;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.attr.MetadataInfo;
import com.kuvata.kmf.attr.DateAttr;
import com.kuvata.kmf.attr.NumberAttr;
import com.kuvata.kmf.attr.StringAttr;
import com.kuvata.kmm.SearchOption;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * Base class for all persistent classes.
 * 
 * @author Jeff Randesi
 */
public abstract class Entity implements HTMLRenderable {
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Entity.class );
	public abstract Long getEntityId();
	
	/**
	 * Constructor
	 */
	public Entity(){}
	
	public String renderHTML()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<label>");
		sb.append( "Entity ID: "+ this.getEntityId() );
		sb.append("</label>");
		return sb.toString();
	}
	
	/**
	 * Method used by KMF's custom IdentifierGenerator class to generate
	 * a unique identifier based on the entity_seq sequence.
	 * 
	 * For each instance of a KMF entity, this method creates a new record
	 * in the ENTITY_INSTANCE table and returns the newly created entity id.
	 * 
	 * @param className
	 * @return
	 * @throws HibernateException
	 */
	public static Long makeEntityId(String className) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		EntityInstance ei = new EntityInstance();
		EntityClass ec = EntityClass.getEntityClass( className );
		ei.setEntityClass( ec );
		ei.setLastModified( new Date() );
		
		// Get the current appuser out of the KmfSession 
		// and set the appUserId property of the entity instance object if we can
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null && kmfSession.getAppUserId() != null ){			
			ei.setAppUserId( new Long( kmfSession.getAppUserId() ) );
		}				
		Long newEntityId = ((Long)session.save( ei ));			
		return newEntityId;	
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public Long save() throws HibernateException{
		return save( true );
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public Long save(boolean createPermissionEntries) throws HibernateException{
		Session session = HibernateSession.currentSession();
		if( HibernateSession.getBulkmode() == false ){
			HibernateSession.beginTransaction();
		}
		Long newId = null;
		try {
			newId = (Long)session.save( this );			
			if( createPermissionEntries ){
				// Create permissionEntry objects for this entity (not necessary to reset permissions since we are creating a new object)
				this.createPermissionEntries( false );
			}
			if( HibernateSession.getBulkmode() == false ){
					HibernateSession.commitTransaction();
			}
		} catch (HibernateException e) {	
			// Rollback the transaction if an error occurred
			if( HibernateSession.getBulkmode() == true ){
				HibernateSession.rollbackBulkmode();
			}else{
				HibernateSession.rollbackTransaction();
			}
			throw e;
		}
		return newId;
	}

	public void update() throws HibernateException
	{
		update(true, false);
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public void update(boolean updateLastModified, boolean bubbleException) throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		if( HibernateSession.getBulkmode() == false ){	
			HibernateSession.beginTransaction();
		}
		try 
		{
			// Update the last modified date of this entity
			if(updateLastModified){
				this.updateLastModified();
			}
			session.update( this );		
			
			if( HibernateSession.getBulkmode() == false ){	
				HibernateSession.commitTransaction(bubbleException);
			}					
		} catch (HibernateException e) {			
			// Rollback the transaction if an error occurred
			if( HibernateSession.getBulkmode() == true )
			{
				HibernateSession.rollbackBulkmode();
			}
			else
			{
				HibernateSession.rollbackTransaction();
			}
			throw e;
		}			
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public void delete() throws HibernateException{
		delete(this, true);		
	}
	
	public static void preDelete(List ids){
		/*
		 * Instantiate a new per-delete thread which would delete the rows from
		 * entity_instance, permission_entry and attr
		*/
		PreDeleteThread pdt = new PreDeleteThread();
		// Set the schema name and the ids
		pdt.setSchemaName(SchemaDirectory.getSchemaName());
		pdt.setIds(ids);
		
		// Start the thread
		pdt.start();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void delete(boolean doFlush) throws HibernateException
	{		
		delete(this, doFlush);		
	}
	
	/**
	 * 
	 * @param e
	 * @throws HibernateException
	 */
	private void delete(Entity e, boolean doFlush) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		if( HibernateSession.getBulkmode() == false ){	
			HibernateSession.beginTransaction();
		}
		try 
		{			
			
			// Call Pre Delete before deleting the entity itself
			ArrayList al = new ArrayList();
			al.add(e.getEntityId());
			
			// Start a background thread to do the pre-delete process
			preDelete(al);
			
			// Finally, delete this entity
			session.delete( e );
			
			if( doFlush ){
				if( HibernateSession.getBulkmode() == false ){	
					HibernateSession.commitTransaction();
				}				
			}
		} catch (HibernateException ex) {			
			// Rollback the transaction if an error occurred
			if( HibernateSession.getBulkmode() == true )
			{
				HibernateSession.rollbackBulkmode();
			}
			else
			{
				HibernateSession.rollbackTransaction();
			}
			throw ex;
		}			
	}
	
	/**
	 * Returns an Entity object with the given entityId
	 * 
	 * @param entityId
	 * @return
	 * @throws Exception
	 */
	public static Entity load(Class c, Long entityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();						
		Entity result = (Entity)session.createCriteria( c )
			.add( Expression.eq( "id", entityId ) )
			.uniqueResult();		
		return result; 			
	}
	/**
	 * Returns an Entity object with the given entityId
	 * 
	 * @param entityId
	 * @return
	 * @throws Exception
	 */
	public static List load(Class c, List<Long> entityIds) throws HibernateException
	{
		List result = new ArrayList();
		Session session = HibernateSession.currentSession();
		if( entityIds != null && entityIds.size() > 0 )
		{						
			// Make a copy of the entityIds so that manipulating it does not affect other processing
			List copyOfEntityIds = new ArrayList(entityIds);
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
					List l = session.createCriteria( c ).add( Restrictions.in( "id", copyOfEntityIds.subList( 0, toIndex ) ) ).list();
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
	 * Updates the lastModified property for the entity instance associated with this entity
	 */
	public void updateLastModified()
	{
		// Update the lastModified property for this entity instance
		String hql = "UPDATE EntityInstance "
			+ "SET last_modified = :lastModified "						
			+ "WHERE entity_id = :entityId";
		Session session = HibernateSession.currentSession();	
		session.createQuery( hql )
			.setParameter("lastModified", new Date())
			.setParameter("entityId", this.getEntityId())
			.executeUpdate();	
	}	
	
	/**
	 * Copies any metatdata associated with this entity and assigns the given ownerId to the copied metadata
	 */
	public void copyMetadata(Long ownerId) throws ClassNotFoundException
	{
		// Copy any metadata associated with this entity
		HashMap<AttrDefinition, List<Attr>> attrDefinitions = AttrDefinition.getAttributeDefinitionMap(this);
		for( Iterator i=attrDefinitions.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry)i.next();
			AttrDefinition ad = (AttrDefinition)entry.getKey();
			List<Attr> attrs = (List<Attr>)entry.getValue();
			
			// If there is an attr object associated with this asset
			for(Attr attr : attrs)
			{
				if( attr instanceof StringAttr ){
					StringAttr oldAttr = (StringAttr)attr;
					StringAttr newAttr = new StringAttr();
					newAttr.setAttrDefinition( ad );
					newAttr.setAttrName( oldAttr.getAttrName() );
					newAttr.setAttrType( oldAttr.getAttrType() );
					newAttr.setOwnerId( ownerId );
					newAttr.setValue( oldAttr.getValue() );
					newAttr.save();
				}else if( attr instanceof DateAttr ){
					DateAttr oldAttr = (DateAttr)attr;
					DateAttr newAttr = new DateAttr();
					newAttr.setAttrDefinition( ad );
					newAttr.setAttrName( oldAttr.getAttrName() );
					newAttr.setAttrType( oldAttr.getAttrType() );
					newAttr.setOwnerId( ownerId );
					newAttr.setValue( oldAttr.getValue() );
					newAttr.save();
				}else if( attr instanceof NumberAttr ){
					NumberAttr oldAttr = (NumberAttr)attr;
					NumberAttr newAttr = new NumberAttr();
					newAttr.setAttrDefinition( ad );
					newAttr.setAttrName( oldAttr.getAttrName() );
					newAttr.setAttrType( oldAttr.getAttrType() );
					newAttr.setOwnerId( ownerId );
					newAttr.setValue( oldAttr.getValue() );
					newAttr.save();
				}
			}
		}	
	}
	
	/**
	 * Copies any overlapping permission entries of the given entity into this entity
	 * @param entityToCopy
	 */
	public void copyPermissionEntries(Entity entityToCopy)
	{
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null && kmfSession.getAppUserId() != null )
		{
			AppUser appUser = AppUser.getAppUser( new Long( kmfSession.getAppUserId() ) );
			if( appUser != null )
			{					
				// For each permission entry associated with the given entityToCopy
				List l = PermissionEntry.getPermissionEntries( entityToCopy.getEntityId() );
				for( Iterator i=l.iterator(); i.hasNext(); )
				{
					// If the current user is an admin or the current user belongs to the role associated with this permission entry
					PermissionEntry pe = (PermissionEntry)i.next();
					if( appUser.getIsAdmin() || appUser.isMemberOf( pe.getRole() )  )
					{
						// Create a new permission entry for this entity
						PermissionEntry.create( this.getEntityId(), pe.getRole() );
					}
				}	
			}
		}		
	}
	
	/**
	 * Copies any overlapping permission entries of the given entity into this entity
	 * @param entityToCopy
	 */
	public void copyPermissionEntries(Entity entityToCopy, boolean copyAllRoles, boolean removeExtraRoles)
	{
		ArrayList<Long> roles = new ArrayList();
		for(Role role : this.getRoles(true)){
			roles.add(role.getRoleId());
		}
		
		for(Role role : entityToCopy.getRoles(copyAllRoles)){
			// If this entity doesn't already contain this role
			if(roles.contains(role.getRoleId()) == false){
				// Add the role
				PermissionEntry.create( this.getEntityId(), role );
			}
			
			roles.remove(role.getRoleId());
		}
		
		// If we are meant to remove the other roles that are not a part of the entity to copy
		if(removeExtraRoles){
			for(Iterator<PermissionEntry> i = PermissionEntry.getPermissionEntries(this.getEntityId()).iterator(); i.hasNext();){
				PermissionEntry pe = i.next();
				if(roles.contains(pe.getRole().getRoleId())){
					// Remove this role
					pe.delete();
				}
			}
		}
	}
	
	/**
	 * Creates a permissionEntry object for each role the current appUser has set as default
	 * @param entityId
	 */
	public void createPermissionEntries(boolean resetPermissionEntries)
	{
		if( this.getEntityId() != null )
		{
			// If the flag to reset permissions was passed in
			if( resetPermissionEntries ) {
				PermissionEntry.resetPermissionEntries( this.getEntityId() );
			}

			KmfSession kmfSession = KmfSession.getKmfSession();
			if( kmfSession != null && kmfSession.getAppUserId() != null )
			{
				AppUser appUser = AppUser.getAppUser( new Long( kmfSession.getAppUserId() ) );
				if( appUser != null )
				{
					// Add a permission entry for each role this appUser is a member of
					for( Iterator i=appUser.getAppUserRoles().iterator(); i.hasNext(); )
					{
						AppUserRole appUserRole = (AppUserRole)i.next();
						if( appUserRole.getIsDefault() != null && appUserRole.getIsDefault() == true ){
							PermissionEntry.create( this.getEntityId(), appUserRole.getRole() );	
						}
					}
				}
			}
			
			// Irrespective of whether there is a valid kmfSession and appUser or not, create this row
			if(this instanceof Asset){
				PermissionEntry.manageNoRolesForAssets((Asset)this);
			}
		}
	}
	
	/**
	 * Get the roles that are associated with this entity and the currently logged in user
	 *
	 */
	public List<Role> getRoles(boolean getAllRoles)
	{	
		String hql = "";
		KmfSession kmfSession = KmfSession.getKmfSession();
		
		// If the currently logged in user is an admin -- ignore its member roles		
		if( getAllRoles || kmfSession.isAdmin() ){
			hql = "SELECT role FROM Role as role "
				+ "WHERE role.roleId != 0 AND role.roleId IN "
				+ "(SELECT DISTINCT permissionEntryRole.roleId "
				+ "FROM PermissionEntry as permissionEntry "				
				+ "JOIN permissionEntry.role as permissionEntryRole "
				+ "WHERE permissionEntry.permissionEntityId = '"+ this.getEntityId() +"')";			
		}
		else
		{
			// If there are not any roles associated with this appUser
			String appUserRoleIdsParam = kmfSession.getAppUserRoleIdsParam();
			if( appUserRoleIdsParam == null || appUserRoleIdsParam.length() == 0 ) 
			{
				// Create a temporary string containing a dummy roleId in order to avoid an illegal sql "IN ()" statement
				appUserRoleIdsParam = "-1";
			}
			hql = "SELECT role FROM Role as role "
				+ "WHERE role.roleId != 0 AND role.roleId IN "
				+ "(SELECT DISTINCT permissionEntryRole.roleId "
				+ "FROM PermissionEntry as permissionEntry "
				+ "JOIN permissionEntry.role as permissionEntryRole "
				+ "WHERE permissionEntry.permissionEntityId = '"+ this.getEntityId() +"' "
				+ "AND permissionEntryRole.roleId IN ("+ appUserRoleIdsParam +"))";	
		}						
		Session session = HibernateSession.currentSession();		
		return session.createQuery( hql ).list();
	}
	
	public Date getLastModified()
	{
		Date result = null;
		EntityInstance ei = EntityInstance.getEntityInstance( this.getEntityId() );
		if( ei != null ){
			result = ei.getLastModified();
		}
		return result;
	}
	
	/**
	 * Attempts to locate an EntityInstance associated with this entity
	 * and returns the lastModified property formatted for display purposes.
	 * @return
	 */
	public String getLastModifiedFormatted()
	{
		String result = "";
		EntityInstance ei = EntityInstance.getEntityInstance( this.getEntityId() );
		if( ei != null ){
			result = ei.getLastModifiedFormatted();
		}
		return result;
	}
	
	public static String getLastModifiedFormatted(Long entityId)
	{
		String result = null;
		EntityInstance ei = EntityInstance.getEntityInstance( entityId );
		if( ei != null && ei.getLastModified() != null ){
			result = EntityInstance.dateFormat.format(ei.getLastModified());
		}
		return result;
	}
	
	/**
	 * Retrieves a list of AttrDefinition objects for the given entity
	 * 
	 * @return
	 * @throws Exception
	 */
	public List getAdditionalAttributes() throws Exception
	{				
		return Attr.getAdditionalAttributes( this.getEntityId() );		
	}	
	
	/**
	 * Returns a list of LabelValueBeans for each name/value pair of metadata associated with this entity
	 * @param includeNullValues
	 * @return
	 * @throws Exception
	 */	
	public List<LabelValueBean> getCustomMetadata(boolean includeNullValues) throws Exception
	{
		List<LabelValueBean> result = new LinkedList<LabelValueBean>();
		
		// Get the class name of this entity
		EntityInstance ei = EntityInstance.getEntityInstance( this.getEntityId() );		
		String className = ei.getEntityClass().getClassName();
		List<Object[]> l = AttrDefinition.getAttributeDefinitions( this.getEntityId(), className );
		
		String prevAttrDefinitionId = null;
		String prevAttrDefinitionName = null;
		String attrValues = null;
		for (Iterator<Object[]> i = l.iterator(); i.hasNext();) {
			Object[] o = (Object[])i.next();
			AttrDefinition ad = (AttrDefinition)o[0];
			Attr attr = (Attr)o[1];
			
			String attrDefinitionId = String.valueOf((Long)ad.getAttrDefinitionId());
			String attrDefinitionName = (String)ad.getAttrDefinitionName();
			String searchInterface = (String)ad.getSearchInterface().getPersistentValue();
			String attrValue = attr != null ? (String)attr.getFormattedValue() : "";
			
			if ( searchInterface.equalsIgnoreCase(SearchInterfaceType.MULTI_SELECT.getPersistentValue())) {
				if (prevAttrDefinitionId != null && !prevAttrDefinitionId.equalsIgnoreCase(attrDefinitionId) && attrValues != null) {
					result.add( new LabelValueBean( prevAttrDefinitionName, attrValues ));
					attrValues = null;
				}
				
				if (attrValue != null) {
					if (attrValues == null)
						attrValues = "";
					
					if(attrValues != null && attrValues.length() > 0){
						attrValues += ", ";
					}
					attrValues += attrValue;
				}
				else
					attrValues = "";
			}
			else {
				if (attrValues != null) {
					result.add( new LabelValueBean( prevAttrDefinitionName, attrValues ));
					attrValues = null;
				}
			
				if (includeNullValues || (includeNullValues == false && attrValue != null && attrValue.length() > 0)) {
					result.add( new LabelValueBean( attrDefinitionName, attrValue ));
				}
			}
			
			prevAttrDefinitionId = attrDefinitionId;
			prevAttrDefinitionName = attrDefinitionName;
		}
		
		return result;
	}
	
	public List<LabelValueBean> getCustomMetadata(boolean includeNullValues, List<String> metadataIds) throws Exception
	{
		List<LabelValueBean> result = new LinkedList<LabelValueBean>();
		
		// Get the class name of this entity
		EntityInstance ei = EntityInstance.getEntityInstance( this.getEntityId() );		
		String className = ei.getEntityClass().getClassName();
		List<Object[]> l = AttrDefinition.getAttributeDefinitions( this.getEntityId(), className );
		
		String prevAttrDefinitionId = null;
		String prevAttrDefinitionName = null;
		String attrValues = null;
		for (Iterator<Object[]> i = l.iterator(); i.hasNext();) {
			Object[] o = (Object[])i.next();
			AttrDefinition ad = (AttrDefinition)o[0];
			Attr attr = (Attr)o[1];
			
			String attrDefinitionId = String.valueOf((Long)ad.getAttrDefinitionId());
			String attrDefinitionName = (String)ad.getAttrDefinitionName();
			String searchInterface = (String)ad.getSearchInterface().getPersistentValue();
			String attrValue = attr != null ? (String)attr.getFormattedValue() : "";
			
			if ( searchInterface.equalsIgnoreCase(SearchInterfaceType.MULTI_SELECT.getPersistentValue())) {
				if (prevAttrDefinitionId != null && !prevAttrDefinitionId.equalsIgnoreCase(attrDefinitionId) && attrValues != null) {
					if (metadataIds == null || metadataIds.contains(attrDefinitionId))
						result.add( new LabelValueBean( prevAttrDefinitionName, attrValues ));
					attrValues = null;
				}
				
				if (attrValue != null) {
					if (attrValues == null)
						attrValues = "";
					
					if(attrValues != null && attrValues.length() > 0){
						attrValues += ", ";
					}
					attrValues += attrValue;
				}
				else
					attrValues = "";
			}
			else {
				if (attrValues != null) {
					if (metadataIds == null || metadataIds.contains(prevAttrDefinitionId))
						result.add( new LabelValueBean( prevAttrDefinitionName, attrValues ));
					attrValues = null;
				}
			
				if (includeNullValues || (includeNullValues == false && attrValue != null && attrValue.length() > 0)) {
					if (metadataIds == null || metadataIds.contains(attrDefinitionId))
						result.add( new LabelValueBean( attrDefinitionName, attrValue ));
				}
			}
			
			prevAttrDefinitionId = attrDefinitionId;
			prevAttrDefinitionName = attrDefinitionName;
		}
		
		return result;
	}
	
	/**
	 * Returns a list of CustomMetadataInfo including name, type, userInterface, values and selected values 
	 * of all metadata associated with this entity
	 * @return List<CustomMetadataInfo>
	 * @throws Exception
	 */
	public List<MetadataInfo> getMetadataInfo() throws Exception
	{		
		List<MetadataInfo> result = new LinkedList<MetadataInfo>();
		HashMap attrDefinitions = AttrDefinition.getAttributeDefinitionMap(this);		
		for( Iterator i=attrDefinitions.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry)i.next();
			AttrDefinition ad = (AttrDefinition)entry.getKey();
			List<Attr> attrs = (List<Attr>)entry.getValue();
			
			// If there is an attr object associated with this asset
			String selectedAttrValue = "";
			for(Attr attr : attrs){
				if(attr != null && attr.getValue() != null && attr.getFormattedValue().length() > 0){
					if(selectedAttrValue.length() > 0){
						selectedAttrValue += ", ";
					}
					selectedAttrValue += attr.getFormattedValue();
				}
			}
			
			// If there is a type associated with this attrDefinition
			String attrType = "";
			String attrValues = "";
			if( ad.getType() != null )
			{
				attrType = ad.getType().getAttrTypeName();
				
				// Get all values for this attr definition
				for(String strAttrValue : ad.getAttrValues()){
					if (attrValues.length() > 0) {
						attrValues += ", ";
					}
					attrValues += strAttrValue;
				}												
			}
			
			String userInterface = "";
			if( ad.getSearchInterface() != null ){
				userInterface = ad.getSearchInterface().getPersistentValue();
			}
						
			// Create a CustomMetadataInfo object and add it to the collection
			MetadataInfo cmi = new MetadataInfo( ad.getAttrDefinitionId().toString(), ad.getAttrDefinitionName(), attrValues, selectedAttrValue, attrType, userInterface );			
			result.add( cmi );
		}
		return result;
	}	
	
	/**
	 * Returns string value for a metadata field for an object
	 * @param metadataField
	 * @return
	 * @throws Exception
	 */
	public String getMetadataInfo(String metadataType, String metadataField) throws Exception
	{
		String attrValue = null;
		
		// Retrieve the metadata value for this metadata field
		AttrDefinition attrDefinition = AttrDefinition.getAttributeDefinition( metadataType, metadataField );
		if( attrDefinition != null )
		{
			/*
			 * Attempt to retrieve an Attr object for this device/attr definition.
			 * NOTE: Currently supporting only Device metadata. We may want to consider
			 * supporting other types of metadata (i.e. Segment, Playlist), but it would
			 * require a significant code change in the Content Scheduler (in order to determine
			 * the "current" Segment or Playlist) which we've decided to hold off on. 
			 */
			Attr a = Attr.getAttribute( this.getEntityId(), attrDefinition.getAttrDefinitionId() );
			try {
				attrValue = a.getFormattedValue();
			}
			catch (Exception e) {
				attrValue = null;
			}
		}
		return attrValue;
	}
	
	/**
	 * Updates each metadata name/value pair that was passed in
	 * 
	 * @param className
	 * @param metadata
	 * @return
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 */
	public String updateMetadata(String className, String[][] metadata, boolean append) throws ClassNotFoundException, ParseException
	{
		String result = "";
		boolean valueHasChanged = false;
		
		// For each metadata name/value pair that was passed in
		for( int i=0; i<metadata.length; i++ )
		{
			String attrName = metadata[i][0];
			String attrValue = metadata[i][1];
						
			if( attrName.toUpperCase().indexOf( Constants.METADATA_PREFIX ) >= 0 ){
				attrName = attrName.substring( attrName.toUpperCase().indexOf( Constants.METADATA_PREFIX ) + Constants.METADATA_PREFIX.length() ).trim();
			}
			
			// If we found an attrDefinition with the given name
			AttrDefinition ad = AttrDefinition.getAttributeDefinition( className, attrName );
			if( ad != null )
			{
				if(attrValue != null){
					try{
						// If we are deleting this metadata
						if(attrValue.equalsIgnoreCase("null")){
							List<Attr> attrs = ad.getAttributes(this);
							if(attrs != null && attrs.size() > 0){
								for(Attr attr : attrs){
									attr.delete(true, false);
								}
							}
						}
						
						// Make sure the corresponding value matches the attr type
						else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
						{
							DateFormat dateTimeFormatDisplayable = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
							try {					
								// First try the date/time format
								dateTimeFormatDisplayable.parse( attrValue );
								if (AttrDefinition.saveOrUpdateAttr(this, attrValue, null, ad, true, append))
									valueHasChanged = true;
								
							} catch(ParseException pe1) {
								try {
									// Try 24 hour format
									DateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
									Date date = dateFormat.parse( attrValue );
									if (AttrDefinition.saveOrUpdateAttr(this, dateTimeFormatDisplayable.format(date), null, ad, true, append))
										valueHasChanged = true;
								} catch (ParseException pe2) {
									try {
										// Try the date format without time
										DateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy hh:mm a" );
										Date date = dateFormat.parse( attrValue );
										if (AttrDefinition.saveOrUpdateAttr(this, dateTimeFormatDisplayable.format(date), null, ad, true, append))
											valueHasChanged = true;
									} catch (ParseException pe3) {
										try {
											// Try without seconds
											DateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy HH:mm" );
											Date date = dateFormat.parse( attrValue );
											if (AttrDefinition.saveOrUpdateAttr(this, dateTimeFormatDisplayable.format(date), null, ad, true, append))
												valueHasChanged = true;
										} catch (ParseException pe4) {
											try {
												// Try without seconds in 24 hour format
												DateFormat dateFormat = new SimpleDateFormat( Constants.DATE_FORMAT_DISPLAYABLE );
												Date date = dateFormat.parse( attrValue );
												if (AttrDefinition.saveOrUpdateAttr(this, dateTimeFormatDisplayable.format(date), null, ad, true, append))
													valueHasChanged = true;
											} catch (ParseException pe5) {
												result += "\n     WARNING (Invalid date format: \""+ attrValue +"\". Ignoring metadata field: \""+ attrName +"\".)";
											}
										}
									}
								}
							}	
						}
						else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
						{
							try
							{
								// Make sure this is a valid number
								new Float( attrValue );
								if (AttrDefinition.saveOrUpdateAttr(this, attrValue, null, ad, true, append))
									valueHasChanged = true;
							} catch(NumberFormatException nfe) {
								result += "\n     WARNING (Invalid number format: \""+ attrValue +"\". Ignoring metadata field: \""+ attrName +"\".)";
							}					
						}	
						else
						{
							if(ad.getSearchInterface().equals(SearchInterfaceType.MULTI_SELECT)){
								String[] attrValues = attrValue.split(",");
								
								// Trim each value
								int count = 0;
								for( String s : attrValues){
									attrValues[count++] = s.trim();
								}
								
								if (AttrDefinition.saveOrUpdateAttr(this, null, attrValues, ad, false, append))
									valueHasChanged = true;
							}else{
								if (AttrDefinition.saveOrUpdateAttr(this, attrValue, null, ad, true, append))
									valueHasChanged = true;
							}
						}
					}catch(KmfException kmfe){
						result += kmfe.getMessage();
					}
				}
			}
		}
		
		if (valueHasChanged)
			this.update();
		
		return result;
	}	
	
	/***
	 * Helper method to call the getSearchOptions function which includes the "Date Modified" field
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static List getSearchOptions(String className) throws ClassNotFoundException
	{
		return getSearchOptions( className, true, true );
	}
	
	/***
	 * Helper method to call the getSearchOptions function which includes the "Date Modified" field
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static List getSearchOptions(String className, boolean includeDateModified) throws ClassNotFoundException
	{
		return getSearchOptions( className, includeDateModified, true );
	}
	
	/**
	 * Returns a list of SearchOption objects -- one for each attribute definition associated
	 * with the Asset class
	 * 
	 * @param assetTypes
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static List getSearchOptions(String className, boolean includeDateModified, boolean includeNoMetadata) throws ClassNotFoundException
	{
		LinkedList result = new LinkedList();
		
		// Create a "No Metadata" search option and add it to the collection
		if(includeNoMetadata){
			SearchOption noMetadataSearchOption = new SearchOption();
			noMetadataSearchOption.setAttrDefinitionId( Constants.NO_METADATA );
			noMetadataSearchOption.setName( Constants.NO_METADATA );
			noMetadataSearchOption.setAttrType( AttrType.STRING );				
			result.add( noMetadataSearchOption );
		}
		
		// If the flag to include the "Date Modified" search option was specified
		if( includeDateModified )
		{
			// Create a "Date Modified" search option and add it to the collection
			SearchOption dateModified = new SearchOption();
			dateModified.setAttrDefinitionId( Constants.DATE_MODIFIED );
			dateModified.setName( Constants.DATE_MODIFIED );
			dateModified.setAttrType( AttrType.DATE );				
			result.add( dateModified );	
		}
				
		// Since we did not request attribute definitions from the super class, we can assume we returned only one list
		List l = AttrDefinition.getAttributeDefinitions( className, false );		
		if( l.size() > 0 )
		{
			// For each asset metadata property
			List attrDefinitions = (List)l.get( 0 );
			for( Iterator i=attrDefinitions.iterator(); i.hasNext(); )
			{			
				AttrDefinition ad = (AttrDefinition)i.next();
				
				// Create a new SearchOption object and add it to the collection
				SearchOption searchOption = new SearchOption();
				searchOption.setAttrDefinitionId( ad.getAttrDefinitionId().toString() );
				searchOption.setName( ad.getAttrDefinitionName() );
				searchOption.setAttrType( ad.getType() );
				searchOption.setSearchInterface( ad.getSearchInterface() );			
				result.add( searchOption );
			}		
		}
		return result;
	}	
	
	/**
	 * Determines if the currently logged in user has read access to this entity.
	 */
	public boolean allowReadAccess()
	{
		boolean result = false;
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null )
		{
			// If the currently logged in user is an admin, or has universal read access, or permissions are not enabled, return true		
			if( kmfSession.isAdmin() || kmfSession.isUniversalDataAccess() || kmfSession.isEnablePermissions() == false )
			{
				result = true;
			}
			else
			{
				// If there are not any viewable roles associated with this appUser
				String appUserRoleIdsParam = kmfSession.getAppUserViewableRoleIdsParam();
				if( appUserRoleIdsParam == null || appUserRoleIdsParam.length() == 0 ) 
				{
					// Create a temporary string containing a dummy roleId in order to avoid an illegal sql "IN ()" statement
					appUserRoleIdsParam = "-1";					
				}
				
				// If there are any entries in the permission entry table for this entity and roles that this user is a member of, 
				// or if this user is the owner of the entity -- the user must have read access to this entity				
				String hql = "SELECT entity.id FROM "+ this.getClass().getName() +" as entity "
				+ "WHERE entity.id IN ("
					+ "SELECT permissionEntry.permissionEntityId FROM PermissionEntry as permissionEntry "
					+ "WHERE permissionEntry.permissionEntityId = "+ this.getEntityId() +" "
					+ "AND permissionEntry.role.roleId IN ("+ appUserRoleIdsParam +")) "
				+ "OR entity.id IN ("
					+ "SELECT entityInstance.entityId FROM EntityInstance as entityInstance "
					+ "WHERE entityInstance.entityId = "+ this.getEntityId() +" "
					+ "AND entityInstance.appUserId = "+ kmfSession.getAppUserId() +")";				
				Session session = HibernateSession.currentSession();				
				Iterator i = session.createQuery( hql ).iterate();				
				result = i.hasNext() ? true : false;
				Hibernate.close( i );
			}
		}
		return result;	
	}
	
	/**
	 * Determines if the currently logged in user has read access to this entity.
	 */
	public static boolean allowReadAccess(String className, Long entityId)
	{
		boolean result = false;
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null )
		{
			// If the currently logged in user is an admin, or has universal read access, or permissions are not enabled, return true		
			if( kmfSession.isAdmin() || kmfSession.isUniversalDataAccess() || kmfSession.isEnablePermissions() == false )
			{
				result = true;
			}
			else
			{
				// If there are not any viewable roles associated with this appUser
				String appUserRoleIdsParam = kmfSession.getAppUserViewableRoleIdsParam();
				if( appUserRoleIdsParam == null || appUserRoleIdsParam.length() == 0 ) 
				{
					// Create a temporary string containing a dummy roleId in order to avoid an illegal sql "IN ()" statement
					appUserRoleIdsParam = "-1";					
				}
				
				// If there are any entries in the permission entry table for this entity and roles that this user is a member of, 
				// or if this user is the owner of the entity -- the user must have read access to this entity				
				String hql = "SELECT entity.id FROM "+ className +" as entity "
				+ "WHERE entity.id IN ("
					+ "SELECT permissionEntry.permissionEntityId FROM PermissionEntry as permissionEntry "
					+ "WHERE permissionEntry.permissionEntityId = "+ entityId +" "
					+ "AND permissionEntry.role.roleId IN ("+ appUserRoleIdsParam +")) "
				+ "OR entity.id IN ("
					+ "SELECT entityInstance.entityId FROM EntityInstance as entityInstance "
					+ "WHERE entityInstance.entityId = "+ entityId +" "
					+ "AND entityInstance.appUserId = "+ kmfSession.getAppUserId() +")";				
				Session session = HibernateSession.currentSession();				
				Iterator i = session.createQuery( hql ).iterate();				
				result = i.hasNext() ? true : false;
				Hibernate.close( i );
			}
		}
		return result;	
	}

	/**
	 * Retrieves the appUser associated with this entity
	 * @return
	 */
	public String getOwner()
	{
		String result = "";
		EntityInstance ei = EntityInstance.getEntityInstance( this.getEntityId() );
		if( ei != null && ei.getAppUserId() != null ){
			AppUser appUser = AppUser.getAppUser( ei.getAppUserId() );
			if( appUser != null ){
				result = appUser.getName();
			}
		}			
		return result;
	}
		
}
