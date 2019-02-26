package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class DateType extends PersistentStringEnum{
	public static final DateType CURRENT_DAY = new DateType("Current Day", "Current Day");
	public static final DateType DAYS_AGO = new DateType("Days Ago", "Days Ago");
	public static final DateType MONTH_TO_DATE = new DateType("Month to Date", "Month to Date");
	public static final DateType PREVIOUS_MONTH = new DateType("Previous Month", "Previous Month");
	public static final DateType NEXT_MONTH = new DateType("Next Month", "Next Month");
	public static final DateType SELECTED_DATE = new DateType("Selected Date", "Selected Date");
	public static final Map INSTANCES = new LinkedHashMap();	

	static
	{
		INSTANCES.put(CURRENT_DAY.toString(), CURRENT_DAY);
		INSTANCES.put(DAYS_AGO.toString(), DAYS_AGO);
		INSTANCES.put(MONTH_TO_DATE.toString(), MONTH_TO_DATE);
		INSTANCES.put(PREVIOUS_MONTH.toString(), PREVIOUS_MONTH);
		INSTANCES.put(NEXT_MONTH.toString(), NEXT_MONTH);
		INSTANCES.put(SELECTED_DATE.toString(), SELECTED_DATE);
	}
	
	public DateType(){}
	
	protected DateType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString()
	{
		return this.name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public static List<DateType> getDateTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext())
		{
			l.add(i.next());
		}
		
		return l;
	}
	
	public static DateType getDateType(String dateTypeName)
	{
		return (DateType) INSTANCES.get( dateTypeName );
	}
}
