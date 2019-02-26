package com.kuvata.kmf;

import java.sql.Clob;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.struts.util.LabelValueBean;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import parkmedia.usertype.AlertDefinitionType;

import com.kuvata.dispatcher.services.Dispatchable;
import com.kuvata.dispatcher.services.DispatchableServiceLocator;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableSet;

/**
 *
 * 
 * @author Jeff Randesi
 * Created on Sep. 15, 2005
 * Copyright 2005, Kuvata, Inc.
 */
public class Alert extends Entity implements Historizable
{
	public static final String HOUR_FORMAT = "h";
	public static final String MINUTE_FORMAT = "mm";
	public static final String AMPM_FORMAT = "a";
	private Long alertId;
	private String alertName;
	private AlertDefinitionType className;
	private String parameters;
	private String frequency;
	private Boolean isActive;
	private Boolean alertAllDevices;
	private Boolean alertAllCampaigns;
	private Date activeStartTime;
	private Date activeEndTime;
	private Clob lastResults;
	private Boolean onlyAlertOnChange;
	private Integer minimumTime;
	private Boolean attemptToFix;
	private Boolean alertIfContentScheduled;
	private Boolean showHeartbeatingDevices;
	private Boolean showContentRemaining;
	private Boolean includeFutureContent;
	private Date lastRunDt;
	private Integer numDevices;
	private Set<AlertUser> alertUsers = new HistorizableSet<AlertUser>();
	private Set<AlertDevice> alertDevices = new HistorizableSet<AlertDevice>();
	private Set<AlertCampaign> alertCampaigns = new HistorizableSet<AlertCampaign>();
	

	public final static Logger logger = Logger.getLogger(Alert.class);
	
	/**
	 * 
	 *
	 */
	public Alert()
	{		
	}	
	
	/**
	 * Returns a list of all alerts, both active and not active
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getAlerts() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List l = session.createQuery(				
				"SELECT alert "
				+ "FROM Alert as alert "				
				+ "ORDER BY UPPER(alert.alertName)"						
				).list();				
		return l;
	}	
	
	/**
	 * Returns a list of all active alerts.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getActiveAlerts() throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List l = session.createCriteria(Alert.class)
				.add( Expression.eq("isActive", Boolean.TRUE) )
				.addOrder( Order.desc("alertName") )
				.list();						
		return l;
	}	
	
	/**
	 * Returns true if an alert with the given name already exists in the database
	 * 
	 * @param deviceName
	 * @return
	 */
	public static boolean alertExists(String alertName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		Alert alert = (Alert) session.createCriteria(Alert.class)
					.add( Expression.eq("alertName", alertName).ignoreCase() )
					.uniqueResult();
				
		// If an alert with the given name already exists in the database
		if(alert != null)
			return true;
		else
			return false;
	}		
	
	/**
	 * 
	 * @param deviceId
	 * @return
	 * @throws HibernateException
	 */
	public static Alert getAlert(AlertDefinitionType alertType) throws HibernateException
	{
		Alert alert = null;
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(Alert.class)
					.add( Expression.eq("className", alertType) )
					.list();
		
		// It it possible to have the same alert class assigned to more than one alert
		// As such, arbitrarily return the first one
		if( l != null && l.size() > 0 ){
			alert = (Alert)l.get(0);
		}
		return alert;		
	}
	
