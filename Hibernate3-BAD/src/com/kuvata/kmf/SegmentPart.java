package com.kuvata.kmf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.kuvata.kmf.logging.HistorizableCollectionMember;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public abstract class SegmentPart extends Entity implements HistorizableCollectionMember{

	private Long segmentPartId;
	private Segment segment;
	private Integer seqNum;
	/**
	 * 
	 *
	 */
	public SegmentPart()
	{		
	}
	/**
	 * 
	 * @return
	 */
	public abstract float getLength();
	/**
	 * 
	 */
	
	/*
	 * This method checks that all the parts are in the source list.
	 * As these parts are not being read from the database, they don't
	 * have segmentPartIds due to which we need to remove segment partIds
	 * from the source list.
	 */
	public static boolean arePartsInSource(List source, List parts) throws Exception{
		
		ArrayList sourceCopy = new ArrayList();
		
		for( Iterator i=source.iterator(); i.hasNext(); ){
			SegmentPart segmentPart = (SegmentPart)i.next();
			SegmentPart sp = null;
			if( segmentPart instanceof AssetSegmentPart )
				sp = new AssetSegmentPart();
			else if( segmentPart instanceof PlaylistSegmentPart )
				sp = new PlaylistSegmentPart();
			// We want to prevent the object changes from being persisted
			// so we make a copy of the segment part
			PropertyUtils.copyProperties(sp, segmentPart);
			sp.setSegmentPartId(null);
			sourceCopy.add(sp);
		}
		
		for(Iterator i=parts.iterator();i.hasNext();){
			SegmentPart segmentPart = (SegmentPart)i.next();
			if( segmentPart instanceof AssetSegmentPart ){
				AssetSegmentPart asp = (AssetSegmentPart)segmentPart;
				if( sourceCopy.contains( asp ) )
					// Remove the part from source to make sure its not
					// compared to another asset
					sourceCopy.remove(asp);
				else
					return false;
			}else if( segmentPart instanceof PlaylistSegmentPart ){
				PlaylistSegmentPart psp = (PlaylistSegmentPart)segmentPart;
				if( sourceCopy.contains( psp ) )
					// Remove the part from source to make sure its not
					// compared to another asset
					sourceCopy.remove(psp);
				else
					return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getSegment().getSegmentId();
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
	 * @return Returns the segmentPartId.
	 */
	public Long getSegmentPartId() {
		return segmentPartId;
	}

	/**
	 * @param segmentPartId The segmentPartId to set.
	 */
	public void setSegmentPartId(Long segmentPartId) {
		this.segmentPartId = segmentPartId;
	}

	/**
	 * @return Returns the seqNum.
	 */
	public Integer getSeqNum() {
		return seqNum;
	}

	/**
	 * @param seqNum The seqNum to set.
	 */
	public void setSeqNum(Integer seqNum) {
		this.seqNum = seqNum;
	}

}
