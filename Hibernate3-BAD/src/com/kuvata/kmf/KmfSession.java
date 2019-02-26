package com.kuvata.kmf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.HibernateException;

import parkmedia.KMFLogger;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;


/**
 * This class is used to store session information for the
 * currently logged in user. We use this class as a layer
 * of abstraction between the HttpSession and our business objects.
 * 
 * @author jrandesi
 *
 */
public class KmfSession {

	private static ThreadLocal<KmfSession> kmfSession = new ThreadLocal<KmfSession>();
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( KmfSession.class );
	private String appUserId;	
	private String appUsername;
	private String appUserRoleIdsParam;			// Comma delimited string of appUserRoleIds for use in filter queries
	private String appUserViewableRoleIdsParam;	// Comma delimited string of viewable appUserRoleIds for use in filter queries
	private boolean isAdmin;
	private boolean universalDataAccess;
	private boolean viewDataWithNoRoles;
	private boolean enablePermissions;
	private Set<Long> appUserRoleIds = new HashSet<Long>();
	private Set<Long> appUserViewableRoleIds = new HashSet<Long>();
	private AppUser currentAppUser;
	
	public KmfSession()
	{		
	}
	
	public static KmfSession create(String appUserId)
	{
		KmfSession kmfSession = new KmfSession();				
		boolean enablePermissions = false;
		try {
			// Attempt to locate the kuvata.property to enable permissions
			enablePermissions = Boolean.valueOf( KuvataConfig.getPropertyValue( Constants.ENABLE_PERMISSIONS ) ).booleanValue();
		} catch (KmfException e) {
			logger.info("Unable to locate property: "+ Constants.ENABLE_PERMISSIONS +". Using default: false");
		}
		kmfSession.setEnablePermissions( enablePermissions );

		AppUser appUser = null;
		try
		{
			// NOTE: For certain programs, we are intentionally passing in a string as the appUserId (e.g. "Dispatcher"),
			// as opposed to an actual appUserId. We do this for logging/history_entry purposes. 
			appUser = AppUser.getAppUser( Long.valueOf( appUserId ) );
		} 
		catch (NumberFormatException e) 
		{
			// Do not log
		}
		
		if( appUser != null )
		{			
			// If we're enabling permissions for this session
			if( enablePermissions )
			{			
				// Build the comma delimited list of role ids for this appuser
				String appUserRoleIds = "";
				for( Iterator<AppUserRole> i=appUser.getAppUserRoles().iterator(); i.hasNext(); )
				{
					AppUserRole pg = i.next();					
					if( appUserRoleIds.length() > 0 ){
						appUserRoleIds += ", ";
					}
					appUserRoleIds += "'"+ pg.getRole().getRoleId() +"'";
					
					// Add this roleId to our collection
					kmfSession.getAppUserRoleIds().add( pg.getRole().getRoleId() );
				}
				kmfSession.setAppUserRoleIdsParam( appUserRoleIds );
				
				// Build the comma delimited list of viewable role ids for this appuser
				String viewableAppUserRoleIds = "";
				for( Iterator<AppUserRole> i=appUser.getAppUserRoles().iterator(); i.hasNext(); )
				{
					AppUserRole pg = i.next();		
					if( pg.getIsViewable() != null && pg.getIsViewable() == Boolean.TRUE ){
						if( viewableAppUserRoleIds.length() > 0 ){
							viewableAppUserRoleIds += ", ";
						}
						viewableAppUserRoleIds += "'"+ pg.getRole().getRoleId() +"'";
						
						// Add this roleId to our collection
						kmfSession.getAppUserViewableRoleIds().add( pg.getRole().getRoleId() );						
					}
				}
				kmfSession.setAppUserViewableRoleIdsParam( viewableAppUserRoleIds );								
				kmfSession.setAdmin( appUser.getIsAdmin() );
				kmfSession.setAppUserId( appUser.getAppUserId().toString() );
				kmfSession.setUniversalDataAccess( appUser.getUniversalDataAccess() != null ? appUser.getUniversalDataAccess().booleanValue() : false );
				kmfSession.setViewDataWithNoRoles( appUser.getViewDataWithNoRoles() != null ? appUser.getViewDataWithNoRoles().booleanValue() : false );
			}	
			
			// Set the AppUsername even if permissions are not applied for this action so that it can be used for logging purposes
			kmfSession.setAppUsername( appUser.getName() );
			kmfSession.setCurrentAppUser(appUser);
		}
		else
		{
			// Even though we did not find a valid appUser, 
			// set the AppUsername, which will be used when logging/recording history entries
			kmfSession.setAppUsername( appUserId );
		}

		/*
		 * NOTE: We want to close the hibernate session here so that the next time the hibernate session is requested,
		 * the correct username (as supplied by the KmfSession) will be associated with the HistoryInterceptor.
		 * We have a bit of a chicken and egg situation going on here. When we call KmfSession.create(),
		 * we're trying to setup everything that will be used for subsequent database access.
		 * However, in order to get the information to populate the KmfSession, we're having to query the AppUser table.
		 * This is causing the SchemaInstance to be initialized, which in turn is initializing the HistoryInterceptor,
		 * but it's not using the "current" KmfSession information associated with this thread!
		 * By closing the hibernate session here, it will ensure that the next time the hibernate session is accessed,
		 * it will be accessed with the correct KmfSession information.
		 */
		HibernateSession.closeSession();
		return kmfSession;
	}

