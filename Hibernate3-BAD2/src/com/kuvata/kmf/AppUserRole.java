/*
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import com.kuvata.kmf.logging.HistorizableCollectionMember;


/**
 * Represents the app_user_permission_group table in the BASE_SCHEMA.
 * 
 * @author Jeff Randesi
 */
public class AppUserRole implements HistorizableCollectionMember
{
	private Long appUserRoleId;	
	private AppUser appUser;
	private Role role;
	private Boolean isDefault;
	private Boolean isViewable;

	public AppUserRole() 
	{		
	}
	
	public static void createOrUpdate(AppUser appUser, Role role, boolean isDefault, boolean isViewable)
	{		
		boolean isUpdate = true;
		boolean doUpdate = false;
				
		AppUserRole appUserRole = appUser.getAppUserRole( role );
		
		// If this appUser is not already a member of this role
		if( appUserRole == null )
		{
			// Create a new AppUserRole object
			appUserRole = new AppUserRole();
			appUserRole.setRole( role );
			appUserRole.setAppUser( appUser );
			isUpdate = false;
		}
		
		// If either of these properties have changed -- set the property to the new value and make sure we update the object
		if( appUserRole.getIsDefault() == null || appUserRole.getIsDefault().booleanValue() != isDefault ){
			appUserRole.setIsDefault( Boolean.valueOf( isDefault ) );
			doUpdate = true;
		}
		if( appUserRole.getIsViewable() == null || appUserRole.getIsViewable().booleanValue() != isViewable ){
			appUserRole.setIsViewable( Boolean.valueOf( isViewable ) );
			doUpdate = true;
		}
		
		// If we're updating an existing AppUserRole object and one or more of the properties have changed
		if( isUpdate && doUpdate ){
			//appUserRole.update();
		}else{
			//appUserRole.save();
		}		
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAppUserRoleId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getAppUser().getAppUserId();
	}	
	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getRole().getRoleName();
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
	

	/**
	 * @return Returns the appUserRoleId.
	 */
	public Long getAppUserRoleId() {
		return appUserRoleId;
	}
	

	/**
	 * @param appUserRoleId The appUserRoleId to set.
	 */
	public void setAppUserRoleId(Long appUserRoleId) {
		this.appUserRoleId = appUserRoleId;
	}
	

	/**
	 * @return Returns the isDefault.
	 */
	public Boolean getIsDefault() {
		return isDefault;
	}
	

	/**
	 * @param isDefault The isDefault to set.
	 */
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
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
	 * @return Returns the isViewable.
	 */
	public Boolean getIsViewable() {
		return isViewable;
	}
	

	/**
	 * @param isViewable The isViewable to set.
	 */
	public void setIsViewable(Boolean isViewable) {
		this.isViewable = isViewable;
	}
	
	

	
}
