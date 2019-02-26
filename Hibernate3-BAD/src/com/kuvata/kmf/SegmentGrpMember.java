package com.kuvata.kmf;

import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class SegmentGrpMember extends GrpMember
{
	private Long segmentGrpMemberId;
	private Segment segment;	
	/**
	 * 
	 *
	 */	
	public SegmentGrpMember()
	{		
	}
	
	/**
	 * Implements the abstract method GrpMember.getName()
	 */	
	public String getName()
	{
		return this.getSegment().getSegmentName();
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof SegmentGrpMember) ) result = false;
		
		SegmentGrpMember sgm = (SegmentGrpMember) other;		
		if(this.hashCode() == sgm.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "SegmentGrpMember".hashCode();
		result = Reformat.getSafeHash( this.getGrp().getGrpId(), result, 13 );
		result = Reformat.getSafeHash( this.getSegment().getSegmentId(), result, 13 );
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
		return this.getSegment();
	}
	
	/**
	 * @return Returns the segment.
	 */
	public Segment getSegment() {
		return segment;
	}

	/**
	 * @param segment The segment to set.
	 */
	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	/**
	 * @return Returns the segmentGrpMemberId.
	 */
	public Long getSegmentGrpMemberId() {
		return segmentGrpMemberId;
	}

	/**
	 * @param segmentGrpMemberId The segmentGrpMemberId to set.
	 */
	public void setSegmentGrpMemberId(Long segmentGrpMemberId) {
		this.segmentGrpMemberId = segmentGrpMemberId;
	}

}
