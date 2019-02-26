package com.kuvata.kmf;

import java.util.Date;

public class PlaybackEventAggregation 
{
	private Long playbackEventAggregationId;
	private Long deviceId;
	private String deviceName;
	private Long assetId;
	private String assetName;
	private Double airingLength;
	private Double displayAiringLength;
	private Date startDatetime;
	private Integer displaysCount;
	private Integer displayExceptionsCount;
	private Integer clickCount;
	private Integer numAirings;
	
	public Long getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public Long getAssetId() {
		return assetId;
	}
	public void setAssetId(Long assetId) {
		this.assetId = assetId;
	}
	public String getAssetName() {
		return assetName;
	}
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	public Date getStartDatetime() {
		return startDatetime;
	}
	public void setStartDatetime(Date startDatetime) {
		this.startDatetime = startDatetime;
	}
	public Integer getDisplaysCount() {
		return displaysCount;
	}
	public void setDisplaysCount(Integer displaysCount) {
		this.displaysCount = displaysCount;
	}
	public Integer getDisplayExceptionsCount() {
		return displayExceptionsCount;
	}
	public void setDisplayExceptionsCount(Integer displayExceptionsCount) {
		this.displayExceptionsCount = displayExceptionsCount;
	}
	public Integer getClickCount() {
		return clickCount;
	}
	public void setClickCount(Integer clickCount) {
		this.clickCount = clickCount;
	}
	public Integer getNumAirings() {
		return numAirings;
	}
	public void setNumAirings(Integer numAirings) {
		this.numAirings = numAirings;
	}

	public Double getAiringLength() {
		return airingLength;
	}

	public void setAiringLength(Double airingLength) {
		this.airingLength = airingLength;
	}

	public Double getDisplayAiringLength() {
		return displayAiringLength;
	}

	public void setDisplayAiringLength(Double displayAiringLength) {
		this.displayAiringLength = displayAiringLength;
	}

	public Long getPlaybackEventAggregationId() {
		return playbackEventAggregationId;
	}

	public void setPlaybackEventAggregationId(Long playbackEventAggregationId) {
		this.playbackEventAggregationId = playbackEventAggregationId;
	}

}
