package com.kuvata.kmf.usertype;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.reports.Report;
import com.kuvata.kmf.usertype.PersistentStringEnum;

public class OperatorType extends PersistentStringEnum{
	public static final OperatorType EQUAL_TO = new OperatorType("=", "=");
	public static final OperatorType NOT_EQUAL_TO = new OperatorType("not =", "!=");
	public static final OperatorType GREATER_THAN = new OperatorType(">", ">");
	public static final OperatorType LESS_THAN = new OperatorType("<", "<");
	public static final OperatorType GREATER_THAN_EQUAL_TO = new OperatorType(">=", ">=");
	public static final OperatorType LESS_THAN_EQUAL_TO = new OperatorType("<=", "<=");
	public static final Map INSTANCES = new LinkedHashMap();	

	static
	{
		INSTANCES.put(EQUAL_TO.toString(), EQUAL_TO);
		INSTANCES.put(NOT_EQUAL_TO.toString(), NOT_EQUAL_TO);
		INSTANCES.put(GREATER_THAN.toString(), GREATER_THAN);
		INSTANCES.put(LESS_THAN.toString(), LESS_THAN);
		INSTANCES.put(GREATER_THAN_EQUAL_TO.toString(), GREATER_THAN_EQUAL_TO);
		INSTANCES.put(LESS_THAN_EQUAL_TO.toString(), LESS_THAN_EQUAL_TO);
	}
	
	public OperatorType(){}
	
	protected OperatorType(String name, String persistentValue) {
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
	
	public static List<OperatorType> getOperatorTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext())
		{
			l.add(i.next());
		}
		
		return l;
	}
	
	public static OperatorType getOperatorType(String dateTypeName)
	{
		return (OperatorType) INSTANCES.get( dateTypeName );
	}
}
