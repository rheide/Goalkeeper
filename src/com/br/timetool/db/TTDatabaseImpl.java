package com.br.timetool.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.br.timetool.TimeTools;
import com.br.timetool.entity.Budget;
import com.br.timetool.entity.Category;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.Goal;
import com.br.timetool.entity.ScheduleTask;
import com.br.timetool.entity.TTEntity;
import com.br.timetool.entity.Task;

/**
 * Reads and writes TT entities to and from a database.
 * <p>
 * 
 */
public class TTDatabaseImpl implements TTDatabase
{	
	private SQLiteDatabase db;
	
	public TTDatabaseImpl()
	{
		
	}
	
	public void close()
	{
		if (this.db != null)
		{
			try
			{
				db.close();
				db = null;
			}
			catch (Exception e) 
			{ 
				e.printStackTrace(); 
				Log.e("DB", e.getMessage(), e);
			}
		}
	}
	
	public void init(Context context)
	{
		//close prev conn
		if (this.db != null)
		{
			try
			{
				db.close();
				db = null;
			}
			catch (Exception e) 
			{ 
				e.printStackTrace(); 
				Log.e("DB", e.getMessage(), e);
			}
		}
		
		//connect
		TTDatabaseBuilder builder = new TTDatabaseBuilder(context);
		this.db = builder.getWritableDatabase();
		
	}
		
	
	public Day getPlannedDay(Date date)
	{
		return getDay(date, true);
	}
	
	public Day getRecordedDay(Date date)
	{
		return getDay(date, false);
	}
	
	private Day getDay(Date date, boolean planned)
	{
		String queryString = 
			"SELECT id, planned FROM day "+
			"WHERE date = ? AND planned = ?";
		
		String[] args = new String[2];
		args[0] = ""+TimeTools.toDatabase(date);
		args[1] = planned ? "1" : "0";
		
		Cursor cursor = db.rawQuery(queryString,args);
		
		Day day = new Day(date);
		day.setPlanned(planned);
				
		if (cursor.moveToNext())
		{	
			//fetch day from db
			
			day.setId(cursor.getLong(0));
			day.setPlanned(cursor.getInt(1) == 1);
			cursor.close();
			
			loadScheduledTasks(day);
		}
		else //no record in db, return default day
			cursor.close();
		
		return day;
	}
	
	private void loadScheduledTasks(Day day)
	{	
		String[] args = new String[] {""+day.getId()};
		
		//load scheduled tasks
		String queryString = 
			"SELECT "+ScheduleTask.FIELDS+" "+
			"FROM "+ScheduleTask.TABLE+" "+
			"WHERE day_id = ?";
		Cursor cursor = db.rawQuery(queryString, args);
		//store the Tasks, which might be shared by multiple ScheduleTasks,
		//in a temp map to be loaded later
		HashMap<Long,Task> taskMap = new HashMap<Long, Task>(); //cache for tasks
		while (cursor.moveToNext())
		{
			ScheduleTask task = ScheduleTask.buildFromCursor(day, cursor, taskMap);
			day.addScheduleTask(task);
		}
		cursor.close();
		
		//load tasks and goals
		queryString = 
			"SELECT "+Task.FIELDS+", "+ //task
			Goal.FIELDS+", "+ //goal
			Category.FIELDS+" "+ //category
			"FROM  " +ScheduleTask.TABLE+" "+
			"INNER JOIN "+Task.TABLE+" ON st.task_id = t.id " +
			"INNER JOIN "+Goal.TABLE+" ON  t.goal_id = g.id "+
			"LEFT JOIN "+Category.TABLE+" ON g.category_id = c.id "+
			"WHERE st.day_id = ?";
		cursor = db.rawQuery(queryString, args);
		HashMap<Long,Goal> goalMap = new HashMap<Long, Goal>(); //cache for goals
		while (cursor.moveToNext())
		{
			Long taskId = cursor.getLong(0);
			Long goalId = cursor.getLong(4);
			
			Goal goal = goalMap.get(goalId);
			if (goal == null) //new goal, let's add it to the map
			{
				goal = Goal.buildFromCursor(null, cursor, 4); //load from cursor, 5th column
				Category cat = Category.buildFromCursor(null, cursor, 9);
				goal.setCategory(cat); //DEBUG cache Categories!
				goalMap.put(goalId, goal);
			}
			
			Task task = taskMap.get(taskId); //this should never be null
			Task.buildFromCursor(task, cursor, 0, goal); //populate the task and add it to goal
		}
		cursor.close();
	}
		
