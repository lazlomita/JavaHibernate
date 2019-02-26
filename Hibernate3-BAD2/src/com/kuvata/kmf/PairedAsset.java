package com.kuvata.kmf;

import java.util.List;
import java.util.Vector;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class PairedAsset extends Entity implements HistorizableCollectionMember{

	private Long pairedAssetId;
	private PairedDisplayarea pairedDisplayarea;
	private IAsset asset;
	private Integer seqNum;
	private Float length;	
	/**
	 * 
	 * @throws HibernateException
	 */
	public PairedAsset() throws HibernateException
	{	
	}
	/**
	 * 
	 */
	public void delete() throws HibernateException
	{
		// Remove this object from the parent collections
		this.pairedDisplayarea.getPairedAssets().remove( this );	
		this.asset.getPairedAssets().remove( this );
		super.delete();		
				
		// If we just removed the last paired asset in this paired displayarea
		if( this.pairedDisplayarea.getPairedAssets().size() == 0 )
		{
			// Delete the paired displayarea
			this.pairedDisplayarea.delete();
		}		
	}
	/**
	 * 
	 * @param pairedAssetId
	 * @return
	 * @throws HibernateException
	 */
	public static PairedAsset getPairedAsset(Long pairedAssetId) throws HibernateException
	{
		return (PairedAsset)Entity.load(PairedAsset.class, pairedAssetId);		
	}
	
	public static List<PairedAsset> getPairedAssetFromAsset(Long assetId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		return session.createCriteria(PairedAsset.class)
			  .add(Expression.eq("asset.id", assetId))				 
			  .list();
	}
	
	public static PairedAsset getPairedAssetFromAsset(Long assetId, Integer seqNum) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l = session.createCriteria(PairedAsset.class)
			  .add(Expression.eq("asset.id", assetId))		
			  .add(Expression.eq("seqNum", seqNum))
			  .list();	
		
		PairedAsset pairedAsset = null;
		if (l.size() > 0) {
			pairedAsset = (PairedAsset)l.get(0);
		}
		return pairedAsset;
	}
	
	public static List<PairedAsset> getPairedAssetsFromAssetPresentation(AssetPresentation assetPresentation, Integer minSeqNum, Integer maxSeqNum) throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l = session.createCriteria(PairedAsset.class)
			.createAlias("pairedDisplayarea", "pairedDisplayarea")
			.createAlias("pairedDisplayarea.assetPresentation", "assetPresentation")
			.add(Expression.eq("assetPresentation.id", assetPresentation.getAssetPresentationId()))
			.add(Expression.ge("seqNum", minSeqNum))
			.add(Expression.le("seqNum", maxSeqNum))
			.list();
			
		List<PairedAsset> typedList = new Vector<PairedAsset>();
		for (int i=0; i<l.size(); i++) {
			typedList.add((PairedAsset)l.get(i));
		}
		return typedList;
	}
	
	public static PairedAsset create(PairedDisplayarea pda, Asset asset, String length)
	{
		PairedAsset pa = new PairedAsset();
		pa.setPairedDisplayarea( pda );
		pa.setAsset( asset );
		int numAssets = (pda != null && pda.getPairedAssets() != null) ? pda.getPairedAssets().size() : 0;
		pa.setSeqNum( new Integer(numAssets)  );
		
		// If "Default" length was selected
		if( length.equalsIgnoreCase( Constants.DEFAULT ) ){
			pa.setLength( asset.getAssetPresentation().getLength() );
		}else{
			pa.setLength( Float.valueOf( length ) );	
		}			
		pa.save();
		return pa;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof PairedAsset) ) result = false;
		
		PairedAsset pa = (PairedAsset) other;		
		if(this.hashCode() == pa.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "PairedAsset".hashCode();
		result = Reformat.getSafeHash( this.getPairedDisplayarea().getPairedDisplayareaId(), result, 13 );
		result = Reformat.getSafeHash( this.getAsset().getAssetId(), result, 13 );
		result = Reformat.getSafeHash( this.getSeqNum(), result, 13 );
		return result;
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPairedAssetId();
	}
	
	/**
	 * Return either the playlist or asset associated with this paired asset
	 */
	public Long getHistoryEntityId()
	{
		if( this.getPairedDisplayarea().getAssetPresentation().getPlaylistAsset() != null ) {
			return this.getPairedDisplayarea().getAssetPresentation().getPlaylistAsset().getPlaylist().getPlaylistId();
		}else if (this.getPairedDisplayarea().getAssetPresentation().getAsset() != null ) {
			return this.getPairedDisplayarea().getAssetPresentation().getAsset().getAssetId();
		}
		else {
			return this.getPairedAssetId();
		}
	}		
	
	/**
	 * Prepend the name of the displayarea that this paired asset is associated with
	 */
	public String getEntityName()
	{
		return this.getPairedDisplayarea().getDisplayarea().getDisplayareaName() +": "+ this.getAsset().getAssetName();
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
	 * @return Returns the pairedAssetId.
	 */
	public Long getPairedAssetId() {
		return pairedAssetId;
	}

	/**
	 * @param pairedAssetId The pairedAssetId to set.
	 */
	public void setPairedAssetId(Long pairedAssetId) {
		this.pairedAssetId = pairedAssetId;
	}

	/**
	 * @return Returns the pairedDisplayarea.
	 */
	public PairedDisplayarea getPairedDisplayarea() {
		return pairedDisplayarea;
	}

	/**
	 * @param pairedDisplayarea The pairedDisplayarea to set.
	 */
	public void setPairedDisplayarea(PairedDisplayarea pairedDisplayarea) {
		this.pairedDisplayarea = pairedDisplayarea;
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
