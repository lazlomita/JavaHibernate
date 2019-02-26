package com.kuvata.kmf;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * Persistent class for table GrpMember
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public abstract class GrpMember extends Entity implements HistorizableCollectionMember
{
	private Long grpMemberId;
	private Grp grp;	
		
	/**
	 * Constructor
	 */
	public GrpMember()
	{		
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof GrpMember) ) result = false;
		
		GrpMember gm = (GrpMember) other;		
		if(this.hashCode() == gm.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "GrpMember".hashCode();
		result = Reformat.getSafeHash( this.getGrp().getGrpId(), result, 13 );
		result = Reformat.getSafeHash( this.getGrpMemberId(), result, 13 );
		return result;
	}	
	
	/**
	 * Implementation of the inherited abstract method Entity.getEntityId().
	 * Returns the grpMemberId.
	 */	
	public Long getEntityId()
	{
		return this.getGrpMemberId();
	}
	
	public String getEntityName()
	{
		return this.getName();
	}		
	
	/**
	 * 
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Implementation of the inherited abstract method Groupable.getChild().
	 * returns the childGrp.
	 */	
	public abstract Entity getChild();

	/**
	 * @return Returns the grpMemberId.
	 */
	public Long getGrpMemberId() {
		return grpMemberId;
	}

	/**
	 * @param grpMemberId The grpMemberId to set.
	 */
	public void setGrpMemberId(Long grpMemberId) {
		this.grpMemberId = grpMemberId;
	}

	/**
	 * @return Returns the grp.
	 */
	public Grp getGrp() {
		return grp;
	}

	/**
	 * @param grp The grp to set.
	 */
	public void setGrp(Grp grp) {
		this.grp = grp;
	}

}