	/**
	 * 
	 * @return
	 */
	public static KmfSession getKmfSession()
	{
		return (KmfSession)kmfSession.get();
	}
	
	/**
	 * 
	 */
	public static void setKmfSession(KmfSession currentKmfSession) throws HibernateException
	{
		kmfSession.set( currentKmfSession );
	}

	/**
	 * @return Returns the appUserId.
	 */
	public String getAppUserId() {
		return appUserId;
	}
	

	/**
	 * @param appUserId The appUserId to set.
	 */
	public void setAppUserId(String appUserId) {
		this.appUserId = appUserId;
	}

	/**
	 * @param appUserRoleIds The appUserRoleIds to set.
	 */
	public void setAppUserRoleIds(Set<Long> appUserRoleIds) {
		this.appUserRoleIds = appUserRoleIds;
	}

	/**
	 * @return Returns the appUserRoleIds.
	 */
	public Set<Long> getAppUserRoleIds() {
		return appUserRoleIds;
	}

	/**
	 * @return Returns the appUserRoleIdParam.
	 */
	public String getAppUserRoleIdsParam() {
		return appUserRoleIdsParam;
	}
	

	/**
	 * @param appUserRoleIdParam The appUserRoleIdParam to set.
	 */
	public void setAppUserRoleIdsParam(String appUserRoleIdParam) {
		this.appUserRoleIdsParam = appUserRoleIdParam;
	}

	/**
	 * @return Returns the isAdmin.
	 */
	public boolean isAdmin() {
		return isAdmin;
	}
	

	/**
	 * @param isAdmin The isAdmin to set.
	 */
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	

	/**
	 * @return Returns the enablePermissions.
	 */
	public boolean isEnablePermissions() {
		return enablePermissions;
	}
	

	/**
	 * @param enablePermissions The enablePermissions to set.
	 */
	public void setEnablePermissions(boolean enablePermissions) {
		this.enablePermissions = enablePermissions;
	}

	/**
	 * @return Returns the universalDataAccess.
	 */
	public boolean isUniversalDataAccess() {
		return universalDataAccess;
	}
	

	/**
	 * @param universalDataAccess The universalDataAccess to set.
	 */
	public void setUniversalDataAccess(boolean universalDataAccess) {
		this.universalDataAccess = universalDataAccess;
	}

	/**
	 * @return Returns the appUsername.
	 */
	public String getAppUsername() {
		return appUsername;
	}
	

	/**
	 * @param appUsername The appUsername to set.
	 */
	public void setAppUsername(String appUsername) {
		this.appUsername = appUsername;
	}

	/**
	 * @return Returns the appUserViewableRoleIdsParam.
	 */
	public String getAppUserViewableRoleIdsParam() {
		return appUserViewableRoleIdsParam;
	}
	

	/**
	 * @param appUserViewableRoleIdsParam The appUserViewableRoleIdsParam to set.
	 */
	public void setAppUserViewableRoleIdsParam(String appUserViewableRoleIdsParam) {
		this.appUserViewableRoleIdsParam = appUserViewableRoleIdsParam;
	}

	/**
	 * @return Returns the appUserViewableRoleIds.
	 */
	public Set<Long> getAppUserViewableRoleIds() {
		return appUserViewableRoleIds;
	}
	

	/**
	 * @param appUserViewableRoleIds The appUserViewableRoleIds to set.
	 */
	public void setAppUserViewableRoleIds(Set<Long> appUserViewableRoleIds) {
		this.appUserViewableRoleIds = appUserViewableRoleIds;
	}

	/**
	 * @return Returns the viewDataWithNoRoles.
	 */
	public boolean isViewDataWithNoRoles() {
		return viewDataWithNoRoles;
	}
	

	/**
	 * @param viewDataWithNoRoles The viewDataWithNoRoles to set.
	 */
	public void setViewDataWithNoRoles(boolean viewDataWithNoRoles) {
		this.viewDataWithNoRoles = viewDataWithNoRoles;
	}

	public AppUser getCurrentAppUser() {
		return currentAppUser;
	}

	public void setCurrentAppUser(AppUser currentAppUser) {
		this.currentAppUser = currentAppUser;
	}
}
