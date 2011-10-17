package com.br.timetool.entity;

import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;

import com.br.timetool.TimeTools;

public class ScheduleTask extends TTEntity
{
	public static final String TABLE = "schedule_task st";
	public static final String FIELDS = 
		"st.id, st.start_time, st.end_time, st.satisfaction, st.do_not_disturb, st.rollover_type, st.task_id";
	
	//bit masks for rollover!
	public static final int ROLLOVER_TYPE_NONE = 0;
	public static final int ROLLOVER_TYPE_NEXT_DAY = 1;
	public static final int ROLLOVER_TYPE_PREVIOUS_DAY = 2;
	
	private Long id;
	
	private Date startTime; //date+time
	private Date endTime;
	
	private Day day;
	private Task task;
	
	//helper fields
	private ScheduleTask prevTask = null;
	private ScheduleTask nextTask = null;
	
	private int satisfaction = 0;
	
	private int rolloverType = ROLLOVER_TYPE_NONE;
	
	private boolean doNotDisturb = false;
	
	public ScheduleTask()
	{
		
	}
	
	public ScheduleTask(Day day)
	{	
		this.day = day;
	}
	
	@Override
	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		
		cv.put("id", getId());
		cv.put("start_time", TimeTools.toDatabase(startTime));
		cv.put("end_time", TimeTools.toDatabase(endTime));
		cv.put("satisfaction", satisfaction);
		cv.put("do_not_disturb", doNotDisturb);
		cv.put("rollover_type", rolloverType);
		cv.put("day_id", day != null ? day.getId() : null);
		cv.put("task_id", task != null ? task.getId() : null);
		
		return cv;
	}
	
	public Day getDay()
	{
		return day;
	}
	
	protected void setDay(Day day)
	{
		this.day = day;
	}
		
	public int getSatisfaction() //i can't get no
	{
		return satisfaction;
	}
	
	public void setSatisfaction(int satisfaction)
	{
		this.satisfaction = satisfaction;
	}
	
	public void setStartTime(Date time)
	{
		this.startTime = time;
	}
	
	public Date getStartTime()
	{
		return startTime;
	}
	
	public Date getEndTime()
	{
		return endTime;
	}
	
	public boolean isDoNotDisturb()
	{
		return doNotDisturb;
	}
	
	public void setDoNotDisturb(boolean doNotDisturb)
	{
		this.doNotDisturb = doNotDisturb;
	}
	
	public void setEndTime(Date endTime)
	{
		this.endTime = endTime;
	}
	
	public Long getId()
	{
		return id;
	}
	
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Task getTask()
	{
		return task;
	}
	
	public void setTask(Task task)
	{
		this.task = task;
	}
	
	/**
	 * Should only be called from a class that validates the schedule!
	 * <p>
	 * Eg. calling class should ensure that the no. of hours and order 
	 * of tasks in Day arraylist is correct.
	 * 
	 * @param nextTask
	 */
	protected void setNextTask(ScheduleTask nextTask)
	{
		this.nextTask = nextTask;
	}
	
	/**
	 * Should only be called from a class that validates the schedule!
	 */
	protected void setPreviousTask(ScheduleTask prevTask)
	{
		this.prevTask = prevTask;
	}
	
	public ScheduleTask getNextTask()
	{
		return nextTask;
	}
	
	public ScheduleTask getPreviousTask()
	{
		return prevTask;
	}
	
	/**
	 * 
	 * @return The duration of this task in hours
	 */
	public float getDuration()
	{
		return TimeTools.getDuration(this);
	}
	
	public int getRolloverType()
	{
		return rolloverType;
	}
	
	public void setRolloverType(int rolloverType)
	{
		this.rolloverType = rolloverType;
	}
	
	/**
	 * Builds a new ScheduleTask object from a record in the database.
	 * <p>
	 * The taskMap is used to look up the ScheduleTask's Task object by id. If no Task
	 * can be found in the map, then a dummy Task object will be created and added to the map. 
	 * 
	 * @param day
	 * @param cursor
	 * @param taskMap A map of Tasks keyed by their id. Can be empty but should never be null. 
	 * @return
	 */
	public static ScheduleTask buildFromCursor(Day day, Cursor cursor, HashMap<Long,Task> taskMap)
	{
		ScheduleTask scheduleTask = new ScheduleTask(day);
		
		scheduleTask.setId(cursor.getLong(0));
		scheduleTask.setStartTime(TimeTools.fromDatabase(cursor.getLong(1)));
		scheduleTask.setEndTime(  TimeTools.fromDatabase(cursor.getLong(2)));
		scheduleTask.setSatisfaction(cursor.getInt(3));
		scheduleTask.setDoNotDisturb(cursor.getInt(4) > 0);
		scheduleTask.setRolloverType(cursor.getInt(5));
		
		//add a dummy task object (we don't have info about the real task)
		Long taskId = cursor.getLong(6);
		if (!taskMap.containsKey(taskId))
		{
			Task tmpTask = new Task();
			tmpTask.setId(taskId);
			taskMap.put(tmpTask.getId(), tmpTask);
		}
		
		scheduleTask.setTask(taskMap.get(taskId));
		
		return scheduleTask;
	}
}
