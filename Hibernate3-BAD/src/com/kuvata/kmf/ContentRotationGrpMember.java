package com.kuvata.kmf;

import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class ContentRotationGrpMember extends GrpMember
{
	private Long contentRotationGrpMemberId;
	private ContentRotation contentRotation;	
	/**
	 * 
	 *
	 */	
	public ContentRotationGrpMember()
	{		
	}
	
	/**
	 * Implements the abstract method GrpMember.getName()
	 */	
	public String getName()
	{
		return this.getContentRotation().getContentRotationName();
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof ContentRotationGrpMember) ) result = false;
		
		ContentRotationGrpMember sgm = (ContentRotationGrpMember) other;		
		if(this.hashCode() == sgm.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "ContentRotationGrpMember".hashCode();
		result = Reformat.getSafeHash( this.getGrp().getGrpId(), result, 29 );
		result = Reformat.getSafeHash( this.getContentRotation().getContentRotationId(), result, 31 );		
		return result;
	}	
	/**
	 * 
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
		return this.getGrp().getGrpId();
	}		
	/**
	 * 
	 */
	public Entity getChild()
	{
		return this.getContentRotation();
	}

	/**
	 * @return Returns the contentRotation.
	 */
	public ContentRotation getContentRotation() {
		return contentRotation;
	}
	

	/**
	 * @param contentRotation The contentRotation to set.
	 */
	public void setContentRotation(ContentRotation contentRotation) {
		this.contentRotation = contentRotation;
	}
	

	/**
	 * @return Returns the contentRotationGrpMemberId.
	 */
	public Long getContentRotationGrpMemberId() {
		return contentRotationGrpMemberId;
	}
	

	/**
	 * @param contentRotationGrpMemberId The contentRotationGrpMemberId to set.
	 */
	public void setContentRotationGrpMemberId(Long contentRotationGrpMemberId) {
		this.contentRotationGrpMemberId = contentRotationGrpMemberId;
	}
	
	


}
