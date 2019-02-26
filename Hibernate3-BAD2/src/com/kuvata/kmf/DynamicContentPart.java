package com.kuvata.kmf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import com.kuvata.kmf.logging.HistorizableCollectionMember;

public class DynamicContentPart extends PersistentEntity implements HistorizableCollectionMember{
	
	private Long dynamicContentPartId;
	private Playlist parentPlaylist;
	private ContentRotation parentContentRotation;
	private ContentRotation contentRotation;
	private Layout layout;
	private Displayarea displayarea;
	private Integer seqNum;
	
	public static DynamicContentPart create(Playlist parentPlaylist, ContentRotation parentContentRotation, ContentRotation cr, Layout layout, Displayarea displayarea, int seqNum){
		DynamicContentPart dcp = new DynamicContentPart();
		dcp.setParentPlaylist(parentPlaylist);
		dcp.setParentContentRotation(parentContentRotation);
		dcp.setContentRotation(cr);
		dcp.setLayout(layout);
		dcp.setDisplayarea(displayarea);
		dcp.setSeqNum(seqNum);
		dcp.save();
		return dcp;
	}
	
	public static DynamicContentPart getDynamicContentPart(Long dynamicContentPartId){
		Session session = HibernateSession.currentSession();				
		return (DynamicContentPart)session.createCriteria(DynamicContentPart.class)
				.add( Expression.eq("dynamicContentPartId", dynamicContentPartId) )		
				.uniqueResult();
	}
	
	public static List<DynamicContentPart> getDynamicContentParts(Playlist parentPlaylist){
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(DynamicContentPart.class)
				.add( Expression.eq("parentPlaylist", parentPlaylist) )		
				.addOrder(Order.asc("seqNum")).list();
	}
	
	public static List<DynamicContentPart> getDynamicContentParts(ContentRotation parentContentRotation){
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(DynamicContentPart.class)
				.add( Expression.eq("parentContentRotation", parentContentRotation) )		
				.addOrder(Order.asc("seqNum")).list();
	}
	
	public static List<Long> getContentRotationsToExclude(Long contentRotationId){
		Session session = HibernateSession.currentSession();
		List<Long> l = session.createQuery("SELECT parentContentRotation.contentRotationId FROM DynamicContentPart WHERE contentRotation.contentRotationId = :contentRotationId")
						.setParameter("contentRotationId", contentRotationId).list();
		
		// Exclude all parent content rotations for all of these content rotations
		List<Long> addedItems = new ArrayList<Long>();
		for(Long crId : l){
			addedItems.addAll(getContentRotationsToExclude(crId));
		}
		
		l.addAll(addedItems);
		return l;
	}
	
	public static List<DynamicContentPart> getChildDynamicContentParts(ContentRotation contentRotation){
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(DynamicContentPart.class)
				.add( Expression.eq("contentRotation", contentRotation) ).list();
	}
	
	public static Set<ContentRotation> getRecursiveContentRotationsToUpdate(ContentRotation cr, boolean ignoreParam){
		Set<ContentRotation> result = new LinkedHashSet<ContentRotation>();
		
		// If this is a dynamic content rotation
		if(cr.getType().equalsIgnoreCase(Constants.DYNAMIC)){
			// If this content rotation is set to play X assets
			if(cr.getNumAssets() != null){
				for(DynamicContentPart dcp : DynamicContentPart.getDynamicContentParts(cr)){
					ContentRotation childContentRotation = dcp.getContentRotation();
					
					// Recurse through this child content rotation to get it's child content rotations
					result.addAll(getRecursiveContentRotationsToUpdate(childContentRotation, false));
				}
			}
			// Add this content rotation
			if(ignoreParam == false){
				result.add(cr);
			}
		}
		
		return result;
	}
	
	public static Set<ContentRotation> getRecursiveContentRotationsToUpdate(Playlist p){
		Set<ContentRotation> result = new LinkedHashSet<ContentRotation>();
		
		// If this is a dynamic playlist
		if(p.getType().equalsIgnoreCase(Constants.DYNAMIC)){
			// If this playlist is set to play X assets
			if(p.getNumAssets() != null){
				for(DynamicContentPart dcp : DynamicContentPart.getDynamicContentParts(p)){
					ContentRotation childContentRotation = dcp.getContentRotation();
					
					// Recurse through this child content rotation to get it's child content rotations
					result.addAll(getRecursiveContentRotationsToUpdate(childContentRotation, false));
				}
			}
		}
		
		return result;
	}
	
