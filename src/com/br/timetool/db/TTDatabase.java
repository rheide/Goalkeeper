package com.br.timetool.db;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.br.timetool.entity.*;

public interface TTDatabase
{
	/**
	 * Saves a Goal, its Category and its Tasks
	 * <p>
	 * Note: the Budget is not updated! Use validateBudget() to update the budget.
	 * 
	 * @param goal
	 */
	public void save(Goal goal);
	
	/**
	 * Saves a Category
	 * 
	 * @param cat
	 */
	public void save(Category cat);
	
	/**
	 * Saves a single task
	 * <p>
	 * Note: the Budget is not updated! Use validateBudget() to update the budget.
	 * 
	 * @param task
	 */
	public void save(Task task);
	
	/**
	 * Saves a single scheduletask
	 * <p>
	 * Note: this does not preserve consistency: the saved ScheduleTask
	 * might turn out to overlap with other ScheduleTasks.
	 * 
	 * @param task
	 */
	public void save(ScheduleTask task);
		
	/**
	 * Saves a Budget. Goals and Tasks contained in the budget should be saved first!
	 * 
	 * @param budget
	 */
	public void save(Budget budget);
	
	/**
	 * Validates the most recently created budget.
	 * <p>
	 * Ensures that both goals and tasks in the most recent budget are active (not deleted).
	 * <p>
	 * If deleted items are found, two things can happen:
	 * <ul>
	 * <li>If the budget was made today, it is updated</li>
	 * <li>If the budget was made before today, a new updated budget will be created</li> 
	 * </ul>
	 */
	public void validateBudget();
	
	/**
	 * Saves the Day (and all of its ScheduleTasks).
	 * <p>
	 * Assumes that the ScheduleTasks are already in a consistent state (not overlapping or overbudgeting)
	 * 
	 * @param day
	 */
	public void save(Day day);
	
	/**
	 * Gets a Day object and ScheduledTasks for that day.
	 * <p>
	 * 
	 * @param date The Day's date obtained from TimeTools.toDay() method.
	 * 
	 * @see TimeTools.toDay()
	 * 
	 * @return
	 */
	public Day getPlannedDay(Date date);
	
	/**
	 * @see getPlannedDay
	 * 
	 * @param date
	 * @return
	 */
	public Day getRecordedDay(Date date);
	
	
	
	/**
	 * Fetches all current goals from the database, including their tasks. 
	 * <p>
	 * Budget- or schedule-related classes are not retrieved at this point and 
	 * should be retrieved with the getBudget() and getDay() methods. 
	 * <p>
	 * Deleted tasks cannot be retrieved via this method. They can still 
	 * be viewed by accessing a Day object that contains it instead. 
	 * 
	 * @return A list of Goal objects and the active Tasks they contain. 
	 */
	public List<Goal> getGoals();
	
	/**
	 * Retrieves a single goal and its category and tasks
	 * 
	 * @param id
	 * @return
	 */
	public Goal getGoal(long id);
	
	/**
	 * Returns all active goals and tasks for this date.<p>
	 * (not just this date, but all goals that are still active: 
	 * not completed, not deleted and with no deadline or 
	 * a deadline after the given date)
	 * 
	 * @param date
	 * @return A list of Goal objects and the active Tasks they contain
	 */
	public List<Goal> getGoals(Date date); 
	
	/**
	 * 
	 * @return The Budget for today, or else the most recently created Budget.
	 */
	public Budget getBudget();
	
	/**
	 * 
	 * @param date
	 * @return The Budget object that was made on this date. If no Budget was made on the given date, null. 
	 */
	public Budget getBudget(Date date); 
	
	/**
	 * 
	 * @param date
	 * @return A budget made before or on the given date (less than)
	 */
	public Budget getBudgetBefore(Date date);
	
	/**
	 * 
	 * @param date
	 * @return A budget made after the given date (larger than)
	 */
	public Budget getBudgetAfter(Date date);
	
	
	public Category getCategory(String name);
	
	/**
	 * Gets a map of all active (not yet deleted) categories.
	 * 
	 * @return
	 */
	public HashMap<String,Category> getCategories();
	
	public void deleteCategory(String name);
	
	/**
	 * Marks a goal and all of its tasks as deleted
	 * 
	 * @param goal
	 */
	public void deleteGoal(Goal goal);
	
	/**
	 * Marks a task as deleted
	 * 
	 * @param task
	 */
	public void deleteTask(Task task);
	
	/**
	 * Deletes a budget 
	 * 
	 * @param date
	 */
	public void deleteBudget(long budgetId);
	
	/**
	 * Deletes a day and all of its scheduled tasks
	 * 
	 * @param day
	 */
	public void deleteDay(Day day);
}
