/*
 * Created on Nov 11, 2004
 */
package com.kuvata.kmf;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
/**
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class ContentScheduleEvent 
{
	/*
	 * A ContentScheduleEvent can represent a Segment, Playlist, or Asset
	 * scheduling event onto a Device.
	 * 
	 * The table is completely denormalized and should have no foreign keys
	 * to the actual segment, playlist, or asset tables, so that even if assets,
	 * playlists, or segments are modified or deleted, there is a record of how
	 * they appeared at scheduling time.
	 */
	private Long contentScheduleEventId;
	private Long deviceId;
	private String deviceName;
	private Long segmentId;			// to support SegmentBlock
	private String segmentName;			// to support SegmentBlock
	private Long playlistId;
	private String playlistName;
	private Float playlistLength;	
	private Long assetId;
	private String assetName;
	private Float assetLength;
	private Long layoutId;
	private String layoutName;
	private Long displayareaId;
	private String displayareaName;
	private Date startDatetime;
	private Date endDatetime;
	private Float offsetIntoSegment;				// to support SegmentBlock
	private Float offsetIntoPlaylist;
	private Float offsetIntoAsset;
	private Float segmentBlockLength;
	private Integer continuationPart;	// to support SegmentBlock
	private String origin;
	private Integer displaysCount;
	private Integer displayExceptionsCount;
	private String clickUrl;
	
	/**
	 * 
	 *
	 */
	public ContentScheduleEvent() 
	{
	}
	/**
	 * 
	 */
	public static List getContentScheduleEvents(Device device, Long selectedAssetsSelectionId, Date startDatetime, Date endDatetime) throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT pe FROM ContentScheduleEvent as pe WHERE pe.deviceId = :deviceId AND pe.startDatetime >= :startDatetime AND pe.startDatetime < :endDatetime";
		if(selectedAssetsSelectionId != null){
			hql += " AND pe.assetId IN (SELECT entityId FROM SelectedEntities WHERE selectionId = :selectionId )";
		}
		hql += " ORDER BY pe.startDatetime";
		Query q = session.createQuery(hql);
		q.setParameter("deviceId", device.getDeviceId()).setParameter("startDatetime", startDatetime).setParameter("endDatetime", endDatetime);
		if(selectedAssetsSelectionId != null){
			q.setParameter("selectionId", selectedAssetsSelectionId);
		}
		return q.list();
	}
	/**
	 * 
	 */
	public static ContentScheduleEvent getContentScheduleEvent(Device device, Date endDatetime) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		List l = session.createCriteria(ContentScheduleEvent.class)
				.add( Expression.eq("deviceId", device.getDeviceId()) )
				.add( Expression.eq("endDatetime", endDatetime))
				.list();
		return l.size() > 0 ? (ContentScheduleEvent)l.get(0) : null;
	}
	
	/**
	 * @return Returns the assetId.
	 */
	public Long getAssetId() {
		return assetId;
	}
	/**
	 * @param assetId The assetId to set.
	 */
	public void setAssetId(Long assetId) {
		this.assetId = assetId;
	}
	/**
	 * @return Returns the assetLength.
	 */
	public Float getAssetLength() {
		return assetLength;
	}
	/**
	 * @param assetLength The assetLength to set.
	 */
	public void setAssetLength(Float assetLength) {
		this.assetLength = assetLength;
	}
	/**
	 * @return Returns the assetName.
	 */
	public String getAssetName() {
		return assetName;
	}
	/**
	 * @param assetName The assetName to set.
	 */
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	/**
	 * @return Returns the contentScheduleEventId.
	 */
	public Long getContentScheduleEventId() {
		return contentScheduleEventId;
	}
	/**
	 * @param contentScheduleEventId The contentScheduleEventId to set.
	 */
	public void setContentScheduleEventId(Long contentScheduleEventId) {
		this.contentScheduleEventId = contentScheduleEventId;
	}
	/**
	 * @return Returns the continuationPart.
	 */
	public Integer getContinuationPart() {
		return continuationPart;
	}
	/**
	 * @param continuationPart The continuationPart to set.
	 */
	public void setContinuationPart(Integer continuationPart) {
		this.continuationPart = continuationPart;
	}
	/**
	 * @return Returns the deviceId.
	 */
	public Long getDeviceId() {
		return deviceId;
	}
	/**
	 * @param deviceId The deviceId to set.
	 */
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	/**
	 * @return Returns the deviceName.
	 */
	public String getDeviceName() {
		return deviceName;
	}
	/**
	 * @param deviceName The deviceName to set.
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	/**
	 * @return Returns the displayareaId.
	 */
	public Long getDisplayareaId() {
		return displayareaId;
	}
	/**
	 * @param displayareaId The displayareaId to set.
	 */
	public void setDisplayareaId(Long displayareaId) {
		this.displayareaId = displayareaId;
	}
	/**
	 * @return Returns the displayareaName.
	 */
	public String getDisplayareaName() {
		return displayareaName;
	}
	/**
	 * @param displayareaName The displayareaName to set.
	 */
	public void setDisplayareaName(String displayareaName) {
		this.displayareaName = displayareaName;
	}
	/**
	 * @return Returns the endDatetime.
	 */
	public Date getEndDatetime() {
		return endDatetime;
	}
	/**
	 * @param endDatetime The endDatetime to set.
	 */
	public void setEndDatetime(Date endDatetime) {
		this.endDatetime = endDatetime;
	}
	/**
	 * @return Returns the layoutId.
	 */
	public Long getLayoutId() {
		return layoutId;
	}
	/**
	 * @param layoutId The layoutId to set.
	 */
	public void setLayoutId(Long layoutId) {
		this.layoutId = layoutId;
	}
	/**
	 * @return Returns the layoutName.
	 */
	public String getLayoutName() {
		return layoutName;
	}
	/**
	 * @param layoutName The layoutName to set.
	 */
	public void setLayoutName(String layoutName) {
		this.layoutName = layoutName;
	}
	/**
	 * @return Returns the offsetIntoAsset.
	 */
	public Float getOffsetIntoAsset() {
		return offsetIntoAsset;
	}
	/**
	 * @param offsetIntoAsset The offsetIntoAsset to set.
	 */
	public void setOffsetIntoAsset(Float offsetIntoAsset) {
		this.offsetIntoAsset = offsetIntoAsset;
	}
	/**
	 * @return Returns the offsetIntoPlaylist.
	 */
	public Float getOffsetIntoPlaylist() {
		return offsetIntoPlaylist;
	}
	/**
	 * @param offsetIntoPlaylist The offsetIntoPlaylist to set.
	 */
	public void setOffsetIntoPlaylist(Float offsetIntoPlaylist) {
		this.offsetIntoPlaylist = offsetIntoPlaylist;
	}
	/**
	 * @return Returns the offsetIntoSegment.
	 */
	public Float getOffsetIntoSegment() {
		return offsetIntoSegment;
	}
	/**
	 * @param offsetIntoSegment The offsetIntoSegment to set.
	 */
	public void setOffsetIntoSegment(Float offsetIntoSegment) {
		this.offsetIntoSegment = offsetIntoSegment;
	}
	/**
	 * @return Returns the origin.
	 */
	public String getOrigin() {
		return origin;
	}
	/**
	 * @param origin The origin to set.
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	/**
	 * @return Returns the playlistId.
	 */
	public Long getPlaylistId() {
		return playlistId;
	}
	/**
	 * @param playlistId The playlistId to set.
	 */
	public void setPlaylistId(Long playlistId) {
		this.playlistId = playlistId;
	}
	/**
	 * @return Returns the playlistLength.
	 */
	public Float getPlaylistLength() {
		return playlistLength;
	}
	/**
	 * @param playlistLength The playlistLength to set.
	 */
	public void setPlaylistLength(Float playlistLength) {
		this.playlistLength = playlistLength;
	}
	/**
	 * @return Returns the playlistName.
	 */
	public String getPlaylistName() {
		return playlistName;
	}
	/**
	 * @param playlistName The playlistName to set.
	 */
	public void setPlaylistName(String playlistName) {
		this.playlistName = playlistName;
	}
	/**
	 * @return Returns the segmentBlockLength.
	 */
	public Float getSegmentBlockLength() {
		return segmentBlockLength;
	}
	/**
	 * @param segmentBlockLength The segmentBlockLength to set.
	 */
	public void setSegmentBlockLength(Float segmentBlockLength) {
		this.segmentBlockLength = segmentBlockLength;
	}
	/**
	 * @return Returns the segmentId.
	 */
	public Long getSegmentId() {
		return segmentId;
	}
	/**
	 * @param segmentId The segmentId to set.
	 */
	public void setSegmentId(Long segmentId) {
		this.segmentId = segmentId;
	}
	/**
	 * @return Returns the segmentName.
	 */
	public String getSegmentName() {
		return segmentName;
	}
	/**
	 * @param segmentName The segmentName to set.
	 */
	public void setSegmentName(String segmentName) {
		this.segmentName = segmentName;
	}
	/**
	 * @return Returns the startDatetime.
	 */
	public Date getStartDatetime() {
		return startDatetime;
	}
	/**
	 * @param startDatetime The startDatetime to set.
	 */
	public void setStartDatetime(Date startDatetime) {
		this.startDatetime = startDatetime;
	}
	/**
	 * @return the displaysCount
	 */
	public Integer getDisplaysCount() {
		return displaysCount;
	}
	/**
	 * @param displaysCount the displaysCount to set
	 */
	public void setDisplaysCount(Integer displaysCount) {
		this.displaysCount = displaysCount;
	}
	/**
	 * @return the displayExceptionsCount
	 */
	public Integer getDisplayExceptionsCount() {
		return displayExceptionsCount;
	}
	/**
	 * @param displayExceptionsCount the displayExceptionsCount to set
	 */
	public void setDisplayExceptionsCount(Integer displayExceptionsCount) {
		this.displayExceptionsCount = displayExceptionsCount;
	}
	/**
	 * @return the clickUrl
	 */
	public String getClickUrl() {
		return clickUrl;
	}
	/**
	 * @param clickUrl the clickUrl to set
	 */
	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
	}
}
