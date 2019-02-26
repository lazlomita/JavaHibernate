package com.kuvata.kmf.attr;

import com.kuvata.kmf.usertype.AttrType;



/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * Persistent class for table ATTR.
 * 
 * @author Jeff Randesi
 */
public class NumberAttr extends Attr implements INumberAttr {
	
	private Float value;
	
	/**
	 * Constructor	 
	 */
	public NumberAttr()
	{		
		super();
	}
	
	public AttrType getAttrType()
	{
		return AttrType.NUMBER;
	}

	/**
	 * @return Returns the value.
	 */
	public Float getValue() {
		return value;
	}
	

	/**
	 * @param value The value to set.
	 */
	public void setValue(Float value) {
		this.value = value;
	}
	
	

}
