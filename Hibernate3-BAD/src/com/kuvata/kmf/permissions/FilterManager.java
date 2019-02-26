package com.kuvata.kmf.permissions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.Filter;
import org.hibernate.Session;
import parkmedia.KmfException;
import parkmedia.KuvataConfig;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.KmfSession;
import com.kuvata.kmf.SchemaDirectory;

/**
 * This class is used to apply permissions/filters on an action by action basis.
 * It uses the FilterType class to determine which filters are mapped to which actions.
 *  
 * @author jrandesi
 */
public abstract class FilterManager {
	
	/**
	 * References the FilterType class to enable each filter that is defined for 
	 * the given action. If the kuvata.property to enable permissions is turned off,
	 * no filters will be enabled.
	 * 
	 * @param actionType
	 */
	public static void enableFilters(ActionType actionType)
	{
		boolean applyFilters = false;
		try {
			// Attempt to locate the kuvata.property to enable permissions
			String enablePermissions = KuvataConfig.getPropertyValue( Constants.ENABLE_PERMISSIONS );
			applyFilters = Boolean.valueOf( enablePermissions ).booleanValue();
		} catch (KmfException e) {
			logger.info("Unable to locate property: "+ Constants.ENABLE_PERMISSIONS +". Using default: false");
		}
		
		// If the flag was set to enable permissions
		if( applyFilters == true )
		{									
			// If the HibernateSession has been initialized properly
			try
			{								
				// For each filters that is associated with this action				
				HashSet filters = FilterType.getFilterTypes( actionType );				
				for( Iterator i=filters.iterator(); i.hasNext(); )
				{
					// Enable the filter
					FilterType filterType = (FilterType)i.next();		
					enableFilter( filterType, applyFilters, false );								
				}
			}catch(KmfException e){
				logger.error("HibernateSession not yet initialized. Unable to enable filters.");
			}
		}				
	}
	
	/**
	 * Helper method to call enableFilter, which will query kuvata.properties
	 * to see if permissions are enabled or not
	 * @param filterType
	 */
	public static void enableFilter(FilterType filterType){
		enableFilter( filterType, false, true );
	}
	
	/**
	 * Enables the given filter type
	 * @param filterType
	 */
	public static void enableFilter(FilterType filterType, boolean enableFilters, boolean checkEnableFilters)
	{
		boolean applyFilters = false;
		
		/*
		 * If the flag to check whether or not to enable filters was passed in -- query kuvata.properties
		 * We do this for performance reasons, because when called from enableFilters, we've already queried
		 * kuvata.properties for this property. When called directly, we have not
		 */
		if( checkEnableFilters )
		{
			try {
				// Attempt to locate the kuvata.property to enable permissions
				String enablePermissions = KuvataConfig.getPropertyValue( Constants.ENABLE_PERMISSIONS );
				applyFilters = Boolean.valueOf( enablePermissions ).booleanValue();
			} catch (KmfException e) {
				logger.info("Unable to locate property: "+ Constants.ENABLE_PERMISSIONS +". Using default: false");
			}			
		}else{
			applyFilters = enableFilters;
		}
		
		// If the flag was set to enable permissions
		if( applyFilters == true )
		{	
			try {												
				// If this user is an admin, or has universalDataAccess
				KmfSession kmfSession = KmfSession.getKmfSession();
				if( kmfSession != null && (kmfSession.isAdmin() || kmfSession.isUniversalDataAccess()) )
				{
					// Look for the "Admin" version of this filter
					FilterType adminFilterType = FilterType.getFilterType( filterType.getName() +"Admin" );
					if( adminFilterType != null ){						
						filterType = adminFilterType;
					}
					
					// If this is the ContentSchedulerStatus or AssetIngesterStatus filter, and we're an admin -- do not apply the filter at all
					if( filterType.getName().equalsIgnoreCase( FilterType.CONTENT_SCHEDULER_STATUS_FILTER.getName() )
							|| filterType.getName().equalsIgnoreCase( FilterType.ASSET_INGESTER_STATUS_FILTER.getName() )
							|| filterType.getName().equalsIgnoreCase( FilterType.DEVICE_INGESTER_STATUS_FILTER.getName() )){
						return;
					}
				}				
				Session hibernateSession = HibernateSession.currentSession();

				// Do not enable filters for the KMF schema
				if( SchemaDirectory.getSchemaName() != null && SchemaDirectory.getSchemaName().equalsIgnoreCase( Constants.BASE_SCHEMA ) == false )
				{
					Filter filter = hibernateSession.enableFilter( filterType.getName() );
					// Set each parameter of the filter
					for( int j=0; j<filterType.getParams().length; j++ )
					{
						String paramName = filterType.getParams()[j];
						setParam( filter, paramName );
					}	
				}				
			} catch (IllegalArgumentException e) {
				logger.error("An unexpected error occurred while attempting to enable filter: "+ filterType.getName() +".", e);
			}			
		}
	}
	
