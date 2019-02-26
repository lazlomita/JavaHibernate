package com.kuvata.kmf;

import com.kuvata.dispatcher.scheduling.SegmentBlock;

/**
 * Helper object used to render the schedule screen
 * 
 * @author jrandesi
 */
public class ScheduleBlock {
	
	Integer startRow;
	Integer endRow;
	String cssClass;
	String width;
	SegmentBlock segmentBlock;
	String length;
	/**
	 * 
	 *
	 */	
	public ScheduleBlock()
	{		
	}

	/**
	 * @return Returns the cssClass.
	 */
	public String getCssClass() {
		return cssClass;
	}
	/**
	 * @param cssClass The cssClass to set.
	 */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	
	/**
	 * @return Returns the endRow.
	 */
	public Integer getEndRow() {
		return endRow;
	}
	/**
	 * @param endRow The endRow to set.
	 */
	public void setEndRow(Integer endRow) {
		this.endRow = endRow;
	}
	/**
	 * @return Returns the startRow.
	 */
	public Integer getStartRow() {
		return startRow;
	}
	/**
	 * @param startRow The startRow to set.
	 */
	public void setStartRow(Integer startRow) {
		this.startRow = startRow;
	}
	/**
	 * @return Returns the segmentBlock.
	 */
	public SegmentBlock getSegmentBlock() {
		return segmentBlock;
	}
	/**
	 * @param segmentBlock The segmentBlock to set.
	 */
	public void setSegmentBlock(SegmentBlock segmentBlock) {
		this.segmentBlock = segmentBlock;
	}
	/**
	 * @return Returns the width.
	 */
	public String getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(String width) {
		this.width = width;
	}
	
	/**
	 * @return Returns the length.
	 */
	public String getLength() {
		return length;
	}
	/**
	 * @param length The length to set.
	 */
	public void setLength(String length) {
		this.length = length;
	}
}