	public static Float calculateAvgLoopLength(List<DynamicContentPart> parts, int numAssetsPerPart){
		
		// Save off the iterators on each content rotation
		HashMap<Long, List> lists = new HashMap<Long, List>();
		HashMap<Long, Iterator> iterators = new HashMap<Long, Iterator>();
		
		// Total content length
		float length = 0f;
		
		// Total times through the loop
		float numLoops = 0;
		
		// Get current date
		Date now = new Date();
		
		// Generate content for at least a whole day
		while(length < Constants.MILLISECONDS_IN_A_DAY){
			
			// For each content rotation
			for(DynamicContentPart dcp : parts){
				ContentRotation cr = dcp.getContentRotation();
				
				if( lists.containsKey(cr.getContentRotationId()) == false ) {
					// Fiter assets that are not current
					List myList = new LinkedList();
					List craList = cr.getContentRotationAssets();
					Iterator iterator = craList.iterator();
					while( iterator.hasNext() ) {
						ContentRotationAsset cra = (ContentRotationAsset)iterator.next();
						Asset a = Asset.convert(cra.getAsset());						
						// Make sure this is a current asset
						if( (a.getStartDate() == null || a.getStartDate().before(now)) && (a.getEndDate() == null || a.getEndDate().after(now)) ){
							myList.add( cra );
						}
					}
					lists.put(cr.getContentRotationId(), myList);
				}
				List l = lists.get( cr.getContentRotationId() );
				
				// Make sure that this content rotation has assets
				if(l.size() > 0){
					Iterator i;
					if(iterators.containsKey(cr.getContentRotationId())){
						i =  iterators.get(cr.getContentRotationId());
					}else{
						i = l.iterator();
					}
					
					// Get numX assets
					for(int j=0; j<numAssetsPerPart; j++){
						// Get next ContentRotationAsset
						if(i.hasNext() == false){
							i = l.iterator();
						}
						ContentRotationAsset cra  = (ContentRotationAsset)i.next();
						length += cra.getLength() * 1000f;
					}
					
					// Update iterator map
					iterators.put(cr.getContentRotationId(), i);
				}
			}
			
			// Break out of the loop if there are no results after an iteration thru all sub parts.
			if(length == 0){
				break;
			}
			
			// Increment number of loops
			numLoops++;
		}
		
		// Avg loop length
		Float avgLoopLength = 0f;
		if(numLoops != 0){
			avgLoopLength = length / numLoops;
		}
		
		return avgLoopLength;
	}
	
	public String getEntityName(){
		return contentRotation.getContentRotationName();
	}
	public Long getHistoryEntityId(){
		return parentPlaylist != null ? parentPlaylist.getPlaylistId() : parentContentRotation.getContentRotationId();
	}
	public Long getEntityId(){
		return dynamicContentPartId;
	}
	public Playlist getParentPlaylist() {
		return parentPlaylist;
	}
	public void setParentPlaylist(Playlist parentPlaylist) {
		this.parentPlaylist = parentPlaylist;
	}
	public ContentRotation getParentContentRotation() {
		return parentContentRotation;
	}
	public void setParentContentRotation(ContentRotation parentContentRotation) {
		this.parentContentRotation = parentContentRotation;
	}
	public ContentRotation getContentRotation() {
		return contentRotation;
	}
	public void setContentRotation(ContentRotation contentRotation) {
		this.contentRotation = contentRotation;
	}
	public Long getDynamicContentPartId() {
		return dynamicContentPartId;
	}
	public void setDynamicContentPartId(Long dynamicContentPartId) {
		this.dynamicContentPartId = dynamicContentPartId;
	}

	public Integer getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(Integer seqNum) {
		this.seqNum = seqNum;
	}

	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public Displayarea getDisplayarea() {
		return displayarea;
	}

	public void setDisplayarea(Displayarea displayarea) {
		this.displayarea = displayarea;
	}
}