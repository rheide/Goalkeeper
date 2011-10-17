package com.br.timetool.entity;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.br.timetool.TimeTools;

import android.content.ContentValues;

/**
 * A Budget stores for each Goal and Task how many hours are assigned to it (in a one-week timespan)
 * 
 */
public class Budget extends TTEntity
{
	private Long id;
	
	private Date date;
	
	private Map<Task, Float> taskBudget = new LinkedHashMap<Task, Float>();
	
	public Budget()
	{
		
	}
	
	public Budget(Date date)
	{
		this.date = date;
	}
	
	@Override
	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		
		cv.put("id", id);
		cv.put("date", TimeTools.toDatabase(date));
		
		return cv;
	}
		
	public Date getDate()
	{
		return date;
	}
	
	public void setDate(Date date)
	{
		this.date = date;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public void removeGoal(Goal goal)
	{	
		for (Task task : taskBudget.keySet())
		{
			if (task.getGoal().equals(goal))
				taskBudget.remove(task);
		}
	}
	
	public void removeTask(Task task)
	{
		taskBudget.remove(task);
	}
	
	public float getHours(Goal goal)
	{
		float hours = 0.0f;
		for (Task task : taskBudget.keySet())
			if (task.getGoal().equals(goal))
				hours += taskBudget.get(task);
		return hours;
	}
	
	public void setHours(Task task, float hours)
	{
		this.taskBudget.put(task, hours);
	}
	
	public void remove(Goal goal)
	{
		for (Task task : goal.getTasks())
			taskBudget.remove(task);
	}
	
	public void remove(Task task)
	{
		taskBudget.remove(task);
	}
	
	public float getHours(Task task)
	{
		if (this.taskBudget.containsKey(task))
			return this.taskBudget.get(task);
		return 0;
	}
	
	public float getTotalTime()
	{
		float total = 0.0f;
		
		for (Float tmpValue : taskBudget.values())
			total += tmpValue;
		
		return total;
	}
	
	public Set<Goal> getAllGoals()
	{
		Set<Goal> goals = new LinkedHashSet<Goal>();
		for (Task task : taskBudget.keySet())
			goals.add(task.getGoal());
		
		return goals;
	}
	
	public Set<Task> getAllTasks()
	{
		return taskBudget.keySet();
	}
	
	
	/**
	 * Validates the budget
	 * <p>
	 * Ensures that:
	 * <ul>
	 * <li>The budget is fully planned (24*7 hours)</li>
	 * <li>The sum of all Task hours for one goal do not exceed the allocated goal hours</li>
	 * </ul>
	 * 
	 * @return A list of error messages for each problem. A list of size zero represents success
	 */
	public List<String> validateSchedule()
	{
		
		
		return null; //TODO
	}
}