	public static Alert createOrUpdate(Long alertId, String alertName, AlertDefinitionType alertDefinition, String parameters, String frequency, Integer minimumTime, Boolean isActive,
			Boolean alertAllDevices, Boolean alertAllCampaigns, Boolean onlyAlertOnChange, Boolean attemptToFix, Boolean alertIfContentScheduled, Boolean showHeartbeatingDevices,
			Boolean showContentRemaining, Boolean includeFutureContent, Integer numDevices, Date activeStartTime, Date activeEndTime) throws ParseException
	{		
		Alert alert = Alert.getAlert( alertId );
		boolean isUpdate = true;
		boolean resetAlert = false;
		if( alert == null )
		{
			// Create a new Alert
			alert = new Alert();
			isUpdate = false;							
		}
		else
		{
			// If any of the parameters or the frequency of the alert have changed			
			if( alert.getParameters() != null && alert.getParameters().equalsIgnoreCase( parameters ) == false ){
				resetAlert = true;
			}else if( alert.getFrequency() != null && alert.getFrequency().equalsIgnoreCase( frequency ) == false ){
				resetAlert = true;
			}else if( alert.getActiveStartTime() != null && alert.getActiveStartTime() != activeStartTime ){
				resetAlert = true;
			}else if( alert.getActiveEndTime() != null && alert.getActiveEndTime() != activeEndTime ){
				resetAlert = true;
			}else if( Device.propertyHasChanged( alert.getOnlyAlertOnChange(), onlyAlertOnChange ) ){
				resetAlert = true;
			}else if( Device.propertyHasChanged( alert.getAttemptToFix(), attemptToFix ) ){
				resetAlert = true;
			}else if( Device.propertyHasChanged( alert.getMinimumTime(), minimumTime ) ){
				resetAlert = true;
			}else if( Device.propertyHasChanged( alert.getAlertIfContentScheduled(), alertIfContentScheduled ) ){
				resetAlert = true;
			}else if( Device.propertyHasChanged( alert.getShowHeartbeatingDevices(), showHeartbeatingDevices ) ){
				resetAlert = true;
			}else if( Device.propertyHasChanged( alert.getShowContentRemaining(), showContentRemaining ) ){
				resetAlert = true;
			}else if( Device.propertyHasChanged( alert.getIncludeFutureContent(), includeFutureContent ) ){
				resetAlert = true;
			}
		}
		alert.setAlertName( alertName );				
		alert.setClassName( alertDefinition );
		alert.setParameters( parameters );
		alert.setFrequency( frequency );
		alert.setMinimumTime( minimumTime );
		alert.setIsActive( isActive );
		alert.setAlertAllDevices( alertAllDevices );
		alert.setAlertAllCampaigns(alertAllCampaigns);
		alert.setOnlyAlertOnChange( onlyAlertOnChange );
		alert.setAttemptToFix( attemptToFix );
		alert.setActiveStartTime( activeStartTime );
		alert.setActiveEndTime( activeEndTime );
		alert.setAlertIfContentScheduled(alertIfContentScheduled);
		alert.setShowHeartbeatingDevices(showHeartbeatingDevices);
		alert.setShowContentRemaining(showContentRemaining);
		alert.setIncludeFutureContent(includeFutureContent);
		alert.setNumDevices(numDevices);
		
		// Either update the existing alert or save the new alert
		if( isUpdate ){
			alert.update();				
		}else{
			alert.save();
		}
		
		// Make the web services call to reset this alert in the dispatcher web space
		if( resetAlert ){
			try {
				Dispatchable dispatcher = DispatchableServiceLocator.getJobServerDispatcher();
				dispatcher.resetAlert( SchemaDirectory.getSchema().getSchemaName(), alert.getAlertId() );
			} catch (Exception e) {	
				logger.error("An unexpected error occurred while executing resetAlert web service request.", e);
			}				
		}		
		return alert;
	}
	