	/**
	 * Helper method to set one of the property of the given filter.
	 * Use the current user's KmfSession to retrieve the appropriate value
	 * for this particular named parameter.
	 *  
	 * @param filter
	 * @param paramName
	 */
	private static void setParam(Filter filter, String paramName)
	{
		// Use the current user's KmfSession to set the parameter of this filter
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( paramName.equalsIgnoreCase( FilterType.PARAM_ROLE_IDS ) )
		{
			Set roleIds = new HashSet();
			if( (kmfSession.isAdmin() || kmfSession.isUniversalDataAccess()) && kmfSession.isViewDataWithNoRoles() ){
				roleIds.add(new Long(0)); // Add entities with no roles
			}
			
			// If there are not any viewable roles associated with this appUser
			if(kmfSession.getAppUserViewableRoleIds().size() == 0) 
			{
				// Create a temporary hashset containing a dummy roleId in order to avoid an illegal sql "IN ()" statement  
				roleIds.add( new Long(-1) );
				filter.setParameterList( paramName, roleIds );
			}
			else
			{
				roleIds.addAll(kmfSession.getAppUserViewableRoleIds());
				filter.setParameterList( paramName, roleIds );				
			}
		}
		else if( paramName.equalsIgnoreCase( FilterType.PARAM_APP_USER_ID ) )
		{
			filter.setParameter( paramName, kmfSession.getAppUserId() );
		}			
		else if( paramName.equalsIgnoreCase( FilterType.PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES ) )
		{
			// If this appUser is set to view data that does not have any roles assigned to it
			if( kmfSession.isViewDataWithNoRoles() ){
				filter.setParameter( paramName, new Long(0) );
			}else{
				// Set the filter parameter to a value that will intentionally cause the sql statement to return false
				filter.setParameter( paramName, new Long(-1) );
			}
		}			
	}
	
	/**
	 * Disables any filters associated with the given action.
	 * @param actionType
	 */
	public static void disableFilters(ActionType actionType)
	{
		Session hibernateSession = HibernateSession.currentSession();
		HashSet filters = FilterType.getFilterTypes( actionType );
		for( Iterator i=filters.iterator(); i.hasNext(); )
		{
			// Disable the filter
			FilterType filterType = (FilterType)i.next();					
			disableFilter( filterType );													
		}
	}
	
	/**
	 * Disables any filters associated with the given action.
	 * @param actionType
	 */
	public static void disableFilter(FilterType filterType)
	{		
		// If this user is an admin, or has universalDataAccess
		KmfSession kmfSession = KmfSession.getKmfSession();
		if( kmfSession != null && (kmfSession.isAdmin() || kmfSession.isUniversalDataAccess()) )
		{
			// Look for the "Admin" version of this filter
			FilterType adminFilterType = FilterType.getFilterType( filterType.getName() +"Admin" );
			if( adminFilterType != null ){						
				filterType = adminFilterType;
			}			
		}	

		// Disable the filter
		Session hibernateSession = HibernateSession.currentSession();
		hibernateSession.disableFilter( filterType.getName() );												
	}	
}
