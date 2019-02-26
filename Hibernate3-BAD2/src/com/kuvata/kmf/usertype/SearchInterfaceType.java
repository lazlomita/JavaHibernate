package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;


/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class SearchInterfaceType extends PersistentStringEnum 
{	
	public static final SearchInterfaceType INPUT_BOX = new SearchInterfaceType("Input Box", "Input Box", true);	
	public static final SearchInterfaceType MEMO = new SearchInterfaceType("Memo", "Memo", true);	
	public static final SearchInterfaceType MULTI_SELECT = new SearchInterfaceType("Multi-select", "Multi-select", true);
	public static final SearchInterfaceType DATE_PICKER = new SearchInterfaceType("Date Picker", "Date Picker", false);
	
	public static final Map INSTANCES = new HashMap();
	private boolean isSelectable;
			    
	static
	{
		INSTANCES.put(INPUT_BOX.toString(), INPUT_BOX);
		INSTANCES.put(MEMO.toString(), MEMO);
		INSTANCES.put(MULTI_SELECT.toString(), MULTI_SELECT);
		INSTANCES.put(DATE_PICKER.toString(), DATE_PICKER);
	}
	
	public SearchInterfaceType() {}

	public SearchInterfaceType(String name, String persistentValue, boolean isSelectable) {
		super(name, persistentValue);
		
		// If this search interface type should be selectable in the user interface
		this.isSelectable = isSelectable;
	}
	
	public String toString()
	{
		return this.name;
	}
	
	public String getSearchInterfaceTypeName()
	{
		return this.name;
	}

	/**
	 * 
	 * @param searchToolTypeName
	 * @return
	 */
	public static SearchInterfaceType getSearchInterfaceType(String searchToolTypePersistentValue)
	{
		for( Iterator i=INSTANCES.values().iterator(); i.hasNext(); )
		{
			SearchInterfaceType searchToolType = (SearchInterfaceType)i.next();
			if( searchToolType.getPersistentValue().equalsIgnoreCase( searchToolTypePersistentValue ) ){
				return searchToolType;
			}
		}
		return null;
	}		
	
	/**
	 * Returns a list of all search interface types that are able to be selected in the UI
	 * @return
	 */
	public static List getSelectableSearchInterfaceTypes()
	{
		List l = new LinkedList();
		Iterator i = SearchInterfaceType.INSTANCES.values().iterator();
		while(i.hasNext()) {
			SearchInterfaceType sit = (SearchInterfaceType)i.next();
			if( sit.isSelectable() ){
				l.add( sit );
			}
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);		
		return l;
	}

	/**
	 * @return Returns the isSelectable.
	 */
	public boolean isSelectable() {
		return isSelectable;
	}
	

	/**
	 * @param isSelectable The isSelectable to set.
	 */
	public void setSelectable(boolean isSelectable) {
		this.isSelectable = isSelectable;
	}
		
	
}
