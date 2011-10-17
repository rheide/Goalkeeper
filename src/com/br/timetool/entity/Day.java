package com.br.timetool.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.br.timetool.TimeTools;

import android.content.ContentValues;

/**
 * One earth day<p>
 * A Day can either be:
 * <ul>
 * <li>A scheduled day that either happened already or is planned to happen in the future</li>
 * <li>A 'recorded' day that stores the results/events/tasks that actually happened on a day in the past</li>
 * </ul>
 * <p>
 * Contains a list of ScheduleTask objects that represent a complete list of all tasks schedules/happening on that day.
 * 
 *
 */
public class Day extends TTEntity
{
	private final static Comparator<ScheduleTask> taskComp = 
		new Comparator<ScheduleTask>()
		{
			public int compare(ScheduleTask one, ScheduleTask two)
			{
				long st1 = one.getStartTime() != null ? one.getStartTime().getTime() : 0;
				long st2 = two.getStartTime() != null ? two.getStartTime().getTime() : 0;
				
				if (st1 == st2)
				{
					long et1 = one.getEndTime() != null ? one.getEndTime().getTime() : 0;
					long et2 = two.getEndTime() != null ? two.getEndTime().getTime() : 0;
					
					if (et1 == et2)
						return 0;
					else
						return et1 > et2 ? 1 : -1;
				}
				else 
					return st1 > st2 ? 1 : -1;
			}
		};
	
	private Long id = null;
	
	private Date date;
	
	private ArrayList<ScheduleTask> tasks = new ArrayList<ScheduleTask>();
	
	private boolean planned = true; //by default a task is scheduled, not recorded
	
	public Day()
	{
		
	}
	
	public Day(Date date)
	{
		this.date = date;
	}
	
	@Override
	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		
		if (id != null)
			cv.put("id", id);
		
		cv.put("date", TimeTools.toDatabase(date));
		
		cv.put("planned", planned ? 1 : 0);
		
		return cv;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Date getDate()
	{
		return date;
	}
	
	protected void setDate(Date date)
	{
		//should not be called after the constructor 
		//because it would invalidate all SchedulesTask's dates!
		this.date = date;
	}
	
	public List<ScheduleTask> getScheduleTasks()
	{
		return tasks;
	}
	
	/**
	 * Adds a task to the list of tasks.
	 * <p>
	 * The task is inserted at the correct position based on start time. 
	 * <p>
	 * Overlap checking is not done in this method.
	 * 
	 * @param task
	 */
	public void addScheduleTask(ScheduleTask task)
	{
		task.setDay(this);
		this.tasks.add(task);
		sortScheduleTasks();
	}
	
	public void removeScheduleTask(ScheduleTask task)
	{
		this.tasks.remove(task);
		sortScheduleTasks();
	}
		
	private synchronized void sortScheduleTasks()
	{
		Collections.sort(this.tasks, taskComp);
		
		//update linked-list representation
		ArrayList<ScheduleTask> tmpTasks = this.tasks; //local var=faster
		ScheduleTask prevTask = null;
		for (int i=0;i<tmpTasks.size();i++)
		{
			ScheduleTask task = tmpTasks.get(i);
			task.setPreviousTask(prevTask);
			
			if (i + 1 < tmpTasks.size())
				task.setNextTask(tmpTasks.get(i+1));
			else
				task.setNextTask(null);
			
			prevTask = task;
		}
	}
	
	public boolean isRecorded()
	{
		return !planned;
	}
	
	public boolean isPlanned()
	{
		return planned;
	}
	
	public void setPlanned(boolean planned)
	{
		this.planned = planned;
	}
	
	/**
	 * Validates the schedule
	 * <p>
	 * Ensures that:
	 * <ul>
	 * <li>The day is fully scheduled (24 hours)</li>
	 * <li>No tasks are overlapping</li>
	 * </ul>
	 * This method does not care whether the day is a planned or recorded day.
	 * 
	 * @return A list of error messages for each problem. A list of size zero represents success
	 */
	public List<String> validateSchedule()
	{
		ArrayList<String> errors = new ArrayList<String>();
		
		ArrayList<ScheduleTask> tmpTasks = this.tasks;
		
		if (tmpTasks.size() == 0)
		{
			errors.add("No tasks defined");
		}
		else
		{
			//validate first task
			ScheduleTask firstTask = tmpTasks.get(0);
			
			Date shouldStartDate = TimeTools.toDateTime(this.date, "00:00");
			if (firstTask.getStartTime() == null || 
				firstTask.getStartTime().getTime() > shouldStartDate.getTime())
				errors.add("First task does not start at 00:00");
		}
		
		
		for (int i=1;i<tmpTasks.size()-1;i++)
		{
			ScheduleTask prevTask = tmpTasks.get(i-1);
			ScheduleTask task = tmpTasks.get(i);
			ScheduleTask nextTask = tmpTasks.get(i+1);
			
			if (prevTask.getEndTime() == null)
				errors.add("Task does not have end time");
			else
			{
				if (!prevTask.getEndTime().equals(task.getStartTime()))
					errors.add("Error between tasks "+(i-1)+" and "+i);
			}
			
			if (nextTask.getStartTime() == null)
				errors.add("Task does not have start time");
			else
			{
				if (!nextTask.getStartTime().equals(task.getEndTime()))
					errors.add("Error between tasks "+i+" and "+(i+1));
			}
		}
		
		return errors;
	}
}
