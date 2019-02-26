package com.kuvata.kmf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.KMFLogger;

import com.kuvata.kmf.logging.Historizable;


/**
 *
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class Role extends KmfEntity implements Historizable, Comparable<Role>
{
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Role.class );
	private Long roleId;
	private String roleName;	
	private Set<AppUserRole> appUserRoles = new HashSet<AppUserRole>();
	
	
	/**
	 * 
	 *
	 */
	public Role()
	{		
	}	
	
	/**
	 * 
	 * @param dirtyId
	 * @return
	 * @throws HibernateException
	 */
	public static Role getRole(Long roleId) throws HibernateException
	{
		return (Role)KmfEntity.load(Role.class, roleId);		
	}
	
	/**
	 * 
	 * @param roleName
	 * @return
	 * @throws HibernateException
	 */
	public static List getRoles(String roleName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		Criteria crit = session.createCriteria( Role.class );		
		crit.add(Expression.eq("roleName", roleName).ignoreCase());
		List l = crit.list();		
		return l;
	}
	
	/**
	 * Returns a list of all roles.
	 * @return
	 * @throws HibernateException
	 */
	public static List<Role> getAllRoles() throws HibernateException
	{		
		Session session = HibernateSession.currentSession();	
		List l = session.createQuery(				
				"SELECT role "
				+ "FROM Role as role WHERE role.roleId != 0 "
				+ "ORDER BY UPPER(role.roleName)"						
				).list();				
		return l;
	}
	
	/**
	 * Returns a list of roles that the current user is part of.
	 * @return
	 * @throws HibernateException
	 */
	public static List getCurrentRoles() throws HibernateException
	{		
		// If the current user is an admin -- return all roles
		List result = new ArrayList();
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null ){
			if( kmfSession.isAdmin() ){
				result = getAllRoles();
			}
			else
			{
				Session session = HibernateSession.currentSession();	
				result = session.createQuery(				
						"SELECT role "
						+ "FROM Role as role "	
						+ "JOIN role.appUserRoles as appUserRole "
						+ "WHERE appUserRole.appUser.appUserId = "+ kmfSession.getAppUserId() +" "
						+ "AND role.roleId != 0 "
						+ "ORDER BY UPPER(role.roleName)"						
						).list();								
			}		
		}
		return result;
	}		
	
	/**
	 * @return Returns the number of roles available to the current user
	 */
	public static int getCurrentRolesCount() throws HibernateException 
	{
		int result = 0;
		
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null ){
			if( kmfSession.isAdmin() ){
				result = getRolesCount();
			}
			else
			{
				Session session = HibernateSession.currentSession();
				Iterator i = session.createQuery(				
						"SELECT COUNT(role) "
						+ "FROM Role as role "	
						+ "JOIN role.appUserRoles as appUserRole "
						+ "WHERE appUserRole.appUser.appUserId = "+ kmfSession.getAppUserId() +" "
						+ "AND role.roleId != 0 "
						+ "ORDER BY UPPER(role.roleName)"						
						).iterate();
				result = ((Long)i.next()).intValue();
				Hibernate.close( i );
			}		
		}		
		return result;
	}	
	
	/**
	 * @return Returns the number of roles
	 */
	public static int getRolesCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		Iterator i = session.createQuery(
				"SELECT COUNT(role) "
				+ "FROM Role as role WHERE role.roleId != 0"
				).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}
	
	/**
	 * Returns true if a role with the given name already exists
	 * 
	 * @param roleName
	 * @return
	 */
	public static boolean exists(String roleName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(Role.class)
					.add( Expression.eq("roleName", roleName).ignoreCase() )
					.list();
		return l.size() > 0 ? true : false;
	}	
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeAppUserRoles() throws HibernateException
	{
		for( Iterator i=this.appUserRoles.iterator(); i.hasNext(); )
		{
			AppUserRole appUserRole = (AppUserRole)i.next();
			appUserRole.getAppUser().getAppUserRoles().remove( appUserRole );
			i.remove();
		}		
		this.update();
	}	
	
	/**
	 * 
	 */
	public void delete() throws HibernateException
	{
		// Remove the associations to this role we're about to delete 
		this.removeAppUserRoles();
		super.delete();		
	}	
		
	public int compareTo(Role r){
		return this.getRoleName().toLowerCase().compareTo(r.getRoleName().toLowerCase());
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getRoleId();
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
	 * @return Returns the roleId.
	 */
	public Long getRoleId() {
		return roleId;
	}
	

	/**
	 * @param roleId The roleId to set.
	 */
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
	

	/**
	 * @return Returns the roleName.
	 */
	public String getRoleName() {
		return roleName;
	}
	

	/**
	 * @param roleName The roleName to set.
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	


}
