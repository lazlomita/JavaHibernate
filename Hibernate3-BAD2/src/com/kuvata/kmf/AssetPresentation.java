package com.kuvata.kmf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.HistorizableChildEntity;
import com.kuvata.kmf.util.Reformat;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class AssetPresentation extends PersistentEntity implements HistorizableChildEntity {

	private Long assetPresentationId;
	private Float length;
	private Displayarea displayarea;
	private Layout layout;
	private IAsset asset;
	private PlaylistAsset playlistAsset;
	private Boolean variableLength;
	private Set pairedDisplayareas = new HashSet();
	/**
	 * 
	 *
	 */
	public AssetPresentation()
	{		
	}
	/**
	 * 
	 * @param assetPresentationId
	 * @return
	 * @throws HibernateException
	 */
	public static AssetPresentation getAssetPresentation(Long assetPresentationId) throws HibernateException
	{
		return (AssetPresentation)PersistentEntity.load(AssetPresentation.class, assetPresentationId);		
	}
	/**
	 * 
	 * @param pairedDisplayarea
	 * @return
	 * @throws HibernateException
	 */	
	public PairedDisplayarea getPairedDisplayarea(Displayarea pairedDisplayarea) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		PairedDisplayarea pd = (PairedDisplayarea)session.createCriteria(PairedDisplayarea.class)
					.add( Expression.eq("displayarea.displayareaId", pairedDisplayarea.getDisplayareaId()) )
					.add( Expression.eq("assetPresentation.assetPresentationId", this.getAssetPresentationId()) )
					.uniqueResult();
		return pd;
	}
	
	/**
	 * Retrieve all Displayareas that this assetPresentation could be paired with.
	 *  
	 * @return	List of Displayarea objects
	 */
	public List getOtherPairedDisplayareas() throws HibernateException
	{
		List result = new LinkedList();
		Long defaultDisplayareaId = this.getDisplayarea().getDisplayareaId();
		
		// Get all displayareas in this default layout
		Iterator i = this.getLayout().getLayoutDisplayareas().iterator();
		while(i.hasNext())
		{			
			// Add all displayareas except the default
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			if(lda.getDisplayarea().getDisplayareaId() != defaultDisplayareaId)
			{
				Hibernate.initialize( lda.getDisplayarea() );
				result.add( lda.getDisplayarea() );
			}
		}
		BeanPropertyComparator comparator = new BeanPropertyComparator("displayareaName");		
		Collections.sort(result, comparator);
		return result;
	}
	
	/**
	 * Removes all paired assets associated with this asset presentation
	 *
	 */
	public void removePairedAssets()
	{
		// Remove all paired assets associated with this asset presentation
		for( Iterator i=this.getPairedDisplayareas().iterator(); i.hasNext(); )
		{
			PairedDisplayarea pda = (PairedDisplayarea)i.next();
			pda.delete();
		}
		this.update();
	}
	
	/**
	 * Copies this asset presentation and assigns the given asset
	 * @param asset
	 * @return
	 */
	public AssetPresentation copy()
	{
		// Copy the asset presentation object		
		AssetPresentation newAssetPresentation = new AssetPresentation();
		newAssetPresentation.setDisplayarea( this.getDisplayarea() );
		newAssetPresentation.setLayout( this.getLayout() );
		newAssetPresentation.setLength( this.getLength() );
		newAssetPresentation.setVariableLength( this.getVariableLength() );
		newAssetPresentation.save();
		
		// Copy the paired displayareas of this asset presentation				
		for( Iterator<PairedDisplayarea> k = this.getPairedDisplayareas().iterator(); k.hasNext(); )
		{
			PairedDisplayarea pda = k.next();
			PairedDisplayarea newPairedDisplayarea = new PairedDisplayarea();
			newPairedDisplayarea.setAssetPresentation( newAssetPresentation );
			newPairedDisplayarea.setDisplayarea( pda.getDisplayarea() );
			newPairedDisplayarea.save();
			
			// Copy the paired assets of this paired displayarea					
			for( Iterator<PairedAsset> n = pda.getPairedAssets().iterator(); n.hasNext(); )
			{
				PairedAsset pairedAsset = n.next();
				PairedAsset newPairedAsset = new PairedAsset();
				newPairedAsset.setPairedDisplayarea( newPairedDisplayarea );
				newPairedAsset.setAsset( pairedAsset.getAsset() );
				newPairedAsset.setLength( pairedAsset.getLength() );
				newPairedAsset.setSeqNum( pairedAsset.getSeqNum() );
				newPairedAsset.save();
			}					
		}	
		return newAssetPresentation;
	}
	
	public void delete(){
		// Delete all pairings
		for(PairedDisplayarea pda : this.getPairedDisplayareas()){
			pda.getPairedAssets().clear();
		}
		
		super.delete();
	}
	
	/**
	 * 
	 */	
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof AssetPresentation) ) result = false;
		
		AssetPresentation ap = (AssetPresentation) other;		
		if(this.hashCode() == ap.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "AssetPresentation".hashCode();
		result = Reformat.getSafeHash( this.getDisplayarea().getDisplayareaId(), result, 13 );
		result = Reformat.getSafeHash( this.getAssetPresentationId(), result, 29 );
		return result;
	}		
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAssetPresentationId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		// Return either the id of the playlist or the asset associated with this asset presentation
		return this.getPlaylistAsset() != null ? this.getPlaylistAsset().getPlaylist().getPlaylistId() : this.getAsset().getAssetId();
	}		
	
	/**
	 * If this asset presentation is associated with a playlist,
	 * return the name of the asset within the playlist. This will cause the 
	 * history entry to be saved as "AssetName: value", which is valuable
	 * when viewing the history of a playlist (you'll know which playlist asset was updated).
	 * On the other hand, if this asset presentation is associated with an asset,
	 * leave the entityName blank because there is no need to qualify the history_entry
	 * with the assetName since we are already associating this asset presentation with the asset itself.
	 */
	public String getEntityName()
	{
		String result = "";
		if( this.getPlaylistAsset() != null ){
			result = this.getPlaylistAsset().getAsset().getAssetName();
		}
		return result;
	}	

	/**
	 * @return Returns the assetPresentationId.
	 */
	public Long getAssetPresentationId() {
		return assetPresentationId;
	}

	/**
	 * @param assetPresentationId The assetPresentationId to set.
	 */
	public void setAssetPresentationId(Long assetPresentationId) {
		this.assetPresentationId = assetPresentationId;
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
	 * @return Returns the playlistAsset.
	 */
	public PlaylistAsset getPlaylistAsset() {
		return playlistAsset;
	}
	/**
	 * @param playlistAsset The playlistAsset to set.
	 */
	public void setPlaylistAsset(PlaylistAsset playlistAsset) {
		this.playlistAsset = playlistAsset;
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
	 * @return Returns the layout.
	 */
	public Layout getLayout() {
		return layout;
	}

	/**
	 * @param layout The layout to set.
	 */
	public void setLayout(Layout layout) {
		this.layout = layout;
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
	 * @param pairedDisplayareas The pairedDisplayareas to set.
	 */
	public void setPairedDisplayareas(Set pairedDisplayareas) {
		this.pairedDisplayareas = pairedDisplayareas;
	}

	/**
	 * @return Returns the pairedDisplayareas.
	 */
	public Set<PairedDisplayarea> getPairedDisplayareas() {
		return pairedDisplayareas;
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
