package com.kuvata.kmf;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;


import com.kuvata.kmf.logging.HistorizableChildEntity;

public class PermissionEntry extends PersistentEntity implements HistorizableChildEntity
{
	private static Logger logger = Logger.getLogger(PermissionEntry.class);
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

		
		Asset asset = Asset.getAsset(entityId);

	}
	
	
	/**
	 * Removes all existing PermissionEntry objects for the given entity
	 * @param entityId
	 */
	protected static void resetPermissionEntries(Long entityId)
	{
		
		// Remove all existing permisionEntries for this entity that this user belongs to
		List<PermissionEntry> permissionEntries = PermissionEntry.getPermissionEntries( entityId );
		for( Iterator<PermissionEntry> i=permissionEntries.iterator(); i.hasNext(); ){
			PermissionEntry pe = i.next();
			
		
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
