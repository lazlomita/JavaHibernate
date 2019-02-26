package com.kuvata.kmf;

import com.kuvata.kmf.usertype.AssetType;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class DisplayareaAssetType implements HistorizableCollectionMember
{
	private Displayarea displayarea;
	private AssetType assetType;
	/**
	 * 
	 *
	 */
	public DisplayareaAssetType()
	{		
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		// The displayarea_asset_type table does not have a primary key
		// Therefore we must create a composite key in order to ensure a unique id (for logging purposes)
		int result = 0;		
		result = Reformat.getSafeHash( this.getDisplayarea().getDisplayareaId().hashCode(), result, 13 );
		result = Reformat.getSafeHash( this.getAssetType().hashCode(), result, 29 );
		return  Long.valueOf( result );
	}	
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getDisplayarea().getDisplayareaId();
	}	
	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getAssetType().getName();
	}		
		
	/**
	 * @return Returns the displayarea.
	 */
	public Displayarea getDisplayarea() {
		return displayarea;
	}

	/**
	 * @param displayarea The displayarea to set.
	 */
	public void setDisplayarea(Displayarea displayarea) {
		this.displayarea = displayarea;
	}

	/**
	 * @return Returns the assetType.
	 */
	public AssetType getAssetType() {
		return assetType;
	}

	/**
	 * @param assetType The assetType to set.
	 */
	public void setAssetType(AssetType assetType) {
		this.assetType = assetType;
	}

}
