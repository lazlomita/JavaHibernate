package com.kuvata.kmf.attr;

import java.util.Date;

import parkmedia.usertype.AttrType;




/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * Persistent class for table ATTR.
 * 
 * @author Jeff Randesi
 */
public class DateAttr extends Attr implements IDateAttr {
	
	private Date value;
	
	/**
	 * Constructor	 
	 */
	public DateAttr()
	{		
		super();
	}
	
	public AttrType getAttrType()
	{
		return AttrType.DATE;
	}

	/**
	 * @return Returns the value.
	 */
	public Date getValue() {
		return value;
	}
	

	/**
	 * @param value The value to set.
	 */
	public void setValue(Date value) {
		this.value = value;
	}
	
	

}
