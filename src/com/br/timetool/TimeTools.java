package com.br.timetool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.br.timetool.entity.ScheduleTask;

public class TimeTools
{
	private static final SimpleDateFormat dateTimeFormat = 
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final SimpleDateFormat dateFormat = 
		new SimpleDateFormat("yyyy-MM-dd");
	
	private static final SimpleDateFormat timeFormat = 
		new SimpleDateFormat("HH:mm");
	
	public final static String toDayString(Date date)
	{
		return dateFormat.format(date);
	}
	
	public final static String toTimeString(Date date)
	{
		return timeFormat.format(date);
	}
	
	public final static String toDateTimeString(Date date)
	{
		return dateTimeFormat.format(date);
	}

	public final static Long toDatabase(Date date)
	{
		if (date == null) 
			return null;
		return date.getTime() / 1000l; //SQLite stores epoch time in seconds, java in milliseconds
	}
	
	public final static Date fromDatabase(long dateTime)
	{
		if (dateTime > 0)
			return new Date(dateTime * 1000l);
		else return null;
	}
	
	
	/**
	 * 
	 * @param task
	 * @return The duration in hours of the given ScheduleTask
	 */
	public final static float getDuration(ScheduleTask task)
	{
		return getDuration(task.getStartTime(), task.getEndTime());
	}
	
	public final static float getDuration(Date start, Date end)
	{
		if (start == null ||
			end == null) return 0; //invalid
		
		long mt1 = start.getTime();
		long mt2 = end.getTime();
		
		long duration = (mt2-mt1) / 1000l; //now we have seconds
		
		return duration / (3600.0f); //seconds->minutes->hours
	}
	
	/**
	 * Converts a day-of-the-year to a Date object
	 * 
	 * @param dayOfYear The date as yyyy-MM-dd
	 * @return A date object, or null if the date is unparseable
	 */
	public final static Date toDay(String dayOfYear)
	{
		try 
		{
			return dateFormat.parse(dayOfYear);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Rounds off a Date object to a full day
	 * 
	 * @param verySpecificDateAndTime
	 * @return
	 */
	public final static Date toDay(Date verySpecificDateAndTime)
	{
		String dateStr = dateFormat.format(verySpecificDateAndTime);
		try 
		{
			return dateFormat.parse(dateStr);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null; //this can never happen!
		}
	}
	
	/**
	 * Rounds off a date to the nearest x minutes. 
	 * <p>
	 * Example: specifying xMinutes = 15 will round off the date
	 * to the nearest 15 minutes (x:00, x:15, x:30, x:45), either up
	 * or down
	 * 
	 * @param date
	 * @param xMinutes
	 * @return
	 */
	public final static Date toNearestMinute(Date date, int xMinutes)
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		int minutes = cal.get(Calendar.MINUTE);
		double tmpMinutes = (double)minutes / (double)xMinutes;
		tmpMinutes = Math.round(tmpMinutes);
		
		cal.set(Calendar.MINUTE, (int)(tmpMinutes * xMinutes));
		
		return cal.getTime();
	}
	
	
	/**
	 * Creates an hour+minute-specific Date object for the given date and time
	 * 
	 * @param date The day-of-year
	 * @param time the time formatted as 'HH:mm'.
	 * @return A Date object of the given date+time, or null if time could not be parsed
	 */
	public final static Date toDateTime(Date date, String time)
	{
		String dateTimeStr = dateFormat.format(date) + " " + time + ":00";
		Date output = null;
		try 
		{
			output = dateTimeFormat.parse(dateTimeStr);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return output;
	}
	
	
	
}