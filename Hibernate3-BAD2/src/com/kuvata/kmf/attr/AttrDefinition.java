package com.kuvata.kmf.attr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.ContentRotation;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.DynamicQueryPart;
import com.kuvata.kmf.Entity;
import com.kuvata.kmf.EntityClass;
import com.kuvata.kmf.EntityInstance;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.PreDeleteThread;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.TargetedAssetMember;
import com.kuvata.kmf.util.Reformat;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Oooo.TV, Inc.
 * 
 * Persistent class for table ATTR_DEFINITION
 * 
 * @author Jeff Randesi
 */
public class AttrDefinition extends Entity {

	private Long attrDefinitionId;
	private EntityClass entityClass;
	private String attrDefinitionName;	
	private AttrType type;
	private SearchInterfaceType searchInterface;
	private Boolean adServer = Boolean.FALSE;
	private Boolean showInReport = Boolean.FALSE;
	private Set attrs = new HashSet();
	private Integer seqNum;
	private static Logger logger = Logger.getLogger(AttrDefinition.class);
	
	/**
	 * Constructor
	 */
	public AttrDefinition()
	{		
	}
	
	/**
	 * 
	 * @param attrDefinitionId
	 * @return
	 * @throws HibernateException
	 */
	public static AttrDefinition getAttrDefinition(Long attrDefinitionId) throws HibernateException
	{
		return (AttrDefinition)Entity.load(AttrDefinition.class, attrDefinitionId);		
	}
	
