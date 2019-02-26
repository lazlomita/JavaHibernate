package com.kuvata.kmf.usertype;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.reports.Report;
import com.kuvata.kmf.usertype.PersistentStringEnum;

public class DateRangeType extends PersistentStringEnum{
	public static final DateRangeType TODAY = new DateRangeType("Today", "Today");	
	public static final DateRangeType YESTERDAY = new DateRangeType("Yesterday", "Yesterday");
	public static final DateRangeType LAST_7_DAYS = new DateRangeType("Last 7 Days", "Last 7 Days");
	public static final DateRangeType LAST_14_DAYS = new DateRangeType("Last 14 Days", "Last 14 Days");
	public static final DateRangeType PREVIOUS_MONTH = new DateRangeType("Previous Month", "Previous Month");
	public static final DateRangeType CURRENT_MONTH = new DateRangeType("Current Month", "Current Month");
	public static final Map INSTANCES = new LinkedHashMap();	

	static
	{
		INSTANCES.put(TODAY.toString().toLowerCase(), TODAY);
		INSTANCES.put(YESTERDAY.toString().toLowerCase(), YESTERDAY);
		INSTANCES.put(LAST_7_DAYS.toString().toLowerCase(), LAST_7_DAYS);
		INSTANCES.put(LAST_14_DAYS.toString().toLowerCase(), LAST_14_DAYS);
		INSTANCES.put(PREVIOUS_MONTH.toString().toLowerCase(), PREVIOUS_MONTH);
		INSTANCES.put(CURRENT_MONTH.toString().toLowerCase(), CURRENT_MONTH);
	}
	
	public DateRangeType(){}
	
	protected DateRangeType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	
	public String toString()
	{
		return this.name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public static List<DateRangeType> getDateRangeTypes(){
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext()){
			l.add(i.next());
		}
		
		return l;
	}
	
	public String[] getDateRange(Integer startDay, Integer endDay){
		
		String[] result = null;
		
		// Get the start time for today
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		
		if(this.equals(TODAY)){
			Calendar end = (Calendar)todayStart.clone();
			end.add(Calendar.DATE, 1);
			result = new String[]{Report.UI_DATE_FORMAT.format(todayStart.getTime()), Report.UI_DATE_FORMAT.format(end.getTime())};
		}else if(this.equals(YESTERDAY)){
			Calendar start = (Calendar)todayStart.clone();
			start.add(Calendar.DATE, -1);
			result = new String[]{Report.UI_DATE_FORMAT.format(start.getTime()), Report.UI_DATE_FORMAT.format(todayStart.getTime())};
		}else if(this.equals(LAST_7_DAYS)){
			// Include today
			todayStart.add(Calendar.DATE, 1);
			Calendar start = (Calendar)todayStart.clone();
			start.add(Calendar.DATE, -7);
			result = new String[]{Report.UI_DATE_FORMAT.format(start.getTime()), Report.UI_DATE_FORMAT.format(todayStart.getTime())};
		}else if(this.equals(LAST_14_DAYS)){
			// Include today
			todayStart.add(Calendar.DATE, 1);
			Calendar start = (Calendar)todayStart.clone();
			start.add(Calendar.DATE, -14);
			result = new String[]{Report.UI_DATE_FORMAT.format(start.getTime()), Report.UI_DATE_FORMAT.format(todayStart.getTime())};
		}else if(this.equals(PREVIOUS_MONTH)){
			
			// Get the start date
			startDay = startDay != -1 ? startDay : 32;
			Calendar start = (Calendar)todayStart.clone();
			start.add(Calendar.MONTH, -1);
			
			if(startDay <= start.getActualMaximum(Calendar.DAY_OF_MONTH)){
				start.set(Calendar.DAY_OF_MONTH, startDay);
			}else{
				start.set(Calendar.DAY_OF_MONTH, start.getActualMinimum(Calendar.DAY_OF_MONTH));
			}
			
			// Get the end date
			endDay = endDay != -1 ? endDay : 32;
			Calendar end = (Calendar)todayStart.clone();
			end.add(Calendar.MONTH, -1);
			
			if(endDay <= end.getActualMaximum(Calendar.DAY_OF_MONTH)){
				end.set(Calendar.DAY_OF_MONTH, endDay);
			}else{
				end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
			}
			
			// Include the end date
			end.add(Calendar.DAY_OF_MONTH, 1);
			
			// Make sure that end date is after the start date
			if(end.after(start) == false){
				end.add(Calendar.MONTH, 1);
			}
			
			result = new String[]{Report.UI_DATE_FORMAT.format(start.getTime()), Report.UI_DATE_FORMAT.format(end.getTime())};
		}else if(this.equals(CURRENT_MONTH)){

			// Get the start date
			startDay = startDay != -1 ? startDay : 32;
			Calendar start = (Calendar)todayStart.clone();
			
			if(startDay <= start.getActualMaximum(Calendar.DAY_OF_MONTH)){
				start.set(Calendar.DAY_OF_MONTH, startDay);
			}else{
				start.set(Calendar.DAY_OF_MONTH, start.getActualMinimum(Calendar.DAY_OF_MONTH));
			}
			
			// Get the end date
			endDay = endDay != -1 ? endDay : 32;
			Calendar end = (Calendar)todayStart.clone();
			
			if(endDay <= end.getActualMaximum(Calendar.DAY_OF_MONTH)){
				end.set(Calendar.DAY_OF_MONTH, endDay);
			}else{
				end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
			}
			
			// Include the end date
			end.add(Calendar.DAY_OF_MONTH, 1);
			
			result = new String[]{Report.UI_DATE_FORMAT.format(start.getTime()), Report.UI_DATE_FORMAT.format(end.getTime())};
		}
		
		return result;
	}
	
	public static DateRangeType getDateRangeType(String dateRangeTypeName){
		return (DateRangeType) INSTANCES.get( dateRangeTypeName.toLowerCase() );
	}
}
