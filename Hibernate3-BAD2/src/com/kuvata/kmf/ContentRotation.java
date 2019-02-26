package com.kuvata.kmf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.ContentRotationSearchType;
import com.kuvata.kmf.usertype.DirtyType;
import com.kuvata.kmf.usertype.PlaylistImportStatus;
import com.kuvata.kmf.usertype.PlaylistOrderType;
import com.kuvata.kmf.usertype.SearchInterfaceType;
import com.kuvata.dispatcher.scheduling.ContentScheduler;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableLinkedList;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.permissions.ActionType;
import com.kuvata.kmf.permissions.FilterManager;
import com.kuvata.kmf.permissions.FilterType;
import com.kuvata.kmf.usertype.PresentationStyleContentRotation;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class ContentRotation extends Entity implements Historizable {

	private static Logger logger = Logger.getLogger(ContentRotation.class);
	
	private Long contentRotationId;
	private String contentRotationName;
	private Date csvImportDate;
	private PlaylistImportStatus csvImportStatus;
	private Clob csvImportDetail;
	private String hql;
	private String customMethod;
	private String type;
	private String dynamicContentType;
	private PresentationStyleContentRotation presentationStyle;
	private Integer runFromContentScheduler;
	private Integer maxResults;	
	private PlaylistOrderType contentRotationOrder;
	private List<ContentRotationAsset> contentRotationAssets = new HistorizableLinkedList<ContentRotationAsset>();
	private Set<ContentRotationDisplayarea> contentRotationDisplayareas = new HashSet<ContentRotationDisplayarea>();	
	private List<ContentRotationDisplayarea> contentRotationDisplayareasSorted;
	private Set<ContentRotationGrpMember> contentRotationGrpMembers = new HashSet<ContentRotationGrpMember>();
	private Boolean useRoles;
	private Integer numAssets;
	private Date lastDynamicUpdateDt;
	private String contentRotationImportDetail;
	private Integer avgLoopLength;
	private Set<ContentRotationImport> contentRotationImports = new HistorizableSet<ContentRotationImport>();
	
	/*
	 * Used by content scheduler for non-persistent copies of contentRotationAssets
	 * (used for asset exclusion and randomization)
	 */
	private List myContentRotationAssets = null;
	private float myLength;
	private long myDeviceId; // Device for which myContentRotationAssets / myLength was calculated
	
	/**
	 * 
	 *
	 */
	public ContentRotation()
	{		
	}
	
	/**
	 * 
	 * @param contentRotationId
	 * @return
	 * @throws HibernateException
	 */
	public static ContentRotation getContentRotation(Long contentRotationId) throws HibernateException
	{
		return (ContentRotation)Entity.load(ContentRotation.class, contentRotationId);		
	}
	
	/**
	 * 
	 * @param p
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static ContentRotation getContentRotation(Playlist p, Displayarea da) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		ContentRotation cr = (ContentRotation)session.createCriteria(ContentRotation.class)
				.add( Expression.eq("playlist.playlistId", p.getPlaylistId()) )
				.add( Expression.eq("displayarea.displayareaId", da.getDisplayareaId()) )				
				.uniqueResult();							
		return cr;		
	}
	
	/**
	 * Returns a content rotation with the given name if one exists
	 *
	 * @param contentRotationName
	 * @return
	 */
	public static List getContentRotations(String contentRotationName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(ContentRotation.class)
			.add( Expression.eq("contentRotationName", contentRotationName).ignoreCase() )
			.list();
		return l;
	}		
	
	/**
	 * Returns a Playlist with the given playlistId
	 * 
	 * @param playlistId
	 * @return
	 * @throws HibernateException
	 */
	public static List getContentRotations(List contentRotationIds) throws HibernateException
	{
		return Entity.load(ContentRotation.class, contentRotationIds);		
	}
	
	/**
	 * Create an unnamed content rotation object
	 * @return
	 */
	public static ContentRotation create()
	{
		return create( null );
	}
	
	/**
	 * Create an unnamed content rotation object
	 * @return
	 */
	public static ContentRotation create(String contentRotationName)
	{
		ContentRotation cr = new ContentRotation();
		cr.setContentRotationName( contentRotationName );
		cr.setType(Constants.STATIC);
		cr.save();
		
		return cr;
	}	
	
	/**
	 * Copies this ContentRotation and assigns the given new ContentRotation name.
	 * 
	 * @param newPlaylistName
	 * @return
	 */
	/**
	 * @param newContentRotationName
	 * @return
	 * @throws HibernateException
	 * @throws CloneNotSupportedException
	 */
	public Long copy(String newContentRotationName) throws Exception
	{		
		Session session = HibernateSession.currentSession();
		session.lock( this, LockMode.READ );
		
		// First create a new ContentRotation object
		ContentRotation newContentRotation = new ContentRotation();
		newContentRotation.setContentRotationName( newContentRotationName );

		// Save the content rotation but do not create permission entries since we are going to copy them
		Long newContentRotationId = newContentRotation.save( false );
		newContentRotation.copyPermissionEntries( this );
				
		// Copy all content rotations displayareas associated with this content rotation		
		for( Iterator i = this.getContentRotationDisplayareas().iterator(); i.hasNext(); )
		{
			ContentRotationDisplayarea crd = (ContentRotationDisplayarea)i.next();

			// Create a ContentRotationDisplayarea object
			ContentRotationDisplayarea newCrd =  new ContentRotationDisplayarea();
			newCrd.setContentRotation( newContentRotation );
			newCrd.setDisplayarea( crd.getDisplayarea() );	
			newContentRotation.getContentRotationDisplayareas().add( crd );					
		}
		
		// Set type and dynamic content if dynamic CR
		if(this.getType().equals("dynamic")){
			if(this.getDynamicContentType() != null && this.getDynamicContentType().equals("metadata")){
				for(DynamicQueryPart dqp : DynamicQueryPart.getDynamicQueryParts(this)){
					DynamicQueryPart.create(null, newContentRotation, dqp.getAttrDefinition(), dqp.getOperator(), dqp.getValue(), dqp.getSelectedDate(), dqp.getNumDaysAgo(), dqp.getIncludeNull(), dqp.getSeqNum().intValue());
				}
			}else if(this.getNumAssets() != null){
				newContentRotation.setNumAssets(this.getNumAssets());
				for(DynamicContentPart dcp : DynamicContentPart.getDynamicContentParts(this)){
					DynamicContentPart.create(null, newContentRotation, dcp.getContentRotation(), null, null, dcp.getSeqNum());
				}
			}else if(this.getHql() != null){
				newContentRotation.setHql(this.getHql());
			}else if(this.getCustomMethod() != null){
				newContentRotation.setCustomMethod(this.getCustomMethod());
			}
			
			newContentRotation.setPresentationStyle(this.getPresentationStyle());
			newContentRotation.setRunFromContentScheduler(this.getRunFromContentScheduler());
			newContentRotation.setUseRoles(this.getUseRoles());
			newContentRotation.setMaxResults(this.getMaxResults());
			newContentRotation.setDynamicContentType(this.getDynamicContentType());
		}
		
		newContentRotation.setType(this.getType());
		newContentRotation.setContentRotationOrder(this.getContentRotationOrder());
		newContentRotation.update();
		
		// Copy all the content rotation group members		
		for( Iterator i = this.getContentRotationGrpMembers().iterator(); i.hasNext(); )
		{
			ContentRotationGrpMember crgm = (ContentRotationGrpMember)i.next();
			ContentRotationGrpMember newContentRotationGrpMember = new ContentRotationGrpMember();
			newContentRotationGrpMember.setGrp( crgm.getGrp() );
			newContentRotationGrpMember.setContentRotation( newContentRotation );
			newContentRotationGrpMember.save();			
		}	
		
		// Copy all content rotations assets associated with this content rotation
		if(this.getType().equals("dynamic")){
			newContentRotation.updateContentRotation(null, null, false);
		}else{
			for( Iterator i = this.getContentRotationAssets().iterator(); i.hasNext(); )
			{
				ContentRotationAsset cra = (ContentRotationAsset)i.next();
				ContentRotationAsset.create( newContentRotation, Asset.convert( cra.getAsset() ), cra.getLength().toString(), cra.getVariableLength(), false );						
			}
		}
		
		// Update the new content rotation
		newContentRotation.update();
		
		return newContentRotationId;
	}	
	
	public void addContentRotationAsset(Asset asset, Float length, Boolean variableLength)
	{
		ContentRotationAsset cra = new ContentRotationAsset();
		cra.setContentRotation( this );
		cra.setAsset( asset );			
		cra.setSeqNum( new Integer(this.getContentRotationAssets().size()) );
		cra.setLength( length );							
		cra.setVariableLength( variableLength );
		cra.save();								
		this.getContentRotationAssets().add( cra );
	}
	
	/**
	 * @return Returns the content rotation assets in the given order
	 */
	public Page getContentRotationAssets(int pageNum, int iSelectedItemsPerPage) throws HibernateException 
	{
		Page result = null;		
		Session session = HibernateSession.currentSession();
		String hql = "SELECT cra "
			+ "FROM ContentRotationAsset cra "
			+ "WHERE cra.contentRotation.contentRotationId = "+ this.getContentRotationId().toString() +" "			
			+ "ORDER BY cra.seqNum";	
		Query q = session.createQuery( hql );
		result = new ContentRotationPage(q, pageNum, iSelectedItemsPerPage);				
		return result;
	}
	
	/**
	 * This method reorders all assets that are associated with this ContentRotation
	 * It is called after removing content rotation assets from this ContentRotation 
	 * so there is not a gap in the sequence numbers.
	 * 
	 * @throws HibernateException
	 */
	public void orderContentRotationAssets() throws HibernateException
	{		
		int seqCounter = 0;
		
		// Re-order all assets in playlist
		List l = this.getContentRotationAssets();
		Iterator i = l.iterator();
		while(i.hasNext())
		{
			ContentRotationAsset cra = (ContentRotationAsset)i.next();
			cra.setSeqNum( new Integer(seqCounter) );
			cra.update();
			seqCounter++;
		}		
	}	
	
	/**
	 * Calculates the sum total of all assets for this content rotation
	 * @return
	 * @throws HibernateException
	 */
	public float calculateLength() throws HibernateException
	{		
		Session session = HibernateSession.currentSession();
		float result = 0;
		
		// Get the lengths of all the assets for this content rotation
		String hql = "SELECT SUM(cra.length) "
			+ "FROM ContentRotation as cr "
			+ "JOIN cr.contentRotationAssets as cra "
			+ "WHERE cr.contentRotationId = "+ this.getContentRotationId();
		Iterator i = session.createQuery( hql ).iterate();
		if(i.hasNext()) {
			Double d = (Double)i.next();
			if( d != null ) {
				result = d.floatValue();					
			}
		}		
		Hibernate.close( i );
		return result;
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getNamedContentRotations() throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l = session.createQuery(				
				"SELECT cr "
				+ "FROM ContentRotation as cr "				
				+ "WHERE cr.contentRotationName IS NOT NULL "
				+ "ORDER BY UPPER(cr.contentRotationName)"						
				).list();	
		return l;
	}	
	
	/**
	 * Returns true if a content rotation with the given name already exists in the database
	 * 
	 * @param contentRotationName
	 * @return
	 */
	public static boolean contentRotationExists(String contentRotationName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(ContentRotation.class)
					.add( Expression.eq("contentRotationName", contentRotationName).ignoreCase() )
					.list();		
		return l.size() > 0 ? true : false;
	}	
	
	/**
	 * @return Returns the number of content rotations
	 */
	public static int getContentRotationsCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT COUNT(cr) "
			+ "FROM ContentRotation as cr "
			+ "WHERE cr.contentRotationName IS NOT NULL";
		Iterator i = session.createQuery( hql ).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}	
	
	/**
	 * 
	 * @return
	 */
	public List getContentRotationDisplayareasSorted()
	{
		if(this.contentRotationDisplayareas != null)
		{				
			List l = new LinkedList( contentRotationDisplayareas );
			BeanPropertyComparator comparator1 = new BeanPropertyComparator("displayareaName");
			BeanPropertyComparator comparator2 = new BeanPropertyComparator("displayarea", comparator1 );
			Collections.sort( l, comparator2 );
			contentRotationDisplayareasSorted = l ;			
		}
		return contentRotationDisplayareasSorted;
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeContentRotationDisplayareas(boolean removeDisplayareaReferences) throws HibernateException
	{
		// If the flag to remove references to displayareas was passed in
		if( removeDisplayareaReferences )
		{
			for( Iterator i=this.contentRotationDisplayareas.iterator(); i.hasNext(); )
			{
				ContentRotationDisplayarea crd = (ContentRotationDisplayarea)i.next();
				List l = PlaylistContentRotation.getPlaylistContentRotations( this, crd.getDisplayarea() );
				for( Iterator k=l.iterator(); k.hasNext(); )
				{
					PlaylistContentRotation pcr = (PlaylistContentRotation)k.next();
					pcr.delete();
				}
			}			
		}
		
		this.contentRotationDisplayareas.clear();		
		this.update();
	}	
	
	/**
	 * Copies the contentRotationAssets associated with the given contentRotationToCopy.
	 * @param contentRotationToCopy
	 */
	public void copyContentRotationAssets(ContentRotation contentRotationToCopy)
	{
		copyContentRotationAssets( contentRotationToCopy, null, null );
	}
	
	/**
	 * Copies the contentRotationAssets associated with the given contentRotationToCopy.
	 * If a contentRotationAssetToModify and modifiedContentRotationAssetLength are passed in, 
	 * replace the appropriate contentRotationAsset length with the given modifiedContentRotationAssetLength
	 * and return the new ContentRotationAsset that was modified.
	 * 
	 * @see CreateContenRotationAsset.finishModify()
	 * @param contentRotationToCopy
	 */
	public ContentRotationAsset copyContentRotationAssets(ContentRotation contentRotationToCopy, ContentRotationAsset contentRotationAssetToModify, Float modifiedContentRotationAssetLength)
	{
		// Save off the existing content rotation assets
		ContentRotationAsset modifiedContentRotationAsset = null;
		LinkedList savedContentRotationAssets = new LinkedList( this.getContentRotationAssets() );
		int seqNum = 0;
		for( Iterator i=contentRotationToCopy.getContentRotationAssets().iterator(); i.hasNext(); )
		{
			ContentRotationAsset contentRotationAssetToCopy = (ContentRotationAsset)i.next();
			ContentRotationAsset newContentRotationAsset = new ContentRotationAsset();
			newContentRotationAsset.setAsset( contentRotationAssetToCopy.getAsset() );
			newContentRotationAsset.setContentRotation( this );
			
			// If this is the contentRotationAsset whose length needs to be modified
			if( contentRotationAssetToModify != null && contentRotationAssetToModify.getContentRotationAssetId() == contentRotationAssetToCopy.getContentRotationAssetId() ) {
				newContentRotationAsset.setLength( modifiedContentRotationAssetLength );
				modifiedContentRotationAsset = newContentRotationAsset;
			} else {
				newContentRotationAsset.setLength( contentRotationAssetToCopy.getLength() );	
			}			
			newContentRotationAsset.setSeqNum( contentRotationAssetToCopy.getSeqNum() );
			newContentRotationAsset.setVariableLength( contentRotationAssetToCopy.getVariableLength() );
			newContentRotationAsset.save();
			
			this.contentRotationAssets.add( newContentRotationAsset );
			seqNum++;
		}
				
		// Re-set the sequence number of the saved content rotation assets
		for( Iterator i=savedContentRotationAssets.iterator(); i.hasNext(); )
		{
			ContentRotationAsset cra = (ContentRotationAsset)i.next();
			cra.setSeqNum( seqNum++ );
			cra.update();
		}	
		return modifiedContentRotationAsset;
	}	
	
	/**
	 * 
	 * @throws HibernateException
	 * @throws SQLException
	 */
	public void deleteContentRotationAssets() throws HibernateException, SQLException
	{
		this.getContentRotationAssets().clear();
		this.update();
	}	
	
	/**
	 * Returns a list of GrpGrpMember objects of the "Content Rotation Groups" group
	 * @return
	 */
	public static List getAllContentRotationGroups()
	{
		List result = null;
		Grp groups = Grp.getUniqueGrp( Constants.CONTENT_ROTATION_GROUPS );
		if( groups != null )
		{			
			result = new ArrayList( groups.getGrpMembers() );
			BeanPropertyComparator comparator1 = new BeanPropertyComparator( "grpName" );
			BeanPropertyComparator comparator2 = new BeanPropertyComparator( "childGrp", comparator1 );				
			Collections.sort( result, comparator2 );
			
			// Initialize the child groups within the list
			for( Iterator i=result.iterator(); i.hasNext(); )
			{
				GrpGrpMember ggm = (GrpGrpMember)i.next();
				Hibernate.initialize( ggm.getChildGrp() );
			}
		}
		return result;
	}
	
	/**
	 * Gets the count of the contentRotations according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @return
	 * @throws ParseException
	 */
	public static int searchContentRotationsCount(ContentRotationSearchType contentRotationSearchType, AttrDefinition attrDefinition, String contentRotationNameSearchString, String selectedDisplayareaId, String selectedGroup, String selectedSearchOption, String searchString, String[] selectedSearchOptions, 
			String minDate, String maxDate, String minNumber, String maxNumber, int startingRecord, boolean excludeDynamicContentRotations) throws ParseException
	{
		int result = 0;
		String hql = buildSearchHql( contentRotationSearchType, contentRotationNameSearchString, selectedDisplayareaId, selectedGroup, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, null, true, false, excludeDynamicContentRotations );		
		
		// If an attrDefinition object was not passed in, we must be searching by device group		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = KMMServlet.getRecordCount( hql, selectedSearchOptions, "attrValues" );					
			}else{
				result = KMMServlet.getRecordCount( hql );
			}
		}	
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) };
				result = KMMServlet.getRecordCount( hql, params );
			}		
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}		
		}
		return result;
	}
	
	/**
	 * Returns a page of content rotations according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @param selectedItemsPerPage
	 * @return
	 * @throws ParseException
	 */
	public static Page searchContentRotations(ContentRotationSearchType contentRotationSearchType, AttrDefinition attrDefinition, String contentRotationNameSearchString, String selectedDisplayareaId, String selectedGroupId, 
			String selectedSearchOption, String searchString, String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, 
			String maxNumber, String orderBy, int startingRecord, int selectedItemsPerPage, boolean isEntityOverviewPage, boolean excludeDynamicContentRotations) throws ParseException
	{
		Page result = null;
		int pageNum = startingRecord / selectedItemsPerPage;
		String hql = buildSearchHql( contentRotationSearchType, contentRotationNameSearchString, selectedDisplayareaId, selectedGroupId, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, orderBy, false, isEntityOverviewPage, excludeDynamicContentRotations );
		
		// If an attrDefinition object was not passed in, we must be searching by asset type		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = ContentRotation.getContentRotations( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = ContentRotation.getContentRotations( hql, pageNum, selectedItemsPerPage, selectedSearchOptions, "attrValues", isEntityOverviewPage );
			}else{
				result = ContentRotation.getContentRotations( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );
			}
		}
		
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = ContentRotation.getContentRotations( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );	
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = ContentRotation.getContentRotations( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );					
			}			
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = ContentRotation.getContentRotations( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );	
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = ContentRotation.getContentRotations( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );						
			}			
		}
		return result;
	}	
	
	/**
	 * Builds the hql to retrieve assets according to the given search criteria.
	 *  
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param getCount
	 * @return
	 */
	private static String buildSearchHql(ContentRotationSearchType contentRotationSearchType, String contentRotationSearchString, String selectedDisplayareaId, String selectedContentRotationGroupId, String selectedSearchOption, String searchString, 
			String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, String maxNumber, String orderBy, boolean getCount, boolean isEntityOverviewPage, boolean excludeDynamicContentRotations)
	{		
		String hql = "";
		
		// If the contentRotationNameSearchString search string was left blank, use wildcard
		if( contentRotationSearchString == null || contentRotationSearchString.trim().length() == 0 ){
			contentRotationSearchString = "%";
		}
		
		// Imply *
		if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_ID) == false){
			if(contentRotationSearchString.startsWith("*") == false){
				contentRotationSearchString = "*" + contentRotationSearchString;
			}
			if(contentRotationSearchString.endsWith("*") == false){
				contentRotationSearchString = contentRotationSearchString + "*";
			}
		}
		
		if(searchString != null && searchString.length() > 0){
			if(searchString.startsWith("*") == false){
				searchString = "*" + searchString;
			}
			if(searchString.endsWith("*") == false){
				searchString = searchString + "*";
			}
		}
		
		// Convert any "*" to "%" for wildcard searches
		contentRotationSearchString = contentRotationSearchString.replaceAll("\\*", "\\%");	
		contentRotationSearchString = Reformat.oraesc(contentRotationSearchString);		
		
		// Default
		if( orderBy == null || orderBy.length() == 0 ){
			orderBy = "UPPER(contentRotation.contentRotationName)";
		}
		
		// If we are counting the number of records
		if( getCount == true) {
			hql = "SELECT COUNT(contentRotation) ";
		} else {
			if( isEntityOverviewPage ){
				hql = "SELECT contentRotation, ei.lastModified ";
			}else{
				hql = "SELECT contentRotation ";
			}
		}

		/*
		 * If the "No Metadata", or "Date Modified" option was selected, exclude the metadata from the search criteria 
		 */
		boolean excludeMetadataCriteria = false;
		if( selectedSearchOption.equalsIgnoreCase( Constants.NO_METADATA ) || selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ))
		{
			excludeMetadataCriteria = true;
		}
		else
		{
			/*
			 * Search by this attr definition
			 */
			AttrDefinition ad = AttrDefinition.getAttrDefinition( new Long( selectedSearchOption ) );
			if( ad != null)
			{				
				// If this is a String attr
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If this is a multi-select
					if( ad.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) )
					{
						// If no items were selected
						if( selectedSearchOptions.length == 0 )
						{
							// Exclude the attrDefinition criteria							
							excludeMetadataCriteria = true;
						}
						else
						{
							// Get all content rotations that have a StringAttr with the given criteria
							hql += "FROM ContentRotation as contentRotation ";
							if( isEntityOverviewPage ){
								hql += ", EntityInstance as ei "+
									"WHERE contentRotation.contentRotationId = ei.entityId "+
									"AND ";
							}else{
								hql += " WHERE ";
							}
							hql	+= " contentRotation.contentRotationId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND attr.value IN (:attrValues) ) ";
														
							// If the "All Content Rotations" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedContentRotationGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND contentRotation.contentRotationId IN "
								+ 	" (SELECT contentRotation.contentRotationId "
								+ 	"	FROM ContentRotationGrpMember as crgm "
								+ 	"	JOIN crgm.contentRotation as contentRotation "
								+ 	"	WHERE crgm.grp.grpId = '"+ selectedContentRotationGroupId +"') ";																		
							}
							// If we're filtering by displayarea
							if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
							{				
								// Get all content rotations that are allowed in this displayarea
								hql += "AND (SIZE(contentRotation.contentRotationDisplayareas) = 0 "
									+ 	" OR contentRotation.contentRotationId IN "
									+	" (SELECT cr.contentRotationId "
									+ 	" FROM ContentRotation as cr "
									+	" LEFT JOIN cr.contentRotationDisplayareas as contentRotationDisplayarea "					
									+	" WHERE contentRotationDisplayarea.displayarea.displayareaId = "+ selectedDisplayareaId +"))";					
							}
							
							if(excludeDynamicContentRotations){
								hql += "AND contentRotation.type = 'static'";
							}
							
							if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_NAME)){
								hql += "AND UPPER(contentRotation.contentRotationName) LIKE UPPER('"+ contentRotationSearchString +"') ";
							}else if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_ID)){
								hql += "AND contentRotation.contentRotationId = "+ contentRotationSearchString +" ";
							}
							hql	+= "ORDER BY " + orderBy;									
						}																		
					}
					else
					{						
						// If the searchString was left blank 
						if( searchString == null || searchString.trim().length() == 0 )
						{
							// Exclude the attrDefinition criteria					
							excludeMetadataCriteria = true;						
						}
						else
						{
							// Convert any "*" to "%" for wildcard searches		
							searchString = searchString.replaceAll("\\*", "\\%");	
							searchString = Reformat.oraesc(searchString);											
							
							// Get all content rotations that have a StringAttr with the given criteria
							hql += "FROM ContentRotation as contentRotation ";
							if( isEntityOverviewPage ){
								hql += ", EntityInstance as ei "+
									"WHERE contentRotation.contentRotationId = ei.entityId "+
									"AND ";
							}else{
								hql += " WHERE ";
							}
							hql	+= " contentRotation.contentRotationId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND UPPER(attr.value) LIKE UPPER ('"+ searchString +"') ) ";
							// If the "All Content Rotations" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedContentRotationGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND contentRotation.contentRotationId IN "
								+ 	" (SELECT contentRotation.contentRotationId "
								+ 	"	FROM ContentRotationGrpMember as crgm "
								+ 	"	JOIN crgm.contentRotation as contentRotation "
								+ 	"	WHERE crgm.grp.grpId = '"+ selectedContentRotationGroupId +"') ";																		
							}			
							// If we're filtering by displayarea
							if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
							{				
								// Get all content rotations that are allowed in this displayarea
								hql += "AND (SIZE(contentRotation.contentRotationDisplayareas) = 0 "
									+ 	" OR contentRotation.contentRotationId IN "
									+	" (SELECT cr.contentRotationId "
									+ 	" FROM ContentRotation as cr "
									+	" LEFT JOIN cr.contentRotationDisplayareas as contentRotationDisplayarea "					
									+	" WHERE contentRotationDisplayarea.displayarea.displayareaId = "+ selectedDisplayareaId +"))";					
							}

							if(excludeDynamicContentRotations){
								hql += "AND contentRotation.type = 'static'";
							}
							
							if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_NAME)){
								hql += "AND UPPER(contentRotation.contentRotationName) LIKE UPPER('"+ contentRotationSearchString +"') ";
							}else if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_ID)){
								hql += "AND contentRotation.contentRotationId = "+ contentRotationSearchString +" ";
							}
							hql	+= "ORDER BY " + orderBy;
						}										
					}
				}
				// If this is a Date attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					// If both the min and max dates were left blank
					if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all content rotations that have a DateAttr.value between the two dates
						hql += "FROM ContentRotation as contentRotation ";
						if( isEntityOverviewPage ){
							hql += ", EntityInstance as ei "+
								"WHERE contentRotation.contentRotationId = ei.entityId "+
								"AND ";
						}else{
							hql += " WHERE ";
						}
						hql	+= " contentRotation.contentRotationId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM DateAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";						
						// If the "All Content Rotations" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
						if( selectedContentRotationGroupId.equalsIgnoreCase("-1") == false )
						{					
							hql += "AND contentRotation.contentRotationId IN "
							+ 	" (SELECT contentRotation.contentRotationId "
							+ 	"	FROM ContentRotationGrpMember as crgm "
							+ 	"	JOIN crgm.contentRotation as contentRotation "
							+ 	"	WHERE crgm.grp.grpId = '"+ selectedContentRotationGroupId +"') ";																		
						}	
						// If we're filtering by displayarea
						if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
						{				
							// Get all content rotations that are allowed in this displayarea
							hql += "AND (SIZE(contentRotation.contentRotationDisplayareas) = 0 "
								+ 	" OR contentRotation.contentRotationId IN "
								+	" (SELECT cr.contentRotationId "
								+ 	" FROM ContentRotation as cr "
								+	" LEFT JOIN cr.contentRotationDisplayareas as contentRotationDisplayarea "					
								+	" WHERE contentRotationDisplayarea.displayarea.displayareaId = "+ selectedDisplayareaId +"))";					
						}

						if(excludeDynamicContentRotations){
							hql += "AND contentRotation.type = 'static'";
						}
						
						if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_NAME)){
							hql += "AND UPPER(contentRotation.contentRotationName) LIKE UPPER('"+ contentRotationSearchString +"') ";
						}else if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_ID)){
							hql += "AND contentRotation.contentRotationId = "+ contentRotationSearchString +" ";
						}
						hql	+= "ORDER BY " + orderBy;
					}
				}
				// If this is a Number attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// If both the min and max numbers were left blank
					if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a NumberAttr.value between the two dates
						hql += "FROM ContentRotation as contentRotation ";
						if( isEntityOverviewPage ){
							hql += ", EntityInstance as ei "+
								"WHERE contentRotation.contentRotationId = ei.entityId "+
								"AND ";
						}else{
							hql += " WHERE ";
						}
						hql	+= " contentRotation.contentRotationId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM NumberAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						// If the "All Content Rotations" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
						if( selectedContentRotationGroupId.equalsIgnoreCase("-1") == false )
						{					
							hql += "AND contentRotation.contentRotationId IN "
							+ 	" (SELECT contentRotation.contentRotationId "
							+ 	"	FROM ContentRotationGrpMember as crgm "
							+ 	"	JOIN crgm.contentRotation as contentRotation "
							+ 	"	WHERE crgm.grp.grpId = '"+ selectedContentRotationGroupId +"') ";																		
						}		
						// If we're filtering by displayarea
						if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
						{				
							// Get all content rotations that are allowed in this displayarea
							hql += "AND (SIZE(contentRotation.contentRotationDisplayareas) = 0 "
								+ 	" OR contentRotation.contentRotationId IN "
								+	" (SELECT cr.contentRotationId "
								+ 	" FROM ContentRotation as cr "
								+	" LEFT JOIN cr.contentRotationDisplayareas as contentRotationDisplayarea "					
								+	" WHERE contentRotationDisplayarea.displayarea.displayareaId = "+ selectedDisplayareaId +"))";					
						}

						if(excludeDynamicContentRotations){
							hql += "AND contentRotation.type = 'static'";
						}
						
						if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_NAME)){
							hql += "AND UPPER(contentRotation.contentRotationName) LIKE UPPER('"+ contentRotationSearchString +"') ";
						}else if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_ID)){
							hql += "AND contentRotation.contentRotationId = "+ contentRotationSearchString +" ";
						}
						hql	+= "ORDER BY " + orderBy;
					}
				}				
			}
		}
	
		// If we're not excluding the metadata criteria in the query
		if( excludeMetadataCriteria )
		{
			hql += "FROM ContentRotation as contentRotation ";
			if( isEntityOverviewPage ){
				hql += ", EntityInstance as ei "+
					"WHERE contentRotation.contentRotationId = ei.entityId "+
					"AND ";
			}else{
				hql += " WHERE ";
			}
			
			if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_NAME)){
				hql += " UPPER(contentRotation.contentRotationName) LIKE UPPER('"+ contentRotationSearchString +"') ";
			}else if(contentRotationSearchType.equals(ContentRotationSearchType.CONTENT_ROTATION_ID)){
				hql += " contentRotation.contentRotationId = "+ contentRotationSearchString +" ";
			}

				// If the "All Content Rotations" group id ("-1") was not passed in -- limit the search to the given contentRotationGroupId
				if( selectedContentRotationGroupId.equalsIgnoreCase("-1") == false )
				{					
					hql += "AND contentRotation.contentRotationId IN "
					+ 	" (SELECT contentRotation.contentRotationId "
					+ 	"	FROM ContentRotationGrpMember as crgm "
					+ 	"	JOIN crgm.contentRotation as contentRotation "
					+ 	"	WHERE crgm.grp.grpId = '"+ selectedContentRotationGroupId +"') ";																		
				}	
				// If we're filtering by displayarea
				if( selectedDisplayareaId != null && selectedDisplayareaId.length() > 0 )
				{				
					// Get all content rotations that are allowed in this displayarea
					hql += "AND (SIZE(contentRotation.contentRotationDisplayareas) = 0 "
						+ 	" OR contentRotation.contentRotationId IN "
						+	" (SELECT cr.contentRotationId "
						+ 	" FROM ContentRotation as cr "
						+	" LEFT JOIN cr.contentRotationDisplayareas as contentRotationDisplayarea "					
						+	" WHERE contentRotationDisplayarea.displayarea.displayareaId = "+ selectedDisplayareaId +"))";					
				}					
				// If we're filtering by last modified date
				if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) )
				{
					hql +=   " AND contentRotation.contentRotationId IN "
					+	"(SELECT ei.entityId "
					+	" FROM EntityInstance as ei "	
					+ 	" WHERE ei.entityClass.className = '"+ ContentRotation.class.getName() +"' "					
					+	" AND ei.lastModified >= ? "
					+	" AND ei.lastModified <= ?) ";					
				}

				if(excludeDynamicContentRotations){
					hql += "AND contentRotation.type = 'static'";
				}
				
				hql += "ORDER BY " + orderBy;					
		}
		return hql;
	}	
	
	/**
	 * @return Returns a page of displayarea
	 */
	public static Page getContentRotations(String hql, int pageNum, int iSelectedItemsPerPage, Object[] params, String namedParameter, boolean isEntityOverviewPage) throws HibernateException 
	{
		Session session = HibernateSession.currentSession();		
		Query q = session.createQuery( hql );

		// If the params parameter was passed in, use them in the query
		if( params != null )
		{
			// If a namedParameter was passed in -- use it
			// This is required because .setParameterList(int, Object[]) is not supported
			if( namedParameter != null ){
				q.setParameterList( namedParameter, params );
			}
			else{
				for( int i=0; i<params.length; i++ ){
					q.setParameter( i, params[i] );
				}
			}
		}
		if( isEntityOverviewPage ){
			return new EntityOverviewPage(q, pageNum, iSelectedItemsPerPage);
		}else{
			return new ContentRotationResultsPage(q, pageNum, iSelectedItemsPerPage);
		}
	}		
	
	/**
	 * 
	 * @param getAllContentRotations
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param formatter
	 * @return
	 * @throws HibernateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static String treeViewFormat(boolean getAllContentRotations, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, Method formatter) 
		throws HibernateException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException
	{		
		StringBuffer result = new StringBuffer();
		Method allBranchMethod = null;		
		
		if(getAllContentRotations == true)
		{	
			// Declare the method that will be used to build the "All Content Rotations" branch of the tree
			Class[] methodParamTypes = { boolean.class, boolean.class, boolean.class, boolean.class };
			allBranchMethod = Class.forName(ContentRotation.class.getName()).getDeclaredMethod("getAllContentRotationsBranch", methodParamTypes);
		}
					
		result.append( Grp.getTree(Constants.CONTENT_ROTATION_GROUPS, includeLeaves, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, "type_content_rotation_group", formatter, allBranchMethod) );
		return result.toString();
	}
	/**
	 * 
	 */
	public String treeViewFormat(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();	
		String onClick = "null";
		String onDoubleClick = "";		
		if(includeLeaves == true)
		{	
			// Build the string for each device					
			if( includeHref )
			{
				onClick= "\'javascript:top.contentRotationOnClick("+ this.getContentRotationId() +")\'";	
			}	
			if( includeDoubleClick )
			{
				onDoubleClick = " onDblClick=\\\"add('treeNodeContentRotation')\\\"";
			}			
			
			// Build the string for each content rotation			
			result.append("[");					
			result.append("{id:"+ this.getContentRotationId() +"}, \"<span class=\\\"treeNodeContentRotation\\\""+ onDoubleClick +">"+ Reformat.jspEscape(this.getContentRotationName()) + "</span>\", "+ onClick +", null, type_content_rotation,");
			result.append("],\n");
		}
		return result.toString();
	}
	/**
	 * 
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @return
	 * @throws HibernateException
	 */
	public static String getAllContentRotationsBranch(boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, boolean includeAllLeaves) throws HibernateException
	{		
		String onClick = "";
		String onDoubleClick = "";
		if( ( includeHref ) && (doubleClickLeavesOnly == false) )
		{
			onClick = "\'javascript:grpOnClick(-1)\'";
		}	
		if( ( includeDoubleClick ) && (doubleClickLeavesOnly == false) )
		{
			onDoubleClick = " onDblClick=\\\"grpOnDoubleClick(-1, '"+ Constants.CONTENT_ROTATION_GROUP_SUFFIX +"')\\\"";	
		}	
				
		StringBuffer result = new StringBuffer();
		result.append("[");
		result.append("{id:-1}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">All Content Rotation Groups</span>\", "+ onClick +", null, type_content_rotation_group,\n");		
					
		int counter = 1;		
		for( Iterator i=ContentRotation.getNamedContentRotations().iterator(); i.hasNext() && includeAllLeaves; )
		{
			ContentRotation cr = (ContentRotation)i.next();
			result.append( cr.treeViewFormat(0, true, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, null, null) );
			
			// Limit the number of child nodes
			if( counter++ >= Constants.MAX_CHILD_NODES ){
				
				// Append a "more" child node and break
				result.append("[");					
				result.append("{id:0}, \"<span class=\\\"treeNodeContentRotation\\\">...more</span>\", null, null, type_content_rotation,");
				result.append("],\n");
				break;
			}
		}				
		result.append("],\n");
		return result.toString();
	}		
	
	/**
	 * 
	 * @param playlistGrpMember
	 */
	public void addContentRotationGrpMember(ContentRotationGrpMember grpMember) 
	{
		if (grpMember == null)
			throw new IllegalArgumentException("Null grpMember!");				
		contentRotationGrpMembers.add( grpMember );
	}
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeContentRotationGrpMembers() throws HibernateException
	{
		this.contentRotationGrpMembers.clear();
		this.update();
	}	
	
	/**
	 * Make dirty any playlists associated with this content rotation 
	 *
	 */
	public void makeDirty() throws HibernateException
	{		
		// Create a dirty object for this content rotation only if this is a named content rotation
		if( this.getContentRotationName() != null && this.getContentRotationName().length() > 0 )
		{
			// If there is not a dirty object for this object		
			Dirty d = Dirty.getDirty( this.getEntityId() );		
			if(d == null)
			{		
				// Create a new dirty object
				d = new Dirty();			
				d.setDirtyEntityId( this.getEntityId() );
				d.setDirtyType( DirtyType.CONTENT_ROTATION );
				d.save();
			}else{
				d.setDirtyType( DirtyType.CONTENT_ROTATION );
				d.update();
			}
			
			// For each playlist associated with this content rotation
			List l = PlaylistContentRotation.getPlaylistContentRotations( this, false );
			for( Iterator i=l.iterator(); i.hasNext(); )
			{
				PlaylistContentRotation pcr = (PlaylistContentRotation)i.next();
				if( pcr.getPlaylist() != null )
				{
					// Make dirty any devices associated with this playlist
					Device.makeDirty( pcr.getPlaylist() );
				}
			}
			
			// For each dynamic content part referring this content rotation
			List<DynamicContentPart> dynamicContentParts = DynamicContentPart.getChildDynamicContentParts(this);
			for(DynamicContentPart dcp : dynamicContentParts){
				if(dcp.getParentPlaylist() != null){
					dcp.getParentPlaylist().makeDirty();
				}else if(dcp.getParentContentRotation() != null){
					dcp.getParentContentRotation().makeDirty();
				}
			}
		}
		// If this is an unnamed content rotation
		else
		{
			// For each playlist associated with this content rotation
			List l = PlaylistContentRotation.getPlaylistContentRotations( this, false );
			for( Iterator i=l.iterator(); i.hasNext(); )
			{
				PlaylistContentRotation pcr = (PlaylistContentRotation)i.next();
				if( pcr.getPlaylist() != null ){
					pcr.getPlaylist().makeDirty();
				}
			}			
		}		
	}	
	
	/**
	 * If this is a dynamic content rotation, updates the content rotation assets based on the dynamic type.
	 * If a playlist is given, updates the content rotation assets based on the roles of the playlist. 
	 * Otherwise, updates the content rotation assets based on the roles of the content rotation.
	 * We do this because if we're updating a content rotation that is part of a playlist, we want to use 
	 * the permissions of the playlist. If we're updating a stand-alone content rotation, we want to use 
	 * the permission of the content rotation.  
	 * @param playlist
	 * @throws Exception
	 */
	public void updateContentRotation(Playlist playlist, Date dateToUse, boolean recurseChildren) throws Exception
	{
		// If this is a dynamic content rotation
		if( this.getType().equalsIgnoreCase( Constants.DYNAMIC ) )
		{
			ContentRotationUpdater t = new ContentRotationUpdater();
			t.contentRotation = this;
			t.playlist = playlist;
			t.recurseChildren = recurseChildren;
			t.schemaName = SchemaDirectory.getSchemaName();
			t.dateToUse = dateToUse;
			
			// Evict this content rotation from this session since its going to change
			HibernateSession.currentSession().evict(this);
			
			t.start();
			t.join();
		}		
	}
	
	private static class ContentRotationUpdater extends Thread{
		public ContentRotation contentRotation;
		public Playlist playlist;
		public boolean recurseChildren;
		public String schemaName;
		public Date dateToUse;
		
		public void run(){
			try {
				SchemaDirectory.initialize(schemaName, "ContentRotationUpdater", "ContentRotationUpdater", true, false);
				
				// Lock this content rotation to this session
				HibernateSession.currentSession().lock(contentRotation, LockMode.NONE);
				
				// Add each roles associated with this playlist to the collection of appUserViewableRoleIds
				// Now the subsequent queries should reflect the permissions associated with the playlist 
				// (as opposed to the permissions associated with the currently logged in user)
				KmfSession kmfSession = KmfSession.getKmfSession();			
				
				// Save off the appUserViewableRoleIds and isAdmin properties				
				Set<Long> savedAppUserViewableRoleIds = new HashSet(kmfSession.getAppUserViewableRoleIds());
				boolean savedViewDataWithNoRoles = kmfSession.isViewDataWithNoRoles();
				boolean savedIsAdmin = kmfSession.isAdmin();
				
				// Temporarily clear the appUserViewableRolesIds collection and set isAdmin to true
				// We do this because we do not want the roles associated with this playlist to be filtered based on
				// the currently logged in user's roles
				kmfSession.getAppUserViewableRoleIds().clear();				
				kmfSession.setViewDataWithNoRoles( false );
				kmfSession.setAdmin( true );
				
				// Add each roles associated with this content rotation (or playlist) to the collection of appUserViewableRoleIds
				// Now the subsequent queries should reflect the permissions associated with the playlist 
				// (as opposed to the permissions associated with the currently logged in user)
				List<Role> roles = playlist != null ? playlist.getRoles(false) : contentRotation.getRoles(false);
				for( Role role : roles ){
					kmfSession.getAppUserViewableRoleIds().add( role.getRoleId() );
				}			
				
				// If we need to apply permissions
				if(contentRotation.getUseRoles() == Boolean.TRUE){
					FilterManager.enableFilters( ActionType.DYNAMIC_QUERY );
				}
							
				// Since we could be updating content rotations from the content scheduler,
				// permissions to entities are constantly changing and so could be sessions.
				// Initialize and lock this object to avoid LAZY-LOADING exceptions.
				//HibernateSession.currentSession().lock(contentRotation, LockMode.NONE);
				
				// Update the content rotation assets
				if( contentRotation.getDynamicContentType() != null && contentRotation.getDynamicContentType().equals("metadata") )
				{
					contentRotation.updateContentRotationAssetsWithMetadata(dateToUse);
				}
				else if( contentRotation.getHql() != null && contentRotation.getHql().length() > 0 )
				{							
					contentRotation.updateContentRotationAssetsWithHQL( contentRotation.getHql() ); 
				}
				else if( contentRotation.getCustomMethod() != null && contentRotation.getCustomMethod().length() > 0 )
				{
					HibernateSession.startBulkmode();
					contentRotation.updateContentRotationAssetsWithCustomMethod();
					HibernateSession.stopBulkmode();
				}
				else if( contentRotation.getNumAssets() != null ){
					contentRotation.updateContentRotationAssetsWithContentRotations(dateToUse, recurseChildren);
				}
				
				// Apply the presentation style if any
				if( contentRotation.getPresentationStyle() != null && contentRotation.getPresentationStyle().getPersistentValue().length() > 0 )
				{
					HibernateSession.startBulkmode();
					contentRotation.updateContentRotationAssetsWithPresentationStyle();
					HibernateSession.stopBulkmode();
				}	
				
				// Make the playlist dynamic
				contentRotation.setType(Constants.DYNAMIC);
				contentRotation.update();
				
				// Now that all the queries have been performed -- reset the properties that we had previously overwritten
				kmfSession.setAppUserViewableRoleIds( savedAppUserViewableRoleIds );
				kmfSession.setViewDataWithNoRoles( savedViewDataWithNoRoles );
				kmfSession.setAdmin(savedIsAdmin);

				// Disable the filter so to not unintentionally affect subsequent processing
				FilterManager.disableFilters( ActionType.DYNAMIC_QUERY );
				
				// To avoid a SQL state exception due to the permission changes in the above method,
				// it is recommended to clear the cache here.
				HibernateSession.clearCache();
			} catch (Exception e) {
				logger.error(e);
			} finally {
				HibernateSession.closeSession();
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void updateContentRotationAssetsWithHQL(String hql) throws Exception
	{
		// First, delete all assets associated with this content rotation
		this.deleteContentRotationAssets();
		
		// If we need to allow UDA
		if(this.getUseRoles() == Boolean.FALSE){
			FilterManager.disableFilter(FilterType.ASSETS_FILTER);
		}
		
		Session session = HibernateSession.currentSession();
		Query q = session.createQuery( DynamicQueryAction.getHqlForCurrentAssets(hql) );
		this.addAssetsToContentRotation( q, this.getMaxResults() );
		
		q = session.createQuery( DynamicQueryAction.getHqlForFutureAssets(hql) );
		this.addAssetsToContentRotation( q, null );
		
		// If we need to allow UDA
		if(this.getUseRoles() == Boolean.FALSE){
			FilterManager.enableFilter(FilterType.ASSETS_FILTER);
		}
	}
	
	private void addAssetsToContentRotation(Query q, Integer maxResults)
	{
		HibernateSession.startBulkmode();
		
		// If we are limiting the number of results to return
		if( maxResults != null && maxResults > 0 ){			
			q.setMaxResults( this.getMaxResults() );
		}
		
		List<IAsset> l = q.list();
		
		int seqCounter = this.getContentRotationAssets().size();
		int flushCount = 0;
		for( Iterator<IAsset> i = l.iterator(); i.hasNext(); ) 
		{			
			IAsset a = (IAsset)i.next();			
			ContentRotationAsset cra = new ContentRotationAsset();
			cra.setContentRotation( this );
			cra.setAsset( a );
			cra.setLength( a.getAssetPresentation().getLength() );
			cra.setSeqNum( Integer.valueOf(seqCounter) );
			cra.setVariableLength( a.getAssetPresentation().getVariableLength() );
			cra.save();
			
			this.getContentRotationAssets().add(cra);
						
			if (flushCount == 150)
			{
				HibernateSession.stopBulkmode();
				HibernateSession.startBulkmode();
				flushCount = 0;		
			}
			flushCount++;
			seqCounter++;
		}				
		HibernateSession.stopBulkmode();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void updateContentRotationAssetsWithCustomMethod() throws Exception
	{
		// First, delete any assets associated with this content rotation
		Hibernate.initialize( this );
		this.deleteContentRotationAssets();
		
		// If we need to allow UDA
		if(this.getUseRoles() == Boolean.FALSE){
			FilterManager.disableFilter(FilterType.ASSETS_FILTER);
		}
		
		// Parse the class name from the customMethod
		String className = this.getCustomMethod().substring(0, this.getCustomMethod().lastIndexOf("."));
		String methodName = this.getCustomMethod().substring(this.getCustomMethod().lastIndexOf(".") + 1);		
		try
		{
			// Invoke the static method on the given class
			Class c = Class.forName( className );
			Class[] params = { ContentRotation.class };		
			Method m = c.getDeclaredMethod( methodName, params );
			Object[] methodParams = { this };
			m.invoke( null, methodParams );
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		// If we need to allow UDA
		if(this.getUseRoles() == Boolean.FALSE){
			FilterManager.enableFilter(FilterType.ASSETS_FILTER);
		}
	}
	
	public void updateContentRotationAssetsWithContentRotations(Date dateToUse, boolean recurseChildren) throws Exception
	{
		// First, delete any assets associated with this content rotation
		this.deleteContentRotationAssets();
		
		// If we are meant to recurse and update all sub content rotations
		if(recurseChildren){
			// Get all underlying content rotations first
			HashSet<ContentRotation> contentRotationsToUpdate = new HashSet<ContentRotation>();
			for(ContentRotation cr : DynamicContentPart.getRecursiveContentRotationsToUpdate(this, true)){
				contentRotationsToUpdate.add(cr);
			}
			
			// Update all underlying content rotations
			for(ContentRotation cr : contentRotationsToUpdate){
				// Since each update applies its own permissions and then the cache is cleared, we need to lock this to the current session.
				HibernateSession.currentSession().lock(cr, LockMode.NONE);
				cr.updateContentRotation(null, dateToUse, false);
			}
			
			// Re-attach this content rotation to this session
			HibernateSession.clearCache();
			HibernateSession.currentSession().lock(this, LockMode.NONE);
		}
		
		// If we need to allow UDA
		if(this.getUseRoles() == Boolean.FALSE){
			FilterManager.disableFilter(FilterType.ASSETS_FILTER);
		}
		
		// Add assets from content rotation parts
		HashMap<Long, Iterator> iterators = new HashMap<Long, Iterator>();
		
		// Do this in bulk mode since we might be creating hundreds of playlist assets.
		HibernateSession.startBulkmode();
		
		int numResults = 0;
		float length = 0f;
		
		// Get the list of dynamic content parts
		List<DynamicContentPart> dcps = DynamicContentPart.getDynamicContentParts(this);
		
		// Generate content for a whole day
		Date now = new Date();
		while(length < Constants.MILLISECONDS_IN_A_DAY && (this.maxResults == null || numResults < this.maxResults)){
			
			// For each content rotation
			for(DynamicContentPart dcp : dcps){
				ContentRotation cr = dcp.getContentRotation();
				
				// Make sure that this content rotation has assets
				if(cr.getContentRotationAssets().size() > 0 && (this.maxResults == null || numResults < this.maxResults)){
					Iterator i;
					if(iterators.containsKey(cr.getContentRotationId())){
						i =  iterators.get(cr.getContentRotationId());
					}else{
						// Randomize if needed
						List crAssets = new LinkedList(cr.getContentRotationAssets());
						if(cr.getContentRotationOrder() != null && cr.getContentRotationOrder().equals(PlaylistOrderType.RANDOM)){
							Collections.shuffle(crAssets);
						}
						cr.setMyContentRotationAssets(crAssets);
						i = cr.getMyContentRotationAssets().iterator();
					}
					
					// Get numX assets
					int numAssetsSkipped = 0;
					for(int j=0; j-numAssetsSkipped<this.getNumAssets() && length < Constants.MILLISECONDS_IN_A_DAY && (this.maxResults == null || numResults < this.maxResults); j++){
						
						// Get next asset
						if(i.hasNext() == false){
							if(numAssetsSkipped == cr.getContentRotationAssets().size()){
								break;
							}else{
								i = cr.getMyContentRotationAssets().iterator();
								numAssetsSkipped = 0;
							}
						}
						
						ContentRotationAsset cra = (ContentRotationAsset)i.next();
						Asset a = Asset.convert(cra.getAsset());
						
						// Check to see if this is a current asset
						if( (a.getStartDate() == null || a.getStartDate().before(now)) && (a.getEndDate() == null || a.getEndDate().after(now)) ){
							ContentRotationAsset newCra = ContentRotationAsset.create(this, a, cra.getLength().toString(), cra.getVariableLength(), false);
							this.getContentRotationAssets().add(newCra);
							numResults++;
							length += cra.getLength() * 1000f;
						}else{
							numAssetsSkipped++;
						}
					}
					
					// Update iterator map
					iterators.put(cr.getContentRotationId(), i);
				}
			}
			
			// Break out of the loop if there are no results after an iteration thru all sub parts.
			if(numResults == 0){
				break;
			}
		}
		
		// Pull in future assets
		// For each content rotation
		for(DynamicContentPart dcp : dcps){
			ContentRotation cr = dcp.getContentRotation();
			
			// Make sure that this content rotation has assets
			if(cr.getContentRotationAssets().size() > 0){
				for(Iterator i = cr.getMyContentRotationAssets().iterator(); i.hasNext();){
					ContentRotationAsset cra = (ContentRotationAsset)i.next();
					Asset a = Asset.convert(cra.getAsset());
					
					// Check to see if this is a future asset
					if((a.getStartDate() != null && a.getStartDate().after(now))){
						ContentRotationAsset newCra = ContentRotationAsset.create(this, a, cra.getLength().toString(), cra.getVariableLength(), false);
						this.getContentRotationAssets().add(newCra);
						length += cra.getLength() * 1000f;
					}
				}
			}
		}
		
		// Update the avg loop length
		this.setAvgLoopLength(DynamicContentPart.calculateAvgLoopLength(dcps, this.getNumAssets()).intValue());
		
		// Update the content rotation
		this.update();
		
		// Stop bulk mode
		HibernateSession.stopBulkmode();
		
		// If we need to allow UDA
		if(this.getUseRoles() == Boolean.FALSE){
			FilterManager.enableFilter(FilterType.ASSETS_FILTER);
		}
	}
	
	/**
	 * 
	 * @param p
	 * @throws Exception
	 */
	public void updateContentRotationAssetsWithMetadata(Date dateToUse) throws Exception
	{
		// Re-generate the hql
		this.generateHql(dateToUse);
		
		// If we generated a valid hql
		if(this.getHql() != null && this.getHql().length() > 0){
			
			// First, delete all assets associated with this content rotation
			this.deleteContentRotationAssets();
			
			// If we need to allow UDA
			if(this.getUseRoles() == Boolean.FALSE){
				FilterManager.disableFilter(FilterType.ASSETS_FILTER);
			}		
			
			Session session = HibernateSession.currentSession();		
			Query q = session.createQuery( DynamicQueryAction.getHqlForCurrentAssets(this.getHql()));
			this.addAssetsToContentRotation( q, this.getMaxResults() );
			
			q = session.createQuery( DynamicQueryAction.getHqlForFutureAssets(this.getHql()));
			this.addAssetsToContentRotation( q, null );
			
			// If we need to allow UDA
			if(this.getUseRoles() == Boolean.FALSE){
				FilterManager.enableFilter(FilterType.ASSETS_FILTER);
			}
		}
	}	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void updateContentRotationAssetsWithPresentationStyle() throws Exception
	{
		Date now = new Date();
		List<Asset> futureAssets = new ArrayList<Asset>();
		List<Long> crAssetsToDelete = new ArrayList<Long>();
		for(ContentRotationAsset cra : this.getContentRotationAssets()){
			Asset a = Asset.convert(cra.getAsset());
			// If this is a future asset
			if(a.getStartDate() != null && a.getStartDate().after(now)){
				futureAssets.add(a);
				crAssetsToDelete.add(cra.getContentRotationAssetId());
			}
		}
		
		if(crAssetsToDelete.size() > 0){
			for(Long crAssetId : crAssetsToDelete){
				ContentRotationAsset cra = ContentRotationAsset.getContentRotationAsset(crAssetId);
				cra.delete();
			}
			this.update();
		}
		
		// Parse the class name from the customMethod
		String className = this.getPresentationStyle().getPersistentValue().substring(0, this.getPresentationStyle().getPersistentValue().lastIndexOf("."));
		String methodName = this.getPresentationStyle().getPersistentValue().substring(this.getPresentationStyle().getPersistentValue().lastIndexOf(".") + 1);
		
		try{
			// Invoke the static method on the given class
			Class c = Class.forName( className );
			Class[] params = { ContentRotation.class };		
			Method m = c.getDeclaredMethod( methodName, params );
			Object[] methodParams = { this };
			m.invoke( null, methodParams );
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		for(Asset a : futureAssets){
			ContentRotationAsset.create(this, a, a.getAssetPresentation().getLength().toString(), true, false);
		}
	}
	
	public void generateHql(Date dateToUse){
		String hql = DynamicQueryPart.generateHql(DynamicQueryPart.getDynamicQueryParts(this), dateToUse);
		this.setHql(hql);
		this.update();
	}
	
	public void importContentRotation(ContentRotation contentRotation, boolean appendAssets, boolean makeDirty) throws SQLException
	{
		// Start a new thread
		ImportContentRotationThread t = new ImportContentRotationThread(){
			public void run(){
				
				// Initialize hibernate session now
				SchemaDirectory.initialize(schemaName, "ImportContentRotationThread", appUsername, true, false);
				
				// If something other than the "Default" layout was passed in, attempt to locate the layout		
				String appendMessage = appendAssets ? "was appended to" : "replaced";	
						
				// Replace the playlist assets if specified
				if( appendAssets == false ){
					try {
						ContentRotation cr = ContentRotation.getContentRotation(entityId);
						cr.deleteContentRotationAssets();
						
						// Re-set session after JDBC deletes
						HibernateSession.closeSession();
						HibernateSession.currentSession();
					} catch (Exception e) {
						logger.error(e);
					}
				}
				
				// Load objects in this session
				ContentRotation cr = ContentRotation.getContentRotation(entityId);
				ContentRotation contentRotation = ContentRotation.getContentRotation(contentRotationId);
				
				// Start bulk mode
				HibernateSession.startBulkmode();
				
				if( contentRotation != null && contentRotation.getContentRotationAssets().size() > 0 ){
					
					// Append each content rotation asset to this playlist
					for( Iterator i=contentRotation.getContentRotationAssets().iterator(); i.hasNext(); ){
						// If we did not find an existing playlist asset in this playlist,
						// use the default presentation properties of this content rotation asset
						ContentRotationAsset cra = (ContentRotationAsset)i.next();
						
						// Create a new PlaylistAsset for this playlist
						ContentRotationAsset.create( cr, Asset.convert(cra.getAsset()), cra.getLength().toString(), cra.getVariableLength(), true );
					}
				}
				
				// Set the contentRotationImportDetail message to be displayed in the interface		
				SimpleDateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				String contentRotationImportDetail = "Successful import of Content Rotation: "+ contentRotation.getContentRotationName() 
					+" "+ appendMessage +" the assets in this Content Rotation on "+dateTimeFormat.format( new Date() ) +".";
				cr.setContentRotationImportDetail( contentRotationImportDetail );
				
				// Set the playlist to be static
				cr.setType(Constants.STATIC);
				
				if( makeDirty ){
					cr.makeDirty();
				}
				
				// Stop bulk mode
				HibernateSession.stopBulkmode();
			}
		};
		t.entityId = this.contentRotationId;
		t.contentRotationId = contentRotation.getContentRotationId();
		t.appendAssets = appendAssets;
		t.makeDirty = makeDirty;
		t.appUsername = KmfSession.getKmfSession() != null && KmfSession.getKmfSession().getCurrentAppUser() != null ? KmfSession.getKmfSession().getCurrentAppUser().getName() : "Auto";
		
		// We need to init the kmf schema to get the schema name
		SchemaDirectory.initialize("kmf", "", t.appUsername, true, false);
		t.schemaName = KmfSession.getKmfSession() != null && KmfSession.getKmfSession().getCurrentAppUser() != null ? KmfSession.getKmfSession().getCurrentAppUser().getSchema().getSchemaName() : SchemaDirectory.KUVATA_SCHEMA;
		
		// Switch back to the kuvata schema
		SchemaDirectory.initialize(SchemaDirectory.KUVATA_SCHEMA, "", t.appUsername, true, false);
		
		// Wait for all the updates to finish
		try {
			t.start();
			t.join();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private class ImportContentRotationThread extends Thread{
		Long entityId, contentRotationId;
		boolean appendAssets;
		boolean makeDirty;
		String schemaName;
		String appUsername;
	}
	
	public void delete(){
		// Delete all dynamic content parts
		for(DynamicContentPart dcp : DynamicContentPart.getDynamicContentParts(this)){
			dcp.delete();
		}
		for(DynamicContentPart dcp : DynamicContentPart.getChildDynamicContentParts(this)){
			dcp.delete();
		}
		
		// Delete all dynamic query parts
		for(DynamicQueryPart dqp : DynamicQueryPart.getDynamicQueryParts(this)){
			dqp.delete();
		}
		
		super.delete();
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof ContentRotation) ) result = false;
		
		ContentRotation c = (ContentRotation) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "ContentRotation".hashCode();
		result = Reformat.getSafeHash( this.getContentRotationId().toString(), result, 3 );
		result = Reformat.getSafeHash( this.getContentRotationName(), result, 11 );		
		return result < 0 ? -result : result;
	}		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getContentRotationId();
	}
	
	/**
	 * @return Returns the contentRotationId.
	 */
	public Long getContentRotationId() {
		return contentRotationId;
	}

	/**
	 * @param contentRotationId The contentRotationId to set.
	 */
	public void setContentRotationId(Long contentRotationId) {
		this.contentRotationId = contentRotationId;
	}

	/**
	 * @return Returns the contentRotationAssets.
	 */
	public List<ContentRotationAsset> getContentRotationAssets() {
		return contentRotationAssets;
	}
	/**
	 * @param contentRotationAssets The contentRotationAssets to set.
	 */
	public void setContentRotationAssets(List contentRotationAssets) {
		this.contentRotationAssets = contentRotationAssets;
	}
	/**
	 * @return Returns the contentRotationName.
	 */
	public String getContentRotationName() {
		return contentRotationName;
	}
	
	/**
	 * @param contentRotationName The contentRotationName to set.
	 */
	public void setContentRotationName(String contentRotationName) {
		this.contentRotationName = contentRotationName;
	}

	/**
	 * @return Returns the contentRotationDisplayareas.
	 */
	public Set getContentRotationDisplayareas() {
		return contentRotationDisplayareas;
	}
	

	/**
	 * @param contentRotationDisplayareas The contentRotationDisplayareas to set.
	 */
	public void setContentRotationDisplayareas(Set contentRotationDisplayareas) {
		this.contentRotationDisplayareas = contentRotationDisplayareas;
	}
	/**
	 * @return Returns the csvImportDate.
	 */
	public Date getCsvImportDate() {
		return csvImportDate;
	}
	

	/**
	 * @param csvImportDate The csvImportDate to set.
	 */
	public void setCsvImportDate(Date csvImportDate) {
		this.csvImportDate = csvImportDate;
	}

	

	/**
	 * @return the csvImportDetail
	 */
	public Clob getCsvImportDetail() {
		return csvImportDetail;
	}

	/**
	 * @param csvImportDetail the csvImportDetail to set
	 */
	public void setCsvImportDetail(Clob csvImportDetail) {
		this.csvImportDetail = csvImportDetail;
	}

	/**
	 * @return Returns the csvImportStatus.
	 */
	public PlaylistImportStatus getCsvImportStatus() {
		return csvImportStatus;
	}
	

	/**
	 * @param csvImportStatus The csvImportStatus to set.
	 */
	public void setCsvImportStatus(PlaylistImportStatus csvImportStatus) {
		this.csvImportStatus = csvImportStatus;
	}

	/**
	 * @return Returns the contentRotationGrpMembers.
	 */
	public Set<ContentRotationGrpMember> getContentRotationGrpMembers() {
		return contentRotationGrpMembers;
	}
	

	/**
	 * @param contentRotationGrpMembers The contentRotationGrpMembers to set.
	 */
	public void setContentRotationGrpMembers(Set<ContentRotationGrpMember> contentRotationGrpMembers) {
		this.contentRotationGrpMembers = contentRotationGrpMembers;
	}

	/**
	 * @return the hql
	 */
	public String getHql() {
		return hql;
	}

	/**
	 * @param hql the hql to set
	 */
	public void setHql(String hql) {
		this.hql = hql;
	}

	/**
	 * @return the customMethod
	 */
	public String getCustomMethod() {
		return customMethod;
	}

	/**
	 * @param customMethod the customMethod to set
	 */
	public void setCustomMethod(String customMethod) {
		this.customMethod = customMethod;
	}

	/**
	 * @return the presentationStyle
	 */
	public PresentationStyleContentRotation getPresentationStyle() {
		return presentationStyle;
	}

	/**
	 * @param presentationStyle the presentationStyle to set
	 */
	public void setPresentationStyle(
			PresentationStyleContentRotation presentationStyle) {
		this.presentationStyle = presentationStyle;
	}

	/**
	 * @return the runFromContentScheduler
	 */
	public Integer getRunFromContentScheduler() {
		return runFromContentScheduler;
	}

	/**
	 * @param runFromContentScheduler the runFromContentScheduler to set
	 */
	public void setRunFromContentScheduler(Integer runFromContentScheduler) {
		this.runFromContentScheduler = runFromContentScheduler;
	}

	/**
	 * @return the maxResults
	 */
	public Integer getMaxResults() {
		return maxResults;
	}

	/**
	 * @param maxResults the maxResults to set
	 */
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * @param contentRotationDisplayareasSorted the contentRotationDisplayareasSorted to set
	 */
	public void setContentRotationDisplayareasSorted(
			List contentRotationDisplayareasSorted) {
		this.contentRotationDisplayareasSorted = contentRotationDisplayareasSorted;
	}

	/**
	 * @return the contentRotationOrder
	 */
	public PlaylistOrderType getContentRotationOrder() {
		return contentRotationOrder;
	}

	/**
	 * @param contentRotationOrder the contentRotationOrder to set
	 */
	public void setContentRotationOrder(PlaylistOrderType contentRotationOrder) {
		this.contentRotationOrder = contentRotationOrder;
	}

	/**
	 * @param device Device for which ContentRotationAssets are to be calculated, excluding asset exclusions, and possibly randomized
	 * @return
	 */
	public List getMyContentRotationAssets(Device device) {
		if( device.getDeviceId().longValue() == this.myDeviceId )
		{
			return this.myContentRotationAssets;		
		}
		
		this.myContentRotationAssets = new LinkedList();
		this.myDeviceId = device.getDeviceId().longValue();

		/*
		 * Remove excluded assets from the content rotation
		 */
        List craList = this.getContentRotationAssets();
        for( Iterator i = craList.iterator(); i.hasNext(); )
        {
        	ContentRotationAsset cra = (ContentRotationAsset) (i.next());
        	if( ContentScheduler.isAssetExcludedOrExpired(cra.getAsset().getAssetId(), device) == false )
        	{        		
        		this.myContentRotationAssets.add( cra );
    		}
        }

        if( this.contentRotationOrder != null && this.contentRotationOrder.getPersistentValue().equals(PlaylistOrderType.RANDOM.getPersistentValue()) )
        {
    		Collections.shuffle( this.myContentRotationAssets );
        }
        
		return this.myContentRotationAssets;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getUseRoles() {
		return useRoles;
	}

	public void setUseRoles(Boolean useRoles) {
		this.useRoles = useRoles;
	}

	public Integer getNumAssets() {
		return numAssets;
	}

	public void setNumAssets(Integer numAssets) {
		this.numAssets = numAssets;
	}

	public List getMyContentRotationAssets() {
		return myContentRotationAssets;
	}

	public void setMyContentRotationAssets(List myContentRotationAssets) {
		this.myContentRotationAssets = myContentRotationAssets;
	}

	public Date getLastDynamicUpdateDt() {
		return lastDynamicUpdateDt;
	}

	public void setLastDynamicUpdateDt(Date lastDynamicUpdateDt) {
		this.lastDynamicUpdateDt = lastDynamicUpdateDt;
	}

	public String getDynamicContentType() {
		return dynamicContentType;
	}

	public void setDynamicContentType(String dynamicContentType) {
		this.dynamicContentType = dynamicContentType;
	}

	public String getContentRotationImportDetail() {
		return contentRotationImportDetail;
	}

	public void setContentRotationImportDetail(String contentRotationImportDetail) {
		this.contentRotationImportDetail = contentRotationImportDetail;
	}

	public Set<ContentRotationImport> getContentRotationImports() {
		return contentRotationImports;
	}

	public void setContentRotationImports(
			Set<ContentRotationImport> contentRotationImports) {
		this.contentRotationImports = contentRotationImports;
	}

	public Integer getAvgLoopLength() {
		return avgLoopLength;
	}

	public void setAvgLoopLength(Integer avgLoopLength) {
		this.avgLoopLength = avgLoopLength;
	}

}
