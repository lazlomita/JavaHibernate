package com.kuvata.kmf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.usertype.RecurrenceEndType;
import parkmedia.usertype.RecurrenceType;

import com.kuvata.kmf.logging.HistorizableChildEntity;
import com.kuvata.kmf.logging.HistorizableSet;
import com.kuvata.kmf.reports.SavedReport;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public abstract class Recurrence extends Entity implements HistorizableChildEntity
{
	private Long recurrenceId;
	private RecurrenceType recurrenceType;
	private Date startDate;
	private RecurrenceEndType endType;
	private Integer endAfterOccurrences;
	private Date endAfterDate;
	private Segment segment;
	private SavedReport savedReport;
	private Boolean applyContinuation = false;
	private Set<RecurrenceStarttime> recurrenceStarttimes = new HistorizableSet<RecurrenceStarttime>();
	public abstract void copy(Segment newSegment);
	
	/**
	 * 
	 *
	 */
	public Recurrence()
	{		
	}
	
	public abstract boolean recurToday();
	
	
	public void copy(Recurrence newRecurrence, Segment newSegment)
	{
		newRecurrence.setRecurrenceType( this.getRecurrenceType() );
		newRecurrence.setStartDate( this.getStartDate() );
		newRecurrence.setEndType( this.getEndType() );
		newRecurrence.setEndAfterOccurrences( this.getEndAfterOccurrences() );
		newRecurrence.setEndAfterDate( this.getEndAfterDate() );
		newRecurrence.setSegment( newSegment );
		newRecurrence.setApplyContinuation( this.getApplyContinuation() );
		newRecurrence.save();
		
		// Copy any recurrence start times
		for( Iterator i=this.getRecurrenceStarttimes().iterator(); i.hasNext(); )
		{
			RecurrenceStarttime rst = (RecurrenceStarttime)i.next();
			RecurrenceStarttime newRst = new RecurrenceStarttime();
			newRst.setRecurrence( newRecurrence );
			newRst.setStarttime( rst.getStarttime() );
			newRst.save();
		}
		newRecurrence.update();		
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getRecurrenceId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		if(this.getSegment() != null){
			return this.getSegment().getSegmentId();
		}else{
			return this.getSavedReport().getSavedReportId();
		}
	}			
		
	/**
	 * 
	 * @param segmentId
	 * @return
	 * @throws HibernateException
	 */
	public static Recurrence getSegmentRecurrence(Long segmentId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Recurrence r = (Recurrence)session.createCriteria(Recurrence.class)
				.add( Expression.eq("segment.segmentId", segmentId) )
				.uniqueResult();

		// If a recurrence with the given segment_id exists in the database
		return r;
	}
	
	public static Recurrence getSavedReportRecurrence(Long savedReportId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Recurrence r = (Recurrence)session.createCriteria(Recurrence.class)
				.add( Expression.eq("savedReport.savedReportId", savedReportId) )
				.uniqueResult();

		// If a recurrence with the given savedReportId exists in the database
		return r;
	}
	
	public boolean makeDirtyRequired(RecurrenceType recurrenceType, Date startDate, RecurrenceEndType endType, Integer endAfterOccurrences,
			Date endAfterDate, Boolean applyContinuation)
	{
		// If any of these properties have changed
		if( (this.recurrenceType != null && this.recurrenceType.equals( recurrenceType ) == false)
				|| (this.recurrenceType == null && recurrenceType != null) ){
			return true;
		}else if( (this.startDate != null && startDate != null && this.startDate.getTime() != startDate.getTime() )
				|| (this.startDate == null && startDate != null) ){
			return true;
		}else if( (this.endType != null && this.endType.equals( endType ) == false)
				|| (this.endType == null && endType != null) ){
			return true;
		}else if( (this.endAfterOccurrences != null && this.endAfterOccurrences.equals( endAfterOccurrences ) == false)
				|| (this.endAfterOccurrences == null && endAfterOccurrences != null) ){
			return true;
		}else if( (this.endAfterDate != null && endAfterDate != null && this.endAfterDate.getTime() != endAfterDate.getTime() )
				|| (this.endAfterDate == null && endAfterDate != null) ){
			return true;
		}else if( (this.applyContinuation != null && this.applyContinuation.equals( applyContinuation ) == false)
				|| (this.applyContinuation == null && applyContinuation != null) ){
			return true;
		}		
		return false;
	}
	
	public void addRecurrenceStarttime(List<String> times, boolean apiCall) throws ParseException 
	{
		DateFormat recurrenceStartTimeFormat = new SimpleDateFormat("hh:mm:ss a");
		ArrayList startTimesToAdd = new ArrayList();
		
		for(String time : times)
		{
			Date d = recurrenceStartTimeFormat.parse( time );
			RecurrenceStarttime rs = new RecurrenceStarttime();
			rs.setRecurrence( this );
			rs.setStarttime( d );
			startTimesToAdd.add( rs );
		}
		
		/*
		 * Compare the list of segmentPartsToAdd with the list of existing segment parts
		 * 1. If the size of the two collections are different, we know stuff has changed.
		 * 2. If the size of the two collections are the same, but there are any differences between them.
		 */
		boolean makeDirty = false;
		if( recurrenceStarttimes == null && startTimesToAdd.size() > 0 ){
			makeDirty = true;
		}
		else if( recurrenceStarttimes != null && startTimesToAdd.size() != recurrenceStarttimes.size() ){
			makeDirty = true;
		}
		else 
		{
			for( Iterator i=startTimesToAdd.iterator(); i.hasNext(); ) {
				RecurrenceStarttime rs = (RecurrenceStarttime)i.next();
				if( recurrenceStarttimes.contains( rs ) == false ){
					makeDirty = true;
					break;
				}				
			}
		}
		
		// If any of the segment parts have changed
		if( makeDirty )
		{
			// Remove all start times from this recurrence
			recurrenceStarttimes.clear();
			
			// For history purposes, first add to list
			for( Iterator i=startTimesToAdd.iterator(); i.hasNext(); )
			{
				RecurrenceStarttime rs = (RecurrenceStarttime)i.next();
				addRecurrenceStarttime( rs );
			}
			
			// Save the segment parts and add them to the segmentParts collection
			for( Iterator i=startTimesToAdd.iterator(); i.hasNext(); )
			{
				RecurrenceStarttime rs = (RecurrenceStarttime)i.next();
				rs.save();
			}			
			
			update();
			if (!apiCall)
				segment.makeDirty( true );
		}
	}
	/**
	 * 
	 * @param rs
	 */
	public void addRecurrenceStarttime(RecurrenceStarttime rs) 
	{
		if (rs == null)
			throw new IllegalArgumentException("Null RecurrenceStarttime!");
				
		this.recurrenceStarttimes.add( rs );
	}	
	
	/**
	 * @return Returns the endAfterDate.
	 */
	public Date getEndAfterDate() {
		return endAfterDate;
	}

	/**
	 * @param endAfterDate The endAfterDate to set.
	 */
	public void setEndAfterDate(Date endAfterDate) {
		this.endAfterDate = endAfterDate;
	}

	/**
	 * @return Returns the endAfterOccurrence.
	 */
	public Integer getEndAfterOccurrences() {
		return endAfterOccurrences;
	}

	/**
	 * @param endAfterOccurrence The endAfterOccurrence to set.
	 */
	public void setEndAfterOccurrences(Integer endAfterOccurrences) {
		this.endAfterOccurrences = endAfterOccurrences;
	}

	/**
	 * @return Returns the recurrenceId.
	 */
	public Long getRecurrenceId() {
		return recurrenceId;
	}

	/**
	 * @param recurrenceId The recurrenceId to set.
	 */
	public void setRecurrenceId(Long recurrenceId) {
		this.recurrenceId = recurrenceId;
	}

	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return Returns the segment.
	 */
	public Segment getSegment() {
		return segment;
	}

	/**
	 * @param segment The segment to set.
	 */
	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	/**
	 * @return Returns the recurrenceType.
	 */
	public RecurrenceType getRecurrenceType() {
		return recurrenceType;
	}

	/**
	 * @param recurrenceType The recurrenceType to set.
	 */
	public void setRecurrenceType(RecurrenceType recurrenceType) {
		this.recurrenceType = recurrenceType;
	}

	/**
	 * @return Returns the endType.
	 */
	public RecurrenceEndType getEndType() {
		return endType;
	}
	/**
	 * @param endType The endType to set.
	 */
	public void setEndType(RecurrenceEndType endType) {
		this.endType = endType;
	}
	/**
	 * @return Returns the recurrenceStarttimes.
	 */
	public Set<RecurrenceStarttime> getRecurrenceStarttimes() {
		return recurrenceStarttimes;
	}
	/**
	 * @param recurrenceStarttimes The recurrenceStarttimes to set.
	 */
	public void setRecurrenceStarttimes(Set recurrenceStarttimes) {
		this.recurrenceStarttimes = recurrenceStarttimes;
	}
	/**
	 * @return Returns the applyContinuation.
	 */
	public Boolean getApplyContinuation() {
		return applyContinuation;
	}
	
	/**
	 * @param applyContinuation The applyContinuation to set.
	 */
	public void setApplyContinuation(Boolean applyContinuation) {
		this.applyContinuation = applyContinuation;
	}

	public SavedReport getSavedReport() {
		return savedReport;
	}

	public void setSavedReport(SavedReport savedReport) {
		this.savedReport = savedReport;
	}
	
}