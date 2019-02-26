package com.kuvata.kmf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import parkmedia.usertype.RecurrenceEndType;
import parkmedia.usertype.RecurrenceType;
import parkmedia.usertype.TrueFalseUserType;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class WeeklyRecurrence extends Recurrence 
{
	private Long weeklyRecurrenceId;
	private Integer weeklyFrequency;
	private TrueFalseUserType mon;
	private TrueFalseUserType tue;
	private TrueFalseUserType wed;
	private TrueFalseUserType thu;
	private TrueFalseUserType fri;
	private TrueFalseUserType sat;
	private TrueFalseUserType sun;
	/**
	 * 
	 *
	 */
	public WeeklyRecurrence()
	{		
	}	

	public void copy(Segment newSegment)
	{
		WeeklyRecurrence newWeeklyRecurrence = new WeeklyRecurrence();
		newWeeklyRecurrence.setWeeklyFrequency( this.getWeeklyFrequency() );
		newWeeklyRecurrence.setMon( this.getMon() );
		newWeeklyRecurrence.setTue( this.getTue() );
		newWeeklyRecurrence.setWed( this.getWed() );
		newWeeklyRecurrence.setThu( this.getThu() );
		newWeeklyRecurrence.setFri( this.getFri() );
		newWeeklyRecurrence.setSat( this.getSat() );
		newWeeklyRecurrence.setSun( this.getSun() );
		super.copy( newWeeklyRecurrence, newSegment );
	}
	
	/**
	 * 
	 * @param f
	 * @param wr
	 */
	public void setWeeklyRecurrence(Integer weeklyFrequency, String[] selectedDays)
	{
		this.setWeeklyFrequency( weeklyFrequency );
		
		// First, set all days to false
		this.setMon( TrueFalseUserType.FALSE );
		this.setTue( TrueFalseUserType.FALSE );
		this.setWed( TrueFalseUserType.FALSE );
		this.setThu( TrueFalseUserType.FALSE );
		this.setFri( TrueFalseUserType.FALSE );
		this.setSat( TrueFalseUserType.FALSE );
		this.setSun( TrueFalseUserType.FALSE );
		
		if( selectedDays != null)
		{				
			for(int i=0; i<selectedDays.length; i++)
			{
				if(selectedDays[i].equals("mon")) {
					this.setMon( TrueFalseUserType.TRUE );
				}else if(selectedDays[i].equals("tue")) {
					this.setTue( TrueFalseUserType.TRUE );
				}else if(selectedDays[i].equals("wed")) {
					this.setWed( TrueFalseUserType.TRUE );
				}else if(selectedDays[i].equals("thu")) {
					this.setThu( TrueFalseUserType.TRUE );
				}else if(selectedDays[i].equals("fri")) {
					this.setFri( TrueFalseUserType.TRUE );
				}else if(selectedDays[i].equals("sat")) {
					this.setSat( TrueFalseUserType.TRUE );
				}else if(selectedDays[i].equals("sun")) {
					this.setSun( TrueFalseUserType.TRUE );
				}					
			}
		}			
	}
	
	public List<String> getSelectedDays(boolean getUiValue)
	{
		List<String> selectedDays = new ArrayList<String>();
		if(this.getSun().equals(TrueFalseUserType.TRUE)){
			if(getUiValue){
				selectedDays.add("Sunday");
			}else{
				selectedDays.add("sun");
			}
		}
		if(this.getMon().equals(TrueFalseUserType.TRUE)){
			if(getUiValue){
				selectedDays.add("Monday");
			}else{
				selectedDays.add("mon");
			}
		}
		if(this.getTue().equals(TrueFalseUserType.TRUE)){
			if(getUiValue){
				selectedDays.add("Tuesday");
			}else{
				selectedDays.add("tue");
			}
		}
		if(this.getWed().equals(TrueFalseUserType.TRUE)){
			if(getUiValue){
				selectedDays.add("Wednesday");
			}else{
				selectedDays.add("wed");
			}
		}
		if(this.getThu().equals(TrueFalseUserType.TRUE)){
			if(getUiValue){
				selectedDays.add("Thursday");
			}else{
				selectedDays.add("thu");
			}
		}
		if(this.getFri().equals(TrueFalseUserType.TRUE)){
			if(getUiValue){
				selectedDays.add("Friday");
			}else{
				selectedDays.add("fri");
			}
		}
		if(this.getSat().equals(TrueFalseUserType.TRUE)){
			if(getUiValue){
				selectedDays.add("Saturday");
			}else{
				selectedDays.add("sat");
			}
		}
		return selectedDays;
	}
	
	/**
	 * 
	 * @param recurrenceCal
	 * @return
	 */
	public boolean matchDayOfWeek(Calendar recurrenceCal)
	{
	    if( recurrenceCal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && mon != null && mon.equals(TrueFalseUserType.TRUE))
	    {
	        return true;
	    }
	    else if( recurrenceCal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && tue != null && tue.equals(TrueFalseUserType.TRUE))
	    {
	        return true;
	    }
	    else if( recurrenceCal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && wed != null && wed.equals(TrueFalseUserType.TRUE))
	    {
	        return true;
	    }
	    else if( recurrenceCal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY && thu != null && thu.equals(TrueFalseUserType.TRUE))
	    {
	        return true;
	    }
	    else if( recurrenceCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && fri != null && fri.equals(TrueFalseUserType.TRUE))
	    {
	        return true;
	    }
	    else if( recurrenceCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && sat != null && sat.equals(TrueFalseUserType.TRUE))
	    {
	        return true;
	    }
	    else if( recurrenceCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && sun != null && sun.equals(TrueFalseUserType.TRUE))
	    {
	        return true;
	    }
	    else
	    {
	        return false;
	    }
	}
	
	public boolean makeDirtyRequired(RecurrenceType recurrenceType, Date startDate, RecurrenceEndType endType, Integer endAfterOccurrences,
			Date endAfterDate, Boolean applyContinuation, Integer weeklyFrequency, String[] selectedDays)
	{
		// If any of theese properties have changed
		if( super.makeDirtyRequired( recurrenceType, startDate, endType, endAfterOccurrences, endAfterDate, applyContinuation ) ){
			return true;
		}else if( (this.weeklyFrequency != null && this.weeklyFrequency.equals( weeklyFrequency ) == false)
				|| (this.weeklyFrequency == null && weeklyFrequency != null) ){
			return true;
		}
		
		if( this.mon.equals( TrueFalseUserType.FALSE ) ){
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("mon") ) {
					return true;
				}					
			}
		}else if( this.mon.equals( TrueFalseUserType.TRUE ) ){
			boolean foundDay = false;
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("mon") ) {
					foundDay = true;
				}					
			}
			if( foundDay == false ){ return true; };
		}	
		if( this.tue.equals( TrueFalseUserType.FALSE ) ){
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("tue") ) {
					return true;
				}					
			}
		}else if( this.tue.equals( TrueFalseUserType.TRUE ) ){
			boolean foundDay = false;
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("tue") ) {
					foundDay = true;
				}					
			}
			if( foundDay == false ){ return true; };
		}	
		if( this.wed.equals( TrueFalseUserType.FALSE ) ){
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("wed") ) {
					return true;
				}					
			}
		}else if( this.wed.equals( TrueFalseUserType.TRUE ) ){
			boolean foundDay = false;
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("wed") ) {
					foundDay = true;
				}					
			}
			if( foundDay == false ){ return true; };
		}	
		if( this.thu.equals( TrueFalseUserType.FALSE ) ){
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("thu") ) {
					return true;
				}					
			}
		}else if( this.thu.equals( TrueFalseUserType.TRUE ) ){
			boolean foundDay = false;
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("thu") ) {
					foundDay = true;
				}					
			}
			if( foundDay == false ){ return true; };
		}	
		if( this.fri.equals( TrueFalseUserType.FALSE ) ){
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("fri") ) {
					return true;
				}					
			}
		}else if( this.fri.equals( TrueFalseUserType.TRUE ) ){
			boolean foundDay = false;
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("fri") ) {
					foundDay = true;
				}					
			}
			if( foundDay == false ){ return true; };
		}	
		if( this.sat.equals( TrueFalseUserType.FALSE ) ){
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("sat") ) {
					return true;
				}					
			}
		}else if( this.sat.equals( TrueFalseUserType.TRUE ) ){
			boolean foundDay = false;
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("sat") ) {
					foundDay = true;
				}					
			}
			if( foundDay == false ){ return true; };
		}	
		if( this.sun.equals( TrueFalseUserType.FALSE ) ){
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("sun") ) {
					return true;
				}					
			}
		}else if( this.sun.equals( TrueFalseUserType.TRUE ) ){
			boolean foundDay = false;
			for(int i=0; i<selectedDays.length; i++) {
				if( selectedDays[i].equals("sun") ) {
					foundDay = true;
				}					
			}
			if( foundDay == false ){ return true; };
		}				
		return false;
	}	
	
	public boolean recurToday(){
		boolean result = false;
		Calendar now = Calendar.getInstance();
		if(now.getTime().after(this.getStartDate())){
			// If we are meant to recur on todays day
			if(this.matchDayOfWeek(now)){
				
				long timeDiff = now.getTimeInMillis() - this.getStartDate().getTime();
				long numWeeks = timeDiff / (Constants.MILLISECONDS_IN_A_DAY * 7);
				long numOccurrences = (numWeeks / this.getWeeklyFrequency()) * this.getSelectedDays(false).size();
				
				// Get the time at which last recurrence cycle ended
				long iterationStart = this.getStartDate().getTime() + ((numWeeks / this.getWeeklyFrequency()) * (Constants.MILLISECONDS_IN_A_DAY * 7 * this.getWeeklyFrequency()));
				
				// Recur only if this is the first week of the recurrence cycle
				if((now.getTimeInMillis() - iterationStart) / (Constants.MILLISECONDS_IN_A_DAY * 7) < 1){
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
						// If we are under the occurrences threshold
						if(numOccurrences < this.getEndAfterOccurrences()){
														
							// Remove the hours, minutes, seconds and milliseconds.
						    now.set(Calendar.HOUR_OF_DAY, 0);
						    now.set(Calendar.MINUTE, 0);
						    now.set(Calendar.SECOND, 0);
						    now.set(Calendar.MILLISECOND, 0);
						    
						    // Recur if we did not hit the number of occurrences
							Calendar iterDay = Calendar.getInstance();
							iterDay.setTimeInMillis(iterationStart);
						    
							while(iterDay.before(now) && numOccurrences < this.getEndAfterOccurrences()){
								
								// If we did recur on this day
								if(this.matchDayOfWeek(iterDay)){
									numOccurrences++;
								}
								
								// Add a day
								iterDay.add(Calendar.DATE, 1);
							}
							
							// If the number of occurrences is still below the threshold
							if(numOccurrences <= this.getEndAfterOccurrences()){
								result = true;
							}
						}
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
	 * @return Returns the fri.
	 */
	public TrueFalseUserType getFri() {
		return fri;
	}

	/**
	 * @param fri The fri to set.
	 */
	public void setFri(TrueFalseUserType fri) {
		this.fri = fri;
	}

	/**
	 * @return Returns the mon.
	 */
	public TrueFalseUserType getMon() {
		return mon;
	}

	/**
	 * @param mon The mon to set.
	 */
	public void setMon(TrueFalseUserType mon) {
		this.mon = mon;
	}

	/**
	 * @return Returns the sat.
	 */
	public TrueFalseUserType getSat() {
		return sat;
	}

	/**
	 * @param sat The sat to set.
	 */
	public void setSat(TrueFalseUserType sat) {
		this.sat = sat;
	}

	/**
	 * @return Returns the sun.
	 */
	public TrueFalseUserType getSun() {
		return sun;
	}

	/**
	 * @param sun The sun to set.
	 */
	public void setSun(TrueFalseUserType sun) {
		this.sun = sun;
	}

	/**
	 * @return Returns the thu.
	 */
	public TrueFalseUserType getThu() {
		return thu;
	}

	/**
	 * @param thu The thu to set.
	 */
	public void setThu(TrueFalseUserType thu) {
		this.thu = thu;
	}

	/**
	 * @return Returns the tue.
	 */
	public TrueFalseUserType getTue() {
		return tue;
	}

	/**
	 * @param tue The tue to set.
	 */
	public void setTue(TrueFalseUserType tue) {
		this.tue = tue;
	}

	/**
	 * @return Returns the wed.
	 */
	public TrueFalseUserType getWed() {
		return wed;
	}

	/**
	 * @param wed The wed to set.
	 */
	public void setWed(TrueFalseUserType wed) {
		this.wed = wed;
	}


	/**
	 * @return Returns the weeklyRecurrenceId.
	 */
	public Long getWeeklyRecurrenceId() {
		return weeklyRecurrenceId;
	}

	/**
	 * @param weeklyRecurrenceId The weeklyRecurrenceId to set.
	 */
	public void setWeeklyRecurrenceId(Long weeklyRecurrenceId) {
		this.weeklyRecurrenceId = weeklyRecurrenceId;
	}

	/**
	 * @return Returns the weeklyFrequency.
	 */
	public Integer getWeeklyFrequency() {
		return weeklyFrequency;
	}

	/**
	 * @param weeklyFrequency The weeklyFrequency to set.
	 */
	public void setWeeklyFrequency(Integer weeklyFrequency) {
		this.weeklyFrequency = weeklyFrequency;
	}

}