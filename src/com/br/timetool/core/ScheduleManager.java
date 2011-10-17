package com.br.timetool.core;

import java.util.List;

import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.Budget;
import com.br.timetool.entity.Day;

/**
 * Ensures that consistency is managed for in-memory entities and database entities.
 * 
 */
public class ScheduleManager
{	
	public ScheduleManager()
	{
		
	}
	
	/**
	 * Checks that the tasks for the given day are back-to-back without over- or 
	 * underlapping. 
	 * 
	 * @param day
	 * @return a list of problems with the schedule
	 */
	public List<String> validateSchedule(Day day)
	{
		//TODO should we make a separate class for (ScheduleError)s?
		return null;
	}
	
}
