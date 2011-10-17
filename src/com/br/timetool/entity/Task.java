package com.br.timetool.entity;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * A Task is something that should be accomplished to achieve a goal. 
 * 
 */
public class Task extends TTEntity
{
	public static final String TABLE = "task t";
	public static final String FIELDS = "t.id, t.name, t.description, t.status, t.goal_id";
	
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_DELETED = 1;
	
	private Long id; //ID in database
	
	private String name;
	private String description;
	
	private int status = STATUS_ACTIVE;
	
	private Goal goal;
	
	public Task()
	{
		
	}

	public Task(String name)
	{
		this.name = name;
	}
	
	public String toString()
	{
		return name;
	}
	
	@Override
	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		
		cv.put("id", getId());
		cv.put("name", name);
		cv.put("description", description);
		cv.put("status", status);
		cv.put("goal_id", goal != null ? goal.getId() : null);
		
		return cv;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Goal getGoal()
	{
		return goal;
	}

	protected void setGoal(Goal goal)
	{
		this.goal = goal;
	}
	
	/**
	 * Builds a Task from a database Cursor and assigns it to a Goal. 
	 * <p>
	 * Note: the goal_id is quietly ignored in this method, which is inconsistent with 
	 * the behaviour of ScheduleTask.buildFromCursor(), which requires a Map parameter
	 * for its parent objects. This method just expects whatever class called it to 
	 * handle the Goal assignment
	 * 
	 * @param task
	 * @param cursor
	 * @param index
	 * @param goal The goal to add the task to. Can be null.
	 * @return
	 */
	public static Task buildFromCursor(Task task, Cursor cursor, int index, Goal goal)
	{
		if (task == null)
			task = new Task();
		
		task.setId(cursor.getLong(index++));
		task.setName(cursor.getString(index++));
		task.setDescription(cursor.getString(index++));
		task.setStatus(cursor.getInt(index++));
		
		if (goal != null)
		{
			goal.removeTask(task); //in case a similar object already existed
			goal.addTask(task);
		}
		
		return task;
	}
}
