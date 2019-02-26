package com.kuvata.kmf;

import com.kuvata.kmf.util.Reformat;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class AssetSegmentPart extends SegmentPart {

	private Long assetSegmentPartId;
	private IAsset asset;
	
	// Device side scheduling variable
	private Float length;
	/**
	 * 
	 *
	 */
	public AssetSegmentPart()
	{		
	}
	/**
	 * 
	 */
	public float getLength(){
		if(length != null){
			// This is always set on the device side
			return length;
		}else{
			return asset.getAssetPresentation().getLength().floatValue();
		}
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof AssetSegmentPart) ) result = false;
		else{
			AssetSegmentPart a = (AssetSegmentPart) other;		
			if(this.hashCode() == a.hashCode())
				result =  true;
		}
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "AssetSegmentPart".hashCode();
		result = Reformat.getSafeHash( this.getAssetSegmentPartId(), result, 3 );
		result = Reformat.getSafeHash( this.getAsset().getAssetId(), result, 7 );
		result = Reformat.getSafeHash( this.getSeqNum(), result, 11 );		
		return result;
	}		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAssetSegmentPartId();
	}
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getAsset().getAssetName();
	}		

	/**
	 * @return Returns the asset.
	 */
	public IAsset getAsset() {
		return asset;
	}

	/**
	 * @param asset The asset to set.
	 */
	public void setAsset(IAsset asset) {
		this.asset = asset;
	}

	/**
	 * @return Returns the assetSegmentPartId.
	 */
	public Long getAssetSegmentPartId() {
		return assetSegmentPartId;
	}

	/**
	 * @param assetSegmentPartId The assetSegmentPartId to set.
	 */
	public void setAssetSegmentPartId(Long assetSegmentPartId) {
		this.assetSegmentPartId = assetSegmentPartId;
	}
	public void setLength(Float length) {
		this.length = length;
	}

}
