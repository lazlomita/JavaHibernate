/*
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import parkmedia.KmfException;
import parkmedia.KuvataConfig;

import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.util.TripleDES;

/**
 * Represents the app_user table in the BASE_SCHEMA.
 * 
 * @author Jeff Randesi
 */
public class AppUser extends KmfEntity implements Comparable<AppUser> {
	
	private static Logger logger = Logger.getLogger(AppUser.class);

	private Long appUserId;
	private Long admin;
	private Boolean universalDataAccess = false;
	private String name;
	private String password;
	private String email;
	private Boolean viewDataWithNoRoles = false;
	private Boolean contentManagerAccess = false;
	private Date lastLogin;
	private Schema schema;
	private Set<AppUserRole> appUserRoles = new HistorizableSet<AppUserRole>();
	private List<AppUserRole> appUserRolesSorted;

	public AppUser() 
	{		
	}
	
	/**
	 * Queries the appuser table in the BASE_SCHEMA to try to find a matching username and password
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws HibernateException
	 */
	public static AppUser getAppUser(String username, String unencryptedPassword) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		AppUser user = null;
		String savedSchemaName = null;
		
		try {
			SchemaInstance schema = SchemaDirectory.getSchema();
			savedSchemaName = schema.getSchemaName();
		} catch (KmfException e) {			
			logger.error("SchemaDirectory not yet initialized -- switching to base schema: "+ Constants.BASE_SCHEMA);
		}
		
		// If we have not yet set the schema name
		if( savedSchemaName == null ){
			// Switch to BASE_SCHEMA schema to log in user
			SchemaDirectory.setup(Constants.BASE_SCHEMA, "AppUser");
		}
		
		// Encrypt the password as a string before querying
		TripleDES decrypter = new TripleDES();
		String encryptedPassword = decrypter.encryptToString( unencryptedPassword );
			
		// Try to find an existing AppUser for given username and password
		Session session = HibernateSession.currentSession();	
		user = (AppUser)session.createCriteria(AppUser.class)
					.add( Restrictions.eq("name", username) )
					.add( Restrictions.eq("password", encryptedPassword) )		
					.uniqueResult();
		