	public void delete() throws HibernateException{
		
		// Get the current session and start the transaction
		Session sess = HibernateSession.currentSession();
		if( HibernateSession.getBulkmode() == false ){	
			HibernateSession.beginTransaction();
		}
		
		try{
			String hql = "Select a.attrId from Attr as a where a.attrDefinition = "+this.getAttrDefinitionId();
			Query q = sess.createQuery(hql);
			List attributeIds = q.list();			
			if( attributeIds.size() > 0 )
			{
				// Delete from the attr table
				hql = "Delete from Attr as a where a.attrId in (:ids)";
				q = sess.createQuery(hql);
				
				int startIndex= 0;
				int endIndex = 0;
				while (endIndex < attributeIds.size()) {
					endIndex = attributeIds.size() > endIndex + 500 ? endIndex += 500 : attributeIds.size();
					List tmp = attributeIds.subList(startIndex, endIndex);
					q.setParameterList("ids", tmp);
					q.executeUpdate();
					
					// Delete the attr, permission entries and the entity instances associated with the attributes
					PreDeleteThread pdt = new PreDeleteThread();
					pdt.setSchemaName(SchemaDirectory.getSchemaName());
					pdt.setIds(tmp);
					pdt.start();
					
					startIndex = endIndex;
				}
			}
			
			
			if(this.getEntityClass().getClassName().equals(Asset.class.getName())){
				ArrayList<Playlist> playlistsToUpdate = new ArrayList<Playlist>();
				ArrayList<ContentRotation> contentRotationsToUpdate = new ArrayList<ContentRotation>();
				hql = "FROM DynamicQueryPart WHERE attrDefinition.attrDefinitionId = :attrId";
				q = sess.createQuery(hql).setParameter("attrId", this.getAttrDefinitionId());
				List<DynamicQueryPart> dqps = q.list();
				for(DynamicQueryPart dqp : dqps){
					if(dqp.getPlaylist() != null){
						playlistsToUpdate.add(dqp.getPlaylist());
					}else if(dqp.getContentRotation() != null){
						contentRotationsToUpdate.add(dqp.getContentRotation());
					}
					
					dqp.delete();
				}
				
				// Update hql for associated entities
				for(Playlist p : playlistsToUpdate){
					p.generateHql(null);
				}
				for(ContentRotation cr : contentRotationsToUpdate){
					cr.generateHql(null);
				}
			}else if(this.getEntityClass().getClassName().equals(Device.class.getName())){
				for(TargetedAssetMember tam : TargetedAssetMember.getTargetedAssetMembers(this)){
					tam.setAttrDefinition(null);
					tam.setAttrValue(null);
					tam.update();
				}
			}
			
			if( HibernateSession.getBulkmode() == false ){	
				HibernateSession.commitTransaction();
			}
			
		}catch(HibernateException e){
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
		
		super.delete();
		
	}
	
	/**
	 * @return Returns the attrType.
	 */
	public AttrType getType() {
		return type;
	}
	

	/**
	 * @param attrType The attrType to set.
	 */
	public void setType(AttrType attrType) {
		this.type = attrType;
	}
	

	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof AttrDefinition) ) result = false;
		
		AttrDefinition ad = (AttrDefinition) other;		
		if(this.hashCode() == ad.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "AttrDefinition".hashCode();
		result = Reformat.getSafeHash( this.getAttrDefinitionName(), result, 7 );
		result = Reformat.getSafeHash( this.getEntityClass().getEntityClassId(), result, 13 );		
		if( this.getType() != null ){
			result = Reformat.getSafeHash( this.getType().getPersistentValue(), result, 29 );
		}
		return result;
	}	
	
	/**
	 * Implementation of the inherited abstract method Entity.getEntityId().
	 * Returns the attrDefinitionId.
	 */
	public Long getEntityId()
	{
		return this.getAttrDefinitionId();
	}
	
	/**
	 * Persists a new AttrDefinition object for the given className and attrName
	 * 
	 * @param className
	 * @param attrName
	 * @return Newly created AttrDefinition object
	 * @throws HibernateException, KMFException
	 */
	public static AttrDefinition addAttrDefinition(String className, String attrName, AttrType attrType, SearchInterfaceType searchToolType, Boolean adServer, Boolean showInReport) throws HibernateException
	{							
		
		AttrDefinition ad = new AttrDefinition();
		
		// Retrieve the EntityClass object with the given className
		ad.setEntityClass( EntityClass.getEntityClass( className ));
		ad.setAttrDefinitionName( attrName );
		ad.setType( attrType );
		ad.setSearchInterface( searchToolType );
		ad.setAdServer( adServer );
		ad.setShowInReport( showInReport );
		ad.save();
		
		return ad;
	}
	
	/**
	 * Retrieves the AttrDefinition with the given name
	 * 
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public static AttrDefinition getAttributeDefinition(Class c, String name) throws HibernateException, ClassNotFoundException
	{		
		return AttrDefinition.getAttributeDefinition( c.getName(), name );		
	}	
	
	/**
	 * Attempts to retrieve an AttrDefinition object with the given className and attrName
	 * 
	 * @param className
	 * @param attributeName
	 * @return
	 * @throws Exception
	 */
	public static AttrDefinition getAttributeDefinition(String className, String attrName) throws HibernateException, ClassNotFoundException
	{
		Session session = HibernateSession.currentSession();
		AttrDefinition result = (AttrDefinition)session.createCriteria(AttrDefinition.class)
				.createAlias("entityClass", "ec")
				.add( Expression.eq("ec.className", className).ignoreCase() )
				.add( Expression.eq("attrDefinitionName", attrName).ignoreCase() )
				.uniqueResult();
		return result;			
	}

	/**
	 * Retrieves Metadata information.
	 * 
	 * @param entityName. Can be 'Asset' or 'Device'.
	 * @param metadataIdsFilter. The metadata ids to return. Use null to return all Metadata.
	 * @return
	 * @throws HibernateException
	 * @throws ClassNotFoundException
	 */
	public static List<MetadataInfo> getAttrDefinitionMetadata(String entityName, List<String> metadataIdsFilter) throws HibernateException, ClassNotFoundException
	{
		List<MetadataInfo> result = new LinkedList<MetadataInfo>();
		List<List<AttrDefinition>> allAttrDefinitionsList = AttrDefinition.getAttributeDefinitions( entityName, true );
		
		for( Iterator<List<AttrDefinition>> i=allAttrDefinitionsList.iterator(); i.hasNext(); )
		{
			// For each AttributeDefinition associated with this entityClass
			List<AttrDefinition> attrDefinitionsList = i.next();
			
			for( Iterator<AttrDefinition> j=attrDefinitionsList.iterator(); j.hasNext(); )
			{
				AttrDefinition ad = j.next();
				
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

				
				if (metadataIdsFilter == null || metadataIdsFilter.contains(ad.getAttrDefinitionId().toString())) {
					// Create a CustomMetadataInfo object and add it to the collection
					MetadataInfo cmi = new MetadataInfo( ad.getAttrDefinitionId().toString(), ad.getAttrDefinitionName(), attrValues, "", attrType, userInterface );			
					result.add( cmi );
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves a list of AttrDefinition objects with the given className
	 * where type is not null.
	 * 
	 * @param className
	 * @param attributeName
	 * @return
	 * @throws Exception
	 */
	public static LinkedHashMap<AttrDefinition, List<Attr>> getAttributeDefinitionMap(Entity e) throws HibernateException, ClassNotFoundException
	{
		//Session session = HibernateSession.currentSession();		
		LinkedHashMap<AttrDefinition, List<Attr>> attrDefinitions = new LinkedHashMap<AttrDefinition, List<Attr>>();

		// Get the class name of this entity
		EntityInstance ei = EntityInstance.getEntityInstance( e.getEntityId() );		
		String className = ei.getEntityClass().getClassName();
		
		// For each set of AttributeDefinitions associated with this entityClass
		List<List<AttrDefinition>> attrDefinitionsList = AttrDefinition.getAttributeDefinitions( className, true );		
		for( Iterator<List<AttrDefinition>> i=attrDefinitionsList.iterator(); i.hasNext(); )
		{				
			// For each AttributeDefinition associated with this entityClass
			List<AttrDefinition> l = i.next();
			for( Iterator<AttrDefinition> j=l.iterator(); j.hasNext(); )
			{
				AttrDefinition ad = j.next();
				
				// List of attributes
				List<Attr> attributes = new ArrayList<Attr>();
				
				// Get all values if this is a multi-select
				if(ad.getSearchInterface().equals(SearchInterfaceType.MULTI_SELECT)){
					attributes = Attr.getAttributes(e.getEntityId(), ad.getAttrDefinitionId());
				}else{
					// Attempt to retrieve an Attr object
					attributes.add(Attr.getAttribute( e.getEntityId(), ad.getAttrDefinitionId() ));
				}
						
				// Add the AttrDefinition and Attr to the HashMap
				attrDefinitions.put(ad, attributes);
			}		
		}
		return attrDefinitions;
	}
	
	/**
	 * Returns a list of all AttrDefinition objects associated with the given class
	 * and it's superclasses, if specified, where type is not null.
	 * 
	 * @param e
	 * @return
	 * @throws HibernateException
	 * @throws ClassNotFoundException
	 */
	public static List<List<AttrDefinition>> getAttributeDefinitions(String className, boolean getSuperClassAttrDefinitions) throws HibernateException, ClassNotFoundException
	{
		ArrayList<List<AttrDefinition>> result = new ArrayList<List<AttrDefinition>>();
		Session session = HibernateSession.currentSession();						
		while(className != null)
		{
			List<AttrDefinition> l = session.createCriteria(AttrDefinition.class)
					.createAlias("entityClass", "ec")
					.add( Expression.eq("ec.className", className ) )
					.add( Restrictions.isNotNull("type") )
					.addOrder( Order.asc("seqNum"))
					.addOrder( Order.asc("attrDefinitionName").ignoreCase() )
					.list();
		
			// If we found any AttrDefinitions for the given className
			if(l.size() > 0) {
				result.add( l );
			}	
			
			// If we're looking for the attr definitions of this class's super classes
			if( getSuperClassAttrDefinitions ) {
				Class c = Class.forName(className);
				Class superClass = c.getSuperclass();	
				className = superClass != null ? superClass.getName() : null;					
			}else{
				className = null;
			}		
		}	
		return result;
	}
	
	public static List getAttributeDefinitions(long entityId, String className) {
		Session session = HibernateSession.currentSession();
		List l = session.createQuery("SELECT ad, att from EntityClass as ec "
				+ "inner join ec.attrDefinitions as ad "
				+ "left join ad.attrs as att with att.ownerId=:ownerId "
				+ "where ec.className=:className order by lower(ad.attrDefinitionName) ASC")
					.setParameter("ownerId", entityId)
					.setParameter("className", className).list();
		
		return l;
	}
	
	public static HashMap<String, Object[]> getAttributeDefinitionsMap(long entityId, String className) {
		List l = getAttributeDefinitions(entityId, className);
		
		HashMap<String, Object[]> result = new HashMap<String, Object[]>();
		for (Iterator i=l.listIterator(); i.hasNext();) {
			Object[] row = (Object[])i.next();
			AttrDefinition ad = (AttrDefinition)row[0];
			result.put(ad.getAttrDefinitionId().toString(), row);
		}
		
		return result;
	}
	
	public static List getValues(String attrDefinitionId, String adType) {
		// If there is a type associated with this attrDefinition
		List attrValues = null;
		if( adType != null )
		{
			// If this is a StringAttr
			if( adType.equalsIgnoreCase( AttrType.STRING.getAttrTypeName() ) )
			{
				// Get all values for this attr definition and convert them into a list of LabelValueBeans
				attrValues = new LinkedList();
				AttrDefinition ad = AttrDefinition.getAttrDefinition(Long.parseLong(attrDefinitionId));
				for( Iterator j=ad.getAttrValues().iterator(); j.hasNext(); )
				{
					String strAttrValue = (String)j.next();
				}												
			}
		}
		
		return attrValues;
	}
	
	/**
	 * Determines whether or not an attrDefinition with the given name already
	 * exists for the given entity
	 * 
	 * @return true or false
	 */
	protected static boolean attrDefinitionExists(String className, String attrDefinitionName) throws HibernateException
	{
		boolean result = false;		
		Session session = HibernateSession.currentSession();							
		List l = session.createCriteria(AttrDefinition.class)
					.createAlias("entityClass", "ec")
					.add( Expression.eq("attrDefinitionName", attrDefinitionName).ignoreCase() )
					.add( Expression.eq("ec.className", className) )
					.list();	
		
		// If we retrieved any records
		if(l.size() > 0) {	
			result = true;
		}
		else
		{
			// Make sure that there are no "additional Attr" objects with 
			// the given attrDefinitionName for this entity type (className)		
			List list2 = session.createCriteria(Attr.class)
						.createAlias("attrDefinition", "ad")
						.add( Expression.isNull("ad.attrDefinitionId") )
						.add( Expression.eq("attrName", attrDefinitionName).ignoreCase() )
						.list();
			for(int i=0; i<list2.size(); i++)
			{
				Attr a = (Attr)list2.get(i);
				EntityInstance ei = EntityInstance.getEntityInstance( a.getOwnerId() );
				
				// If the owner of this Attr is of type className -- we've found a duplicate
				if(ei.getEntityClass().getClassName().equalsIgnoreCase( className ) == true)
				{
					result = true;
				}
			}
		}		
		return result;		
	}
	
	/**
	 * Attempts to retreive an Attr object for this AttrDefinition object
	 * with the given entity_id. 
	 * 
	 * @param e
	 * @return The updated or newly created Attr object
	 * @throws HibernateException
	 */
	public Attr getAttribute(Entity e) throws HibernateException
	{		
		// Try to find an existing Attr object for the given AttrDefinition and Entity
		Session session = HibernateSession.currentSession();
		Attr a = (Attr)session.createCriteria(Attr.class)
				.add( Expression.eq("attrDefinition", this) )
				.add( Expression.eq("ownerId", e.getEntityId() ) )
				.uniqueResult();		
		return a;
	}
	
	/**
	 * Attempts to retreive an Attr object for this AttrDefinition object
	 * with the given entity_id. 
	 * 
	 * @param e
	 * @return The updated or newly created Attr object
	 * @throws HibernateException
	 */
	public List<Attr> getAttributes(Entity e) throws HibernateException
	{		
		// Try to find an existing Attr object for the given AttrDefinition and Entity
		Session session = HibernateSession.currentSession();
		return session.createCriteria(Attr.class)
				.add( Expression.eq("attrDefinition", this) )
				.add( Expression.eq("ownerId", e.getEntityId() ) )
				.list();
	}
	
	/**
	 * Returns true or false depending upon whether or not the given attrValue has changed
	 * @param e
	 * @param attrValue
	 * @return
	 * @throws ParseException
	 */
	public static boolean saveOrUpdateAttr(Entity e, String attrValue, String[] attrValues, AttrDefinition ad, boolean useExistingMultiSelectValues, boolean append) throws ParseException
	{
		// Attempt to locate an attr object for this device and attribute defintion if this is not a multi-select attrDefinition
		Attr attr = ad.getSearchInterface().equals(SearchInterfaceType.MULTI_SELECT) == false ? ad.getAttribute( e ) : null;
		DateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE);
		boolean valueHasChanged = false;

		// If there is a type associated with this attrDefintion
		if( ad.getType() != null )
		{
			// If this is a multi-select
			if(ad.getSearchInterface().equals(SearchInterfaceType.MULTI_SELECT)){
				if(attrValues != null){
					String errors = "";
					
					// If we are trying to update a non-existing attribute
					if(useExistingMultiSelectValues){
						List existingValues = ad.getAttrValues();
						for(int i=0; i<attrValues.length; i++){
							if(existingValues.contains(attrValues[i]) == false){
								errors += "\n     WARNING (Could not locate a value, \""+ attrValues[i] +"\"; for the metadata field, " + ad.getAttrDefinitionName() + ". Ignoring value.)";
								attrValues[i] = null;
							}
						}
					}
					
					List<Attr> existingAttrs = ad.getAttributes(e);
					for(String selectedAttrValue : attrValues){
						StringAttr newAttr = null;
						if(selectedAttrValue != null && selectedAttrValue.length() > 0){
							newAttr = new StringAttr();
							newAttr.setAttrName(ad.getAttrDefinitionName());
							newAttr.setOwnerId(e.getEntityId());
							newAttr.setAttrType(AttrType.STRING);
							newAttr.setValue(selectedAttrValue);
							newAttr.setAttrDefinition(ad);
						}
						if(newAttr != null && existingAttrs.contains(newAttr) == false){
							newAttr.save();
							valueHasChanged = true;
						}else{
							existingAttrs.remove(newAttr);
						}
					}
					
					// If we are not in append mode
					if(append == false){
						// Delete unselected attributes
						for(Attr a : existingAttrs){
							a.delete();
							valueHasChanged = true;
						}
					}
				}
			}
			// If this is not a multi-select
			else if( attrValue != null && attrValue.length() > 0 ){
				
				boolean isNewAttr = attr == null;
				
				// If this is an attr definition of type Date
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					/*
					 * Make sure a valid date was passed in 
					 */
					try {					
						// Try the date only format								
						Date dateValue = dateTimeFormat.parse( attrValue );	

						// If there is not already an Attr object associated with this AttrDefinition
						DateAttr dateAttr = null;
						if( attr != null ) {
							dateAttr = (DateAttr)attr;							
						}else{
							dateAttr = new DateAttr();
						}			
						
						// If the value has changed
						if( dateAttr.getValue() == null || dateAttr.getValue().getTime() != dateValue.getTime() ){
							valueHasChanged = true;
						}
						dateAttr.setValue( dateValue );	
						dateAttr.setAttrName( ad.getAttrDefinitionName() );
						dateAttr.setAttrDefinition( ad );
						dateAttr.setAttrType( ad.getType() );
						dateAttr.setOwnerId( e.getEntityId() );						
						
						// Only save or update if the value has changed
						if( valueHasChanged ){
							// If we're creating a new attr, save it
							if( isNewAttr ){
								dateAttr.save();
							}else{
								dateAttr.update();
							}
						}
					} catch(ParseException pe) {
						// Throw the exception back to the calling method
						throw pe;
					}	
				}
				// If this is an attr definition of type Number
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// If there is not already an Attr object associated with this AttrDefinition
					NumberAttr numberAttr = null;
					if( attr != null ) {
						numberAttr = (NumberAttr)attr;							
					}else{
						numberAttr = new NumberAttr();
					}				
					
					// If the value has changed
					if( numberAttr.getValue() == null || numberAttr.getValue().equals( new Float(attrValue) ) == false ){
						valueHasChanged = true;
					}					
					numberAttr.setValue( new Float(attrValue) );	
					numberAttr.setAttrName( ad.getAttrDefinitionName() );
					numberAttr.setAttrDefinition( ad );
					numberAttr.setAttrType( ad.getType() );
					numberAttr.setOwnerId( e.getEntityId() );						
					
					// Only save or update if the value has changed
					if( valueHasChanged ){
						// If we're creating a new attr, save it
						if( isNewAttr ){
							numberAttr.save();
						}else{
							numberAttr.update();
						}
					}
				}
				// If this is an attr definition of type String
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If there is not already an Attr object associated with this AttrDefinition
					StringAttr stringAttr = null;
					if( attr != null ) {
						stringAttr = (StringAttr)attr;						
					}else{
						stringAttr = new StringAttr();
					}
					
					// Make sure the string value does not exceed 4000 character
					if( attrValue.length() > 4000 ){
						attrValue = attrValue.substring(0, 4000);
					}
					
					// If the value has changed
					if( stringAttr.getValue() == null || stringAttr.getValue().equalsIgnoreCase( attrValue ) == false ){
						valueHasChanged = true;
					}	
					stringAttr.setValue( attrValue );	
					stringAttr.setAttrName( ad.getAttrDefinitionName() );
					stringAttr.setAttrDefinition( ad );
					stringAttr.setAttrType( ad.getType() );
					stringAttr.setOwnerId( e.getEntityId() );
					
					// Only save or update if the value has changed
					if( valueHasChanged ){
						// If we're creating a new attr, save it
						if( isNewAttr ){
							stringAttr.save();
						}else{
							stringAttr.update();
						}
					}
				}
			}
			else
			{
				// If there is already an Attr object associated with the AttrDefinition
				// and no value was passed in, delete the attr object
				if( attr != null ){
					valueHasChanged = true;
					attr.delete();
				}
			}
		}
		
		return valueHasChanged;
	}
	
	public static boolean saveOrUpdateAttr(Entity e, String attrValue, String[] attrValues, String userInterface, AttrDefinition ad, Attr attr, boolean useExistingMultiSelectValues, boolean append) throws ParseException
	{
		DateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE);
		boolean valueHasChanged = false;

		// If there is a type associated with this attrDefintion
		if( ad.getType() != null )
		{
			// If this is a multi-select
			if(userInterface.equals(SearchInterfaceType.MULTI_SELECT.getPersistentValue())){
				if(attrValues != null){
					String errors = "";
					
					// If we are trying to update a non-existing attribute
					if(useExistingMultiSelectValues){
						List existingValues = ad.getAttrValues();
						for(int i=0; i<attrValues.length; i++){
							if(existingValues.contains(attrValues[i]) == false){
								errors += "\n     WARNING (Could not locate a value, \""+ attrValues[i] +"\"; for the metadata field, " + ad.getAttrDefinitionName() + ". Ignoring value.)";
								attrValues[i] = null;
							}
						}
					}
					
					List<Attr> existingAttrs = ad.getAttributes(e);
					for(String selectedAttrValue : attrValues){
						StringAttr newAttr = null;
						if(selectedAttrValue != null && selectedAttrValue.length() > 0){
							newAttr = new StringAttr();
							newAttr.setAttrName(ad.getAttrDefinitionName());
							newAttr.setOwnerId(e.getEntityId());
							newAttr.setAttrType(AttrType.STRING);
							newAttr.setValue(selectedAttrValue);
							newAttr.setAttrDefinition(ad);
						}
						if(newAttr != null && existingAttrs.contains(newAttr) == false){
							newAttr.save();
							valueHasChanged = true;
						}else{
							existingAttrs.remove(newAttr);
						}
					}
					
					// If we are not in append mode
					if(append == false){
						// Delete unselected attributes
						for(Attr a : existingAttrs){
							a.delete();
							valueHasChanged = true;
						}
					}
				}
			}
			// If this is not a multi-select
			else if( attrValue != null && attrValue.length() > 0 ){
				
				boolean isNewAttr = attr == null;
				
				// If this is an attr definition of type Date
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					/*
					 * Make sure a valid date was passed in 
					 */
					try {					
						// Try the date only format								
						Date dateValue = dateTimeFormat.parse( attrValue );	

						// If there is not already an Attr object associated with this AttrDefinition
						DateAttr dateAttr = null;
						if( attr != null ) {
							dateAttr = (DateAttr)attr;							
						}else{
							dateAttr = new DateAttr();
						}			
						
						// If the value has changed
						if( dateAttr.getValue() == null || dateAttr.getValue().getTime() != dateValue.getTime() ){
							valueHasChanged = true;
						}
						dateAttr.setValue( dateValue );	
						dateAttr.setAttrName( ad.getAttrDefinitionName() );
						dateAttr.setAttrDefinition( ad );
						dateAttr.setAttrType( ad.getType() );
						dateAttr.setOwnerId( e.getEntityId() );						
						
						// Only save or update if the value has changed
						if( valueHasChanged ){
							// If we're creating a new attr, save it
							if( isNewAttr ){
								dateAttr.save();
							}else{
								dateAttr.update();
							}
						}
					} catch(ParseException pe) {
						// Throw the exception back to the calling method
						throw pe;
					}	
				}
				// If this is an attr definition of type Number
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// If there is not already an Attr object associated with this AttrDefinition
					NumberAttr numberAttr = null;
					if( attr != null ) {
						numberAttr = (NumberAttr)attr;							
					}else{
						numberAttr = new NumberAttr();
					}				
					
					// If the value has changed
					if( numberAttr.getValue() == null || numberAttr.getValue().equals( new Float(attrValue) ) == false ){
						valueHasChanged = true;
					}					
					numberAttr.setValue( new Float(attrValue) );	
					numberAttr.setAttrName( ad.getAttrDefinitionName() );
					numberAttr.setAttrDefinition( ad );
					numberAttr.setAttrType( ad.getType() );
					numberAttr.setOwnerId( e.getEntityId() );						
					
					// Only save or update if the value has changed
					if( valueHasChanged ){
						// If we're creating a new attr, save it
						if( isNewAttr ){
							numberAttr.save();
						}else{
							numberAttr.update();
						}
					}
				}
				// If this is an attr definition of type String
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If there is not already an Attr object associated with this AttrDefinition
					StringAttr stringAttr = null;
					if( attr != null ) {
						stringAttr = (StringAttr)attr;						
					}else{
						stringAttr = new StringAttr();
					}
					
					// Make sure the string value does not exceed 4000 character
					if( attrValue.length() > 4000 ){
						attrValue = attrValue.substring(0, 4000);
					}
					
					// If the value has changed
					if( stringAttr.getValue() == null || stringAttr.getValue().equalsIgnoreCase( attrValue ) == false ){
						valueHasChanged = true;
					}	
					stringAttr.setValue( attrValue );	
					stringAttr.setAttrName( ad.getAttrDefinitionName() );
					stringAttr.setAttrDefinition( ad );
					stringAttr.setAttrType( ad.getType() );
					stringAttr.setOwnerId( e.getEntityId() );
					
					// Only save or update if the value has changed
					if( valueHasChanged ){
						// If we're creating a new attr, save it
						if( isNewAttr ){
							stringAttr.save();
						}else{
							stringAttr.update();
						}
					}
				}
			}
			else
			{
				// If there is already an Attr object associated with the AttrDefinition
				// and no value was passed in, delete the attr object
				if( attr != null ){
					valueHasChanged = true;
					attr.delete();
				}
			}
		}
		
		return valueHasChanged;
	}
	
	/**
	 * Deletes any attr objects associated with this attrDefinition object
	 * that have the given string_value.
	 * 
	 * NOTE: This method assumes we are deleting StringAttr objects. This assumption
	 * can be made because it is currently only called from the EditMetadataValuesAction
	 * class which currently only supports the modifying of StringAttrs.
	 *
	 */
	public void deleteAttrs(String formattedValue)
	{
		Session session = HibernateSession.currentSession();
		List l = session.createCriteria(StringAttr.class)
			.add( Expression.eq("attrDefinition", this) )	
			.add( Expression.eq("value", formattedValue).ignoreCase() )
			.list();
		for( Iterator i=l.iterator(); i.hasNext(); )
		{
			Attr attr = (Attr)i.next();
			attr.delete( true, true );
		}
	}
	
	/**
	 * Returns a list of unique formatted values for each attr associated with this attrDefinition
	 * @return
	 */
	public List<String> getAttrValues()
	{
		Session session = HibernateSession.currentSession();
		List l = session.createQuery("SELECT attr FROM Attr attr WHERE attr.attrDefinition.attrDefinitionId = :attrDefinitionId and attr.ownerId IS NULL")
		.setParameter("attrDefinitionId", this.getAttrDefinitionId()).list();
		
		HashSet attrValues = new HashSet();
		for( Iterator i=l.iterator(); i.hasNext(); ) {
			Attr attr = (Attr)i.next();
			if(attr != null){
				attrValues.add( attr.getFormattedValue() );
			}
		}
		
		// Convert the set to a list
		LinkedList result = new LinkedList();
		result.addAll( attrValues );
		Collections.sort( result );
		return result;
	}
	
	public List<Attr> getMultiSelectAttrValues()
	{
		Session session = HibernateSession.currentSession();
		List l = session.createQuery("SELECT attr FROM Attr attr WHERE attr.attrDefinition.attrDefinitionId = :attrDefinitionId and attr.ownerId IS NULL")
		.setParameter("attrDefinitionId", this.getAttrDefinitionId()).list();
		
		LinkedList attrValues = new LinkedList();
		for( Iterator i=l.iterator(); i.hasNext(); ) {
			Attr attr = (Attr)i.next();
			if(attr != null){
				attrValues.add( attr );
			}
		}
		return attrValues;
	}

	/**
	 * Returns the lowest attr value of type Number
	 * @return
	 */
	public Float getMinNumberValue()
	{
		Float lowestValue = new Float(0);
		for( Iterator i=this.getAttrs().iterator(); i.hasNext(); )
		{
			Attr attr = (Attr)i.next();
			if( attr instanceof NumberAttr ){
				NumberAttr numberAttr = (NumberAttr)attr;
				if( numberAttr.getValue() != null ){
					if( lowestValue == null || numberAttr.getValue() < lowestValue ){
						lowestValue = numberAttr.getValue();
					}
				}
			}
		}		
		return lowestValue;
	}	
	
	/**
	 * Returns the highest attr value of type Number
	 * @return
	 */
	public Float getMaxNumberValue()
	{
		Float highestValue = new Float(0);
		for( Iterator i=this.getAttrs().iterator(); i.hasNext(); )
		{
			Attr attr = (Attr)i.next();
			if( attr instanceof NumberAttr ){
				NumberAttr numberAttr = (NumberAttr)attr;
				if( numberAttr.getValue() != null ){
					if( highestValue == null || numberAttr.getValue() > highestValue ){
						highestValue = numberAttr.getValue();
					}
				}
			}
		}		
		return highestValue;
	}		

	public static void alphabetize(String className){
		List<AttrDefinition> l = HibernateSession.currentSession().createCriteria(AttrDefinition.class)
		.createAlias("entityClass", "ec")
		.add( Expression.eq("ec.className", className ) )
		.add( Restrictions.isNotNull("type") )
		.addOrder( Order.asc("attrDefinitionName").ignoreCase() )
		.list();
		
		for(AttrDefinition ad : l){
			ad.setSeqNum(l.indexOf(ad));
			ad.update();
		}
	}
	
	/**
	 * @return Returns the attrDefinitionId.
	 */
	public Long getAttrDefinitionId() {
		return attrDefinitionId;
	}

	/**
	 * @param attrDefinitionId The attrDefinitionId to set.
	 */
	public void setAttrDefinitionId(Long attrDefinitionId) {
		this.attrDefinitionId = attrDefinitionId;
	}

	/**
	 * @return Returns the attrDefinitionName.
	 */
	public String getAttrDefinitionName() {
		return attrDefinitionName;
	}

	/**
	 * @param attrDefinitionName The attrDefinitionName to set.
	 */
	public void setAttrDefinitionName(String attrDefinitionName) {
		this.attrDefinitionName = attrDefinitionName;
	}

	/**
	 * @return Returns the entityClass.
	 */
	public EntityClass getEntityClass() {
		return entityClass;
	}

	/**
	 * @param entityClass The entityClass to set.
	 */
	public void setEntityClass(EntityClass entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return Returns the attrs.
	 */
	public Set getAttrs() {
		return attrs;
	}

	/**
	 * @param attrs The attrs to set.
	 */
	public void setAttrs(Set attrs) {
		this.attrs = attrs;
	}

	/**
	 * @return Returns the searchInterface.
	 */
	public SearchInterfaceType getSearchInterface() {
		return searchInterface;
	}
	

	/**
	 * @param searchTool The searchInterface to set.
	 */
	public void setSearchInterface(SearchInterfaceType searchInterface) {
		this.searchInterface = searchInterface;
	}

	/**
	 * @return the adServer
	 */
	public Boolean getAdServer() {
		return adServer;
	}

	/**
	 * @param adServer the adServer to set
	 */
	public void setAdServer(Boolean adServer) {
		this.adServer = adServer;
	}

	public Integer getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(Integer seqNum) {
		this.seqNum = seqNum;
	}

	public Boolean getShowInReport() {
		return showInReport;
	}

	public void setShowInReport(Boolean showInReport) {
		this.showInReport = showInReport;
	}
}