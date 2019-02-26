package com.kuvata.kmf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.kmf.attr.Attr;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.attr.DateAttr;
import com.kuvata.kmf.attr.NumberAttr;
import com.kuvata.kmf.attr.StringAttr;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableLinkedSet;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class Layout extends Entity implements Historizable {

	private Long layoutId;
	private String layoutName;
	private Integer width;
	private Integer height;
	private Set<LayoutDisplayarea> layoutDisplayareas = new HistorizableLinkedSet<LayoutDisplayarea>();	
	private List<LayoutDisplayarea> layoutDisplayareasSorted;
	
	private static final String PREVIEW_WIDTH = "800";
	private static final String PREVIEW_HEIGHT = "600";
	
	/**
	 * 
	 *
	 */
	public Layout()
	{		
	}
	/**
	 * 
	 * @param layoutId
	 * @return
	 * @throws HibernateException
	 */
	public static Layout getLayout(Long layoutId) throws HibernateException
	{
		return (Layout)Entity.load(Layout.class, layoutId);		
	}
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<Layout> getLayouts() throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l = session.createQuery(				
				"SELECT l "
				+ "FROM Layout as l "				
				+ "ORDER BY UPPER(l.layoutName)"						
				).list();
		return l;
	}	
	/**
	 * 
	 * @param layoutName
	 * @return
	 * @throws HibernateException
	 */
	public static List getLayouts(String layoutName) throws HibernateException
	{
		Layout layout = null;		
		Session session = HibernateSession.currentSession();		
		Criteria crit = session.createCriteria(Layout.class);		
		crit.add(Expression.eq("layoutName", layoutName).ignoreCase());
		List l = crit.list();		
		return l;
	}
	
	public static List getLayouts(List layoutIds) throws HibernateException
	{
		return Entity.load(Layout.class, layoutIds);		
	}
	
	/**
	 * Returns a HashMap of Layouts and their respective displayareas. Only includes
	 * Layouts that contain at least one displayarea in which the given 
	 * asset type can be displayed. 
	 * 
	 * @param at
	 * @return
	 * @throws HibernateException
	 */
	public static LinkedHashMap getLayoutsAndDisplayareas(AssetType at, AssetPresentation assetPresentation) throws HibernateException
	{		
		LinkedHashMap result = new LinkedHashMap();		
		List layouts = Layout.getLayouts();		
		for( Iterator i = layouts.iterator(); i.hasNext(); )
		{
			boolean includeLayout = false;			
			Layout layout = (Layout)i.next();
			Session s = HibernateSession.currentSession();			
			Set layoutDisplayareas = layout.getLayoutDisplayareas();
			ArrayList validLayoutDisplayareas = new ArrayList();
						
			// Make sure at least one of the display areas in this layout is valid for this asset type			
			for( Iterator j = layoutDisplayareas.iterator(); j.hasNext(); )
			{
				LayoutDisplayarea lda = (LayoutDisplayarea)j.next();
				Displayarea da = lda.getDisplayarea();	
				Set displayareaAssetTypes = da.getDisplayareaAssetTypes();
				
				// If this displayarea does not have any asset types specified (allows any asset types), include the layout
				if( displayareaAssetTypes == null || displayareaAssetTypes.size() == 0 )
				{
					includeLayout = true;
					validLayoutDisplayareas.add( lda );
				}
				else
				{
					for( Iterator k = displayareaAssetTypes.iterator(); k.hasNext(); )
					{					
						DisplayareaAssetType a = (DisplayareaAssetType)k.next();
						AssetType currAt = a.getAssetType();					
						if(currAt.toString().equals(at.toString()))
						{
							includeLayout = true;
							validLayoutDisplayareas.add( lda );					
						}
					}
				}				
			}
			
			// If this is a valid layout -- add the layout and it's displayareas to the collection
			if(includeLayout)
			{				
				result.put( layout, validLayoutDisplayareas );
			}
		}			
		
		if( assetPresentation != null )
		{			
			// If we did not load any layouts (as a result of permissions being applied)
			// Or if we did not add the default layout associated with this asset presentation
			if( result.size() == 0  || result.containsKey( assetPresentation.getLayout() ) == false )
			{
				// Create an entry for the default layout and displayarea of the given assetPresentation
				ArrayList layoutDisplayareas = new ArrayList();		
				layoutDisplayareas.addAll( assetPresentation.getLayout().getLayoutDisplayareas() );
				result.put( assetPresentation.getLayout(), layoutDisplayareas );													
			}		 
		}
				
		// Initialize all layout displayareas before returning to page
		for( Iterator i=result.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry)i.next();
			Layout l = (Layout)( entry.getKey() );
			ArrayList layoutDisplayareas = (ArrayList)( entry.getValue() );			
			for( Iterator j=layoutDisplayareas.iterator(); j.hasNext(); )
			{
				LayoutDisplayarea lda = (LayoutDisplayarea)j.next();
				Hibernate.initialize( lda );
				Hibernate.initialize( lda.getDisplayarea() );				
			}					
		}		
		return result;
	}		
	
	public static LinkedHashMap getLayoutsAndDisplayareas() throws HibernateException
	{		
		return getLayoutsAndDisplayareas( null );
	}
			
	/**
	 * Returns a HashMap of all Layouts and their respective displayareas. 
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static LinkedHashMap getLayoutsAndDisplayareas(AssetPresentation assetPresentation) throws HibernateException
	{		
		LinkedHashMap result = new LinkedHashMap();			
		List layouts = Layout.getLayouts();		
		for( Iterator i = layouts.iterator(); i.hasNext(); )
		{	
			Layout layout = (Layout)i.next();
			Set layoutDisplayareas = layout.getLayoutDisplayareas();
			
			// Only add layouts with one or more displayareas
			if( layoutDisplayareas.size() > 0 ){
				result.put( layout, layoutDisplayareas );
			}
		}			
		
		if( assetPresentation != null )
		{			
			// If we did not load any layouts (as a result of permissions being applied)
			// Or if we did not add the default layout associated with this asset presentation
			if( result.size() == 0  || result.containsKey( assetPresentation.getLayout() ) == false )
			{
				// Create an entry for the default layout and displayarea of the given assetPresentation
				result.put( assetPresentation.getLayout(), assetPresentation.getLayout().getLayoutDisplayareas() );											
			}	
		}
		
		// Initialize all layout displayareas before returning to page
		for( Iterator i=result.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry)i.next();
			Layout l = (Layout)( entry.getKey() );
			Set layoutDisplayareas = (Set)( entry.getValue() );			
			for( Iterator j=layoutDisplayareas.iterator(); j.hasNext(); )
			{
				LayoutDisplayarea lda = (LayoutDisplayarea)j.next();
				Hibernate.initialize( lda );
				Hibernate.initialize( lda.getDisplayarea() );				
			}					
		}			
		return result;
	}			

	/**
	 * Returns true if a layout with the given name already exists in the database
	 * 
	 * @param layoutName
	 * @return
	 */
	public static boolean layoutExists(String layoutName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		List l = session.createCriteria(Layout.class)
					.add( Expression.eq("layoutName", layoutName).ignoreCase() )
					.list();
		return l.size() > 0 ? true : false;
	}	
	
	
	/**
	 * @return Returns the number of layouts
	 */
	public static int getLayoutsCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();	
		Iterator i = session.createQuery("SELECT COUNT(l) FROM Layout as l").iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
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
	private static String buildSearchHql(String layoutNameSearchString, String selectedSearchOption, String searchString,
			String[] selectedSearchOptions, String minNumber, String maxNumber, String minDate, String maxDate, boolean getCount)
	{
		String hql = "";
		
		// If the layoutNameSearchString search string was left blank, use wildcard
		if( layoutNameSearchString == null || layoutNameSearchString.trim().length() == 0 ){
			layoutNameSearchString = "%";
		}
		
		// Imply *
		if(layoutNameSearchString.startsWith("*") == false){
			layoutNameSearchString = "*" + layoutNameSearchString;
		}
		if(layoutNameSearchString.endsWith("*") == false){
			layoutNameSearchString = layoutNameSearchString + "*";
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
		layoutNameSearchString = layoutNameSearchString.replaceAll("\\*", "\\%");	
		layoutNameSearchString = Reformat.oraesc(layoutNameSearchString);		
				
		// If we are counting the number of records
		if( getCount == true) {
			hql = "SELECT COUNT(layout) ";
		} else {
			hql = "SELECT layout ";
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
							hql += "FROM Layout as layout "														
								+ "WHERE layout.layoutId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND attr.value IN (:attrValues) ) ";							
							hql += "AND UPPER(layout.layoutName) LIKE UPPER('"+ layoutNameSearchString +"') "	
								+ "ORDER BY UPPER(layout.layoutName)";									
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
							hql += "FROM Layout as layout "															
								+ "WHERE layout.layoutId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND UPPER(attr.value) LIKE UPPER ('"+ searchString +"') ) ";						
							hql	+= "AND UPPER(layout.layoutName) LIKE UPPER('"+ layoutNameSearchString +"') "	
								+ "ORDER BY UPPER(layout.layoutName)";		
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
						// Get all layouts that have a DateAttr.value between the two dates
						hql += "FROM Layout as layout "															
							+ "WHERE layout.layoutId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM DateAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";			
							hql += "AND UPPER(layout.layoutName) LIKE UPPER('"+ layoutNameSearchString +"') "	
							+ "ORDER BY UPPER(layout.layoutName)";						
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
						// Get all layouts that have a NumberAttr.value between the two dates
						hql += "FROM Layout as layout "															
							+ "WHERE layout.layoutId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM NumberAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";					
						hql	+= "AND UPPER(layout.layoutName) LIKE UPPER('"+ layoutNameSearchString +"') "	
							+ "ORDER BY UPPER(layout.layoutName)";			
					}
				}				
			}
		}
	
		// If we're not excluding the metadata criteria in the query
		if( excludeMetadataCriteria )
		{
			hql += "FROM Layout as layout "																														
				+ "WHERE UPPER(layout.layoutName) LIKE UPPER('"+ layoutNameSearchString +"') ";
			// If we're filtering by last modified date
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) )
			{
				hql +=   " AND layout.layoutId IN "
					+	"(SELECT ei.entityId "
					+	" FROM EntityInstance as ei "	
					+ 	" WHERE ei.entityClass.className = '"+ Layout.class.getName() +"' "
					+	" AND ei.lastModified >= ? "
					+	" AND ei.lastModified <= ?) ";					
			}			
			hql += "ORDER BY UPPER(layout.layoutName)";					
		}
		return hql;
	}		
	
		
	
	/**
	 * Copies this layout and assigns the given new layout name.
	 * 
	 * @param newLayoutName
	 * @return
	 */
	public Long copy(String newLayoutName) throws ClassNotFoundException
	{				
		// First, create a new layout object
		Layout newLayout = new Layout();
		newLayout.setLayoutName( newLayoutName );
		newLayout.setWidth( this.getWidth() );
		newLayout.setHeight( this.getHeight() );
		
		// Save the layout but do not create permission entries since we are going to copy them		
		Long newSegmentId = newLayout.save( false );		
						
		// Second, copy all the layout displayareas		
		for( Iterator i = this.getLayoutDisplayareas().iterator(); i.hasNext(); )
		{
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			LayoutDisplayarea newLda = new LayoutDisplayarea();
			Displayarea displayarea = lda.getDisplayarea();
			
			// If this is not a shared diaplayarea
			if( lda.getDisplayarea().getIsShared() != null && lda.getDisplayarea().getIsShared() == Boolean.FALSE ){
				// Create a new displayarea by copying the non-shared one
				displayarea = Displayarea.createOrUpdate( this, null, displayarea.getDisplayareaName(), displayarea.getWidth(), displayarea.getHeight(), displayarea.getAspectRatio(), displayarea.getAudioChannel(), displayarea.getIsShared(), displayarea.getTriggerable() );
			}
			newLda.setLayout( newLayout );
			newLda.setDisplayarea( displayarea );
			newLda.setXpos( lda.getXpos() );
			newLda.setYpos( lda.getYpos() );
			newLda.setSeqNum( lda.getSeqNum() );
			newLda.save();
		}
					
		// Copy any layout metadata
		HashMap attrDefinitions = AttrDefinition.getAttributeDefinitionMap(this);
		for( Iterator i=attrDefinitions.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry)i.next();
			AttrDefinition ad = (AttrDefinition)entry.getKey();
			List<Attr> attrs = (List<Attr>)entry.getValue();
			
			// If there is an attr object associated with this device
			for(Attr attr : attrs){
				if(attr != null && attr.getValue() != null && attr.getFormattedValue().length() > 0){
					if( attr instanceof StringAttr ){
						StringAttr oldAttr = (StringAttr)attr;
						StringAttr newAttr = new StringAttr();
						newAttr.setAttrDefinition( ad );
						newAttr.setAttrName( oldAttr.getAttrName() );
						newAttr.setAttrType( oldAttr.getAttrType() );
						newAttr.setOwnerId( newLayout.getLayoutId() );
						newAttr.setValue( oldAttr.getValue() );
						newAttr.save();
					}else if( attr instanceof DateAttr ){
						DateAttr oldAttr = (DateAttr)attr;
						DateAttr newAttr = new DateAttr();
						newAttr.setAttrDefinition( ad );
						newAttr.setAttrName( oldAttr.getAttrName() );
						newAttr.setAttrType( oldAttr.getAttrType() );
						newAttr.setOwnerId( newLayout.getLayoutId() );
						newAttr.setValue( oldAttr.getValue() );
						newAttr.save();
					}else if( attr instanceof NumberAttr ){
						NumberAttr oldAttr = (NumberAttr)attr;
						NumberAttr newAttr = new NumberAttr();
						newAttr.setAttrDefinition( ad );
						newAttr.setAttrName( oldAttr.getAttrName() );
						newAttr.setAttrType( oldAttr.getAttrType() );
						newAttr.setOwnerId( newLayout.getLayoutId() );
						newAttr.setValue( oldAttr.getValue() );
						newAttr.save();
					}
				}
			}
		}		
		return newSegmentId;
	}		
	
	/**
	 * Determines whether or not the given displayarea has any asset presentation
	 * objects associated with it. Returns true if it doesn't (and is therefore
	 * removable), or false if it does (and therefore cannot be removed)
	 * 
	 * @param da
	 * @return
	 */
	public boolean isRemovable() throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		Iterator i = session.createCriteria(AssetPresentation.class)
				.add( Expression.eq("layout.layoutId", this.getLayoutId()) )
				.list().iterator();
		return i.hasNext() ? false : true;
	}	
	
	/**
	 * Returns a list of all displayareas that are currently referenced
	 * by one or more default asset presentations and therefore "cannot"
	 * be removed from the layout.
	 * 
	 * @return
	 */
	public List getNonRemovableDisplayareas()
	{
		List result = new ArrayList();
		for( Iterator i=this.getLayoutDisplayareas().iterator(); i.hasNext(); )
		{
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			if( lda.isRemovable() == false ){
				result.add( lda.getDisplayarea() );
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<LayoutDisplayarea> getLayoutDisplayareasSorted()
	{		
		if((layoutDisplayareas != null) && (layoutDisplayareasSorted == null))
		{				
			List<LayoutDisplayarea> l = new LinkedList<LayoutDisplayarea>(layoutDisplayareas);
			BeanPropertyComparator  comparator1 = new BeanPropertyComparator( "displayareaName" );
			BeanPropertyComparator  comparator2 = new BeanPropertyComparator( "displayarea", comparator1 );								
			Collections.sort(l,comparator2);
			layoutDisplayareasSorted = l ;			
		}
		return layoutDisplayareasSorted;
	}	
	
	/**
	 * Returns true or false depending upon whether or not the given displayarea 
	 * can be found in the collection of layout displayareas for this layout.
	 * @param da
	 * @return
	 */
	public boolean containsDisplayarea(Displayarea da)
	{
		boolean result = false;
		for( Iterator<LayoutDisplayarea> i=this.layoutDisplayareas.iterator(); i.hasNext(); )
		{
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			if( lda.getDisplayarea().getDisplayareaId().equals(da.getDisplayareaId()) ){
				result = true;
				break;
			}
		}
		return result;
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{		
		// Build the html according to the properties of this layout 
		StringBuffer result = new StringBuffer();				
		
		// For each displayarea in this layout
		for( Iterator<LayoutDisplayarea> i=this.layoutDisplayareas.iterator(); i.hasNext(); )
		{
			// Build a div tag for this displayarea
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			Displayarea da = lda.getDisplayarea();
			String zIndex = String.valueOf( (this.layoutDisplayareas.size() - lda.getSeqNum()) + 1);

			// NOTE: It would be nice to be able to scale the displayareas down, however
			// we cannot do so without a height and width specified for the entire layout itself			
			result.append("    ");
			result.append("<div style=\"width:"+ da.getWidth().toString() +"px; ");
			result.append("height:"+ da.getHeight().toString() +"px; ");
			result.append("left:"+ lda.getXpos().toString() +"px; ");
			result.append("top:"+ lda.getYpos().toString() +"px; ");
			result.append("border-width:thin; ");
			result.append("border-style:solid; ");
			result.append("position:absolute; ");
			result.append("overflow:hidden; ");	
			result.append("background-color: #CCCCCC; ");
			result.append("z-index:"+ zIndex +"\">");
			result.append( da.getDisplayareaName() +" "+ da.getAspectRatio() );
			result.append( "<br>" );
			result.append( "("+ lda.getXpos() +", "+ lda.getYpos() +") "+ da.getWidth() +"x"+ da.getHeight() );
			result.append("</div>\n");
		}
		return result.toString();			
	}	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof Layout) ) result = false;
		
		Layout c = (Layout) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "Layout".hashCode();
		result = Reformat.getSafeHash( this.getLayoutId().toString(), result, 3 );
		result = Reformat.getSafeHash( this.getLayoutName(), result, 11 );		
		return result < 0 ? -result : result;
	}		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getLayoutId();
	}

	/**
	 * @return Returns the layoutId.
	 */
	public Long getLayoutId() {
		return layoutId;
	}

	/**
	 * @param layoutId The layoutId to set.
	 */
	public void setLayoutId(Long layoutId) {
		this.layoutId = layoutId;
	}

	/**
	 * @return Returns the layoutName.
	 */
	public String getLayoutName() {
		return layoutName;
	}

	/**
	 * @param layoutName The layoutName to set.
	 */
	public void setLayoutName(String layoutName) {
		this.layoutName = layoutName;
	}

	/**
	 * @return Returns the layoutDisplayareas.
	 */
	public Set<LayoutDisplayarea> getLayoutDisplayareas() {			
		return layoutDisplayareas;
	}

	/**
	 * @param layoutDisplayareas The layoutDisplayareas to set.
	 */
	public void setLayoutDisplayareas(Set<LayoutDisplayarea> layoutDisplayareas) {
		this.layoutDisplayareas = layoutDisplayareas;
	}
	/**
	 * @param height The height to set.
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}
	
	/**
	 * @param width The width to set.
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}
	
	/**
	 * @return Returns the height.
	 */
	public Integer getHeight() {
		return height;
	}
	
	/**
	 * @return Returns the width.
	 */
	public Integer getWidth() {
		return width;
	}	
	
}