		// If we had previously set the schema name
		if( savedSchemaName != null ){
			// Switch back to the original schema name
			SchemaDirectory.setup(savedSchemaName, "AppUser");
		}		
		return user;
	}
	
	/**
	 * Queries the appuser table in the BASE_SCHEMA to try to find a matching username
	 * 
	 * @param username
	 * @return
	 * @throws HibernateException
	 */
	public static AppUser getAppUser(String username)
	{
		AppUser user = null;
		String savedSchemaName = null;
		
		try {
			SchemaInstance schema = SchemaDirectory.getSchema();
			savedSchemaName = schema.getSchemaName();
		} catch (KmfException e) {			
			logger.error("SchemaDirectory not yet initialized -- switching to base schema: "+ Constants.BASE_SCHEMA);
		}
		
		// If we have not yet set the schema name
		if( savedSchemaName == null ){
			// Switch to BASE_SCHEMA schema to log in user
			SchemaDirectory.setup(Constants.BASE_SCHEMA, "AppUser");
		}
			
		// Try to find an existing AppUser for given username and password
		Session session = HibernateSession.currentSession();	
		user = (AppUser)session.createCriteria(AppUser.class)
					.add( Restrictions.eq("name", username) )
					.uniqueResult();
		
		// If we had previously set the schema name
		if( savedSchemaName != null ){
			// Switch back to the original schema name
			SchemaDirectory.setup(savedSchemaName, "AppUser");
		}		
		return user;
	}
	
	/**
	 * 
	 * @param appUserId
	 * @return
	 * @throws HibernateException
	 * 
	 * This method uses the threadlocal hashmap to store app users to avoid
	 * loading the same app user from the database under the same session/thread
	 */
	public static AppUser getAppUser(Long appUserId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();								
		return (AppUser)session.createQuery("from AppUser appUser where appUser.appUserId=?").setParameter(0, appUserId).uniqueResult();
	}	
		
	/**
	 * Returns true if a user with the given name already exists in the database
	 * 
	 * @param deviceName
	 * @return
	 */
	public static boolean userExists(String userName) throws HibernateException
	{	
		Session session = HibernateSession.currentSession();
		String hql = "SELECT appUser FROM AppUser as appUser "
					+ "WHERE UPPER(appUser.name) = '"+ userName.toUpperCase() +"'";
		AppUser appUser = (AppUser)session.createQuery( hql).uniqueResult();		
		
		// If a user with the given name already exists in the database
		return appUser != null ? true : false;
	}	
	
	/**
	 * Retrieves all AppUsers in the current schema
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws HibernateException
	 */
	public static List<AppUser> getAppUsers() throws HibernateException
	{		
		String hql = "SELECT appUser " 
					+ "FROM AppUser as appUser "
					+ "JOIN appUser.schema as schema "
					+ "WHERE schema.schemaName = '"+ SchemaDirectory.getSchema().getSchemaName() +"' "	
					+ "ORDER BY UPPER(appUser.name)";
		Session session = HibernateSession.currentSession();
		List<AppUser> l = session.createQuery( hql ).list();			
		return l;
	}	
	
	/**
	 * Retrieves all AppUsers who are neither admin nor universalDataAccess
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws HibernateException
	 */
	public static List<AppUser> getNonAdminAppUsers() throws HibernateException
	{		
		String hql = "SELECT appUser " 
					+ "FROM AppUser as appUser "
					+ "JOIN appUser.schema as schema "
					+ "WHERE schema.schemaName = '"+ SchemaDirectory.getSchema().getSchemaName() +"' "
					+ "AND (appUser.admin IS NULL OR appUser.admin = 0) "
					+ "AND (appUser.universalDataAccess IS NULL OR appUser.universalDataAccess = ?) "
					+ "ORDER BY UPPER(appUser.name)";
		Session session = HibernateSession.currentSession();
		List<AppUser> l = session.createQuery( hql ).setParameter(0, Boolean.FALSE).list();			
		return l;
	}	
	
	/**
	 * Retrieves all AppUsers who are either admin or universalDataAccess
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws HibernateException
	 */
	public static List<AppUser> getAdminAppUsers() throws HibernateException
	{		
		String hql = "SELECT appUser " 
					+ "FROM AppUser as appUser "
					+ "JOIN appUser.schema as schema "
					+ "WHERE schema.schemaName = '"+ SchemaDirectory.getSchema().getSchemaName() +"' "
					+ "AND appUser.admin = 1 "
					+ "OR appUser.universalDataAccess = ? "
					+ "ORDER BY UPPER(appUser.name)";
		Session session = HibernateSession.currentSession();
		List<AppUser> l = session.createQuery( hql ).setParameter(0, Boolean.TRUE).list();			
		return l;
	}		
	
	/**
	 * Retrieves the username and schema attributes from the given session
	 * and uses them to determine if the user has administrative priveledges.
	 * @return
	 */
	public static boolean isAdmin(HttpSession httpSession)
	{
		boolean result = false;
		String appUserName = (String)httpSession.getAttribute(Constants.USERNAME);
		String schema = (String)httpSession.getAttribute(Constants.SCHEMA);
		if( appUserName != null && schema != null )
		{
			Session session = HibernateSession.currentSession();		
			String hql = "SELECT appUser FROM AppUser as appUser "
						+ "WHERE appUser.schema.schemaName = '"+ schema +"' "	
						+ "AND appUser.name = '"+ appUserName +"'";
			AppUser appUser = (AppUser)session.createQuery( hql ).uniqueResult();
			if( appUser != null && appUser.getAdmin() != 0 ) {
				result = true;
			}	
		}
		return result;
	}
	
	/**
	 * Sets the necessary session attributes, which will be used to 
	 * validate subsequent requests within this session.
	 * 
	 * @param session
	 * @param username
	 * @param schema
	 */
	public static void loginUser(HttpSession session, AppUser appUser)
	{		
		String enablePermissions = "false";
		try {
			// Attempt to locate the kuvata.property to enable permissions
			enablePermissions = KuvataConfig.getPropertyValue( Constants.ENABLE_PERMISSIONS );
		} catch (KmfException e) {
			logger.info("Unable to locate property: "+ Constants.ENABLE_PERMISSIONS +". Using default: false");
		}
		
		String enableVolumeUpdater = "false";
		try {
			// Attempt to locate the kuvata.property to enable permissions
			enableVolumeUpdater = KuvataConfig.getPropertyValue( Constants.ENABLE_VOLUME_UPDATER );
		} catch (KmfException e) {
			logger.info("Unable to locate property: "+ Constants.ENABLE_VOLUME_UPDATER +". Using default: false");
		}
		
		String contentManagerAccess = appUser.getContentManagerAccess() != null ? appUser.getContentManagerAccess().toString() : Boolean.FALSE.toString();
		loginUser( session, appUser.getAppUserId().toString(), appUser.getName(), appUser.getPassword(), appUser.getSchema().getSchemaName(), appUser.getAdmin().toString(), contentManagerAccess, enablePermissions, enableVolumeUpdater );
		
		// Update last login
		appUser.setLastLogin(new Date());
		appUser.update();
	}
	
	/**
	 * Sets the necessary session attributes, which will be used to 
	 * validate subsequent requests within this session.
	 * 
	 * @param session
	 * @param username
	 * @param schema
	 */
	private static void loginUser(HttpSession session, String userId, String username, String password, String schema, String isAdmin, String contentManagerAccess, String enablePermissions, String enableVolumeUpdater)
	{
		// Save these values as session variables
		session.setAttribute(Constants.USER_ID, userId);
		session.setAttribute(Constants.USERNAME, username);
		session.setAttribute(Constants.PASSWORD, password);
		session.setAttribute(Constants.SCHEMA, schema);	
		session.setAttribute(Constants.IS_ADMIN, isAdmin);
		session.setAttribute(Constants.CONTENT_MANAGER_ACCESS, contentManagerAccess);		
		session.setAttribute(Constants.ENABLE_PERMISSIONS, enablePermissions);
		session.setAttribute(Constants.ENABLE_VOLUME_UPDATER, enableVolumeUpdater);
		session.setAttribute(Constants.NUM_ROLES, Role.getCurrentRoles().size() );
	}
	
	/**
	 * Clears the necessary session attributes and therefore invalidates this session.
	 * 
	 * @param session
	 */
	public static void logoutUser(HttpSession session)
	{
		session.invalidate();
	}	
	
	/**
	 * Retrieves the current username out of the session
	 * 
	 * @param session
	 * @param username
	 * @param schema
	 */
	public static String getCurrentUsername(HttpSession session)
	{
		return (String)session.getAttribute(Constants.USERNAME);		
	}	
	
	/**
	 * Retrieves the current password out of the session
	 * 
	 * @param session
	 * @param username
	 * @param schema
	 */
	public static String getCurrentPassword(HttpSession session) throws Exception
	{
		// Decrypt the password as a string before returning		
		String currentPassword = (String)session.getAttribute(Constants.PASSWORD);
		if( currentPassword != null && currentPassword.length() > 0 ){
			TripleDES decrypter = new TripleDES();
			currentPassword = decrypter.decryptFromString( currentPassword );
		}
		return currentPassword;
	}		
	
	/**
	 * Retrieves the current userId out of the session
	 * 
	 * @param session
	 * @param username
	 * @param schema
	 */
	public static String getCurrentUserId(HttpSession session)
	{
		return (String)session.getAttribute(Constants.USER_ID);		
	}	
	
	/**
	 * Retrieves the current userId out of the session
	 * 
	 * @param session
	 * @param username
	 * @param schema
	 */
	public static AppUser getCurrentUser(String appUserId)
	{
		AppUser currentUser = KmfSession.getKmfSession().getCurrentAppUser();
		if(currentUser == null){
			currentUser = AppUser.getAppUser( new Long( appUserId ) );
			KmfSession.getKmfSession().setCurrentAppUser(currentUser);
		}
		return currentUser;
	}	
	
	public static AppUser create(String username, String password, Long isAdmin, String email, Boolean universalDataAccess, Boolean viewDataWithNoRoles, Boolean contentManagerAccess) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		// Locate the schema associated with the current schemaName
		Schema schema = Schema.getSchema( SchemaDirectory.getSchema().getSchemaName() );
		
		// Encrypt the password as a string
		TripleDES decrypter = new TripleDES();
		String encryptedPassword = decrypter.encryptToString( password );
		
		// Create a new user
		AppUser user = new AppUser();				
		user.setSchema( schema );
		user.setName( username );
		user.setPassword( encryptedPassword );					
		user.setAdmin( isAdmin );
		user.setUniversalDataAccess( universalDataAccess );
		user.setViewDataWithNoRoles( viewDataWithNoRoles );
		user.setContentManagerAccess( contentManagerAccess );
		user.setEmail( email );					
		user.save();
		return user;
	}
	
	/**
	 * Returns true or false depending upon whether or not this user is a member of the given role
	 * @param role
	 * @return
	 */
	public boolean isMemberOf(Role role)
	{
		if( this.getAppUserRole( role ) != null ){
			return true;
		}else{
			return false;
		}
	}
	
	public AppUserRole getAppUserRole(Role role)
	{
		for( Iterator i=this.getAppUserRoles().iterator(); i.hasNext(); )
		{
			AppUserRole appUserRole = (AppUserRole)i.next();
			if( appUserRole.getRole().getRoleId() == role.getRoleId() ){
				return appUserRole;	
			}				
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public Long save() throws HibernateException
	{		
		String savedSchemaName = SchemaDirectory.getSchema().getSchemaName();
		
		// Switch to BASE_SCHEMA schema to log in user	
		HibernateSession.closeSession();
		SchemaDirectory.setup(Constants.BASE_SCHEMA, "AppUser");
		Session session = HibernateSession.currentSession();								
		
		// Save the record
		HibernateSession.beginTransaction();	
		Long newId = (Long)session.save( this );
		HibernateSession.commitTransaction();
		
		// Switch back to the saved schema before returning
		HibernateSession.closeSession();
		SchemaDirectory.setup(savedSchemaName, "AppUser");
		session = HibernateSession.currentSession();			
		return newId;
	}	
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void update(Long isAdmin, String email, String password, Boolean universalDataAccess, Boolean viewDataWithNoRoles, Boolean contentManagerAccess) throws HibernateException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		String savedSchemaName = SchemaDirectory.getSchema().getSchemaName();
		
		// Switch to BASE_SCHEMA schema to log in user	
		HibernateSession.closeSession();
		SchemaDirectory.setup(Constants.BASE_SCHEMA, "AppUser");
		Session session = HibernateSession.currentSession();	

		// If the password has changed
		if( this.getPassword().equalsIgnoreCase( password ) == false )
		{
			// Encrypt the password as a string
			TripleDES decrypter = new TripleDES();
			String encryptedPassword = decrypter.encryptToString( password );
			this.setPassword( encryptedPassword );
		}

		// Update the record		
		this.setAdmin( isAdmin );
		this.setEmail( email );				
		this.setUniversalDataAccess( universalDataAccess );
		this.setViewDataWithNoRoles( viewDataWithNoRoles );
		this.setContentManagerAccess( contentManagerAccess );
		this.update();			
		
		// Switch back to the saved schema before returning
		HibernateSession.closeSession();
		SchemaDirectory.setup(savedSchemaName, "AppUser");
		session = HibernateSession.currentSession();			
	}	
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void update() throws HibernateException
	{
		Session session = HibernateSession.currentSession();								
		HibernateSession.beginTransaction();
		session.update( this );	
		HibernateSession.commitTransaction();	
	}		
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void delete() throws HibernateException
	{
		// First, remove this user from any alerts
		AlertUser.removeUser( this.getAppUserId() );		
		
		String savedSchemaName = SchemaDirectory.getSchema().getSchemaName();
		
		// Switch to BASE_SCHEMA schema to log in user	
		HibernateSession.closeSession();
		SchemaDirectory.setup(Constants.BASE_SCHEMA, "AppUser");
		Session session = HibernateSession.currentSession();
		
		// Delete the record
		HibernateSession.beginTransaction();
		session.delete( this );	
		HibernateSession.commitTransaction();
		
		// Switch back to the saved schema before returning
		HibernateSession.closeSession();
		SchemaDirectory.setup(savedSchemaName, "AppUser");
		session = HibernateSession.currentSession();			
	}	

	/**
	 * 
	 * @param role
	 * @throws HibernateException
	 */
	public void addSelectedRoles(String[] selectedRoles) throws HibernateException
	{				
		// For each existing role associated with this appUser
		ArrayList<AppUserRole> appUserRolesToDelete = new ArrayList<AppUserRole>();
		for( Iterator<AppUserRole> i=this.getAppUserRoles().iterator(); i.hasNext(); )
		{
			// If this role is not in the collection of selectedRoles
			AppUserRole appUserRole = i.next();
			boolean foundRole = false;
			for( int j=0; j<selectedRoles.length; j++ ){
				if( appUserRole.getRole().getRoleId().toString().equalsIgnoreCase( selectedRoles[j] ) ){
					foundRole = true;
					break;
				}
			}
			
			if( foundRole == false ){
				// Add it to the collection of appUserRoles to remove		
				appUserRolesToDelete.add( appUserRole );				
			}
		}
		
		// Remove any appUserRoles that should be
		for( AppUserRole appUserRole : appUserRolesToDelete )
		{
			this.getAppUserRoles().remove( appUserRole );
			appUserRole.delete();
		}

		// For each role that was selected
		for(int i=0; i<selectedRoles.length; i++)
		{
			// If this role is not already associated with this appUser
			boolean foundRole = false;
			for( Iterator<AppUserRole> j=this.getAppUserRoles().iterator(); j.hasNext(); )
			{
				// If this role is not in the collection of selectedRoles
				AppUserRole appUserRole = j.next();
				if( appUserRole.getRole().getRoleId().toString().equalsIgnoreCase( selectedRoles[i] ) ){
					foundRole = true;
					break;
				}
			}
			
			if( foundRole == false ){				
				// Create a new Role object
				Role role = Role.getRole( Long.valueOf( selectedRoles[i] ));	
				this.addAppUserRole( role );
			}
		}
		this.update();
	}	
	
	public AppUserRole addAppUserRole(Role role)
	{
		AppUserRole appUserRole = new AppUserRole();
		appUserRole.setRole( role );
		appUserRole.setAppUser( this );
		appUserRole.setIsDefault( false );
		appUserRole.save();
		this.appUserRoles.add( appUserRole );
		return appUserRole;
	}
	
	/**
	 * For each role in roleIds, find the matching appUserRole object for this user 
	 * and set its default flag to true. Set all other default flags for this user to false.
	 * @param roleIds
	 */
	public boolean setDefaultRoles(String[] roleIds, boolean setDefault, boolean setViewable)
	{				
		// For each role that was selected
		boolean viewableRolesHaveChanged = false;
		for( int i=0; i<roleIds.length; i++ )
		{				
			Role role = Role.getRole( new Long( roleIds[i] ) );
			AppUserRole appUserRole = this.getAppUserRole( role );
			
			// If this is an admin user (or has UDA) and this user is not already a member of this role
			if( (this.getIsAdmin()  || (this.getUniversalDataAccess() != null && this.getUniversalDataAccess())) && appUserRole == null )
			{
				// Add the admin to this role
				appUserRole = this.addAppUserRole( role );
				viewableRolesHaveChanged = true;
			}
			
			// If the isDefault property was false -- set it to true
			if( setDefault && (appUserRole.getIsDefault() == null || appUserRole.getIsDefault() == Boolean.FALSE) ){
				appUserRole.setIsDefault( Boolean.TRUE );
				appUserRole.update();
				viewableRolesHaveChanged = true;
			}
			
			// If the isViewable property was false -- set it to true
			if( setViewable && (appUserRole.getIsViewable() == null || appUserRole.getIsViewable() == Boolean.FALSE) ){
				appUserRole.setIsViewable( Boolean.TRUE );
				appUserRole.update();
				viewableRolesHaveChanged = true;
			}				
		}
		
		// For each existing appUserRole that was not in our list of roles -- set to false
		for( Iterator i=this.appUserRoles.iterator(); i.hasNext(); )
		{
			boolean foundRole = false;
			AppUserRole appUserRole = (AppUserRole)i.next();
			for( int j=0; j<roleIds.length; j++ ){
				if( appUserRole.getRole().getRoleId().toString().equalsIgnoreCase( roleIds[j] ) ){
					foundRole = true;
					break;
				}
			}
			
			// If we did not find the role in our collection of role ids, and the property is not false -- set to false
			if( foundRole == false )
			{
				if( setDefault && appUserRole.getIsDefault() != null && appUserRole.getIsDefault() == Boolean.TRUE ){
					appUserRole.setIsDefault( Boolean.FALSE );
					appUserRole.update();
					viewableRolesHaveChanged = true;
				}
				if( setViewable && appUserRole.getIsViewable() != null && appUserRole.getIsViewable() == Boolean.TRUE ){
					appUserRole.setIsViewable( Boolean.FALSE );
					appUserRole.update();
					viewableRolesHaveChanged = true;
				}					
			}
		}
		return viewableRolesHaveChanged;
	}
	
	/**
	 * Returns a list of role objects that are set to default for this user
	 * @return
	 */
	public List getDefaultRoles()
	{
		// For each role this user currently belongs to
		ArrayList result = new ArrayList();
		for( Iterator i=this.appUserRoles.iterator(); i.hasNext(); )
		{			
			AppUserRole appUserRole = (AppUserRole)i.next();
			if( appUserRole.getIsDefault() != null && appUserRole.getIsDefault() == true ){
				result.add( appUserRole.getRole() );
			}
		}
		return result;
	}
	
	// Used to sort a list of users by user name
	public int compareTo(AppUser au){
		return this.getName().toLowerCase().compareTo(au.getName().toLowerCase());
	}
	
	/**
	 * Helper function to convert the getAdmin() function into a boolean
	 * @return
	 */
	public boolean getIsAdmin()
	{
		if( getAdmin() != null && getAdmin() != 0 ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAppUserId();
	}
	
	/**
	 * @return Returns the admin.
	 */
	public Long getAdmin() {
		return admin;
	}
	/**
	 * @param admin The admin to set.
	 */
	public void setAdmin(Long admin) {
		this.admin = admin;
	}
	/**
	 * @return Returns the appUserId.
	 */
	public Long getAppUserId() {
		return appUserId;
	}
	/**
	 * @param appUserId The appUserId to set.
	 */
	public void setAppUserId(Long appUserId) {
		this.appUserId = appUserId;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return Returns the customer.
	 */
	public Schema getSchema() {
		return schema;
	}
	/**
	 * @param schema The schema to set.
	 */
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
	/**
	 * @return Returns the email.
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email The email to set.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return Returns the appUserRoles.
	 */
	public Set<AppUserRole> getAppUserRoles() {
		return appUserRoles;
	}
	

	/**
	 * @param appUserRoles The appUserRoles to set.
	 */
	public void setAppUserRoles(Set<AppUserRole> appUserRoles) {
		this.appUserRoles = appUserRoles;
	}

	/**
	 * @return Returns the appUserRolesSorted.
	 */
	public List<AppUserRole> getAppUserRolesSorted() {
		if(this.appUserRoles != null)
		{				
			List<AppUserRole> l = new LinkedList<AppUserRole>( appUserRoles );
			BeanPropertyComparator comparator1 = new BeanPropertyComparator("roleName");
			BeanPropertyComparator comparator2 = new BeanPropertyComparator("role", comparator1 );
			Collections.sort( l, comparator2 );
			appUserRolesSorted = l ;			
		}
		return appUserRolesSorted;
	}

	/**
	 * @return Returns the universalDataAccess.
	 */
	public Boolean getUniversalDataAccess() {
		return universalDataAccess;
	}
	

	/**
	 * @param universalDataAccess The universalDataAccess to set.
	 */
	public void setUniversalDataAccess(Boolean universalDataAccess) {
		this.universalDataAccess = universalDataAccess;
	}

	/**
	 * @return Returns the viewDataWithNoRoles.
	 */
	public Boolean getViewDataWithNoRoles() {
		return viewDataWithNoRoles;
	}
	

	/**
	 * @param viewDataWithNoRoles The viewDataWithNoRoles to set.
	 */
	public void setViewDataWithNoRoles(Boolean viewDataWithNoRoles) {
		this.viewDataWithNoRoles = viewDataWithNoRoles;
	}

	/**
	 * @return the contentManagerAccess
	 */
	public Boolean getContentManagerAccess() {
		return contentManagerAccess;
	}

	/**
	 * @param contentManagerAccess the contentManagerAccess to set
	 */
	public void setContentManagerAccess(Boolean contentManagerAccess) {
		this.contentManagerAccess = contentManagerAccess;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	
}
