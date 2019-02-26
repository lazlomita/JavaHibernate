package com.kuvata.kmf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.kuvata.kmf.usertype.AssetIntervalUnitType;
import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.DirtyType;
import com.kuvata.kmf.usertype.InterruptionToleranceUnit;
import com.kuvata.kmf.usertype.RecurrenceEndType;
import com.kuvata.kmf.usertype.RecurrenceType;
import com.kuvata.kmf.usertype.SearchInterfaceType;
import com.kuvata.kmf.usertype.SegmentEndAfterUnit;
import com.kuvata.kmf.usertype.SegmentEndType;
import com.kuvata.kmf.usertype.SegmentInterruptionType;
import com.kuvata.kmf.usertype.SegmentPriority;
import com.kuvata.kmf.usertype.SegmentSearchType;
import com.kuvata.kmf.usertype.SegmentType;
import com.kuvata.kmf.usertype.TrueFalseUserType;

import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableLinkedList;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.util.Reformat;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class Segment extends Entity implements Historizable {

	private Long segmentId;
	private String segmentName;
	private Float length;
	private Date startDatetime;
	private SegmentEndType endType;
	private Integer endAfter;
	private Integer endAfterNumAssets;
	private SegmentEndAfterUnit endAfterUnits;
	private SegmentPriority priority;
	private SegmentInterruptionType interruptionType;	
	private TrueFalseUserType useServerTime;
	private List<SegmentPart> segmentParts = new HistorizableLinkedList<SegmentPart>();
	private Set<SegmentGrpMember> segmentGrpMembers = new HashSet<SegmentGrpMember>();
	private Set<DeviceSchedule> deviceSchedules = new HistorizableSet<DeviceSchedule>();
	private LinkedList<DeviceScheduleInfo> deviceSchedulesSorted;
	private Recurrence recurrence;
	private Integer startBeforeTolerance;
	private InterruptionToleranceUnit startBeforeToleranceUnits;
	private Integer startAfterTolerance;
	private InterruptionToleranceUnit startAfterToleranceUnits;
	private Integer endBeforeTolerance;
	private InterruptionToleranceUnit endBeforeToleranceUnits;
	private Integer endAfterTolerance;
	private InterruptionToleranceUnit endAfterToleranceUnits;	
	private Boolean useAssetIntervalPlaylist;
	private Integer assetIntervalFrequency;
	private Integer assetIntervalNumAssets;
	private Playlist assetIntervalPlaylist;
	private AssetIntervalUnitType assetIntervalUnits;
	private SegmentType type;
	
	// Device side scheduling variable
	private Date dateAdded;
	
	/*
	 * Used by content scheduler for non-persistent copies of segmentParts and Length
	 * (used for maintaining correct lengths of variable length assets)
	 */	
	private List mySegmentPartsLengths = null;
	private long myDeviceId = -1;	
	
	/**
	 * 
	 *
	 */
	public Segment()
	{		
	}	
	/**
	 * 
	 * @param segmentId
	 * @return
	 * @throws HibernateException
	 */
	public static Segment getSegment(Long segmentId) throws HibernateException
	{
		return (Segment)Entity.load(Segment.class, segmentId);		
	}	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getSegmentId();
	}
	/**
	 * 
	 * @param segmentPart
	 */
	public void addSegmentPart(SegmentPart segmentPart) 
	{
		if (segmentPart == null)
			throw new IllegalArgumentException("Null segmentPart!");
				
		segmentParts.add(segmentPart);
	}
	
	/**
	 * Returns true if a segment with the given name already exists in the database
	 * 
	 * @param segmentName
	 * @return
	 */
	public static boolean segmentExists(String segmentName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();				
		Segment s = (Segment) session.createCriteria(Segment.class)
					.add( Expression.eq("segmentName", segmentName).ignoreCase() )
					.uniqueResult();		
		// If a segment with the given name already exists in the database
		if(s != null)
			return true;
		else
			return false;
	}	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeSegmentParts() throws HibernateException
	{
		// Should just be able to clear this set -- mapping problem
		this.segmentParts.clear();		
		this.update();
	}	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeSegmentGrpMembers() throws HibernateException
	{
		// Should just be able to clear this set -- mapping problem
		this.segmentGrpMembers.clear();		
		this.update();
	}		

	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeDeviceSchedules() throws HibernateException
	{
		// Iterate through all the device schedules so we can call the delete function for each
		for( Iterator i=this.deviceSchedules.iterator(); i.hasNext(); )
		{
			DeviceSchedule ds = (DeviceSchedule)i.next();
			i.remove();
			ds.delete(false,true);
		}
		this.update();
	}	
	/**
	 * 
	 * @param segmentGrpMember
	 */
	public void addSegmentGrpMember(SegmentGrpMember segmentGrpMember) 
	{
		if (segmentGrpMember == null)
			throw new IllegalArgumentException("Null segmentGrpMember!");
				
		segmentGrpMembers.add( segmentGrpMember );
	}	
	/**
	 * 
	 * @param getAllSegments
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param formatter
	 * @return
	 * @throws HibernateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static String treeViewFormat(boolean getAllSegments, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, Method formatter) 
		throws HibernateException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException
	{		
		StringBuffer result = new StringBuffer();
		Method allBranchMethod = null;		
		
		if(getAllSegments == true)
		{	
			// Declare the method that will be used to build the "All Segments" branch of the tree
			Class[] methodParamTypes = { boolean.class, boolean.class, boolean.class, boolean.class };
			allBranchMethod = Class.forName(Segment.class.getName()).getDeclaredMethod("getAllSegmentsBranch", methodParamTypes);
		}
					
		result.append( Grp.getTree(Constants.SEGMENT_GROUPS, includeLeaves, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, "type_segment_group", formatter, allBranchMethod) );
		return result.toString();
	}
	/**
	 * 
	 */
	public String treeViewFormat(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();	
		String onClick = "null";
		if(includeLeaves == true)
		{	
			// Build the string for each device					
			if( includeHref )
			{
				onClick= "\'javascript:top.segmentOnClick("+ this.getSegmentId() +")\'";	
			}	
			
			// Build the string for each segment			
			result.append("[");					
			result.append("{id:"+ this.getSegmentId() +"}, \"<span class=\\\"treeNodeSegment\\\">"+ Reformat.jspEscape(this.getSegmentName()) + "</span>\", "+ onClick +", null, type_segment,");
			result.append("],\n");
		}
		return result.toString();
	}
	/**
	 * 
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @return
	 * @throws HibernateException
	 */
	public static String getAllSegmentsBranch(boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, boolean includeAllLeaves) throws HibernateException
	{		
		String onClick = "";
		String onDoubleClick = "";
		if( ( includeHref ) && (doubleClickLeavesOnly == false) )
		{
			onClick = "\'javascript:grpOnClick(-1)\'";
		}	
		if( ( includeDoubleClick ) && (doubleClickLeavesOnly == false) )
		{
			onDoubleClick = " onDblClick=\\\"grpOnDoubleClick(-1, '"+ Constants.SEGMENT_GROUP_SUFFIX +"')\\\"";	
		}	
				
		StringBuffer result = new StringBuffer();
		result.append("[");
		result.append("{id:-1}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">All Schedules</span>\", "+ onClick +", null, type_segment_group,\n");		
					
		int counter = 1;		
		for( Iterator i=Segment.getSegments().iterator(); i.hasNext(); )
		{
			Segment s = (Segment)i.next();
			result.append( s.treeViewFormat(0, true, includeAllLeaves, includeHref, includeDoubleClick, doubleClickLeavesOnly, null, null) );
			
			// Limit the number of child nodes
			if( counter++ >= Constants.MAX_CHILD_NODES ){
				
				// Append a "more" child node and break
				result.append("[");					
				result.append("{id:0}, \"<span class=\\\"treeNodeSegment\\\">...more</span>\", null, null, type_segment,");
				result.append("],\n");
				break;
			}
		}				
		result.append("],\n");
		return result.toString();
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getSegments() throws HibernateException
	{
		Session session = HibernateSession.currentSession();		
		List l = session.createQuery(				
				"SELECT s "
				+ "FROM Segment as s "				
				+ "ORDER BY UPPER(s.segmentName)"						
				).list();	
		return l;
	}	
	
	/**
	 * 
	 * @param segmentId
	 * @return
	 * @throws HibernateException
	 */
	public static List getSegments(List segmentIds) throws HibernateException
	{
		return Entity.load(Segment.class, segmentIds);		
	}
	
	/**
	 * 
	 * @return Returns a list of Segments matching the segmentName
	 * @throws HibernateException
	 */
	public static List getSegments(String segmentName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();			
		List l = session.createCriteria(Segment.class)
				.add( Expression.eq("segmentName", segmentName).ignoreCase() )
				.list();		
		return l;
	}	
	
	
	/**
	 * @return Returns the number of segments
	 */
	public static int getSegmentsCount() throws HibernateException 
	{
		int result = 0;
		Session session = HibernateSession.currentSession();
		Iterator i = session.createQuery(
				"SELECT COUNT(s) "
				+ "FROM Segment as s"							
				).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}	
	
	/**
	 * Returns a list of GrpGrpMember objects of the "Segment Groups" group
	 * @return
	 */
	public static List getAllSegmentGroups()
	{
		List result = null;
		Grp segmentGroups = Grp.getUniqueGrp( Constants.SEGMENT_GROUPS );
		if( segmentGroups != null )
		{			
			result = new ArrayList( segmentGroups.getGrpMembers() );
			BeanPropertyComparator comparator1 = new BeanPropertyComparator( "grpName" );
			BeanPropertyComparator comparator2 = new BeanPropertyComparator( "childGrp", comparator1 );				
			Collections.sort( result, comparator2 );
			
			// Initialize the child groups within the list
			for( Iterator i=result.iterator(); i.hasNext(); ) {
				GrpGrpMember ggm = (GrpGrpMember)i.next();
				Hibernate.initialize( ggm.getChildGrp() );
			}
		}
		return result;
	}	
	
	public static Segment createOrUpdate(Segment s, String segmentName, Date startDatetime, SegmentEndType segmentEndType, SegmentEndAfterUnit seau,
			Integer endAfter, Integer endAfterNumAssets, 
			Integer startAfterTolerance, InterruptionToleranceUnit startAfterToleranceUnits,  
			Integer startBeforeTolerance, InterruptionToleranceUnit startBeforeToleranceUnits, 
			Integer endAfterTolerance, InterruptionToleranceUnit endAfterToleranceUnits,  
			Integer endBeforeTolerance, InterruptionToleranceUnit endBeforeToleranceUnits, 
			SegmentPriority segmentPriority, SegmentInterruptionType segmentInterruptionType,
			TrueFalseUserType useServerTime, Boolean useAssetIntervalPlaylist, Playlist assetIntervalPlaylist,
			Integer assetIntervalFrequency, Integer assetIntervalNumAssets, AssetIntervalUnitType assetIntervalUnit, SegmentType type)
	{
		boolean isUpdate = true;
		boolean makeDirtyRequired = false;
		boolean makeDevicesDirtyRequired = false;
		if( s == null ){
			s = new Segment();
			isUpdate = false;
		} else {
			
			// Determine if any fields have changed that would make the devices associated with this segment dirty
			makeDevicesDirtyRequired = s.makeDevicesDirtyRequired( startDatetime, segmentEndType, seau, endAfter, endAfterNumAssets, 
					startAfterTolerance, startAfterToleranceUnits, startBeforeTolerance, startBeforeToleranceUnits,
					endAfterTolerance, endAfterToleranceUnits, endBeforeTolerance, endBeforeToleranceUnits, 
					segmentPriority, segmentInterruptionType, useServerTime, useAssetIntervalPlaylist, assetIntervalPlaylist,
					assetIntervalFrequency, assetIntervalNumAssets, assetIntervalUnit );			
		}
		
		s.setSegmentName( segmentName );			
		s.setStartDatetime( startDatetime );		
		s.setEndType( segmentEndType );
		
		if(isUpdate == false){
			// Set the segment length temporarily to avoid null pointer -- we'll calculate it later
			s.setLength( new Float(0) );
		}
		
		s.setEndAfterUnits( seau );
		if( segmentEndType.equals( SegmentEndType.END_AFTER ) ){
			s.setEndAfter( endAfter );			
			s.setEndAfterUnits( seau );
		}else if( segmentEndType.equals( SegmentEndType.END_AFTER_NUM_ASSETS ) ){
			s.setEndAfterNumAssets( endAfterNumAssets );			
		}
		
		s.setPriority( segmentPriority );		
		s.setInterruptionType( segmentInterruptionType );		
		s.setUseServerTime( useServerTime );
		s.setType(type);
		
		// Interruption tolerance
		s.setStartAfterTolerance( startAfterTolerance );
		s.setStartAfterToleranceUnits( startAfterToleranceUnits );
		s.setStartBeforeTolerance( startBeforeTolerance );
		s.setStartBeforeToleranceUnits( startBeforeToleranceUnits );		
		s.setEndBeforeTolerance( endBeforeTolerance );	
		s.setEndBeforeToleranceUnits( endBeforeToleranceUnits );
		s.setEndAfterTolerance( endAfterTolerance );			
		s.setEndAfterToleranceUnits( endAfterToleranceUnits );
				
		// Asset Interval
		s.setUseAssetIntervalPlaylist( useAssetIntervalPlaylist );
		s.setAssetIntervalFrequency( assetIntervalFrequency );
		s.setAssetIntervalNumAssets( assetIntervalNumAssets );			
		s.setAssetIntervalPlaylist( assetIntervalPlaylist );	
		s.setAssetIntervalUnits( assetIntervalUnit );
		
		if( isUpdate ) {
			s.update();
			
			// If any properties have changed that would require a publish -- make this segment dirty
			if( makeDevicesDirtyRequired ){
				s.makeDirty( true );
			}else if( makeDirtyRequired ){
				s.makeDirty( false );
			}						
		}else{
			s.save();
		}
		return s;
	}
	
	public Recurrence createOrUpdateRecurrence(Recurrence r, RecurrenceType rt, Integer dailyFrequency, Integer weeklyFrequency, String[] selectedDays,
			Boolean applyContinuation, Date recurrenceStartDatetime, RecurrenceEndType recurrenceEndType, Integer recurrenceOccurrences,
			Date recurrenceEndDatetime)
	{
		boolean isUpdate = false;
		boolean makeDirtyRequired = false;
		
		// Weekly Recurrence
		if(rt.equals(RecurrenceType.WEEKLY)) 
		{							
			// If the recurrence type changed -- delete before creating a new recurrence object
			if( r != null && r.getRecurrenceType().equals( RecurrenceType.WEEKLY ) == false ) {
				makeDirtyRequired = true;
				r.delete();
				r = null;
			}			
			WeeklyRecurrence wr = null;
			if(r == null) {
				makeDirtyRequired = true;
				wr = new WeeklyRecurrence();
			} else {
				wr = (WeeklyRecurrence)r;
				isUpdate = true;
				makeDirtyRequired = wr.makeDirtyRequired( rt, recurrenceStartDatetime, recurrenceEndType, recurrenceOccurrences, recurrenceEndDatetime,
						applyContinuation, weeklyFrequency, selectedDays );				
			}								
			wr.setWeeklyRecurrence( weeklyFrequency, selectedDays );
			r = (Recurrence)wr;
		} 
		// Daily Recurrence
		else if (rt.equals(RecurrenceType.DAILY)) 
		{				
			// If the recurrence type changed -- delete before creating a new recurrence object
			if( r != null && r.getRecurrenceType().equals( RecurrenceType.DAILY ) == false ) {
				makeDirtyRequired = true;
				r.delete();
				r = null;
			}
			
			DailyRecurrence dr = null;
			if(r == null) {
				makeDirtyRequired = true;
				dr = new DailyRecurrence();
			} else {
				dr = (DailyRecurrence)r;
				isUpdate = true;
				makeDirtyRequired = dr.makeDirtyRequired( rt, recurrenceStartDatetime, recurrenceEndType, recurrenceOccurrences, recurrenceEndDatetime,
						applyContinuation, dailyFrequency );				
			}				

			// Save daily recurrence
			dr.setDailyFrequency( dailyFrequency );	
			r = (Recurrence)dr;
		}				
		r.setRecurrenceType( rt );		
		r.setApplyContinuation( applyContinuation );			
		r.setStartDate( recurrenceStartDatetime  );		
		r.setEndType( recurrenceEndType );
		
		if( recurrenceEndType.equals( RecurrenceEndType.END_AFTER ) ) {
			r.setEndAfterOccurrences( recurrenceOccurrences );							
		} else if( recurrenceEndType.equals( RecurrenceEndType.ON_DATE ) ) {			
			r.setEndAfterDate( recurrenceEndDatetime );
		}
		r.setSegment( this );
		
		if( isUpdate ) {
			r.update();						
		} else {
			r.save();
		}	
		
		// If any properties have changed that would require a publish -- make this segment dirty
		if( makeDirtyRequired ){
			r.getSegment().makeDirty( true );
		}	
		return r;
	}	
	
	private boolean makeDirtyRequired(String segmentName)
	{
		// If any of theese properties have changed
		if( (this.segmentName != null && this.segmentName.equalsIgnoreCase( segmentName ) == false)
				|| (this.segmentName == null && segmentName != null) ){
			return true;
		}
		return false;
	}
	
	private boolean makeDevicesDirtyRequired(Date startDatetime, SegmentEndType segmentEndType, SegmentEndAfterUnit seau,
			Integer endAfter, Integer endAfterNumAssets, 
			Integer startAfterTolerance, InterruptionToleranceUnit startAfterToleranceUnits,  
			Integer startBeforeTolerance, InterruptionToleranceUnit startBeforeToleranceUnits, 
			Integer endAfterTolerance, InterruptionToleranceUnit endAfterToleranceUnits,  
			Integer endBeforeTolerance, InterruptionToleranceUnit endBeforeToleranceUnits, 
			SegmentPriority segmentPriority, SegmentInterruptionType segmentInterruptionType,
			TrueFalseUserType useServerTime, Boolean useAssetIntervalPlaylist, Playlist assetIntervalPlaylist,
			Integer assetIntervalFrequency, Integer assetIntervalNumAssets, AssetIntervalUnitType assetIntervalUnits)
	{
		// If any of theese properties have changed, we must make dirty the devices associated with this segment
		if( (this.startDatetime != null && this.startDatetime.getTime() != startDatetime.getTime() )
				|| (this.startDatetime == null && startDatetime != null) ){
			return true;
		}else if( (this.endType != null && this.endType.equals( segmentEndType ) == false)
				|| (this.endType == null && segmentEndType != null) ){
			return true;
		}else if( (this.endAfterUnits != null && this.endAfterUnits.equals( seau ) == false)
				|| (this.endAfterUnits == null && seau != null) ){
			return true;
		}else if( (this.endAfter != null && this.endAfter.equals( endAfter ) == false)
				|| (this.endAfter == null && endAfter != null) ){
			return true;
		}else if( (this.endAfterNumAssets != null && this.endAfterNumAssets.equals( endAfterNumAssets ) == false)
				|| (this.endAfterNumAssets == null && endAfterNumAssets != null) ){
			return true;
		}else if( (this.startAfterTolerance != null && this.startAfterTolerance.equals( startAfterTolerance ) == false)
				|| (this.startAfterTolerance == null && startAfterTolerance != null) ){
			return true;
		}else if( (this.startAfterToleranceUnits != null && this.startAfterToleranceUnits.equals( startAfterToleranceUnits ) == false)
				|| (this.startAfterToleranceUnits == null && startAfterToleranceUnits != null) ){
			return true;
		}else if( (this.startBeforeTolerance != null && this.startBeforeTolerance.equals( startBeforeTolerance ) == false)
				|| (this.startBeforeTolerance == null && startBeforeTolerance != null) ){
			return true;
		}else if( (this.startBeforeToleranceUnits != null && this.startBeforeToleranceUnits.equals( startBeforeToleranceUnits ) == false)
				|| (this.startBeforeToleranceUnits == null && startBeforeToleranceUnits != null) ){
			return true;
		}else if( (this.endAfterTolerance != null && this.endAfterTolerance.equals( endAfterTolerance ) == false)
				|| (this.endAfterTolerance == null && endAfterTolerance != null) ){
			return true;
		}else if( (this.endAfterToleranceUnits != null && this.endAfterToleranceUnits.equals( endAfterToleranceUnits ) == false)
				|| (this.endAfterToleranceUnits == null && endAfterToleranceUnits != null) ){
			return true;
		}else if( (this.endBeforeTolerance != null && this.endBeforeTolerance.equals( endBeforeTolerance ) == false)
				|| (this.endBeforeTolerance == null && endBeforeTolerance != null) ){
			return true;
		}else if( (this.endBeforeToleranceUnits != null && this.endBeforeToleranceUnits.equals( endBeforeToleranceUnits ) == false)
				|| (this.endBeforeToleranceUnits == null && endBeforeToleranceUnits != null) ){
			return true;
		}else if( (this.priority != null && this.priority.equals( segmentPriority ) == false)
				|| (this.priority == null && segmentPriority != null) ){
			return true;
		}else if( (this.interruptionType != null && this.interruptionType.equals( segmentInterruptionType ) == false)
				|| (this.interruptionType == null && segmentInterruptionType != null) ){
			return true;
		}else if( (this.useServerTime != null && this.useServerTime.equals( useServerTime ) == false)
				|| (this.useServerTime == null && useServerTime != null) ){
			return true;
		}else if( (this.useAssetIntervalPlaylist != null && this.useAssetIntervalPlaylist.equals( useAssetIntervalPlaylist ) == false)
				|| (this.useAssetIntervalPlaylist == null && useAssetIntervalPlaylist != null) ){
			return true;
		}else if( (this.assetIntervalPlaylist != null && this.assetIntervalPlaylist.equals( assetIntervalPlaylist ) == false)
				|| (this.assetIntervalPlaylist == null && assetIntervalPlaylist != null) ){
			return true;
		}else if( (this.assetIntervalFrequency != null && this.assetIntervalFrequency.equals( assetIntervalFrequency ) == false)
				|| (this.assetIntervalFrequency == null && assetIntervalFrequency != null) ){
			return true;
		}else if( (this.assetIntervalNumAssets != null && this.assetIntervalNumAssets.equals( assetIntervalNumAssets ) == false)
				|| (this.assetIntervalNumAssets == null && assetIntervalNumAssets != null) ){
			return true;
		}else if( (this.assetIntervalUnits != null && this.assetIntervalUnits.equals( assetIntervalUnits ) == false)
				|| (this.assetIntervalUnits == null && assetIntervalUnits != null) ){
			return true;
		}		
		return false;
	}	
	
	/**
	 * 
	 *
	 */
	public void makeDirty(boolean makeDevicesDirty) throws HibernateException
	{		
		// If there is not a dirty object for this object		
		Dirty d = Dirty.getDirty( this.getEntityId() );		
		if(d == null)
		{		
			// Create a new dirty object
			d = new Dirty();			
			d.setDirtyEntityId( this.getEntityId() );
			d.setDirtyType( DirtyType.SEGMENT );
			d.save();
		}else{
			d.setDirtyType( DirtyType.SEGMENT );
			d.update();
		}
		
		// Make dirty any devices that contain this segment
		if( makeDevicesDirty ){		
			Device.makeDirty( this );
		}
	}
	
	/**
	 * Make dirty any segments that contain the given playlist.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static void makeDirty(Playlist p) throws HibernateException
	{					
		Session session = HibernateSession.currentSession();		
		String hql = "SELECT distinct ps.segment "
				+ "FROM PlaylistSegmentPart as  ps "				
				+ "WHERE ps.playlist.playlistId = "+ p.getPlaylistId().toString() + " "
				+ "OR (ps.segment.assetIntervalPlaylist != null "
				+ "AND ps.segment.assetIntervalPlaylist.playlistId = "+ p.getPlaylistId().toString() +")";
		List<Segment> l = session.createQuery( hql ).list();				
		for( Iterator<Segment> i=l.iterator(); i.hasNext(); ){
			Segment s = i.next();
			s.makeDirty( false );
		}		
	}	
	
	/**
	 * Make dirty any segments that contain the given asset.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static void makeDirty(Asset a) throws HibernateException
	{					
		Session session = HibernateSession.currentSession();		
		String hql = "SELECT distinct asp.segment "
				+ "FROM AssetSegmentPart as  asp "				
				+ "WHERE asp.asset.assetId = "+ a.getAssetId().toString();				
		List<Segment> l = session.createQuery( hql ).list();				
		for( Iterator<Segment> i=l.iterator(); i.hasNext(); ){
			Segment s = i.next();
			s.makeDirty( false );
		}		
	}	
	
	/**
	 * Returns all segments that have a dirty status
	 * @return 
	 */
	public static List getDirtySegments() throws HibernateException 
	{			
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT s "
					+ "FROM Segment s, Dirty d "
					+ "WHERE s.segmentId = d.dirtyEntityId "
					+ "AND d.dirtyType = '"+ DirtyType.SEGMENT.getPersistentValue() + "'";		
		List l = session.createQuery( hql ).list(); 
		return l; 	
	}	
	
	/**
	 * This method is called from the publishAll method. It will
	 * take the updated lengths for each playlist/asset in this segment
	 * and update the total length of the playlist.	 
	 */
	public void updateLength() throws HibernateException
	{
		float segmentLength = new Float(0).floatValue();
		
		// If the segment end type is "When Finished", the segment length
		// should be the sum of the lengths of the segment parts
		if( this.getEndType().equals( SegmentEndType.WHEN_FINISHED ) )
		{
			Iterator i = this.segmentParts.iterator();
			while(i.hasNext())
			{
				SegmentPart sp = (SegmentPart)i.next();
				
				if(sp instanceof PlaylistSegmentPart)
				{
					PlaylistSegmentPart psp = (PlaylistSegmentPart)sp;
					segmentLength += psp.getPlaylist().getLength().floatValue();
				}
				else if(sp instanceof AssetSegmentPart)
				{
					AssetSegmentPart asp = (AssetSegmentPart)sp;
					segmentLength += asp.getAsset().getAssetPresentation().getLength().floatValue();
				}			
			}
		}
		// If the segment end type is "END AFTER", the segment length  
		// should be the amount of time specified by the endAfterUnits values
		else if( this.getEndType().equals( SegmentEndType.END_AFTER ) )
		{
			// If we're ending after a specified number of minutes
			if( this.getEndAfterUnits().equals( SegmentEndAfterUnit.MINUTES ) )
			{
				// Convert segment length to seconds
				segmentLength = this.getEndAfter().intValue() * 60;
			}			
			// If we're ending after a specified number of hours
			else if( this.getEndAfterUnits().equals( SegmentEndAfterUnit.HOURS ) )
			{
				// Convert segment length to seconds
				segmentLength = this.getEndAfter().intValue() * 60 * 60;
			}				
			// If we're ending after a specified number of hours
			else if( this.getEndAfterUnits().equals( SegmentEndAfterUnit.SECONDS ) )
			{
				// Convert segment length to seconds
				segmentLength = this.getEndAfter().intValue();
			}				
		}
		
		// Update the length of this segment
		this.setLength( new Float(segmentLength) );
		this.update();
	}
	
	/**
	 * Update the lengths of all segments that contain the given playlist.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static void updateLengths(Playlist p) throws HibernateException
	{					
		Session session = HibernateSession.currentSession();		
		String hql = "SELECT distinct ps.segment "
				+ "FROM PlaylistSegmentPart as  ps "				
				+ "WHERE ps.playlist.playlistId = "+ p.getPlaylistId().toString();				
		List l = session.createQuery( hql ).list();				
		for( Iterator i=l.iterator(); i.hasNext(); )
		{
			Segment s = (Segment)i.next();
			s.updateLength();
		}		
	}	
	
	/**
	 * Update the lengths of all segments that contain the given playlist.
	 * 
	 * @param a
	 * @throws HibernateException
	 */
	public static List updateLengths(ArrayList playlistIds) throws HibernateException
	{					
		Session session = HibernateSession.currentSession();		
		String hql = "SELECT distinct ps.segment "
				+ "FROM PlaylistSegmentPart as  ps "				
				+ "WHERE ps.playlist.playlistId IN (:list)";				
		List l = session.createQuery( hql ).setParameterList("list", playlistIds).list();		
		
		for(Iterator i = l.iterator();i.hasNext();)
		{
			Segment s = (Segment)i.next();
			s.updateLength();
		}		
		
		return l;
	}	
	
	/**
	 * Gets the count of the assets according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @return
	 * @throws ParseException
	 */
	public static int searchSegmentsCount(SegmentSearchType segmentSearchType, AttrDefinition attrDefinition, String segmentNameSearchString, String selectedSegmentGroup, String selectedSearchOption, String searchString, String[] selectedSearchOptions, 
			String minDate, String maxDate, String minNumber, String maxNumber, int startingRecord) throws ParseException
	{
		int result = 0;
		String hql = buildSearchHql( segmentSearchType, segmentNameSearchString, selectedSegmentGroup, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, null, true, false );		
		
		// If an attrDefinition object was not passed in, we must be searching by device group		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = KMMServlet.getRecordCount( hql, selectedSearchOptions, "attrValues" );					
			}else{
				result = KMMServlet.getRecordCount( hql );
			}
		}	
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) };
				result = KMMServlet.getRecordCount( hql, params );
			}		
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = KMMServlet.getRecordCount( hql );
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = KMMServlet.getRecordCount( hql, params );	
			}		
		}
		return result;
	}
	
	/**
	 * Returns a page of devices according to the given search criteria.
	 * 
	 * @param attrDefinition
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param minDate
	 * @param maxDate
	 * @param minNumber
	 * @param maxNumber
	 * @param startingRecord
	 * @param selectedItemsPerPage
	 * @return
	 * @throws ParseException
	 */
	public static Page searchSegments(SegmentSearchType segmentSearchType, AttrDefinition attrDefinition, String segmentNameSearchString, String selectedSegmentGroupId, 
			String selectedSearchOption, String searchString, String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, 
			String maxNumber, String orderBy, int startingRecord, int selectedItemsPerPage, boolean isEntityOverviewPage) throws ParseException
	{
		Page result = null;
		int pageNum = startingRecord / selectedItemsPerPage;
		String hql = buildSearchHql( segmentSearchType, segmentNameSearchString, selectedSegmentGroupId, selectedSearchOption, searchString, selectedSearchOptions, minDate, maxDate, minNumber, maxNumber, orderBy, false, isEntityOverviewPage );
		
		// If an attrDefinition object was not passed in, we must be searching by asset type		
		if( attrDefinition == null || attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
		{			
			// If we're filtering by last modified date and both the min and max dates were not left blank			
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) 
					&& ((minDate != null && minDate.length() > 0) && (maxDate != null && maxDate.length() > 0)) )
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = Segment.getSegments( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );
			}
			// If this is a multi-select attrDefinition
			else if( attrDefinition != null 
					&& attrDefinition.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) 
					&& selectedSearchOptions != null && selectedSearchOptions.length > 0 )
			{
				// Use the selectedSearchOptions a named parameter in the query object 
				result = Segment.getSegments( hql, pageNum, selectedItemsPerPage, selectedSearchOptions, "attrValues", isEntityOverviewPage );
			}else{
				result = Segment.getSegments( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );
			}
		}
		// Date query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
		{
			// If both the min and max dates were left blank
			if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
			{
				// Exclude the params from the query
				result = Segment.getSegments( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );	
			}
			else
			{
				// Build the param array to use in the query object
				SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
				Date[] params = new Date[]{ df.parse( minDate ), df.parse( maxDate ) }; 
				result = Segment.getSegments( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );					
			}			
		}
		// Number query
		else if( attrDefinition.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
		{
			// If both the min and max numbers were left blank
			if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
			{
				// Exclude the params from the query
				result = Segment.getSegments( hql, pageNum, selectedItemsPerPage, null, null, isEntityOverviewPage );	
			}
			else
			{
				// Build the param array to use in the query object
				Float[] params = new Float[]{ new Float(minNumber), new Float(maxNumber) }; 
				result = Segment.getSegments( hql, pageNum, selectedItemsPerPage, params, null, isEntityOverviewPage );
			}			
		}
		return result;
	}	
	
	/**
	 * Builds the hql to retrieve assets according to the given search criteria.
	 *  
	 * @param assetNameSearchString
	 * @param selectedSearchOption
	 * @param searchString
	 * @param selectedSearchOptions
	 * @param getCount
	 * @return
	 */
	private static String buildSearchHql(SegmentSearchType segmentSearchType, String segmentSearchString, String selectedSegmentGroupId, String selectedSearchOption, String searchString, 
			String[] selectedSearchOptions, String minDate, String maxDate, String minNumber, String maxNumber, String orderBy, boolean getCount, boolean isEntityOverviewPage)
	{
		String hql = "";
		
		// If the segmentName search string was left blank, use wildcard
		if( segmentSearchString == null || segmentSearchString.trim().length() == 0 ){
			segmentSearchString = "%";
		}
		
		// Imply *
		if(segmentSearchType.equals(SegmentSearchType.SEGMENT_ID) == false){
			if(segmentSearchString.startsWith("*") == false){
				segmentSearchString = "*" + segmentSearchString;
			}
			if(segmentSearchString.endsWith("*") == false){
				segmentSearchString = segmentSearchString + "*";
			}
		}
		if(searchString != null && searchString.length() > 0){
			if(searchString.startsWith("*") == false){
				searchString = "*" + searchString;
			}
			if(searchString.endsWith("*") == false){
				searchString = searchString + "*";
			}
		}
		
		// Convert any "*" to "%" for wildcard searches
		segmentSearchString = segmentSearchString.replaceAll("\\*", "\\%");	
		segmentSearchString = Reformat.oraesc(segmentSearchString);		

		// Default
		if( orderBy == null || orderBy.length() == 0 ){
			orderBy = "UPPER(segment.segmentName)";
		}
		
		// If we are counting the number of records
		if( getCount == true) {
			hql = "SELECT COUNT(segment) ";
		} else {
			if( isEntityOverviewPage ){
				hql = "SELECT segment, ei.lastModified ";
			}else{
				hql = "SELECT segment ";
			}
		}

		/*
		 * If the "No Metadata", or "Date Modified" option was selected, exclude the metadata from the search criteria 
		 */
		boolean excludeMetadataCriteria = false;
		if( selectedSearchOption.equalsIgnoreCase( Constants.NO_METADATA ) || selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ))
		{
			excludeMetadataCriteria = true;
		}
		else
		{
			/*
			 * Search by this attr definition
			 */
			AttrDefinition ad = AttrDefinition.getAttrDefinition( new Long( selectedSearchOption ) );
			if( ad != null)
			{				
				// If this is a String attr
				if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					// If this is a multi-select
					if( ad.getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) )
					{
						// If no items were selected
						if( selectedSearchOptions.length == 0 )
						{
							// Exclude the attrDefinition criteria							
							excludeMetadataCriteria = true;
						}
						else
						{						
							// Get all devices that have a StringAttr with the given criteria
							hql += "FROM Segment as segment ";
							if( isEntityOverviewPage ){
								hql += ", EntityInstance as ei "+
									"WHERE segment.segmentId = ei.entityId "+
									"AND ";
							}else{
								hql += " WHERE ";
							}
							hql	+= "segment.segmentId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND attr.value IN (:attrValues) ) ";
														
							// If the "All Devices" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedSegmentGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND segment.segmentId IN "
								+ 	" (SELECT segment.segmentId "
								+ 	"	FROM SegmentGrpMember as sgm "
								+ 	"	JOIN sgm.segment as segment "
								+ 	"	WHERE sgm.grp.grpId = '"+ selectedSegmentGroupId +"') ";																		
							}
							if(segmentSearchType.equals(SegmentSearchType.SEGMENT_NAME)){
								hql += "AND UPPER(segment.segmentName) LIKE UPPER('"+ segmentSearchString +"') "; 
							}else if(segmentSearchType.equals(SegmentSearchType.SEGMENT_ID)){
								hql += "AND segment.segmentId = "+ segmentSearchString +" ";
							}
							hql += 	"ORDER BY " + orderBy;									
						}																		
					}
					else
					{						
						// If the searchString was left blank 
						if( searchString == null || searchString.trim().length() == 0 )
						{
							// Exclude the attrDefinition criteria					
							excludeMetadataCriteria = true;						
						}
						else
						{
							// Convert any "*" to "%" for wildcard searches		
							searchString = searchString.replaceAll("\\*", "\\%");	
							searchString = Reformat.oraesc(searchString);											
							
							// Get all devices that have a StringAttr with the given criteria
							hql += "FROM Segment as segment ";
							if( isEntityOverviewPage ){
								hql += ", EntityInstance as ei "+
									"WHERE segment.segmentId = ei.entityId "+
									"AND ";
							}else{
								hql += " WHERE ";
							}
							hql	+= "segment.segmentId IN "
								+ 	"(SELECT attr.ownerId "
								+	" FROM StringAttr attr "
								+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
								+	" AND UPPER(attr.value) LIKE UPPER ('"+ searchString +"') ) ";
							// If the "All Devices" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedSegmentGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND segment.segmentId IN "
								+ 	" (SELECT segment.segmentId "
								+ 	"	FROM SegmentGrpMember as sgm "
								+ 	"	JOIN sgm.segment as segment "
								+ 	"	WHERE sgm.grp.grpId = '"+ selectedSegmentGroupId +"') ";																		
							}							
							if(segmentSearchType.equals(SegmentSearchType.SEGMENT_NAME)){
								hql += "AND UPPER(segment.segmentName) LIKE UPPER('"+ segmentSearchString +"') "; 
							}else if(segmentSearchType.equals(SegmentSearchType.SEGMENT_ID)){
								hql += "AND segment.segmentId = "+ segmentSearchString +" ";
							}
							hql += 	"ORDER BY " + orderBy;
						}										
					}
				}
				// If this is a Date attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					// If both the min and max dates were left blank
					if( (minDate == null || minDate.length() == 0) && (maxDate == null || maxDate.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a DateAttr.value between the two dates
						hql += "FROM Segment as segment ";
						if( isEntityOverviewPage ){
							hql += ", EntityInstance as ei "+
								"WHERE segment.segmentId = ei.entityId "+
								"AND ";
						}else{
							hql += " WHERE ";
						}
						hql	+= "segment.segmentId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM DateAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						
							// If the "All Segments" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
							if( selectedSegmentGroupId.equalsIgnoreCase("-1") == false )
							{					
								hql += "AND segment.segmentId IN "
								+ 	" (SELECT segment.segmentId "
								+ 	"	FROM SegmentGrpMember as sgm "
								+ 	"	JOIN sgm.segment as segment "
								+ 	"	WHERE sgm.grp.grpId = '"+ selectedSegmentGroupId +"') ";																		
							}							
							if(segmentSearchType.equals(SegmentSearchType.SEGMENT_NAME)){
								hql += "AND UPPER(segment.segmentName) LIKE UPPER('"+ segmentSearchString +"') "; 
							}else if(segmentSearchType.equals(SegmentSearchType.SEGMENT_ID)){
								hql += "AND segment.segmentId = "+ segmentSearchString +" ";
							}
							hql += 	"ORDER BY " + orderBy;
					}
				}
				// If this is a Number attr
				else if( ad.getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					// If both the min and max numbers were left blank
					if( (minNumber == null || minNumber.length() == 0) && (maxNumber == null || maxNumber.length() == 0 ) )
					{
						// Exclude the metadata criteria in the query
						excludeMetadataCriteria = true;
					}
					else
					{
						// Get all assets that have a NumberAttr.value between the two dates
						hql += "FROM Segment as segment ";
						if( isEntityOverviewPage ){
							hql += ", EntityInstance as ei "+
								"WHERE segment.segmentId = ei.entityId "+
								"AND ";
						}else{
							hql += " WHERE ";
						}
						hql	+= "segment.segmentId IN "
							+ 	"(SELECT attr.ownerId "
							+	" FROM NumberAttr attr "
							+	" WHERE attr.attrDefinition.attrDefinitionId = "+ ad.getAttrDefinitionId() +" "
							+	" AND attr.value >= ? "
							+	" AND attr.value <= ? ) ";
						// If the "All Segments" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
						if( selectedSegmentGroupId.equalsIgnoreCase("-1") == false )
						{					
							hql += "AND segment.segmentId IN "
							+ 	" (SELECT segment.segmentId "
							+ 	"	FROM SegmentGrpMember as sgm "
							+ 	"	JOIN sgm.segment as segment "
							+ 	"	WHERE sgm.grp.grpId = '"+ selectedSegmentGroupId +"') ";																		
						}						
						if(segmentSearchType.equals(SegmentSearchType.SEGMENT_NAME)){
							hql += "AND UPPER(segment.segmentName) LIKE UPPER('"+ segmentSearchString +"') "; 
						}else if(segmentSearchType.equals(SegmentSearchType.SEGMENT_ID)){
							hql += "AND segment.segmentId = "+ segmentSearchString +" ";
						}
						hql += 	"ORDER BY " + orderBy;
					}
				}				
			}
		}
	
		// If we are excluding the metadata criteria in the query
		if( excludeMetadataCriteria )
		{
			hql += "FROM Segment as segment ";
			if( isEntityOverviewPage ){
				hql += ", EntityInstance as ei "+
					"WHERE segment.segmentId = ei.entityId "+
					"AND ";
			}else{
				hql += " WHERE ";
			}
			
			if(segmentSearchType.equals(SegmentSearchType.SEGMENT_NAME)){
				hql += "UPPER(segment.segmentName) LIKE UPPER('"+ segmentSearchString +"') "; 
			}else if(segmentSearchType.equals(SegmentSearchType.SEGMENT_ID)){
				hql += "segment.segmentId = "+ segmentSearchString +" ";
			}

			// If the "All Segments" group id ("-1") was not passed in -- limit the search to the given deviceGroupId
			if( selectedSegmentGroupId.equalsIgnoreCase("-1") == false )
			{					
				hql += "AND segment.segmentId IN "
				+ 	" (SELECT segment.segmentId "
				+ 	"	FROM SegmentGrpMember as sgm "
				+ 	"	JOIN sgm.segment as segment "
				+ 	"	WHERE sgm.grp.grpId = '"+ selectedSegmentGroupId +"') ";																		
			}		
			// If we're filtering by last modified date
			if( selectedSearchOption.equalsIgnoreCase( Constants.DATE_MODIFIED ) )
			{
				hql +=   " AND segment.segmentId IN "
					+	"(SELECT ei.entityId "
					+	" FROM EntityInstance as ei "	
					+ 	" WHERE ei.entityClass.className = '"+ Segment.class.getName() +"' "
					+	" AND ei.lastModified >= ? "
					+	" AND ei.lastModified <= ?) ";					
			}
			hql += 	"ORDER BY " + orderBy;
		}
		return hql;
	}	
	
	/**
	 * @return Returns a page of displayarea
	 */
	public static Page getSegments(String hql, int pageNum, int iSelectedItemsPerPage, Object[] params, String namedParameter, boolean isEntityOverviewPage) throws HibernateException 
	{
		EntityOverviewPage result = null;		
		Session session = HibernateSession.currentSession();		
		Query q = session.createQuery( hql );

		// If the params parameter was passed in, use them in the query
		if( params != null )
		{
			// If a namedParameter was passed in -- use it
			// This is required because .setParameterList(int, Object[]) is not supported
			if( namedParameter != null ){
				q.setParameterList( namedParameter, params );
			}
			else{
				for( int i=0; i<params.length; i++ ){
					q.setParameter( i, params[i] );
				}
			}
		}
		
		if( isEntityOverviewPage ){
			return new EntityOverviewPage( q, pageNum, iSelectedItemsPerPage );
		}else{
			return new EntityResultsPage( q, pageNum, iSelectedItemsPerPage );	
		}
	}		
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List getSegmentsWithAssetIntervalPlaylist(Playlist playlist) throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		String hql = "SELECT s "
			+ "FROM Segment as s "				
			+ "WHERE s.assetIntervalPlaylist.playlistId = "+ playlist.getPlaylistId();
		return session.createQuery( hql ).list();	
	}		
	
	/**
	 * Copies this device and assigns the given new segment name.
	 * 
	 * @param newDeviceName
	 * @return
	 */
	public Long copy(String newSegmentName) throws ClassNotFoundException
	{				
		// First, create a new segment object
		Segment newSegment = new Segment();
		newSegment.setSegmentName( newSegmentName );
		newSegment.setLength( this.getLength() );
		newSegment.setStartDatetime( this.getStartDatetime() );
		newSegment.setEndType( this.getEndType() );
		newSegment.setEndAfter( this.getEndAfter() );
		newSegment.setEndAfterNumAssets( this.getEndAfterNumAssets() );
		newSegment.setEndAfterUnits( this.getEndAfterUnits() );
		newSegment.setPriority( this.getPriority() );		
		newSegment.setInterruptionType( this.getInterruptionType() );
		newSegment.setUseServerTime( this.getUseServerTime() );
		newSegment.setStartBeforeTolerance( this.getStartBeforeTolerance() );
		newSegment.setStartBeforeToleranceUnits( this.getStartBeforeToleranceUnits() );
		newSegment.setStartAfterTolerance( this.getStartAfterTolerance() );
		newSegment.setStartAfterToleranceUnits( this.getStartAfterToleranceUnits() );
		newSegment.setEndBeforeTolerance( this.getEndBeforeTolerance() );
		newSegment.setEndBeforeToleranceUnits( this.getEndBeforeToleranceUnits() );
		newSegment.setEndAfterTolerance( this.getEndAfterTolerance() );
		newSegment.setEndAfterToleranceUnits( this.getEndAfterToleranceUnits() );
		newSegment.setUseAssetIntervalPlaylist( this.getUseAssetIntervalPlaylist() );
		newSegment.setAssetIntervalFrequency( this.getAssetIntervalFrequency() );
		newSegment.setAssetIntervalNumAssets( this.getAssetIntervalNumAssets() );
		newSegment.setAssetIntervalPlaylist( this.getAssetIntervalPlaylist() );
		newSegment.setAssetIntervalUnits( this.getAssetIntervalUnits() );
		newSegment.setType( this.getType() );
					
		// Save the segment but do not create permission entries since we are going to copy them		
		Long newSegmentId = newSegment.save( false );
		newSegment.copyPermissionEntries( this );							

		// Second, copy all the segment group members		
		for( Iterator i = this.getSegmentGrpMembers().iterator(); i.hasNext(); )
		{
			SegmentGrpMember sgm = (SegmentGrpMember)i.next();
			SegmentGrpMember newGrpMember = new SegmentGrpMember();
			newGrpMember.setGrp( sgm.getGrp() );
			newGrpMember.setSegment( newSegment );
			newGrpMember.save();			
		}
		
		// Third, copy all device schedules associated with this device
		for( Iterator i= this.getDeviceSchedules().iterator(); i.hasNext(); )
		{
			DeviceSchedule ds = (DeviceSchedule)i.next();
			DeviceSchedule newDeviceSchedule = new DeviceSchedule();			
			newDeviceSchedule.setSegment( newSegment );
			newDeviceSchedule.setDevice( ds.getDevice() );			
			newDeviceSchedule.save();			
		}		
		
		// Fourth, copy any segment parts
		for( Iterator i=this.getSegmentParts().iterator(); i.hasNext(); )
		{
			SegmentPart segmentPart = (SegmentPart)i.next();
			if( segmentPart instanceof PlaylistSegmentPart )
			{
				PlaylistSegmentPart playlistSegmentPart = (PlaylistSegmentPart)segmentPart;
				PlaylistSegmentPart newPlaylistSegmentPart = new PlaylistSegmentPart();
				newPlaylistSegmentPart.setPlaylist( playlistSegmentPart.getPlaylist() );
				newPlaylistSegmentPart.setSegment( newSegment );
				newPlaylistSegmentPart.setSeqNum( playlistSegmentPart.getSeqNum() );
				newPlaylistSegmentPart.save();
			}
			else if( segmentPart instanceof AssetSegmentPart )
			{
				AssetSegmentPart assetSegmentPart = (AssetSegmentPart)segmentPart;
				AssetSegmentPart newAssetSegmentPart = new AssetSegmentPart();
				newAssetSegmentPart.setAsset( assetSegmentPart.getAsset() );
				newAssetSegmentPart.setSegment( newSegment );
				newAssetSegmentPart.setSeqNum( assetSegmentPart.getSeqNum() );
				newAssetSegmentPart.save();
			}						
		}
		
		// Fifth, copy any recurrence
		if( this.getRecurrence() != null ) {
			this.getRecurrence().copy( newSegment );	
		}
					
		// Copy any segment metadata
		this.copyMetadata( newSegment.getSegmentId() );
		return newSegmentId;
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
	 * Returns the list of device schedules sorted by device name
	 * @return
	 */
	public LinkedList<DeviceScheduleInfo> getSortedDevicesScheduleInfos()
	{
		if((deviceSchedules != null) && (deviceSchedulesSorted == null))
		{				
			Session session = HibernateSession.currentSession();
			
			String hql = "SELECT ds.deviceScheduleId, ds.device.deviceId, ds.device.deviceName FROM DeviceSchedule as ds "
				+ "WHERE ds.segment.segmentId = ? AND (device.deleted IS NULL OR device.deleted != ?) "
				+ "ORDER BY UPPER(ds.device.deviceName)";
			
			List<Object[]> l = session.createQuery( hql ).setParameter(0, this.getSegmentId()).setParameter(1, Boolean.TRUE).list();
			
			deviceSchedulesSorted = new LinkedList<DeviceScheduleInfo>();
			
			// For each row returned
			for(Object[] o : l){
				// Create new deviceScheduleInfo Objects
				DeviceScheduleInfo dsi = this.new DeviceScheduleInfo();
				dsi.setDeviceScheduleId((Long)o[0]);
				dsi.setDeviceId((Long)o[1]);
				dsi.setDeviceName((String)o[2]);
				deviceSchedulesSorted.add(dsi);
			}
		}
		return deviceSchedulesSorted;
	}
	
	public boolean isExpired(Date when){
		boolean result = false;
		
		Recurrence recurrence = this.getRecurrence();
		if(recurrence != null){
			// Never expires
			if(recurrence.getEndType().equals(RecurrenceEndType.NO_END)){
				result = false;
			}
			// Will recur on this date. Expires the next day
			else if(recurrence.getEndType().equals(RecurrenceEndType.ON_DATE)){
				if( (when.getTime() - recurrence.getEndAfterDate().getTime()) / Constants.MILLISECONDS_IN_A_DAY >= 1){
					result = true;
				}
			}
			// Expire as long as the last recurrence was prior to today
			else if(recurrence.getEndType().equals(RecurrenceEndType.END_AFTER)){
				if(recurrence.getRecurrenceType().equals(RecurrenceType.DAILY)){
					DailyRecurrence dr = (DailyRecurrence)recurrence;
					long timeDiff = when.getTime() - recurrence.getStartDate().getTime();
					long numDays = timeDiff / Constants.MILLISECONDS_IN_A_DAY;
					long numOccurrences = numDays / dr.getDailyFrequency();
					if(numOccurrences > recurrence.getEndAfterOccurrences()){
						result = true;
					}else{
						float floatOccurrences = numDays / dr.getDailyFrequency();
						// If the last occurrence has already occurred on a previous day
						if((floatOccurrences - numOccurrences) * dr.getDailyFrequency() >= 1){
							result = true;
						}
					}
				}else if(recurrence.getRecurrenceType().equals(RecurrenceType.WEEKLY)){
					WeeklyRecurrence wr = (WeeklyRecurrence)recurrence;
					long timeDiff = when.getTime() - recurrence.getStartDate().getTime();
					long numWeeks = timeDiff / (Constants.MILLISECONDS_IN_A_DAY * 7);
					long numRecurrences = (numWeeks / wr.getWeeklyFrequency()) * wr.getSelectedDays(false).size();
					if(numRecurrences > recurrence.getEndAfterOccurrences()){
						result = true;
					}
					// See if this is the last week of occurrences
					else if(recurrence.getEndAfterOccurrences() - numRecurrences <= wr.getSelectedDays(false).size()){
						Calendar today = Calendar.getInstance();
						Calendar recurrenceStart = Calendar.getInstance();
						recurrenceStart.setTime(recurrence.getStartDate());
						recurrenceStart.add(Calendar.WEEK_OF_YEAR, (int)(numRecurrences * wr.getWeeklyFrequency() / wr.getSelectedDays(false).size()));
						
						List<String> selectedDays = wr.getSelectedDays(false);
						
						// Go through the days of the last recurrence cycle till today
						int recurrencesThisCycle = 0;
						Calendar lastRecurrence = null;
						for(; recurrenceStart.before(today); recurrenceStart.add(Calendar.DATE, 1)){
							switch(recurrenceStart.get(Calendar.DAY_OF_WEEK)){
								case Calendar.SUNDAY:
									if(selectedDays.contains("sun")){
										recurrencesThisCycle++;
									}
									break;
								case Calendar.MONDAY:
									if(selectedDays.contains("mon")){
										recurrencesThisCycle++;
									}
									break;
								case Calendar.TUESDAY:
									if(selectedDays.contains("tue")){
										recurrencesThisCycle++;
									}
									break;
								case Calendar.WEDNESDAY:
									if(selectedDays.contains("wed")){
										recurrencesThisCycle++;
									}
									break;
								case Calendar.THURSDAY:
									if(selectedDays.contains("thu")){
										recurrencesThisCycle++;
									}
									break;
								case Calendar.FRIDAY:
									if(selectedDays.contains("fri")){
										recurrencesThisCycle++;
									}
									break;
								case Calendar.SATURDAY:
									if(selectedDays.contains("sat")){
										recurrencesThisCycle++;
									}
									break;
							}
							
							if(recurrencesThisCycle == wr.getSelectedDays(false).size()){
								lastRecurrence = recurrenceStart;
								break;
							}
						}
						
						numRecurrences += recurrencesThisCycle;
						
						// If the first occurrence is not a recurrence
						if(recurrence.getSegment().getStartDatetime().equals(recurrence.getStartDate())){
							numRecurrences--;
						}
						
						if(numRecurrences > recurrence.getEndAfterOccurrences()){
							result = true;
						}else if(numRecurrences == recurrence.getEndAfterOccurrences() && lastRecurrence != null){
							// Make sure the last recurrence has ended
							if( (when.getTime() - (lastRecurrence.getTime().getTime() + (this.getLength() * 1000) )) / Constants.MILLISECONDS_IN_A_DAY >= 1){
								result = true;
							}
						}
					}
				}
			}
		}else{
			// Expire by date
			if( (when.getTime() - (this.getStartDatetime().getTime() + (this.getLength() * 1000) )) / Constants.MILLISECONDS_IN_A_DAY >= 1){
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * @return Returns the segmentGrpMembers.
	 */
	public Set<SegmentGrpMember> getSegmentGrpMembers() {
		return segmentGrpMembers;
	}

	/**
	 * @param segmentGrpMembers The segmentGrpMembers to set.
	 */
	public void setSegmentGrpMembers(Set<SegmentGrpMember> segmentGrpMembers) {
		this.segmentGrpMembers = segmentGrpMembers;
	}
	
	/**
	 * Returns the list of SegmentGrpMembers sorted by grp name
	 * @return
	 */
	public List<SegmentGrpMember> getSegmentGrpMembersSorted()
	{
		ArrayList<SegmentGrpMember> result = new ArrayList<SegmentGrpMember>( this.getSegmentGrpMembers() );
		if( this.getSegmentGrpMembers() != null ){			
			BeanComparator comparator1 = new BeanComparator("grpName");
			BeanComparator comparator2 = new BeanComparator("grp", comparator1);
			Collections.sort( result, comparator2 );
		}
		return result;
	}	


	/**
	 * @return the segmentParts
	 */
	public List<SegmentPart> getSegmentParts() {
		return segmentParts;
	}
	/**
	 * @param segmentParts the segmentParts to set
	 */
	public void setSegmentParts(List<SegmentPart> segmentParts) {
		this.segmentParts = segmentParts;
	}
	/**
	 * @return the deviceSchedules
	 */
	public Set<DeviceSchedule> getDeviceSchedules() {
		return deviceSchedules;
	}
	/**
	 * @param deviceSchedules the deviceSchedules to set
	 */
	public void setDeviceSchedules(Set<DeviceSchedule> deviceSchedules) {
		this.deviceSchedules = deviceSchedules;
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
	 * @return Returns the endAfter.
	 */
	public Integer getEndAfter() {
		return endAfter;
	}

	/**
	 * @param endAfter The endAfter to set.
	 */
	public void setEndAfter(Integer endAfter) {
		this.endAfter = endAfter;
	}

	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDatetime() {
		return startDatetime;
	}

	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDatetime(Date startDatetime) {
		this.startDatetime = startDatetime;
	}

	/**
	 * @return Returns the endType.
	 */
	public SegmentEndType getEndType() {
		return endType;
	}

	/**
	 * @param endType The endType to set.
	 */
	public void setEndType(SegmentEndType endType) {
		this.endType = endType;
	}

	/**
	 * @return Returns the interruptionType.
	 */
	public SegmentInterruptionType getInterruptionType() {
		return interruptionType;
	}

	/**
	 * @param interruptionType The interruptionType to set.
	 */
	public void setInterruptionType(SegmentInterruptionType interruptionType) {
		this.interruptionType = interruptionType;
	}

	/**
	 * @return Returns the endAfterUnit.
	 */
	public SegmentEndAfterUnit getEndAfterUnits() {
		return endAfterUnits;
	}

	/**
	 * @param endAfterUnit The endAfterUnit to set.
	 */
	public void setEndAfterUnits(SegmentEndAfterUnit endAfterUnits) {
		this.endAfterUnits = endAfterUnits;
	}

	/**
	 * @return Returns the priority.
	 */
	public SegmentPriority getPriority() {
		return priority;
	}

	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(SegmentPriority priority) {
		this.priority = priority;
	}

	/**
	 * @return Returns the useServerTime.
	 */
	public TrueFalseUserType getUseServerTime() {
		return useServerTime;
	}

	/**
	 * @param useServerTime The useServerTime to set.
	 */
	public void setUseServerTime(TrueFalseUserType useServerTime) {
		this.useServerTime = useServerTime;
	}
    /**
     * @return Returns the recurrence.
     */
    public Recurrence getRecurrence() throws HibernateException 
    {
    	if( recurrence == null )
    	{
    		Session session = HibernateSession.currentSession();	    				
    		recurrence = (Recurrence)session.createCriteria(Recurrence.class)
    				.add( Expression.eq("segment.segmentId", this.getSegmentId()) )
    				.uniqueResult();        		
    	}
        return recurrence;
    }
    /**
     * @param recurrence The recurrence to set.
     */
    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

	
	/**
	 * @return Returns the endAfterTolerance.
	 */
	public Integer getEndAfterTolerance() {
		return endAfterTolerance;
	}
	
	/**
	 * @param endAfterTolerance The endAfterTolerance to set.
	 */
	public void setEndAfterTolerance(Integer endAfterTolerance) {
		this.endAfterTolerance = endAfterTolerance;
	}
	
	/**
	 * @return Returns the endAfterToleranceUnits.
	 */
	public InterruptionToleranceUnit getEndAfterToleranceUnits() {
		return endAfterToleranceUnits;
	}
	
	/**
	 * @param endAfterToleranceUnits The endAfterToleranceUnits to set.
	 */
	public void setEndAfterToleranceUnits(
			InterruptionToleranceUnit endAfterToleranceUnits) {
		this.endAfterToleranceUnits = endAfterToleranceUnits;
	}
	
	/**
	 * @return Returns the assetIntervalFrequency.
	 */
	public Integer getAssetIntervalFrequency() {
		return assetIntervalFrequency;
	}
	
	/**
	 * @param assetIntervalFrequency The assetIntervalFrequency to set.
	 */
	public void setAssetIntervalFrequency(Integer assetIntervalFrequency) {
		this.assetIntervalFrequency = assetIntervalFrequency;
	}
	
	/**
	 * @return Returns the assetIntervalNumAssets.
	 */
	public Integer getAssetIntervalNumAssets() {
		return assetIntervalNumAssets;
	}
	
	/**
	 * @param assetIntervalNumAssets The assetIntervalNumAssets to set.
	 */
	public void setAssetIntervalNumAssets(Integer assetIntervalNumAssets) {
		this.assetIntervalNumAssets = assetIntervalNumAssets;
	}
	
	/**
	 * @return Returns the assetIntervalPlaylist.
	 */
	public Playlist getAssetIntervalPlaylist() {
		return assetIntervalPlaylist;
	}
	
	/**
	 * @param assetIntervalPlaylist The assetIntervalPlaylist to set.
	 */
	public void setAssetIntervalPlaylist(Playlist assetIntervalPlaylist) {
		this.assetIntervalPlaylist = assetIntervalPlaylist;
	}
	
	/**
	 * @return Returns the endAfterNumAssets.
	 */
	public Integer getEndAfterNumAssets() {
		return endAfterNumAssets;
	}
	
	/**
	 * @param endAfterNumAssets The endAfterNumAssets to set.
	 */
	public void setEndAfterNumAssets(Integer endAfterNumAssets) {
		this.endAfterNumAssets = endAfterNumAssets;
	}
	
	/**
	 * @return Returns the useAssetIntervalPlaylist.
	 */
	public Boolean getUseAssetIntervalPlaylist() {
		return useAssetIntervalPlaylist;
	}
	
	/**
	 * @param useAssetIntervalPlaylist The useAssetIntervalPlaylist to set.
	 */
	public void setUseAssetIntervalPlaylist(Boolean useAssetIntervalPlaylist) {
		this.useAssetIntervalPlaylist = useAssetIntervalPlaylist;
	}
	
	/**
	 * @param deviceSchedulesSorted The deviceSchedulesSorted to set.
	 */
	public void setDeviceSchedulesSorted(LinkedList deviceSchedulesSorted) {
		this.deviceSchedulesSorted = deviceSchedulesSorted;
	}
	
	/**
	 * @return Returns the endBeforeTolerance.
	 */
	public Integer getEndBeforeTolerance() {
		return endBeforeTolerance;
	}
	
	/**
	 * @param endBeforeTolerance The endBeforeTolerance to set.
	 */
	public void setEndBeforeTolerance(Integer endBeforeTolerance) {
		this.endBeforeTolerance = endBeforeTolerance;
	}
	
	/**
	 * @return Returns the endBeforeToleranceUnits.
	 */
	public InterruptionToleranceUnit getEndBeforeToleranceUnits() {
		return endBeforeToleranceUnits;
	}
	
	/**
	 * @param endBeforeToleranceUnits The endBeforeToleranceUnits to set.
	 */
	public void setEndBeforeToleranceUnits(
			InterruptionToleranceUnit endBeforeToleranceUnits) {
		this.endBeforeToleranceUnits = endBeforeToleranceUnits;
	}
	
	/**
	 * @return Returns the startAfterTolerance.
	 */
	public Integer getStartAfterTolerance() {
		return startAfterTolerance;
	}
	
	/**
	 * @param startAfterTolerance The startAfterTolerance to set.
	 */
	public void setStartAfterTolerance(Integer startAfterTolerance) {
		this.startAfterTolerance = startAfterTolerance;
	}
	
	/**
	 * @return Returns the startAfterToleranceUnits.
	 */
	public InterruptionToleranceUnit getStartAfterToleranceUnits() {
		return startAfterToleranceUnits;
	}
	
	/**
	 * @param startAfterToleranceUnits The startAfterToleranceUnits to set.
	 */
	public void setStartAfterToleranceUnits(
			InterruptionToleranceUnit startAfterToleranceUnits) {
		this.startAfterToleranceUnits = startAfterToleranceUnits;
	}
	
	/**
	 * @return Returns the startBeforeTolerance.
	 */
	public Integer getStartBeforeTolerance() {
		return startBeforeTolerance;
	}
	
	/**
	 * @param startBeforeTolerance The startBeforeTolerance to set.
	 */
	public void setStartBeforeTolerance(Integer startBeforeTolerance) {
		this.startBeforeTolerance = startBeforeTolerance;
	}
	
	/**
	 * @return Returns the startBeforeToleranceUnits.
	 */
	public InterruptionToleranceUnit getStartBeforeToleranceUnits() {
		return startBeforeToleranceUnits;
	}
	
	/**
	 * @param startBeforeToleranceUnits The startBeforeToleranceUnits to set.
	 */
	public void setStartBeforeToleranceUnits(
			InterruptionToleranceUnit startBeforeToleranceUnits) {
		this.startBeforeToleranceUnits = startBeforeToleranceUnits;
	}
	
	public float getStartBeforeToleranceInSeconds()
	{
		if( startBeforeToleranceUnits == null )
		{
			return 0;
		}
		int multiplier = getToleranceMultiplier( startBeforeToleranceUnits );		
		return multiplier * startBeforeTolerance;
	}
	
	public float getStartAfterToleranceInSeconds()
	{
		if( startAfterToleranceUnits == null )
		{
			return 0;
		}
		int multiplier = getToleranceMultiplier( startAfterToleranceUnits );		
		return multiplier * startAfterTolerance;
	}	
	
	public float getEndBeforeToleranceInSeconds()
	{
		if( endBeforeToleranceUnits == null )
		{
			return 0;
		}
		int multiplier = getToleranceMultiplier( endBeforeToleranceUnits );		
		return multiplier * endBeforeTolerance;
	}	
	
	public float getEndAfterToleranceInSeconds()
	{
		if( endAfterToleranceUnits == null )
		{
			return 0;
		}
		int multiplier = getToleranceMultiplier( endAfterToleranceUnits );		
		return multiplier * endAfterTolerance;
	}	
	
	/**
	 * Returns the appropriate multiplier based upon the given field's tolerance unit value.
	 * @param toleranceField
	 * @return
	 */
	private int getToleranceMultiplier(InterruptionToleranceUnit interruptionToleranceUnit)
	{
		int multiplier = 0;
		if( interruptionToleranceUnit.getInterruptionToleranceUnitName().compareTo("minutes") == 0 )
		{
			multiplier = 60;
		}
		else if( interruptionToleranceUnit.getInterruptionToleranceUnitName().compareTo("hours") == 0 )
		{
			multiplier = 3600;
		}
		else if( interruptionToleranceUnit.getInterruptionToleranceUnitName().compareTo("seconds") == 0 )
		{
			multiplier = 1;
		}
		return multiplier;
	}
	
	/**
	 * @return Returns the assetIntervalUnit.
	 */
	public AssetIntervalUnitType getAssetIntervalUnits() {
		return assetIntervalUnits;
	}
	
	/**
	 * @param assetIntervalUnit The assetIntervalUnit to set.
	 */
	public void setAssetIntervalUnits(AssetIntervalUnitType assetIntervalUnit) {
		this.assetIntervalUnits = assetIntervalUnit;
	}

	/*
	 * Returns asset interval frequency in seconds
	 */
	public float getAssetIntervalFrequencyInSeconds()
	{
		if(  assetIntervalFrequency == null || assetIntervalUnits == null )
		{
			return 0;
		}
		int multiplier = getAssetIntervalUnitsMultiplier( assetIntervalUnits );		
		return multiplier * assetIntervalFrequency;
	}	

	/*
	 * Returns the appropriate multiplier based upon the given parameter's unit value
	 * @param assetIntervalUnits
	 */
	private int getAssetIntervalUnitsMultiplier(AssetIntervalUnitType assetIntervalUnits)
	{
		int multiplier = 0;
		if( assetIntervalUnits.getName().toLowerCase().compareTo("hours") == 0 )
		{
			multiplier = 3600;
		}
		else if( assetIntervalUnits.getName().toLowerCase().compareTo("minutes") == 0 )
		{
			multiplier = 60;
		}
		else if( assetIntervalUnits.getName().toLowerCase().compareTo("seconds") == 0 )
		{
			multiplier = 1;
		}
		return multiplier;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof Segment) ) result = false;
		
		Segment c = (Segment) other;		
		if(this.hashCode() == c.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "Segment".hashCode();
		result = Reformat.getSafeHash( this.getSegmentId(), result, 3 );
		result = Reformat.getSafeHash( this.getSegmentName(), result, 11 );		
		return result < 0 ? -result : result;
	}
	/**
	 * @return the mySegmentPartsLengths
	 */
	public List getMySegmentPartsLengths(Device device) 
	{
		if( myDeviceId != device.getDeviceId() || mySegmentPartsLengths == null )
		{
			myDeviceId = device.getDeviceId();
			mySegmentPartsLengths = new LinkedList();
		}
		return mySegmentPartsLengths;
	}
	/**
	 * @param mySegmentPartsLengths the mySegmentPartsLengths to set
	 */
	public void setMySegmentPartsLengths(List mySegmentPartsLengths) {
		this.mySegmentPartsLengths = mySegmentPartsLengths;
	}
	/**
	 * @return the myDeviceId
	 */
	public long getMyDeviceId() {
		return myDeviceId;
	}
	/**
	 * @param myDeviceId the myDeviceId to set
	 */
	public void setMyDeviceId(long myDeviceId) {
		this.myDeviceId = myDeviceId;
	}	
	
	public class DeviceScheduleInfo{
		private Long deviceScheduleId;
		private Long deviceId;
		private String deviceName;
		public Long getDeviceScheduleId() {
			return deviceScheduleId;
		}
		public void setDeviceScheduleId(Long deviceScheduleId) {
			this.deviceScheduleId = deviceScheduleId;
		}
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
	}

	public SegmentType getType() {
		return type;
	}
	public void setType(SegmentType type) {
		this.type = type;
	}
	public Date getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}
}