	/**
	 * Copies this alert and assigns the given new alert name.
	 * 
	 * @param newDeviceName
	 * @return
	 */
	public Long copy(String newAlertName) throws ClassNotFoundException
	{				
		// First, create a new alert object
		Alert newAlert = new Alert();
		newAlert.setAlertName( newAlertName );
		newAlert.setClassName( this.getClassName() );
		newAlert.setParameters( this.getParameters() );
		newAlert.setFrequency( this.getFrequency() );
		newAlert.setMinimumTime( this.getMinimumTime() );
		newAlert.setIsActive( this.getIsActive() );
		newAlert.setAlertAllDevices( this.getAlertAllDevices() );
		newAlert.setActiveStartTime( this.getActiveStartTime() );
		newAlert.setActiveEndTime( this.getActiveEndTime() );
		newAlert.setOnlyAlertOnChange( this.getOnlyAlertOnChange() );
		newAlert.setShowHeartbeatingDevices( this.getShowHeartbeatingDevices() );
		newAlert.setShowContentRemaining( this.getShowContentRemaining() );
		newAlert.setAttemptToFix( this.getAttemptToFix() );
		newAlert.setAlertIfContentScheduled( this.getAlertIfContentScheduled() );
		newAlert.setIncludeFutureContent( this.getIncludeFutureContent() );
		newAlert.setNumDevices( this.getNumDevices() );

		// Save the alert but do not create permission entries since we are going to copy them		
		Long newAlertId = newAlert.save( false );
		newAlert.copyPermissionEntries( this );							
	
		// Second, copy all alertUsers associated with this alert
		for( Iterator<AlertUser> i= this.getAlertUsers().iterator(); i.hasNext(); )
		{
			AlertUser alertUser = i.next();
			AlertUser newAlertUser = new AlertUser();			
			newAlertUser.setAlert( newAlert );
			newAlertUser.setUserId( alertUser.getUserId() );			
			newAlertUser.save();			
		}		

		// Third, copy any alertDevices associated with this alert
		for( Iterator<AlertDevice> i= this.getAlertDevices().iterator(); i.hasNext(); )
		{
			AlertDevice alertDevice = i.next();
			AlertDevice newAlertDevice = new AlertDevice();			
			newAlertDevice.setAlert( newAlert );
			newAlertDevice.setDevice( alertDevice.getDevice() );	
			newAlertDevice.setDeviceGrp( alertDevice.getDeviceGrp() );	
			newAlertDevice.save();			
		}	
		return newAlertId;
	}		
	
	/**
	 * Removes all users from this alert
	 * 
	 * @throws HibernateException
	 */
	public void removeAlertUsers() throws HibernateException
	{
		for( Iterator i=this.alertUsers.iterator(); i.hasNext(); )
		{
			AlertUser au = (AlertUser)i.next();
			au.delete();
			i.remove();
		}
	}	
	
	/**
	 * Removes all devices associated with this alert
	 * 
	 * @throws HibernateException
	 */
	public void removeAlertDevices() throws HibernateException
	{
		for( Iterator i=this.alertDevices.iterator(); i.hasNext(); )
		{
			AlertDevice ad = (AlertDevice)i.next();
			ad.delete();
			i.remove();
		}
	}
	
	/**
	 * Removes all campaigns associated with this alert
	 * 
	 * @throws HibernateException
	 */
	public void removeAlertCampaigns() throws HibernateException
	{
		for( Iterator i=this.alertCampaigns.iterator(); i.hasNext(); )
		{
			AlertCampaign ac = (AlertCampaign)i.next();
			ac.delete();
			i.remove();
		}
	}
	
	/**
	 * Builds a string of all email address associated with all alert users for this alert
	 * @return
	 */
	public String buildEmailAddresses()
	{
		StringBuffer sb = new StringBuffer();
		ArrayList emailAddresses = new ArrayList();
		Long[] userIds = new Long[ this.alertUsers.size() ];
		int idCounter = 0;
		
		// First, get all the userId's out of the alert user objects		
		for( Iterator i=this.alertUsers.iterator(); i.hasNext(); )
		{
			AlertUser au = (AlertUser)i.next();
			userIds[ idCounter++ ] = au.getUserId();
		}
		
		// Next, look up the email address for each user in the base schema
		// and add them to our collection
		for( int i=0; i<userIds.length; i++ )
		{						
			// Get the user associated with this userId
			AppUser user = AppUser.getAppUser( userIds[i] );
			if( user != null && user.getEmail() != null ) 
			{
				emailAddresses.add( user.getEmail() );
			}	
		}		
		
		// Finally, sort the list of email addresses and build the comma delimited string
		Collections.sort( emailAddresses );
		for( Iterator i=emailAddresses.iterator(); i.hasNext(); )
		{
			String emailAddress = (String)i.next();
			if( sb.toString().length() > 0 ) {
				sb.append(", ");
			}				
			sb.append( emailAddress );
		}
		return sb.toString();
	}
	
