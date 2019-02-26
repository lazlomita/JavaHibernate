package com.kuvata.kmf;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.KMFLogger;

import com.kuvata.kmf.logging.HistorizableChildEntity;

public class PermissionEntry extends PersistentEntity implements HistorizableChildEntity
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( PermissionEntry.class );
	private Long permissionEntryId;
	private Long permissionEntityId;
	private Role role;	
	
	
	/**
	 * 
	 *
	 */
	public PermissionEntry()
	{		
	}	
	
	/**
	 * 
	 * @param dirtyId
	 * @return
	 * @throws HibernateException
	 */
	public static PermissionEntry getPermissionGroup(Long permissionEntryId) throws HibernateException
	{
		return (PermissionEntry)PersistentEntity.load(PermissionEntry.class, permissionEntryId);		
	}

	/**
	 * Creates a permissionEntry object for each of the given roleIds
	 * @param entityId
	 */
	public static void create(Long entityId, String[] roleIds, boolean resetPermissions)
	{
		// If the flag to reset permissions was passed in
		if( resetPermissions ) 
		{
			// Remove any existing permission entries that were not in the collection of selected roleIds
			List<PermissionEntry> existingPermissionEntries = PermissionEntry.getPermissionEntries( entityId );	
			for( PermissionEntry existingPermissionEntry : existingPermissionEntries )
			{
				boolean foundEntry = false;
				for( int i=0; i<roleIds.length; i++ )
				{
					if( roleIds[i].equals( existingPermissionEntry.getRole().getRoleId().toString() ) ){
						foundEntry = true;
						break;
					}
				}
				
				// If this existing permission entry was not in the collection of selected roles
				if( foundEntry == false )
				{
					// Don't delete the *NoRoles role
					if(existingPermissionEntry.getRole().getRoleId().longValue() != 0l){
						// Delete it
						existingPermissionEntry.delete();
					}
				}				
			}
		}
		
		for( int i=0; i<roleIds.length; i++ )
		{
			// If there is not already a PermissionEntry object for this entity/role
			Role role = Role.getRole( Long.valueOf( roleIds[i] ) );
			if( PermissionEntry.exists( entityId, role ) == false )
			{
				// Create a new permission entry object
				PermissionEntry.create( entityId, role );				
			}
		}
		
		Asset asset = Asset.getAsset(entityId);
		if(asset != null){
			manageNoRolesForAssets(asset);
		}
	}
	
	protected static void manageNoRolesForAssets(Asset asset){
		
		// Flush all permission entry changes we've made so far in this session
		HibernateSession.currentSession().flush();
		
		// We only want to create a NoRole row if this asset has no others roles
		BigDecimal count = (BigDecimal)HibernateSession.currentSession().createSQLQuery("SELECT COUNT(*) FROM permission_entry WHERE permission_entity_id = :entityId AND role_id != 0").setLong("entityId", asset.getAssetId()).list().get(0);
		if( count.longValue() == 0l ){
			// Add a no role permission entry
			Role noRoles = Role.getRole(0l);
			PermissionEntry.create(asset.getAssetId(), noRoles);
		}else{
			HibernateSession.currentSession().createSQLQuery("DELETE FROM permission_entry WHERE permission_entity_id = :entityId AND role_id = 0").setLong("entityId", asset.getAssetId()).executeUpdate();
		}
		
	}
	
	/**
	 * Removes all existing PermissionEntry objects for the given entity
	 * @param entityId
	 */
	protected static void resetPermissionEntries(Long entityId)
	{
		AppUser appUser = null;
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null && kmfSession.getAppUserId() != null ) {
			appUser = AppUser.getAppUser( Long.valueOf( kmfSession.getAppUserId() ) );
		}
		
		// Remove all existing permisionEntries for this entity that this user belongs to
		List<PermissionEntry> permissionEntries = PermissionEntry.getPermissionEntries( entityId );
		for( Iterator<PermissionEntry> i=permissionEntries.iterator(); i.hasNext(); ){
			PermissionEntry pe = i.next();
			
			// Don't delete the *NoRoles role
			if(pe.getRole().getRoleId().longValue() != 0l){
				// If the current user is an admin, or the current user belongs to the role associated with this permission entry
				if( appUser != null && (appUser.getIsAdmin() || appUser.isMemberOf( pe.getRole() )) ){
					pe.delete();	
				}	
			}		
		}
	}
	
	public static void create(Long entityId, Role role){
		// Don't duplicate the *NoRoles role
		if(role.getRoleId().longValue() == 0l){
			if(exists(entityId, role)){
				return;
			}
		}
		
		PermissionEntry pe = new PermissionEntry();
		pe.setPermissionEntityId( entityId );
		pe.setRole( role );
		pe.save();
	}
	
	/**
	 * Returns true if a PermissionEntry with the given entityId and roleId already exists in the database
	 * @param deviceName
	 * @return
	 */
	public static boolean exists(Long entityId, Role role) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		PermissionEntry pe = (PermissionEntry) session.createCriteria(PermissionEntry.class)
					.add( Expression.eq("permissionEntityId", entityId) )
					.add( Expression.eq("role.roleId", role.getRoleId() ) )
					.uniqueResult();
		return pe != null ? true : false;
	}	
	
	/**
	 * Returns a list of all permissionEntry objects associated with the given entityId
	 * where the role is not null.
	 * @param entityId
	 * @return
	 * @throws HibernateException
	 */	
	public static List<PermissionEntry> getPermissionEntries(Long entityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		return session.createCriteria(PermissionEntry.class)
				.add( Expression.eq("permissionEntityId", entityId) )
				.add( Expression.isNotNull("role.roleId") )
				.list();
	}
			
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPermissionEntryId();
	}
	
	public Long getHistoryEntityId()
	{
		return this.getPermissionEntityId();
	}
	
	public String getEntityName()
	{
		return this.getRole().getRoleName();
	}	
	
	/**
	 * @return Returns the permissionEntryId.
	 */
	public Long getPermissionEntryId() {
		return permissionEntryId;
	}
	

	/**
	 * @param permissionEntryId The permissionEntryId to set.
	 */
	public void setPermissionEntryId(Long permissionEntryId) {
		this.permissionEntryId = permissionEntryId;
	}
	

	/**
	 * @return Returns the role.
	 */
	public Role getRole() {
		return role;
	}
	

	/**
	 * @param role The role to set.
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @return Returns the permissionEntityId.
	 */
	public Long getPermissionEntityId() {
		return permissionEntityId;
	}
	

	/**
	 * @param permissionEntityId The permissionEntityId to set.
	 */
	public void setPermissionEntityId(Long permissionEntityId) {
		this.permissionEntityId = permissionEntityId;
	}
}
