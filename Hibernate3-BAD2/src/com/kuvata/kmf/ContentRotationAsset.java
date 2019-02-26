package com.kuvata.kmf;

import org.hibernate.HibernateException;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class ContentRotationAsset extends PersistentEntity implements HistorizableCollectionMember {

	private Long contentRotationAssetId;
	private ContentRotation contentRotation;
	private IAsset asset;
	private Integer seqNum;
	private Float length;
	private Boolean variableLength;
	
	/**
	 * 
	 *
	 */
	public ContentRotationAsset()
	{		
	}
	/**
	 * 
	 * @param contentRotationAssetId
	 * @return
	 * @throws HibernateException
	 */
	public static ContentRotationAsset getContentRotationAsset(Long contentRotationAssetId) throws HibernateException
	{
		return (ContentRotationAsset)PersistentEntity.load(ContentRotationAsset.class, contentRotationAssetId);		
	}
	
	/**
	 * Creates a new contentRotationAsset object.
	 * 
	 * @param playlist
	 * @param asset
	 * @param length
	 * @param copyPairedAssets
	 * @return
	 */
	public static ContentRotationAsset create(ContentRotation contentRotation, Asset asset, String length, Boolean variableLength, boolean updateContentRotation)
	{				
		// Create a content rotation asset object for the selected asset
		ContentRotationAsset cra = new ContentRotationAsset();			
		int seqNum = contentRotation.getContentRotationAssets().size();
		cra.setAsset( asset );
		cra.setContentRotation( contentRotation );			
		cra.setSeqNum( new Integer(seqNum) );
		cra.setVariableLength(variableLength);
		
		// If "DEFAULT" was submitted, use the length of the selected asset
		if( length.equalsIgnoreCase( Constants.DEFAULT ) ){
			cra.setLength( asset.getAssetPresentation().getLength() );
		} else {
			cra.setLength( new Float( length ) );	
		}
		
		// Add to list before saving for history purposes
		contentRotation.getContentRotationAssets().add( cra );
		cra.save();
		
		if(updateContentRotation){
			contentRotation.update();
		}
		
		return cra;
	}	
	
	/**
	 * 
	 */
	public void delete() throws HibernateException
	{
		// Remove this object from the parent collection
		this.contentRotation.getContentRotationAssets().remove( this );	
		this.asset.getContentRotationAssets().remove( this );
		super.delete();
	}
	
	/**
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof ContentRotationAsset) ) result = false;
		
		ContentRotationAsset c = (ContentRotationAsset) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "ContentRotationAsset".hashCode();
		result = Reformat.getSafeHash( this.getContentRotation().getContentRotationId(), result, 29 );
		result = Reformat.getSafeHash( this.getAsset().getAssetId(), result, 31 );
		result = Reformat.getSafeHash( this.getLength(), result, 37 );
		result = Reformat.getSafeHash( this.getSeqNum(), result, 39 );		
		return result;
	}	
	/**
	 *  (non-Javadoc)
	 * @see com.kuvata.kmf.Entity#getEntityId()
	 */
	public Long getEntityId()
	{
		return this.getContentRotationAssetId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getContentRotation().getContentRotationId();
	}		
	
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
	 * @return Returns the contentRotationAssetId.
	 */
	public Long getContentRotationAssetId() {
		return contentRotationAssetId;
	}

	/**
	 * @param contentRotationAssetId The contentRotationAssetId to set.
	 */
	public void setContentRotationAssetId(Long contentRotationAssetId) {
		this.contentRotationAssetId = contentRotationAssetId;
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
	
	/**
	 * @return Returns the length.
	 */
	public Float getLength() {
		return length;
	}
	/**
	 * @param length The length to set.
	 */
	public void setLength(Float length) {
		this.length = length;
	}
	/**
	 * @return the variableLength
	 */
	public Boolean getVariableLength() {
		return variableLength;
	}
	/**
	 * @param variableLength the variableLength to set
	 */
	public void setVariableLength(Boolean variableLength) {
		this.variableLength = variableLength;
	}
	
}