	/**
	 * Returns the list of alert devices sorted by device name
	 * @return
	 */
	public List<LabelValueBean> getAlertDevicesSorted()
	{
		List result = new LinkedList<LabelValueBean>();							
		for( Iterator<AlertDevice> i = this.getAlertDevices().iterator(); i.hasNext(); )
		{		
			String id = null;
			String name = null;
			AlertDevice alertDevice = i.next();
			if( alertDevice.getDevice() != null )
			{												
				name = alertDevice.getDevice().getDeviceName();
				id = alertDevice.getDevice().getDeviceId().toString();
			}
			else if( alertDevice.getDeviceGrp() != null )
			{												
				name = alertDevice.getDeviceGrp().getGrpName()+ " (DG)";				
				id = alertDevice.getDeviceGrp().getGrpId().toString() + Constants.DEVICE_GROUP_SUFFIX;
			}				
			LabelValueBean bean = new LabelValueBean( name, id );		
			result.add( bean );
		}				
				
		BeanPropertyComparator comparator = new BeanPropertyComparator( "label" );								
		Collections.sort( result, comparator );
		return result;					
	}	
	
	public List<LabelValueBean> getAlertCampaignsSorted()
	{
		List result = new LinkedList<LabelValueBean>();							
		for( Iterator<AlertCampaign> i = this.getAlertCampaigns().iterator(); i.hasNext(); )
		{		
			String id = null;
			String name = null;
			AlertCampaign alertCampaign = i.next();
			if( alertCampaign.getCampaign() != null )
			{												
				name = alertCampaign.getCampaign().getCampaignName();
				id = alertCampaign.getCampaign().getCampaignId().toString();
			}
			LabelValueBean bean = new LabelValueBean( name, id );		
			result.add( bean );
		}				
				
		BeanPropertyComparator comparator = new BeanPropertyComparator( "label" );								
		Collections.sort( result, comparator );
		return result;					
	}	
	
	/**
	 * Returns whether or not this alert is configured to alert specific devices or not
	 * @return
	 */
	public boolean alertSpecificDevices()
	{
		boolean alertSpecificDevices = false;
		if( this.getAlertAllDevices() != null && this.getAlertAllDevices() == Boolean.FALSE ){
			alertSpecificDevices = true;
		}
		return alertSpecificDevices;
	}
	
