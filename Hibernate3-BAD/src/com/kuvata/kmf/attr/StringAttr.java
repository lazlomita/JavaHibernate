package com.kuvata.kmf.attr;

import parkmedia.usertype.AttrType;



/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * Persistent class for table ATTR.
 * 
 * @author Jeff Randesi
 */
public class StringAttr extends Attr implements IStringAttr {
	
	private String value;
	
	/**
	 * Constructor	 
	 */
	public StringAttr()
	{		
		super();
	}
	
	public AttrType getAttrType()
	{
		return AttrType.STRING;
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	

}
