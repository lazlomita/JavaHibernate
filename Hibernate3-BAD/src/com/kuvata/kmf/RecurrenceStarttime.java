package com.kuvata.kmf;

import java.util.Date;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;

public class RecurrenceStarttime extends PersistentEntity implements HistorizableCollectionMember
{
	private Long recurrenceStarttimeId;
	private Recurrence recurrence;
	private Date starttime;
	/**
	 * 
	 *
	 */	
	public RecurrenceStarttime()
	{		
	}	
	
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof RecurrenceStarttime) ) result = false;		
		RecurrenceStarttime sp = (RecurrenceStarttime) other;		
		if(this.hashCode() == sp.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "RecurrenceStarttime".hashCode();
		result = Reformat.getSafeHash( this.getStarttime(), result, 3 );		
		return result;
	}	
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getRecurrenceStarttimeId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getRecurrence().getSegment().getSegmentId();
	}	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getStarttime().toString();
	}		

	/**
	 * @return Returns the recurrence.
	 */
	public Recurrence getRecurrence() {
		return recurrence;
	}
	/**
	 * @param recurrence The recurrence to set.
	 */
	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}
	/**
	 * @return Returns the recurrenceStarttimeId.
	 */
	public Long getRecurrenceStarttimeId() {
		return recurrenceStarttimeId;
	}
	/**
	 * @param recurrenceStarttimeId The recurrenceStarttimeId to set.
	 */
	public void setRecurrenceStarttimeId(Long recurrenceStarttimeId) {
		this.recurrenceStarttimeId = recurrenceStarttimeId;
	}
	/**
	 * @return Returns the starttime.
	 */
	public Date getStarttime() {
		return starttime;
	}
	/**
	 * @param starttime The starttime to set.
	 */
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
}