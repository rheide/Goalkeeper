package com.br.timetool.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.br.timetool.TimeTools;

/**
 * Something that must be achieved to obtain happiness
 */
public class Goal extends TTEntity
{
	public static final String TABLE = "goal g";
	public static final String FIELDS = "g.id, g.name, g.description, g.creation, g.deadline, g.status";
	
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_DELETED = 1;
	public static final int STATUS_ACHIEVED = 2;
	public static final int STATUS_FAILED = 3;
	
	private Long id;
	
	private String name;
	private String description;
	
	private ArrayList<Task> tasks = new ArrayList<Task>();
	
	private Date creation;
	private Date deadline;
	private int status  = STATUS_ACTIVE;
	
	private Category category = null;
	
	public Goal()
	{
		
	}
	
	public Goal(String name)
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
		
		cv.put("id", id);
		
		cv.put("name", name);
		cv.put("description", description);
		
		cv.put("creation", TimeTools.toDatabase(creation));
		cv.put("deadline", TimeTools.toDatabase(deadline));
		
		cv.put("status", status);
		
		cv.put("category_id", category != null ? category.getId() : null);
		
		return cv;
	}
	
	public Category getCategory()
	{
		return category;
	}
	
	public void setCategory(Category category)
	{
		this.category = category;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public Date getDeadline()
	{
		return deadline;
	}
	
	public void setCreation(Date creation)
	{
		this.creation = creation;
	}
	
	public Date getCreation()
	{
		return creation;
	}
	
	public void setDeadline(Date deadline)
	{
		this.deadline = deadline;
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

	public ArrayList<Task> getTasks()
	{
		return tasks;
	}
	
	public void addTask(Task task)
	{
		if (!tasks.contains(task))
		{
			task.setGoal(this);
			tasks.add(task);
		}
	}
	
	public boolean removeTask(Task task)
	{
		if (tasks.remove(task))
		{
			task.setGoal(null);
			return true;
		}
		return false;
	}

	public static void sortByCategory(List<Goal> goals)
	{
		//group goals by category
		Collections.sort(goals, new Comparator<Goal>()
		{
			public int compare(Goal one, Goal two)
			{
				String nm1 = one.getCategory().getName();
				String nm2 = two.getCategory().getName();
				return nm1 != null ? nm1.compareTo(nm2) : 0;
			}
		});
	}
	
	/**
	 * Builds a new Goal object from a database cursor. 
	 * 
	 * @param goal An existing Goal to modify, or null to create a new Goal object. 
	 * @param cursor
	 * @param index The column index to start reading the goal from
	 * @return
	 */
	public static Goal buildFromCursor(Goal goal, Cursor cursor, int index)
	{
		if (goal == null)
			goal = new Goal();
		
		//id, name, description, creation, deadline, status
		goal.setId(cursor.getLong(index++)); //0-indexed -__-
		goal.setName(cursor.getString(index++));
		goal.setDescription(cursor.getString(index++));
		goal.setCreation(TimeTools.fromDatabase(cursor.getLong(index++)));
		goal.setDeadline(TimeTools.fromDatabase(cursor.getLong(index++)));
		goal.setStatus(cursor.getInt(index++));
		
		return goal;
	}
	
}
