package com.kuvata.kmf;

import java.util.Calendar;
import java.util.Date;
import com.kuvata.kmf.usertype.RecurrenceEndType;
import com.kuvata.kmf.usertype.RecurrenceType;

/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class DailyRecurrence extends Recurrence 
{
	private Long dailyRecurrenceId;
	private Integer dailyFrequency;
	/**
	 * 
	 *
	 */	
	public DailyRecurrence()
	{		
	}	
	
	public void copy(Segment newSegment)
	{
		DailyRecurrence newDailyRecurrence = new DailyRecurrence();
		newDailyRecurrence.setDailyFrequency( this.getDailyFrequency() );		
		super.copy( newDailyRecurrence, newSegment );
	}	
	
	public boolean makeDirtyRequired(RecurrenceType recurrenceType, Date startDate, RecurrenceEndType endType, Integer endAfterOccurrences,
			Date endAfterDate, Boolean applyContinuation, Integer dailyFrequency)
	{
		// If any of theese properties have changed
		if( super.makeDirtyRequired( recurrenceType, startDate, endType, endAfterOccurrences, endAfterDate, applyContinuation ) ){
			return true;
		}else if( (this.dailyFrequency != null && this.dailyFrequency.equals( dailyFrequency ) == false)
				|| (this.dailyFrequency == null && dailyFrequency != null) ){
			return true;
		}		
		return false;
	}	
		
	public boolean recurToday(){
		boolean result = false;
		Calendar now = Calendar.getInstance();
		
		// If we are after the start date
		if(now.getTime().after(this.getStartDate())){
			
			long timeDiff = now.getTime().getTime() - this.getStartDate().getTime();
			long numDays = timeDiff / Constants.MILLISECONDS_IN_A_DAY;
			long numOccurrences = numDays / this.getDailyFrequency();
			
			// Get the time at which last recurrence cycle ended
			long iterationStart = this.getStartDate().getTime() + (numOccurrences * this.getDailyFrequency() * Constants.MILLISECONDS_IN_A_DAY);
			
			// Recur if this is the first day in this iteration period
			if((now.getTime().getTime() - iterationStart) / Constants.MILLISECONDS_IN_A_DAY < 1){
				// Recur if there is no recurrence end
				if(this.getEndType().equals(RecurrenceEndType.NO_END)){
					result = true;
				}
				// Recur if the recurrence end date hasn't been reached
				else if(this.getEndType().equals(RecurrenceEndType.ON_DATE)){
					result = now.getTime().before(this.getEndAfterDate());
				}
				// Make sure that we haven't hit the number of occurrence and we are meant to recur today
				else if(this.getEndType().equals(RecurrenceEndType.END_AFTER)){
					if(numOccurrences < this.getEndAfterOccurrences()){
						result = true;
					}
				}
			}
		}
		return result;
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
	public String getEntityName()
	{
		return "";
	}			

	/**
	 * @return Returns the dailyFrequency.
	 */
	public Integer getDailyFrequency() {
		return dailyFrequency;
	}
	/**
	 * @param dailyFrequency The dailyFrequency to set.
	 */
	public void setDailyFrequency(Integer dailyFrequency) {
		this.dailyFrequency = dailyFrequency;
	}
	/**
	 * @return Returns the dailyRecurrenceId.
	 */
	public Long getDailyRecurrenceId() {
		return dailyRecurrenceId;
	}
	/**
	 * @param dailyRecurrenceId The dailyRecurrenceId to set.
	 */
	public void setDailyRecurrenceId(Long dailyRecurrenceId) {
		this.dailyRecurrenceId = dailyRecurrenceId;
	}
}