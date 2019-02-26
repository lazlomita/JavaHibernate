package com.kuvata.kmf;

import java.util.Calendar;

import parkmedia.usertype.RecurrenceEndType;



public class MonthlyRecurrence extends Recurrence 
{
	private Long monthlyRecurrenceId;
	private Integer monthlyFrequency;
	private Integer dayOfMonth;
	
	// This recurrence is not used for segments thus we have
	// an empty implementation of this abstract method.
	public void copy(Segment newSegment){}


	public boolean recurToday(){
		boolean result = false;
		Calendar now = Calendar.getInstance();

		// If we are after the start date
		if(now.getTime().after(this.getStartDate())){
			
			// Remove the hours, minutes, seconds and milliseconds.
		    now.set(Calendar.HOUR_OF_DAY, 0);
		    now.set(Calendar.MINUTE, 0);
		    now.set(Calendar.SECOND, 0);
		    now.set(Calendar.MILLISECOND, 0);
			
			// Get the number of occurrences
			long numMonths = -1;
			Calendar iterStart = Calendar.getInstance();
			iterStart.setTime(this.getStartDate());
			
			while(iterStart.before(now)){
				iterStart.add(Calendar.MONTH, 1);
				numMonths++;
			}
			
			long numOccurrences = numMonths / this.getMonthlyFrequency();
			
			// Get the start of the current iteration cycle
			iterStart.setTime(this.getStartDate());
			for(int i=0;i<numOccurrences;i++){
				iterStart.add(Calendar.MONTH, this.getMonthlyFrequency());
			}
			
			// If we are in the first month of the recurrence cycle
			iterStart.add(Calendar.MONTH, 1);
			if(now.before(iterStart)){
				iterStart.add(Calendar.MONTH, -1);
				
				// Get the day we are meant to recur on this month
				int recurDay = 0;
				if(this.getDayOfMonth() == -1){
					recurDay = iterStart.getActualMaximum(Calendar.DAY_OF_MONTH);
				}else{
					recurDay = iterStart.getActualMaximum(Calendar.DAY_OF_MONTH) < this.getDayOfMonth() ? iterStart.getActualMaximum(Calendar.DAY_OF_MONTH) : this.getDayOfMonth();
				}
				
				// Recur if today is the day
				if(now.get(Calendar.DAY_OF_MONTH) == recurDay){
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
						// If we have not yet hit the occurrences threshold
						if(numOccurrences < this.getEndAfterOccurrences()){
							result = true;
						}
					}
				}
			}
		}
		return result;
	}
	
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

	public Long getMonthlyRecurrenceId() {
		return monthlyRecurrenceId;
	}

	public void setMonthlyRecurrenceId(Long monthlyRecurrenceId) {
		this.monthlyRecurrenceId = monthlyRecurrenceId;
	}

	public Integer getMonthlyFrequency() {
		return monthlyFrequency;
	}

	public void setMonthlyFrequency(Integer monthlyFrequency) {
		this.monthlyFrequency = monthlyFrequency;
	}

	public Integer getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(Integer dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
}