package com.kuvata.kmf;

import com.kuvata.kmf.util.Reformat;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * Persistent class for table GrpGrpMember.
 * 
 * @author Jeff Randesi
 */
public class GrpGrpMember extends GrpMember
{
	private Long grpGrpMemberId;
	private Grp childGrp;	
		
	/**
	 * Constructor
	 */
	public GrpGrpMember()
	{		
	}
	
	/**
	 * Implements the abstract method GrpMember.getName()
	 */	
	public String getName()
	{
		return this.getChildGrp().getGrpName();
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof GrpGrpMember) ) result = false;
		
		GrpGrpMember ggm = (GrpGrpMember) other;		
		if(this.hashCode() == ggm.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "GrpGrpMember".hashCode();
		result = Reformat.getSafeHash( this.getChildGrp().getGrpId(), result, 13 );
		result = Reformat.getSafeHash( this.getGrpMemberId(), result, 13 );
		return result;
	}		
	
	/**
	 * Implementation of the inherited abstract method Entity.getEntityId().
	 * Returns the grpGrpMemberId.
	 */
	public Long getEntityId()
	{
		return super.getEntityId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getEntityId();
	}		
	
	/**
	 * Implementation of the inherited abstract method Groupable.getChild().
	 * returns the childGrp.
	 */
	public Entity getChild()
	{
		return this.getChildGrp();
	}
	
	/**
	 * @return Returns the childGrp.
	 */
	public Grp getChildGrp() {
		return childGrp;
	}

	/**
	 * @param childGrp The childGrp to set.
	 */
	public void setChildGrp(Grp childGrp) {
		this.childGrp = childGrp;
	}

	/**
	 * @return Returns the grpGrpMemberId.
	 */
	public Long getGrpGrpMemberId() {
		return grpGrpMemberId;
	}

	/**
	 * @param grpGrpMemberId The grpGrpMemberId to set.
	 */
	public void setGrpGrpMemberId(Long grpGrpMemberId) {
		this.grpGrpMemberId = grpGrpMemberId;
	}

}
