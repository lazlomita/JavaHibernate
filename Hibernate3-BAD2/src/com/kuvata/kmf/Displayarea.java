package com.kuvata.kmf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.usertype.AspectRatioType;
import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.AudioChannelType;
import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.util.Reformat;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class Displayarea extends Entity implements Historizable { 
	
	private Long displayareaId;
	private String displayareaName;
	private Integer width;
	private Integer height;
	private AspectRatioType aspectRatio;
	private AudioChannelType audioChannel;
	private Boolean triggerable;
	private Boolean isShared = false;
	private Set layoutDisplayareas = new HashSet();
	private Set pairedDisplayareas = new HashSet();	
	private Set<DisplayareaAssetType> displayareaAssetTypes = new HistorizableSet<DisplayareaAssetType>();
	private List displayareaAssetTypesSorted;
	private String previewWidth;
	private String previewHeight;
	
	/**
	 * 
	 *
	 */
	public Displayarea()
	{		
	}
	
	public static Displayarea createOrUpdate(Layout layout, Long displayareaId, String displayareaName, Integer width, Integer height, AspectRatioType aspectRatio, AudioChannelType audioChannel, Boolean isShared, Boolean triggerable)
	{
		Displayarea displayarea = null;
		boolean isUpdate = false;
		
		// If we're creating a "new" displayarea
		if( displayareaId == null || displayareaId.longValue() < 0 ){
			displayarea = new Displayarea();
		}else{
			displayarea = Displayarea.getDisplayarea( displayareaId );
			isUpdate = true;
		}		
		displayarea.setDisplayareaName( displayareaName.trim() );
		displayarea.setWidth( width );
		displayarea.setHeight( height );			
		displayarea.setAspectRatio( aspectRatio );
		displayarea.setAudioChannel(audioChannel);
		displayarea.setIsShared( isShared );
		displayarea.setTriggerable( triggerable );
		if( isUpdate ){
			displayarea.update();
		}else{
			// Copy layout permissions if it is not a shared display area
			if(isShared){
				displayarea.save();
			}else{
				displayarea.save(false);
				displayarea.copyPermissionEntries(layout, true, false);
			}
		}	
		return displayarea;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof Displayarea) ) result = false;
		
		Displayarea c = (Displayarea) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}	
	/**
	 * Return hashCode for unique Displayarea
	 */
	public int hashCode()
	{
		int result = "Displayarea".hashCode();
		result = Reformat.getSafeHash( this.displayareaId, result, 2 );
		result = Reformat.getSafeHash( this.displayareaName, result, 3 );
		result = Reformat.getSafeHash( this.height, result, 5 );
		result = Reformat.getSafeHash( this.width, result, 7 );
		return result;
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<Displayarea> getDisplayareas() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT da "
			+ "FROM Displayarea as da "	
			+ "WHERE da.isShared = ? "
			+ "ORDER BY UPPER(da.displayareaName)"; 		
		return session.createQuery( hql ).setParameter(0, true).list();	
	}	
	
	public static List getDisplayareas(List displayareaIds) throws HibernateException
	{
		return Entity.load(Displayarea.class, displayareaIds);		
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<Displayarea> getAllDisplayareas() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT da "
			+ "FROM Displayarea as da "	
			+ "ORDER BY UPPER(da.displayareaName)"; 		
		return session.createQuery( hql ).list();	
	}		
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeDisplayareaAssetTypes() throws HibernateException
	{
		this.displayareaAssetTypes.clear();		
		this.update();
	}	

	/**
	 * Returns true if a displayarea with the given name already exists in the database
	 * 
	 * @param layoutName
	 * @return
	 */
	public static boolean displayareaExists(String displayareaName) throws HibernateException
	{
		displayareaName = Reformat.oraesc( displayareaName ).toUpperCase();
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT da "
			+ "FROM Displayarea as da "	
			+ "WHERE da.isShared = ? "
			+ "AND UPPER(da.displayareaName) = '"+ displayareaName +"'"; 		
		List l = session.createQuery( hql ).setParameter(0, true).list();
		return l.size() > 0 ? true : false;
	}		
	
	/**
	 * Determines whether or not this displayarea has any asset presentation
	 * objects associated with it. Returns true if it doesn't (and is therefore
	 * removable), or false if it does (and therefore cannot be removed)
	 * 
	 * @return
	 */
	public boolean isRemovable() throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT COUNT(*) " +
					"FROM AssetPresentation ap " +
					"WHERE ap.displayarea.displayareaId = "+ this.getDisplayareaId();
		Iterator i = session.createQuery( hql ).iterate();
		int numRecords = ( (Long) i.next() ).intValue();		
		boolean result = numRecords > 0 ? false : true;
		Hibernate.close( i );
		return result;
	}		

	/**
	 * 
	 * @param displayareaId
	 * @return
	 * @throws HibernateException
	 */
	public static Displayarea getDisplayarea(Long displayareaId) throws HibernateException
	{
		return (Displayarea)Entity.load(Displayarea.class, displayareaId);		
	}
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getDisplayareaId();
	}	
	
	/**
	 * 
	 * @param displayareaName
	 * @return
	 * @throws HibernateException
	 */
	public static List getDisplayareas(String displayareaName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		Criteria crit = session.createCriteria(Displayarea.class);		
		crit.add(Expression.eq("displayareaName", displayareaName).ignoreCase());
		List l = crit.list();		
		return l;
	}
	
	/**
	 * Retrieves a list of displayareas that are valid for the given asset type
	 * @param at
	 * @return
	 */
	public static List getDisplayareas(AssetType at)
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT da from Displayarea as da "
			+ "JOIN da.displayareaAssetTypes as dat  "
			+ "WHERE dat.assetType = '"+ at.getPersistentValue() + "' "
			+ "ORDER BY UPPER(da.displayareaName)";
		List l = session.createQuery( hql ).list();
		return l;
	}
	
	/**
	 * Retrieves a list of displayareas that are valid for the given asset type
	 * and this displayarea. If the asset type can go in any displayarea
	 * (there are no rows in the displayarea_assettype table for this asset type),
	 * return true.
	 * 
	 * @param at
	 * @return
	 */
	public boolean isValidAssetType(AssetType at)
	{
		boolean result = false;
		
		// Get all displayareas for this asset type
		Session session = HibernateSession.currentSession();
		String hql = "SELECT da from Displayarea as da "
			+ "JOIN da.displayareaAssetTypes as dat  "
			+ "WHERE dat.assetType = '"+ at.getPersistentValue() + "' "
			+ "AND da.displayareaId = "+ this.getDisplayareaId();
		List l = session.createQuery( hql ).list();
		
		// If there are no displayareas associated with this asset type, it means
		// this asset type is allowed in all displayareas -- return true
		if( l.size() == 0 ){
			result = true;
		}
		else
		{
			// See if this displayarea is one of the valid displayareaAssetType
			for( Iterator i=l.iterator(); i.hasNext(); )
			{
				Displayarea da = (Displayarea)i.next();
				if( da.getDisplayareaId() == this.getDisplayareaId() ){
					result = true;
					break;
				}
			}
		}		
		return result;
	}	
	
	/**
	 * @return Returns the number of displayareas
	 */
	public static int getDisplayareasCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		String hql = "SELECT COUNT(d) "
			+ "FROM Displayarea as d "			
			+ "WHERE d.isShared = ?";
		Iterator i = session.createQuery( hql ).setParameter(0, true).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}
	
	/**
	 * Gets the count of the assets according to the given search criteria.
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
	public static int searchDisplayareasCount(AttrDefinition attrDefinition, String displayareaNameSearchString, String selectedSearchOption, String searchString, 
			String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, String maxNumber, int startingRecord, boolean getShared) throws ParseException
	{
		int result = 0;
		String hql = buildSearchHql( displayareaNameSearchString, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, getShared, true );		
		
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
	private static String buildSearchHql(String displayareaNameSearchString, String selectedSearchOption, String searchString,
			String[] selectedSearchOptions, String minNumber, String maxNumber, String minDate, String maxDate, boolean getShared, boolean getCount)
	{
		String hql = "";
		
		// If the displayareaNameSearchString search string was left blank, use wildcard
		if( displayareaNameSearchString == null || displayareaNameSearchString.trim().length() == 0 ){
			displayareaNameSearchString = "%";
		}
		
		// Imply *
		if(displayareaNameSearchString.startsWith("*") == false){
			displayareaNameSearchString = "*" + displayareaNameSearchString;
		}
		if(displayareaNameSearchString.endsWith("*") == false){
			displayareaNameSearchString = displayareaNameSearchString + "*";
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
		displayareaNameSearchString = displayareaNameSearchString.replaceAll("\\*", "\\%");	
		displayareaNameSearchString = Reformat.oraesc(displayareaNameSearchString);		
				
		// If we are counting the number of records
		if( getCount == true) {
			hql = "SELECT COUNT(displayarea) ";
		} else {
			hql = "SELECT displayarea ";
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
							// Get all devices that have a StringAttr with the given criteria
							hql += "FROM Displayarea as displayarea "														
								+ "WHERE displayarea.displayareaId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND attr.value IN (:attrValues) ) ";
							hql += getShared ? "AND displayarea.isShared = 1 " : "";
							hql += "AND UPPER(displayarea.displayareaName) LIKE UPPER('"+ displayareaNameSearchString +"') "	
								+ "ORDER BY UPPER(displayarea.displayareaName)";									
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
							
							// Get all layouts that have a StringAttr with the given criteria
							hql += "FROM Displayarea as displayarea "															
								+ "WHERE displayarea.displayareaId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND UPPER(attr.value) LIKE UPPER ('"+ searchString +"') ) ";
							hql += getShared ? "AND displayarea.isShared = 1 " : "";
							hql	+= "AND UPPER(displayarea.displayareaName) LIKE UPPER('"+ displayareaNameSearchString +"') "	
								+ "ORDER BY UPPER(displayarea.displayareaName)";		
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
						// Get all displayareas that have a DateAttr.value between the two dates
						hql += "FROM Displayarea as displayarea "															
							+ "WHERE displayarea.displayareaId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM DateAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						hql += getShared ? "AND displayarea.isShared = 1 " : "";
						hql += "AND UPPER(displayarea.displayareaName) LIKE UPPER('"+ displayareaNameSearchString +"') "	
							+ "ORDER BY UPPER(displayarea.displayareaName)";
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
						// Get all displayareas that have a NumberAttr.value between the two dates
						hql += "FROM Displayarea as displayarea "															
							+ "WHERE displayarea.displayareaId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM NumberAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						hql += getShared ? "AND displayarea.isShared = 1 " : "";
						hql	+= "AND UPPER(displayarea.displayareaName) LIKE UPPER('"+ displayareaNameSearchString +"') "	
							+ "ORDER BY UPPER(displayarea.displayareaName)";			
					}
				}				
			}
		}
	
		// If we're not excluding the metadata criteria in the query
		if( excludeMetadataCriteria )
		{
			hql += "FROM Displayarea as displayarea "																														
				+ "WHERE UPPER(displayarea.displayareaName) LIKE UPPER('"+ displayareaNameSearchString +"') ";
			// If we're filtering by last modified date
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) )
			{
				hql +=   " AND displayarea.displayareaId IN "
					+	"(SELECT ei.entityId "
					+	" FROM EntityInstance as ei "	
					+ 	" WHERE ei.entityClass.className = '"+ Displayarea.class.getName() +"' "
					+	" AND ei.lastModified >= ? "
					+	" AND ei.lastModified <= ?) ";					
			}
			hql += getShared ? "AND displayarea.isShared = 1 " : "";
			hql += "ORDER BY UPPER(displayarea.displayareaName)";					
		}
		return hql;
	}				
	

		
	
	/**
	 * 
	 * @return
	 */
	public List getDisplayareaAssetTypesSorted()
	{
		if(displayareaAssetTypes != null)
		{				
			List l = new LinkedList(displayareaAssetTypes);
			BeanPropertyComparator  comparator = new BeanPropertyComparator("assetType");							
			Collections.sort(l,comparator);
			displayareaAssetTypesSorted = l ;			
		}
		return displayareaAssetTypesSorted;
	}
	
	/**
	 * @return Returns the layoutDisplayareas.
	 */
	public Set getLayoutDisplayareas() {
		return layoutDisplayareas;
	}

	/**
	 * @param layoutDisplayareas The layoutDisplayareas to set.
	 */
	public void setLayoutDisplayareas(Set layoutDisplayareas) {
		this.layoutDisplayareas = layoutDisplayareas;
	}

	/**
	 * @return Returns the pairedDisplayareas.
	 */
	public Set getPairedDisplayareas() {
		return pairedDisplayareas;
	}

	/**
	 * @param pairedDisplayareas The pairedDisplayareas to set.
	 */
	public void setPairedDisplayareas(Set pairedDisplayareas) {
		this.pairedDisplayareas = pairedDisplayareas;
	}


	/**
	 * @return Returns the displayareaAssetTypes.
	 */
	public Set getDisplayareaAssetTypes() {
		return displayareaAssetTypes;
	}

	/**
	 * @param displayareaAssetTypes The displayareaAssetTypes to set.
	 */
	public void setDisplayareaAssetTypes(Set displayareaAssetTypes) {
		this.displayareaAssetTypes = displayareaAssetTypes;
	}

	/**
	 * @param displayareaAssetTypesSorted The displayareaAssetTypesSorted to set.
	 */
	public void setDisplayareaAssetTypesSorted(List displayareaAssetTypesSorted) {
		this.displayareaAssetTypesSorted = displayareaAssetTypesSorted;
	}
	/**
	 * @return Returns the displayareaId.
	 */
	public Long getDisplayareaId() {
		return displayareaId;
	}

	/**
	 * @param displayareaId The displayareaId to set.
	 */
	public void setDisplayareaId(Long displayareaId) {
		this.displayareaId = displayareaId;
	}

	/**
	 * @return Returns the displayareaName.
	 */
	public String getDisplayareaName() {
		return displayareaName;
	}

	/**
	 * @param displayareaName The displayareaName to set.
	 */
	public void setDisplayareaName(String displayareaName) {
		this.displayareaName = displayareaName;
	}

	/**
	 * @return Returns the height.
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * @param height The height to set.
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}

	/**
	 * @return Returns the width.
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param width The width to set.
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}
	/**
	 * @return Returns the aspectRatio.
	 */
	public AspectRatioType getAspectRatio() {
		return aspectRatio;
	}
	
	/**
	 * @param aspectRatio The aspectRatio to set.
	 */
	public void setAspectRatio(AspectRatioType aspectRatio) {
		this.aspectRatio = aspectRatio;
	}
	/**
	 * @return Returns the isShared.
	 */
	public Boolean getIsShared() {
		return isShared;
	}
	
	/**
	 * @param isShared The isShared to set.
	 */
	public void setIsShared(Boolean isShared) {
		this.isShared = isShared;
	}

	/**
	 * @return Returns the previewHeight.
	 */
	public String getPreviewHeight() {
		return previewHeight;
	}
	

	/**
	 * @param previewHeight The previewHeight to set.
	 */
	public void setPreviewHeight(String previewHeight) {
		this.previewHeight = previewHeight;
	}
	

	/**
	 * @return Returns the previewWidth.
	 */
	public String getPreviewWidth() {
		return previewWidth;
	}
	

	/**
	 * @param previewWidth The previewWidth to set.
	 */
	public void setPreviewWidth(String previewWidth) {
		this.previewWidth = previewWidth;
	}

	public AudioChannelType getAudioChannel() {
		return audioChannel;
	}

	public void setAudioChannel(AudioChannelType audioChannel) {
		this.audioChannel = audioChannel;
	}

	public Boolean getTriggerable() {
		return triggerable;
	}

	public void setTriggerable(Boolean triggerable) {
		this.triggerable = triggerable;
	}
	
	
	

	

}
