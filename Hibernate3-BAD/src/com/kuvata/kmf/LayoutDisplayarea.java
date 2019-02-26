package com.kuvata.kmf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class LayoutDisplayarea extends Entity implements HistorizableCollectionMember 
{
	private Long layoutDisplayareaId;
	private Layout layout;
	private Displayarea displayarea;
	private Integer xpos;
	private Integer ypos;
	private Integer seqNum;
	/**
	 * 
	 *
	 */
	public LayoutDisplayarea()
	{		
	}
	/**
	 * 
	 * @param l
	 * @param da
	 * @return
	 * @throws HibernateException
	 */
	public static LayoutDisplayarea getLayoutDisplayarea(Long layoutId, Long displayareaId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();					
		LayoutDisplayarea lda = (LayoutDisplayarea)session.createCriteria(LayoutDisplayarea.class)
				.add( Expression.eq("layout.layoutId", layoutId) )
				.add( Expression.eq("displayarea.displayareaId", displayareaId) )
				.uniqueResult();
		return lda;
	}
	
	/**
	 * Determines whether or not this displayarea/layout combination has any 
	 * asset presentation objects associated with it. Returns true if it doesn't 
	 * (and is therefore removable), or false if it does (and therefore cannot be removed)
	 * 
	 * @param da
	 * @return
	 */
	public boolean isRemovable() throws HibernateException
	{		
		boolean result = false;
		int numResults = -1;
		Session session = HibernateSession.currentSession();			
		String hql = "SELECT COUNT(ap) " +
					"FROM AssetPresentation ap " +
					"WHERE ap.displayarea.displayareaId = "+ this.getDisplayarea().getDisplayareaId() +" " +
					"AND ap.layout.layoutId ="+ this.getLayout().getLayoutId();
		Iterator i = session.createQuery( hql ).iterate();
		numResults = ( (Long) i.next() ).intValue();		
		Hibernate.close( i );
		
		// If we found one or more referenced asset presentations -- we don't need to execute this next query
		if( numResults <= 0 )
		{
			// Locate any playlist_content_rotations associated with this displayarea that have one or more content rotation assets
			hql = "SELECT COUNT(pcr) "
				+ "FROM PlaylistContentRotation as pcr "			
				+ "WHERE pcr.displayarea.displayareaId = "+ this.getDisplayarea().getDisplayareaId() +" "				
				+ "AND SIZE(pcr.contentRotation.contentRotationAssets) > 0";
			Iterator iter = session.createQuery( hql ).iterate();
			numResults = ( (Long) iter.next() ).intValue();
			Hibernate.close( iter );			
			
			// If we found any playlist_content_rotations associated with this displayarea
			if( numResults > 0 )
			{
				// Do an additional check to see if this layout is in use by querying the PlaylistAsset table
				hql = "SELECT COUNT(pa) "
					+ "FROM PlaylistAsset pa "
					+ "WHERE pa.assetPresentation.layout.layoutId = "+ this.getLayout().getLayoutId();
				Iterator iter2 = session.createQuery( hql ).iterate();
				numResults = ( (Long) iter2.next() ).intValue();
				Hibernate.close( iter2 );
			}
		}				
		return numResults > 0 ? false : true;
	}

	
	/**
	 * If the displayarea we're about to remove from this layout
	 * is referenced by one or more asset presentations, either
	 * re-assign the displayarea of the asset presentation (in the case of a default
	 * asset presentation), or remove the asset presentation and playlist asset
	 * (in the case of a playlist asset asset presentation).
	 * Returns the collection of playlist asset presentation ids that were deleted.
	 * @param alternateDisplayarea
	 */
	public List delete(Displayarea alternateDisplayarea)
	{
		// Remove this object from it's parent collection		
		this.layout.getLayoutDisplayareas().remove( this );
		this.layout.getLayoutDisplayareasSorted().remove( this );		
		//this.displayarea.getLayoutDisplayareas().remove( this );					
				
		// If this layoutDisplayarea is associated with any default asset presentations
		// Change the asset's default displayarea to a different displayarea within this layout
		Session session = HibernateSession.currentSession();					
		String hql = "UPDATE AssetPresentation "
			+ "SET displayarea_id = "+ alternateDisplayarea.getDisplayareaId().toString() +" "
			+ "WHERE displayarea_id = "+ this.getDisplayarea().getDisplayareaId().toString() +" "
			+ "AND layout_id = "+ this.getLayout().getLayoutId().toString() +" "
			+ "AND asset_presentation_id IN "
			+ "(SELECT a.assetPresentation.assetPresentationId FROM Asset a)";		
		session.createQuery( hql ).executeUpdate();
		
		/*
		 * Get all playlist asset presentation ids that this layoutDisplayarea is associated with
		 */
		hql = "SELECT pa.assetPresentation.assetPresentationId "			
			+ "FROM PlaylistAsset pa "
			+ "WHERE pa.assetPresentation.displayarea.displayareaId = "+ this.getDisplayarea().getDisplayareaId().toString() +" "
			+ "AND pa.assetPresentation.layout.layoutId = "+ this.getLayout().getLayoutId().toString();
		List l = session.createQuery( hql ).list();
		ArrayList<Long> assetPresentationIds = new ArrayList<Long>();
		for( Iterator i=l.iterator(); i.hasNext(); ){
			Long assetPresentationId = (Long)i.next();
			if( assetPresentationId != null ){
				assetPresentationIds.add(assetPresentationId);
			}
		}
		
		List playlists = null;
		if( assetPresentationIds.size() > 0 )
		{
			// Save these ids in the selection entities tables
			Long selectionId = SelectedEntities.createSelectedEntities(assetPresentationIds, null);
			
			// First save off any playlists that will be affected by this delete
			hql = "SELECT p "
				+ "FROM Playlist as p "
				+ "WHERE p.playlistId IN "			
				+ "(SELECT DISTINCT pa.playlist.playlistId "
				+ "FROM PlaylistAsset as pa "				
				+ "WHERE pa.assetPresentation.assetPresentationId IN ( SELECT entityId FROM SelectedEntities WHERE selectionId = :selectionId ))";
			playlists = session.createQuery( hql ).setParameter("selectionId", selectionId).list(); 
					
			// Delete the playlist assets
			hql = "DELETE FROM PlaylistAsset "
				+ "WHERE asset_presentation_id IN "
				+ "( SELECT entityId FROM SelectedEntities WHERE selectionId = :selectionId )";
			session.createQuery( hql ).setParameter("selectionId", selectionId).executeUpdate();
			
			// Delete the asset presentations
			hql = "DELETE FROM AssetPresentation "
				+ "WHERE asset_presentation_id IN "
				+ "( SELECT entityId FROM SelectedEntities WHERE selectionId = :selectionId )";
			session.createQuery( hql ).setParameter("selectionId", selectionId).executeUpdate();
			
			SelectedEntities.deleteSelectedEntities(selectionId);
		}
		
		// Delete this layout displayarea object
		super.delete();		
		return playlists;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof LayoutDisplayarea) ) result = false;
		
		LayoutDisplayarea lda = (LayoutDisplayarea) other;		
		if(this.hashCode() == lda.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{		
		int result = "LayoutDisplayarea".hashCode();
		result = Reformat.getSafeHash( this.getLayoutDisplayareaId(), result, 2 );
		result = Reformat.getSafeHash( this.getDisplayarea().getDisplayareaId(), result, 3 );
		result = Reformat.getSafeHash( this.getXpos(), result, 5 );
		result = Reformat.getSafeHash( this.getYpos(), result, 7 );		
		if( result < 0 ) {
		    return -result;
		} else {
		    return result;
		}
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getLayoutDisplayareaId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getLayout().getLayoutId();
	}	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getDisplayarea().getDisplayareaName();
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
	 * @return Returns the layoutDisplayareaId.
	 */
	public Long getLayoutDisplayareaId() {
		return layoutDisplayareaId;
	}

	/**
	 * @param layoutDisplayareaId The layoutDisplayareaId to set.
	 */
	public void setLayoutDisplayareaId(Long layoutDisplayareaId) {
		this.layoutDisplayareaId = layoutDisplayareaId;
	}

	/**
	 * @return Returns the xpos.
	 */
	public Integer getXpos() {
		return xpos;
	}

	/**
	 * @param xpos The xpos to set.
	 */
	public void setXpos(Integer xpos) {
		this.xpos = xpos;
	}

	/**
	 * @return Returns the ypos.
	 */
	public Integer getYpos() {
		return ypos;
	}

	/**
	 * @param ypos The ypos to set.
	 */
	public void setYpos(Integer ypos) {
		this.ypos = ypos;
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
