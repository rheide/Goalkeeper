package com.br.timetool.action;

import com.br.timetool.db.TTDatabase;
import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.ScheduleTask;

/**
 * Inserts a new task between two existing tasks.<p>
 * This action reschedules the before and after tasks so that there 
 * will be enough time for the new task.
 *
 */
public class InsertScheduleTaskAction implements TTAction
{
	//TODO where to get time from? should have some mechanism
	
	private TTDatabase db;
	private ScheduleTask beforeTask,afterTask,newTask;
	
	public InsertScheduleTaskAction(ScheduleTask before, ScheduleTask after, ScheduleTask newTask)
	{
		this.beforeTask = before;
		this.afterTask = after;
		this.newTask = newTask;
	}
	
	public void init(TTDatabase db)
	{
		this.db = db;
	}
	
	public synchronized void execute()
	{
		//de-allocate time from existing tasks
		//insert new task in correct pos in linked-list structure
		//add task in right pos in Day arraylist
		//save data to database
	}
	
	public void undo()
	{
		// TODO Auto-generated method stub
		
	}
}
