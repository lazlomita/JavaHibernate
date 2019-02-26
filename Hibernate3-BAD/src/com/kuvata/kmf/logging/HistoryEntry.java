/*
 * Created on Sep 27, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf.logging;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.CallbackException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import com.kuvata.kmf.usertype.DeviceAutoUpdateType;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.PermissionEntry;
import com.kuvata.kmf.Playlist;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.attr.Attr;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.billing.Advertiser;
import com.kuvata.kmf.billing.VenuePartner;

/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public class HistoryEntry
{
	private Long historyId;
	private Long entityId;
	private Class entityClass;
	private String program;
	private String username;
	private String action;
	private String property;	
	private Timestamp timestamp;
	private String oldValue;
	private String newValue;
	private String formattedTimestamp;
	private static final ArrayList<String> propertyNamesToIgnore = new ArrayList<String>();
	protected static final String ACTION_CREATE = "create";
	protected static final String ACTION_UPDATE = "update";
	protected static final String ACTION_DELETE = "delete";
	
	private static final SimpleDateFormat DATE_TIME_FORMAT_DISPLAYABLE = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE);
	
	
	static
	{
		// Populate the collection of properties to ignore
		
		// TODO: Should we be ignoring all collections?? And only explicitly add propertyNames to the propertyNamesToIngore collection?
		propertyNamesToIgnore.add("historyEntries");
		propertyNamesToIgnore.add("heartbeatEvents");
		propertyNamesToIgnore.add("bytesToDownload");
		propertyNamesToIgnore.add("adler32");
		propertyNamesToIgnore.add("csvImportDetail");
		propertyNamesToIgnore.add("lastModified");
		propertyNamesToIgnore.add("lastFileExistsDt");
		propertyNamesToIgnore.add("screenshot");
		propertyNamesToIgnore.add("screenshotUploadTime");
	}
	
	/**
	 * 
	 *
	 */
	public HistoryEntry() 
	{
		this.timestamp = new Timestamp(new Date().getTime());
	}

	/**
	 * Retrieves all history entries for the given entityId whose
	 * timestamp is greater than or equal to daysBeforeCleanup.
	 * 
	 * @param entityId
	 * @return
	 */
	public static List<HistoryEntry> getHistoryEntries(Long entityId, Order orderBy)
	{
		Session session = HibernateSession.currentSession();
		Criteria crit = session.createCriteria( HistoryEntry.class )
			.add( Expression.eq("entityId", entityId));
		
		// If an orderBy was specified
		if( orderBy != null ){
			crit.addOrder( orderBy );
		}
		
		// Default / Second order by
		crit.addOrder( Order.desc("timestamp") );
		
		return crit.setMaxResults(100).list();				
	}
	
	private static HistoryEntry create(String action, Historizable entity, Object newValue, Object oldValue, String propertyName, String program, String username)
	{
		// If this property is one of the properties to ignore -- do not log
		if( propertyNamesToIgnore.contains( propertyName ) ) {
			return null;
		} 
		
		// If we're performing an update -- make sure the value has changed before logging
		if( action.equalsIgnoreCase( ACTION_UPDATE ) )
		{
			// If both the old and new value is null -- do not log
			if (oldValue == null && newValue == null){
				return null;
			}
				  
			if( oldValue != null && newValue != null && oldValue.equals( newValue ) == true ) {
				return null;
			}
			
			// If one is null and the other is an empty string -- do not log
			if( oldValue == null && newValue != null && newValue.toString().length() == 0){
				return null;
			}
			if( newValue == null && oldValue != null && oldValue.toString().length() == 0){
				return null;
			}				
		}
		
		HistoryEntry entry = null;
		Long entityId = entity.getEntityId();
		String strOldValue = format( oldValue );
		String strNewValue = format( newValue );
		
		boolean goAhead = true;
		
		// Make sure that the formatted string values are not same unless this entry doesnt have values at all
		if(strOldValue != null && strNewValue != null && strOldValue.equals(strNewValue)){
			goAhead = false;
		}
		
		if( goAhead ){
			
			// If this entity is a HistorizableChildEntity (which is the case, for example, with AssetPresentation objects)
			if( entity instanceof HistorizableChildEntity )
			{
				// Retrieve the historyEntityId
				// In the case of AssetPresentation, we want to associate the asset presentation with either the asset or the playlist 
				// (as opposed to the asset presentation itself).
				// Also, prepend the name of the entity to the old and new values
				// (For example, Sample Asset: 10)
				HistorizableChildEntity historizableChildMember = (HistorizableChildEntity)entity;
				entityId = historizableChildMember.getHistoryEntityId();
				
				// Since the role underlying the permission entry is lazily loaded,
				// we need to lock it to the new session (since we are in postflush)
				// so that it can be loaded again
				if(historizableChildMember instanceof PermissionEntry){
					PermissionEntry pe = (PermissionEntry)historizableChildMember;
					HibernateSession.currentSession().lock( pe.getRole(), LockMode.NONE );
				}
				
				if( historizableChildMember.getEntityName() != null && historizableChildMember.getEntityName().length() > 0 )
				{
					// If we're performing a create
					if( action.equalsIgnoreCase( ACTION_CREATE ) )
					{
						// Only log the new value
						if( entity instanceof Attr ){
							strNewValue = historizableChildMember.getEntityName() +": "+ ((Attr)entity).getFormattedValue();	
						}else{
							strNewValue = historizableChildMember.getEntityName();
						}					
					}
					else if( action.equalsIgnoreCase( ACTION_UPDATE ) )
					{
						// Log both the old and the new values
						strOldValue = historizableChildMember.getEntityName() +": "+ format( oldValue );
						strNewValue = historizableChildMember.getEntityName() +": "+ format( newValue );	
					}
					else if( action.equalsIgnoreCase( ACTION_DELETE ) )
					{
						// Only log the old value
						if( entity instanceof Attr ){
							strOldValue = historizableChildMember.getEntityName() +": "+ ((Attr)entity).getFormattedValue();	
						}else{
							strOldValue = historizableChildMember.getEntityName();
						}									
					}				
					
				}
			}
			
			if(entityId != null){
				// Generate a new entry				
				entry = new HistoryEntry();
				entry.setAction( action );				
				entry.setEntityId( entityId );
				entry.setEntityClass( entity.getClass() );
				entry.setProgram( program );
				entry.setUsername( username );
				entry.setProperty( propertyName );
				entry.setOldValue( strOldValue );
				entry.setNewValue( strNewValue );
			}	
		}
		
		return entry;
	}
	
	/**
	 * 
	 * @param action
	 * @param entity
	 * @param newValues
	 * @param oldValues
	 * @param propertyNames
	 * @param program
	 * @param username
	 * @param connection
	 * @throws CallbackException
	 * @throws HibernateException
	 */
	public static void logEvent(String action, Historizable entity, Object[] newValues, Object[] oldValues, Object[] propertyNames, String program, String username) throws CallbackException, HibernateException 
	{
		Session tempSession = SchemaDirectory.getSchema().getSessionFactory().openSession();
		Transaction tx = tempSession.beginTransaction();
		try 
		{
			// If we're performing an update, the oldValues and newValues collections should be populated
			if( action.equalsIgnoreCase( ACTION_UPDATE ) )
			{
				// If both oldValues or newValues is null -- do not log
				if( oldValues != null || newValues != null )
				{
					// Log a row for each property that was updated
					for (int i = 0; i < propertyNames.length; i++) 
					{
						Object newValue = newValues != null ? newValues[i] : null;
						Object oldValue = oldValues != null ? oldValues[i] : null;
						HistoryEntry historyEntry = create( action, entity, newValue, oldValue, propertyNames[i].toString(), program, username );
						if( historyEntry != null ){
							tempSession.save( historyEntry );
						}						
					}
				}	

			}
			// If we're performing a create or a delete
			else
			{				
				// If either newValues or oldValues were supplied, we're assuming there is only one item in each array
				Object newValue = (newValues != null && newValues[0] != null ) ? newValues[0] : null;
				Object oldValue = (oldValues != null && oldValues[0] != null ) ? oldValues[0] : null;
				String propertyName = (propertyNames != null && propertyNames[0] != null ) ? propertyNames[0].toString() : null;
				
				// Create the history entry without comparing old and new values
				HistoryEntry historyEntry = create( action, entity, newValue, oldValue, propertyName, program, username );
				if( historyEntry != null ){
					tempSession.save( historyEntry );
				}
			}
		} 
		catch (Exception ex) 
		{
			throw new CallbackException(ex);
		} 
		finally 
		{
			try 
			{
				tempSession.flush();
				tempSession.clear();
				tx.commit();
				tempSession.close();
			} 
			catch (HibernateException ex) 
			{
				throw new CallbackException(ex);
			}
		}
	}	
	
	/**
	 * Helper method
	 * @param message
	 * @param entity
	 * @param program
	 * @param username
	 * @param connection
	 * @throws CallbackException
	 * @throws HibernateException
	 */
	public static void logEvent(String message, Historizable entity, String program, String username) throws CallbackException, HibernateException 
	{
		String newValue = null;
		String oldValue = null;
		String propertyName = null;
		logEvent( message, entity, newValue, oldValue, propertyName, program, username );
	}
	
	/**
	 * Helper method
	 * @param message
	 * @param entity
	 * @param program
	 * @param connection
	 * @throws CallbackException	
	 * @throws HibernateException
	 */
	public static void logEvent(String message, Historizable entity, String newValue, String oldValue, String propertyName, String program, String username) throws CallbackException, HibernateException 
	{
		logEvent( message, entity, new Object[]{ newValue }, new Object[]{ oldValue }, new Object[]{ propertyName }, program, username );
	}
	/**
	 * 
	 * @param obj
	 * @return
	 */
	private static String format(Object obj) 
	{
		// Make sure the value does not exceed the size of the field in the database (currently varchar(4000)) 
		if (obj == null) {
			return null;
		}else if( obj instanceof Device ){
			return ((Device)obj).getDeviceName().toString();
		}else if( obj instanceof Playlist ){
			return ((Playlist)obj).getPlaylistName().toString();
		}else if( obj instanceof Asset ){
			return ((Asset)obj).getAssetName().toString();
		}else if (obj.toString().length() > 4000 ){
			return obj.toString().substring(0, 3999);
		}else if( obj instanceof DeviceAutoUpdateType ){
			return ((DeviceAutoUpdateType)obj).getName();
		}else if( obj instanceof Displayarea ){
			return ((Displayarea)obj).getDisplayareaName();
		}else if( obj instanceof Layout ){
			return ((Layout)obj).getLayoutName();
		}else if( obj instanceof AttrDefinition ){
			return ((AttrDefinition)obj).getAttrDefinitionName();
		}else if( obj instanceof VenuePartner ){
			return ((VenuePartner)obj).getVenuePartnerName();
		}else if( obj instanceof Advertiser ){
			return ((Advertiser)obj).getAdvertiserName();
		}else if( obj instanceof Date ){
			return (DATE_TIME_FORMAT_DISPLAYABLE.format((Date)obj));
		}else {
			return obj.toString();	
		}	  
	}		
	
	/**
	 * @return Returns the action.
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action The action to set.
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return Returns the entityClass.
	 */
	public Class getEntityClass() {
		return entityClass;
	}

	/**
	 * @param entityClass The entityClass to set.
	 */
	public void setEntityClass(Class entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return Returns the entityId.
	 */
	public Long getEntityId() {
		return entityId;
	}

	/**
	 * @param entityId The entityId to set.
	 */
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	/**
	 * @return Returns the newValue.
	 */
	public String getNewValue() {
		return newValue;
	}

	/**
	 * @param newValue The newValue to set.
	 */
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	/**
	 * @return Returns the oldValue.
	 */
	public String getOldValue() {
		return oldValue;
	}

	/**
	 * @param oldValue The oldValue to set.
	 */
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	/**
	 * @return Returns the program.
	 */
	public String getProgram() {
		return program;
	}

	/**
	 * @param program The program to set.
	 */
	public void setProgram(String program) {
		this.program = program;
	}

	/**
	 * @return Returns the property.
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * @param property The property to set.
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return Returns the historyId.
	 */
	public Long getHistoryId() {
		return historyId;
	}

	/**
	 * @param historyId The historyId to set.
	 */
	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}
	
	/**
	 * @param formattedTimestamp The formattedTimestamp to set.
	 */
	public void setFormattedTimestamp(String formattedTimestamp) {
		this.formattedTimestamp = formattedTimestamp;
	}
	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	

}
