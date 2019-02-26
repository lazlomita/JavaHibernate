package com.kuvata.kmf;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.logging.HistorizableLinkedList;
import com.kuvata.kmf.util.Reformat;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class PairedDisplayarea extends Entity {

	private Long pairedDisplayareaId;
	private Displayarea displayarea;
	private AssetPresentation assetPresentation;
	private List<PairedAsset> pairedAssets = new HistorizableLinkedList<PairedAsset>();
	/**
	 * 
	 *
	 */
	public PairedDisplayarea()
	{		
	}
	/**
	 * 
	 * @param pairedDisplayareaId
	 * @return
	 * @throws HibernateException
	 */
	public static PairedDisplayarea getPairedDisplayarea(Long pairedDisplayareaId) throws HibernateException
	{
		return (PairedDisplayarea)Entity.load(PairedDisplayarea.class, pairedDisplayareaId);		
	}	
	/**
	 * 
	 * @param ap
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static PairedDisplayarea getPairedDisplayarea(AssetPresentation ap, Displayarea da) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		PairedDisplayarea pd = (PairedDisplayarea)session.createCriteria(PairedDisplayarea.class)
				.add( Expression.eq("assetPresentation.assetPresentationId", ap.getAssetPresentationId()) )
				.add( Expression.eq("displayarea.displayareaId", da.getDisplayareaId()) )				
				.uniqueResult();					
		return pd;		
	}
	
	public static PairedDisplayarea create(AssetPresentation assetPresentation, Displayarea displayarea)
	{
		PairedDisplayarea pd = new PairedDisplayarea();
		pd.setAssetPresentation( assetPresentation );
		pd.setDisplayarea( displayarea );
		pd.save();
		return pd;
	}
	
	/**
	 * 
	 */
	public void delete() throws HibernateException
	{
		// Remove this object from the parent collection
		this.displayarea.getPairedDisplayareas().remove( this );
		this.assetPresentation.getPairedDisplayareas().remove( this );	
		super.delete();
	}	
	
	public List<PairedAsset> getOrderedPairedAssets() {
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT pa "
		    + "FROM PairedAsset as pa "
			+ "WHERE pa.pairedDisplayarea.id = :id "
			+ "ORDER BY pa.seqNum asc"; 		
		return session.createQuery( hql ).setParameter("id", pairedDisplayareaId).list();		

	}
	/**
	 * This method reorders all assets that are associated with this paired displayarea
	 * It is called after removing paired assets from this paired displayarea 
	 * so there is not a gap in the sequence numbers.
	 * 
	 * @throws HibernateException
	 */
	public void orderPairedAssets() throws HibernateException
	{		
		int seqCounter = 0;
		
		// Re-order all assets in playlist
		List l = this.getPairedAssets();
		Iterator i = l.iterator();
		while(i.hasNext())
		{
			PairedAsset pa = (PairedAsset)i.next();
			pa.setSeqNum( new Integer(seqCounter) );
			pa.update();
			seqCounter++;
		}		
	}	
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof PairedDisplayarea) ) result = false;
		
		PairedDisplayarea pda = (PairedDisplayarea) other;		
		if(this.hashCode() == pda.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "PairedDisplayarea".hashCode();
		result = Reformat.getSafeHash( this.getDisplayarea().getDisplayareaId(), result, 13 );
		result = Reformat.getSafeHash( this.getAssetPresentation().getAssetPresentationId(), result, 13 );
		return result;
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPairedDisplayareaId();
	}

	/**
	 * @return Returns the assetPresentation.
	 */
	public AssetPresentation getAssetPresentation() {
		return assetPresentation;
	}

	/**
	 * @param assetPresentation The assetPresentation to set.
	 */
	public void setAssetPresentation(AssetPresentation assetPresentation) {
		this.assetPresentation = assetPresentation;
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
	 * @return Returns the pairedDisplayareaId.
	 */
	public Long getPairedDisplayareaId() {
		return pairedDisplayareaId;
	}

	/**
	 * @param pairedDisplayareaId The pairedDisplayareaId to set.
	 */
	public void setPairedDisplayareaId(Long pairedDisplayareaId) {
		this.pairedDisplayareaId = pairedDisplayareaId;
	}
	/**
	 * @return the pairedAssets
	 */
	public List<PairedAsset> getPairedAssets() {
		return pairedAssets;
	}
	
	/**
	 * @param pairedAssets the pairedAssets to set
	 */
	public void setPairedAssets(List<PairedAsset> pairedAssets) {
		this.pairedAssets = pairedAssets;
	}


}