	public void save(Day day)
	{
		db.beginTransaction();
		
		//TODO what if the date already exists in the db? prefetch!
		if (day.getId() == null)
		{
			//check if day already exists
			String queryString = "SELECT id FROM day WHERE date = ? AND planned = ?";
			String[] args = {""+TimeTools.toDatabase(day.getDate()), 
						day.isPlanned() ? "1" : "0"};
			Cursor cursor = db.rawQuery(queryString, args);
			if (cursor.moveToNext()) //our day already existed in the db, let's re-use it
			{
				day.setId(cursor.getLong(0));
			}
			cursor.close();
		}
		
		if (day.getId() != null)
		{
			String[] args = {""+day.getId()};
			//drop previous schedule tasks
			db.rawQuery("DELETE FROM schedule_task WHERE day_id = ?", args);
		}
		
		//save the day
		save("day",day);
		
		//and its components
		for (ScheduleTask scheduleTask : day.getScheduleTasks())
		{
			save(scheduleTask);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public void save(Goal goal)
	{
		if (goal.getCategory() != null)
			save(goal.getCategory());
		
		if (goal.getCreation() == null)
			goal.setCreation(new Date());
		
		db.beginTransaction();
		save("goal", goal);
		
		//first, mark all tasks as deleted in the database
		db.execSQL("UPDATE task " +
				"SET status = "+Task.STATUS_DELETED+" "+
				"WHERE goal_id = "+goal.getId());
		
		//iterate over tasks in the in-memory object
		//these are marked as active
		for (Task task : goal.getTasks()) 
			save(task);
		
		//TODO remove deleted tasks from budget?
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.br.timetool.db.TTDatabase#validateBudget()
	 */
	public void validateBudget()
	{
		Budget budget = getBudget();
		
		boolean budgetUpdated = false;
		for (Goal goal : budget.getAllGoals())
		{
			if (goal.getStatus() != Goal.STATUS_ACTIVE)
			{
				budgetUpdated = true;
				budget.removeGoal(goal); //also removes tasks of this goal
			}
		}
		
		for (Task task : budget.getAllTasks())
		{
			if (task.getStatus() != Task.STATUS_ACTIVE)
			{
				budgetUpdated = true;
				budget.removeTask(task);
			}
		}
		
		if (budgetUpdated)
		{
			//we should update the database
			Date today = TimeTools.toDay(new Date());
			if (today.equals(budget.getDate()))
			{
				//budget was made today, we should update
				save(budget);
			}
			else
			{
				//old budget, let's make a new budget for today
				//and leave the old budget intact
				budget.setId(null);
				budget.setDate(today);
				save(budget);
			}
		}
	}
	
	
	public void save(Category cat)
	{	
		if (cat.getId() == null)
		{
			Category dbCat = getCategory(cat.getName());
			if (dbCat != null) //already existed in db, update
				cat.setId(dbCat.getId());
		}
		save("category", cat);
	}

	public void save(Budget budget)
	{	
		db.beginTransaction();
		save("budget", budget);
		
		String[] delArgs = {""+budget.getId()};
		db.execSQL("DELETE FROM budget_task WHERE budget_id = ?", delArgs);
		
		ContentValues cv = new ContentValues();
		cv.put("budget_id", budget.getId());
		for (Task task: budget.getAllTasks())
		{
			cv.put("task_id", task.getId());
			cv.put("hours", budget.getHours(task));
			db.insert("budget_task", null, cv);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void save(ScheduleTask task)
	{
		save("schedule_task", task);
	}

	public void save(Task task)
	{
		save("task", task);
	}
	
	public List<Goal> getGoals()
	{
		return getGoals(new Date());
	}
	
	public Goal getGoal(long id)
	{
		String queryString = 
			"SELECT "+Goal.FIELDS+", "+
					  Category.FIELDS+" "+
			"FROM "+Goal.TABLE+" "+
			"LEFT JOIN "+Category.TABLE+" ON g.category_id = c.id "+
			"WHERE g.id = ? ";
		
		String[] args = {""+id};
		ArrayList<Goal> goals = new ArrayList<Goal>();
		Cursor cursor = db.rawQuery(queryString, args);
		
		if (cursor.moveToNext())
		{
			Goal goal = Goal.buildFromCursor(null, cursor, 0);
			goals.add(goal);
			
			Category cat = Category.buildFromCursor(null, cursor, 6);
			goal.setCategory(cat);
		}
		cursor.close();
		
		if (goals.size() == 0) return null; //goal not found in db
		
		loadTasks(goals); //re-use method
		return goals.get(0);
	}
	
	public List<Goal> getGoals(Date date)
	{
		String queryString = 
			"SELECT "+Goal.FIELDS+", "+
			Category.FIELDS+" "+
			"FROM "+Goal.TABLE+" LEFT JOIN "+Category.TABLE+" ON g.category_id = c.id "+
			"WHERE g.status = "+Goal.STATUS_ACTIVE+" "+
			"AND g.creation <= ? "+
			"ORDER BY c.name, g.deadline, g.name";
		
		String[] args = new String[1];
		args[0] = ""+TimeTools.toDatabase(date);
		
		ArrayList<Goal> goals = new ArrayList<Goal>();
		
		Cursor cursor = db.rawQuery(queryString, args);
		
		while (cursor.moveToNext())
		{
			Goal goal = Goal.buildFromCursor(null, cursor, 0);
			goals.add(goal);
			
			Category cat = Category.buildFromCursor(null, cursor, 6);
			goal.setCategory(cat);
		}
		
		cursor.close();
		
		loadTasks(goals);
		
		return goals;
	}
	
	private void loadTasks(ArrayList<Goal> goals)
	{
		String queryString = 
			"SELECT "+Task.FIELDS+" "+
			"FROM "+Task.TABLE+" "+
			"WHERE goal_id = ? AND status = "+Task.STATUS_ACTIVE+" "+
			"ORDER BY id";
		
		//DEBUG this could be optimized in the future (one query/cached statement)
		String[] args = new String[1];
		Cursor cursor = null;
		for (Goal goal : goals)
		{
			args[0] = ""+goal.getId();
			cursor = db.rawQuery(queryString, args);
			while (cursor.moveToNext())
			{
				Task.buildFromCursor(null, cursor,0, goal);
			}
			cursor.close();
		}
	}
	
	public Budget getBudget()
	{
		return getBudget(new Date());
	}
	
	public Budget getBudget(Date date)
	{	
		//get latest budget made on or before today
		String queryString = "SELECT max(id) FROM budget WHERE date <= ?";
		
		String[] args = {""+TimeTools.toDatabase(date)};
		
		Cursor cursor = db.rawQuery(queryString, args);
		
		if (cursor.moveToNext())
		{
			long id = cursor.getLong(0);
			cursor.close();
			return getBudget(id);
		}
		else
		{
			cursor.close();
			return null;
		}
	}
		
	public Budget getBudgetBefore(Date date)
	{
		String queryString = "SELECT max(id) FROM budget WHERE date < ?";
		String[] args = {""+TimeTools.toDatabase(date)};
		Cursor cursor = db.rawQuery(queryString, args);
		
		if (cursor.moveToNext())
		{
			long id = cursor.getLong(0);
			cursor.close();
			return getBudget(id);
		}
		else
		{
			cursor.close();
			return null;
		}
	}
	
	public Budget getBudgetAfter(Date date)
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, 1); //the next day
		
		String queryString = "SELECT min(id) FROM budget WHERE date >= ?";
		String[] args = {""+TimeTools.toDatabase(cal.getTime())};
		Cursor cursor = db.rawQuery(queryString, args);
		
		if (cursor.moveToNext())
		{
			long id = cursor.getLong(0);
			cursor.close();
			return getBudget(id);
		}
		else
		{
			cursor.close();
			return null;
		}
	}
	
	private Budget getBudget(long id)
	{
		//get the budget most recently made before or on this date
		String queryString = 
			"SELECT id, date FROM budget "+
			"WHERE id = ?";
		String[] args = new String[]{""+id};
		
		Budget budget = new Budget();
		
		Cursor cursor = db.rawQuery(queryString, args);
		if (cursor.moveToNext())
		{
			//we have a previous budget available
			budget.setId(cursor.getLong(0));
			budget.setDate(TimeTools.fromDatabase(cursor.getLong(1)));
			cursor.close();
			
			//load budget items
			HashMap<Long, Goal> goalMap = new HashMap<Long, Goal>();
			loadBudgetGoals(budget, goalMap);
			loadBudgetTasks(budget, goalMap);
			
			return budget;
		}
		else
		{
			cursor.close();
			//no budget available
			return null;
		}
	}
	
	private void loadBudgetTasks(Budget budget, HashMap<Long,Goal> goalMap)
	{
		//loads tasks that are contained in the budget and the hours for each task
		//will assign tasks to goals in the goalmap
		//if the goal for the task is not found in the goalmap, things will go sour
		
		String queryString = 
			"SELECT "+Task.FIELDS+", "+
			"bt.hours "+
			"FROM budget_task bt JOIN "+Task.TABLE+" ON t.id = bt.task_id "+
			"WHERE bt.budget_id = ? AND t.status = "+Task.STATUS_ACTIVE;
		
		Cursor cursor = db.rawQuery(queryString, new String[] {""+budget.getId()});
		while (cursor.moveToNext())
		{
			Goal goal = goalMap.get(cursor.getLong(4));
			Task task = Task.buildFromCursor(null, cursor, 0, goal);
			budget.setHours(task, cursor.getFloat(5));
		}
		cursor.close();
	}
	
	private void loadBudgetGoals(Budget budget, HashMap<Long,Goal> goalMap)
	{
		String queryString = 
			"SELECT DISTINCT "+Goal.FIELDS+", "+
			Category.FIELDS+" "+
			"FROM budget_task bt "+
			"INNER JOIN "+Task.TABLE+" ON bt.task_id = t.id "+
			"INNER JOIN "+Goal.TABLE+" ON g.id = t.goal_id "+
			"LEFT JOIN "+Category.TABLE+" ON g.category_id = c.id "+
			"WHERE bt.budget_id = ?";
		
		Cursor cursor = db.rawQuery(queryString, new String[] {""+budget.getId()});
		while (cursor.moveToNext())
		{
			Long goalId = cursor.getLong(0);
			if (!goalMap.containsKey(goalId))
			{
				Goal goal = Goal.buildFromCursor(null, cursor, 0);
				goal.setCategory(Category.buildFromCursor(null, cursor, 6)); //TODO test this!
				goalMap.put(goalId, goal);
			}
		}
		cursor.close();
	}
	
	
	private void save(String table, TTEntity entity)
	{
		//check in db if day already exists - remove old data and replace
		long id = db.insertWithOnConflict(table, null, entity.getContentValues(), 
				SQLiteDatabase.CONFLICT_REPLACE);
		
		if (id >= 0)
			entity.setId(id);
		else
		{
			//something went wrong!
			Log.e("DB", "Could not insert record in "+table+": "+entity.toString());
		}
	}
	
	public Category getCategory(String name)
	{
		String queryString = "SELECT "+Category.FIELDS+" "+
			"FROM "+Category.TABLE+" WHERE name = ?";
		
		Cursor cursor = db.rawQuery(queryString, new String[] {name});
		if (cursor.moveToNext())
		{
			Category cat = Category.buildFromCursor(null, cursor, 0);
			cursor.close();
			return cat;
		}
		else
		{
			cursor.close();
			Category cat = new Category();
			cat.setName(name);
			return cat;
		}
	}
	
	public HashMap<String,Category> getCategories()
	{
		//DEBUG might be useful to cache this!
		
		String queryString = "SELECT "+Category.FIELDS+" "+
				"FROM "+Category.TABLE+" "+
				"WHERE status = "+Category.STATUS_ACTIVE;
		
		Cursor cursor = db.rawQuery(queryString, new String[0]);
		
		LinkedHashMap<String, Category> cats = new LinkedHashMap<String, Category>();
		
		while (cursor.moveToNext())
		{
			Category cat = Category.buildFromCursor(null, cursor, 0);
			cats.put(cat.getName(),cat);
		}
		
		cursor.close();
		
		return cats;
	}
	
	public void deleteCategory(String name)
	{
		db.execSQL(
				"UPDATE category SET status = "+Category.STATUS_DELETED+
				" WHERE name = ?",new Object[]{name});
	}
	
	public void deleteGoal(Goal goal)
	{
		db.beginTransaction();
		
		db.execSQL("UPDATE task SET status = "+
				Task.STATUS_DELETED+
				" WHERE goal_id = "+goal.getId());
		
		db.execSQL("UPDATE goal SET status = "+
				Goal.STATUS_DELETED+" WHERE id = "+goal.getId());
		
		//TODO update budget!
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public void deleteTask(Task task)
	{
		db.execSQL("UPDATE task SET status = "+
				Task.STATUS_DELETED+
				" WHERE id = "+task.getId());
		
		//TODO update budget!
	}
	
	public void deleteBudget(long budgetId)
	{	
		db.beginTransaction();
		db.execSQL("DELETE FROM budget_task WHERE budget_id = "+budgetId);
		db.execSQL("DELETE FROM budget WHERE id = "+budgetId);
			
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	
	public void deleteDay(Day day)
	{
		if (day.getId() == null) return; //nothing to delete
		
		db.beginTransaction();
		
		String[] args = {""+day.getId()};
		//drop previous schedule tasks
		db.execSQL("DELETE FROM schedule_task WHERE day_id = ?", args);
		db.execSQL("DELETE FROM day WHERE id = ?", args);
		
		db.setTransactionSuccessful();
		db.endTransaction();
		
		day.setId(null);
	}
}