	/**
	 * @return Returns the number of alerts
	 */
	public static int getAlertsCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT COUNT(a) FROM Alert as a";
		Iterator i = session.createQuery( hql ).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}	
	
	public List<Device> getDevices(){
		ArrayList result = new ArrayList();
		for(AlertDevice ad : alertDevices){
			result.add(ad.getDevice());
		}
		return result;
	}
	
	/**
	 * 
	 * @param deviceId
	 * @return
	 * @throws HibernateException
	 */
	public static Alert getAlert(Long alertId) throws HibernateException
	{
		return (Alert)Entity.load(Alert.class, alertId);		
	}

	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAlertId();
	}	
	/**
	 * @return Returns the alertId.
	 */
	public Long getAlertId() {
		return alertId;
	}
	/**
	 * @param alertId The alertId to set.
	 */
	public void setAlertId(Long alertId) {
		this.alertId = alertId;
	}
	/**
	 * @return Returns the alertName.
	 */
	public String getAlertName() {
		return alertName;
	}
	/**
	 * @param alertName The alertName to set.
	 */
	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}
	/**
	 * @return Returns the className.
	 */
	public AlertDefinitionType getClassName() {
		return className;
	}
	/**
	 * @param className The className to set.
	 */
	public void setClassName(AlertDefinitionType className) {
		this.className = className;
	}
	/**
	 * @return Returns the parameters.
	 */
	public String getParameters() {
		return parameters;
	}
	/**
	 * @param parameters The parameters to set.
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	/**
	 * @return Returns the isActive.
	 */
	public Boolean getIsActive() {
		if( isActive == null ) isActive = Boolean.FALSE;
		return isActive;
	}
	/**
	 * @param isActive The isActive to set.
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @return Returns the alertAllDevices.
	 */
	public Boolean getAlertAllDevices() {
		return alertAllDevices;
	}
	

	/**
	 * @param alertAllDevices The alertAllDevices to set.
	 */
	public void setAlertAllDevices(Boolean alertAllDevices) {
		this.alertAllDevices = alertAllDevices;
	}

	/**
	 * @return Returns the frequency.
	 */
	public String getFrequency() {
		return frequency;
	}
	

	/**
	 * @param frequency The frequency to set.
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return Returns the activeEndTime.
	 */
	public Date getActiveEndTime() {
		return activeEndTime;
	}
	

	/**
	 * @param activeEndTime The activeEndTime to set.
	 */
	public void setActiveEndTime(Date activeEndTime) {
		this.activeEndTime = activeEndTime;
	}
	

	/**
	 * @return Returns the activeStartTime.
	 */
	public Date getActiveStartTime() {
		return activeStartTime;
	}
	

	/**
	 * @param activeStartTime The activeStartTime to set.
	 */
	public void setActiveStartTime(Date activeStartTime) {
		this.activeStartTime = activeStartTime;
	}

	/**
	 * @return the lastResults
	 */
	public Clob getLastResults() {
		return lastResults;
	}

	/**
	 * @param lastResults the lastResults to set
	 */
	public void setLastResults(Clob lastResults) {
		this.lastResults = lastResults;
	}

	/**
	 * @return the onlyAlertOnChange
	 */
	public Boolean getOnlyAlertOnChange() {
		return onlyAlertOnChange;
	}

	/**
	 * @param onlyAlertOnChange the onlyAlertOnChange to set
	 */
	public void setOnlyAlertOnChange(Boolean onlyAlertOnChange) {
		this.onlyAlertOnChange = onlyAlertOnChange;
	}

	/**
	 * @return the minimumTime
	 */
	public Integer getMinimumTime() {
		return minimumTime;
	}

	/**
	 * @param minimumTime the minimumTime to set
	 */
	public void setMinimumTime(Integer minimumTime) {
		this.minimumTime = minimumTime;
	}

	/**
	 * @return the attemptToFix
	 */
	public Boolean getAttemptToFix() {
		return attemptToFix;
	}

	/**
	 * @param attemptToFix the attemptToFix to set
	 */
	public void setAttemptToFix(Boolean attemptToFix) {
		this.attemptToFix = attemptToFix;
	}

	/**
	 * @return the alertUsers
	 */
	public Set<AlertUser> getAlertUsers() {
		return alertUsers;
	}

	/**
	 * @param alertUsers the alertUsers to set
	 */
	public void setAlertUsers(Set<AlertUser> alertUsers) {
		this.alertUsers = alertUsers;
	}

	/**
	 * @return the alertDevices
	 */
	public Set<AlertDevice> getAlertDevices() {
		return alertDevices;
	}

	/**
	 * @param alertDevices the alertDevices to set
	 */
	public void setAlertDevices(Set<AlertDevice> alertDevices) {
		this.alertDevices = alertDevices;
	}

	public Boolean getAlertIfContentScheduled() {
		return alertIfContentScheduled;
	}

	public void setAlertIfContentScheduled(Boolean alertIfContentScheduled) {
		this.alertIfContentScheduled = alertIfContentScheduled;
	}

	public Boolean getShowHeartbeatingDevices() {
		return showHeartbeatingDevices;
	}

	public void setShowHeartbeatingDevices(Boolean showHeartbeatingDevices) {
		this.showHeartbeatingDevices = showHeartbeatingDevices;
	}

	public Boolean getAlertAllCampaigns() {
		return alertAllCampaigns;
	}

	public void setAlertAllCampaigns(Boolean alertAllCampaigns) {
		this.alertAllCampaigns = alertAllCampaigns;
	}

	public Set<AlertCampaign> getAlertCampaigns() {
		return alertCampaigns;
	}

	public void setAlertCampaigns(Set<AlertCampaign> alertCampaigns) {
		this.alertCampaigns = alertCampaigns;
	}

	public Boolean getShowContentRemaining() {
		return showContentRemaining;
	}

	public void setShowContentRemaining(Boolean showContentRemaining) {
		this.showContentRemaining = showContentRemaining;
	}

	public Date getLastRunDt() {
		return lastRunDt;
	}

	public void setLastRunDt(Date lastRunDt) {
		this.lastRunDt = lastRunDt;
	}

	public Boolean getIncludeFutureContent() {
		return includeFutureContent;
	}

	public void setIncludeFutureContent(Boolean includeFutureContent) {
		this.includeFutureContent = includeFutureContent;
	}

	public Integer getNumDevices() {
		return numDevices;
	}

	public void setNumDevices(Integer numDevices) {
		this.numDevices = numDevices;
	}
}
