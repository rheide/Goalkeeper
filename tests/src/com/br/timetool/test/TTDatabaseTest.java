package com.br.timetool.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.br.timetool.TimeTools;
import com.br.timetool.db.TTDatabaseBuilder;
import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.Budget;
import com.br.timetool.entity.Category;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.Goal;
import com.br.timetool.entity.ScheduleTask;
import com.br.timetool.entity.Task;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.test.AndroidTestCase;
import android.util.Log;

public class TTDatabaseTest extends AndroidTestCase
{
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private TTDatabaseImpl db;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		
		db = new TTDatabaseImpl();
		db.init(mContext);
	}

	protected void tearDown() throws Exception
	{	
		super.tearDown();
		db.close();
	}


	public void testCreateDatabase()
	{
		TTDatabaseBuilder dbBuilder = new TTDatabaseBuilder(mContext);
		SQLiteDatabase db = dbBuilder.getWritableDatabase();
		
		try
		{
			Cursor cursor = db.rawQuery("SELECT * FROM goal", new String[0]);
			cursor.moveToNext(); //not null
			
			cursor = db.rawQuery("SELECT * FROM badtablename", new String[0]);
			fail("If this is reached then the table existed!");
		}
		catch (SQLException e)
		{
			//good! this table did not exist
		}
		
		db.close();
	}
	
	/*public void testDeleteAll()
	{	
		//DEBUG this is a bit dangerous
		TTDatabaseBuilder dbBuilder = new TTDatabaseBuilder(mContext);
		SQLiteDatabase db = dbBuilder.getWritableDatabase();
		db.execSQL("DELETE FROM goal");
		db.execSQL("DELETE FROM category");
		db.execSQL("DELETE FROM task");
		db.close();
	}*/
	
	public void testCreateCategory()
	{
		db.deleteCategory("Things"); //clean up before test
		
		Category dbCat = db.getCategory("Things");
		assertNotNull(dbCat);
		assertTrue(dbCat.getId() == null ||
				dbCat.getStatus() == Category.STATUS_DELETED);
		
		
		Category cat = new Category();
		cat.setName("Things");
		cat.setColor(Color.rgb(255, 200, 200));
		
		db.save(cat);
		
		assertNotNull(cat.getId());
		
		Category secondCat = new Category();
		secondCat.setName("Things");
		secondCat.setColor(Color.rgb(200, 220, 255));
		
		db.save(secondCat);
		
		assertEquals(cat.getId(), secondCat.getId());
		
		dbCat = db.getCategory("Things");
		assertNotNull(dbCat);
		assertEquals(Color.rgb(200, 220, 255), dbCat.getColor());
		
		
	}
	
	public void testCreateGoal() throws ParseException
	{	
		Category cat = new Category();
		cat.setName("CreateGoalCat");
		cat.setColor(Color.rgb(230,180,255));
		
		
		Goal testGoal = new Goal("Get database to work");
		testGoal.setDescription("Write a unittest to test if saving and loading goals to and from the database works");
		testGoal.setCreation(sdf.parse("2010-08-13 21:00:05"));
		testGoal.setDeadline(null); //should work
		testGoal.setCategory(cat);

		assertNull(testGoal.getId()); //not persisted yet
		
		db.save(testGoal);
		
		Long id = testGoal.getId();
		assertNotNull(id);
		
		//let's get a new db handle to prevent caching
		db.close();
		db = new TTDatabaseImpl();
		db.init(mContext);
		
		Goal dbGoal = getGoal(id);
		assertNotNull(dbGoal);
		assertEquals("Get database to work", dbGoal.getName());
		assertEquals(sdf.parse("2010-08-13 21:00:05"), dbGoal.getCreation());
		assertNull(testGoal.getDeadline());
	}
	
	public void testCreateTask()
	{
		Category cat = new Category();
		cat.setName("CreateTaskCat");
		cat.setColor(Color.rgb(155,255,155));
		
		Goal testGoal = new Goal("Save tasks");
		testGoal.setCategory(cat);
		Task task = new Task("Test database class");
		task.setDescription("To see if it's actually working");
				
		testGoal.addTask(task);
		
		db.save(testGoal);
		
		assertNotNull(testGoal.getId());
		assertNotNull(task.getId());
		
		//to test if it works, get the goal back
		Goal dbGoal = getGoal(testGoal.getId());
		//it must have exactly one task with our task's id
		assertEquals(1, dbGoal.getTasks().size());
		
		Task dbTask = dbGoal.getTasks().iterator().next();
		assertEquals(task.getId(), dbTask.getId());
	}
	
	private Goal getGoal(Long id)
	{
		Collection<Goal> goals = db.getGoals();
		for (Goal g: goals)
			if (id.equals(g.getId()))
				return g;
		return null;
	}
	
	public void testCreateSchedule()
	{
		//cleanup:
		Date myDate = TimeTools.toDay("2010-08-13");
		Day day = db.getPlannedDay(myDate);
		
		int maxTries = 20;
		int tries = 0;
		while (day != null && day.getId() != null)
		{
			db.deleteDay(day);
			day = db.getPlannedDay(myDate);
			tries++;
			if (tries >= maxTries) 
			{
				Log.e("DB","Could not delete test day from db!");
				break;
			}
		}
		
		//setup:
		day = new Day(myDate);
		
		Goal goal = new Goal("Meh");
		Category cat = new Category("CatMeh");
		cat.setColor(Color.rgb(255,100,255));
		goal.setCategory(cat);
		
		
		ScheduleTask st1 = buildTask(day, goal, "00:00", "09:00");
		ScheduleTask st2 = buildTask(day, goal, "09:00", "14:30");
		ScheduleTask st3 = buildTask(day, goal, "14:30", "15:45");
		ScheduleTask st4 = buildTask(day, goal, "15:45", "20:30");
		ScheduleTask st5 = buildTask(day, goal, "20:30", "23:59");
		st1.setDoNotDisturb(true);
		st4.setDoNotDisturb(false);
		
		day.addScheduleTask(st1);
		day.addScheduleTask(st2);
		day.addScheduleTask(st3);
		day.addScheduleTask(st4);
		day.addScheduleTask(st5);
		
		//save tasks first!
		db.save(goal); //+tasks
		db.save(day); //+scheduletasks
		
		//ensure data saved
		assertNotNull(day.getId());
		assertNotNull(st1.getId()); //scheduletasks should have been saved too
		assertNotNull(st2.getId());
		
		//verify
		Day dbDay = db.getPlannedDay(myDate);
		
		assertEquals(day, dbDay);  //db id is the same
		assertNotSame(day, dbDay); //in-memory objects are different
		
		List<ScheduleTask> dbTasks = dbDay.getScheduleTasks();
		assertEquals(5, dbTasks.size());
		
		ScheduleTask dbt1 = dbTasks.get(0);
		assertEquals(st1.getId(), dbt1.getId());
		assertEquals(st1.getTask(), dbt1.getTask());
		assertEquals(TimeTools.toDayString(st1.getStartTime()),
					TimeTools.toDayString(dbt1.getStartTime()));
		assertEquals("09:00", TimeTools.toTimeString(dbt1.getEndTime()));
		assertTrue(dbt1.isDoNotDisturb());
		
		
		ScheduleTask dbt4 = dbTasks.get(3);
		assertEquals(st4.getId(), dbt4.getId());
		assertEquals(st4.getTask(), dbt4.getTask());
		assertFalse(dbt4.isDoNotDisturb());
		assertEquals(TimeTools.toDayString(st4.getStartTime()),
					TimeTools.toDayString(dbt4.getStartTime()));
		assertEquals("20:30", TimeTools.toTimeString(dbt4.getEndTime()));
	}
	
	
	public void testRollover()
	{
		//test if rollover is correctly stored in the database
		Task dummyTask = new Task("Dummy");
		Goal dummyGoal = new Goal("Goal");
		Category dummyCat = new Category("Dummy");
		dummyGoal.setCategory(dummyCat);
		dummyGoal.addTask(dummyTask);
		db.save(dummyGoal);
		
		
		Day dayOne = new Day(TimeTools.toDay("2009-06-02"));
		dayOne.setPlanned(true);
		
		Day dayTwo = new Day(TimeTools.toDay("2009-06-03"));
		dayTwo.setPlanned(true);
		
		ScheduleTask st = new ScheduleTask();
		st.setTask(dummyTask);
		st.setStartTime(TimeTools.toDateTime(dayOne.getDate(), "23:00"));
		st.setEndTime(TimeTools.toDateTime(dayTwo.getDate(), "00:00"));
		st.setRolloverType(ScheduleTask.ROLLOVER_TYPE_NEXT_DAY);
		dayOne.addScheduleTask(st);
		
		ScheduleTask st2 = new ScheduleTask();
		st2.setTask(dummyTask);
		st2.setStartTime(TimeTools.toDateTime(dayTwo.getDate(), "00:00"));
		st2.setEndTime(TimeTools.toDateTime(dayTwo.getDate(), "01:00"));
		st2.setRolloverType(ScheduleTask.ROLLOVER_TYPE_PREVIOUS_DAY);
		dayTwo.addScheduleTask(st2);
		
		db.save(dayOne);
		db.save(dayTwo);
		
		
		//test both tasks
		Day dbDayOne = db.getPlannedDay(TimeTools.toDay("2009-06-02"));
		Day dbDayTwo = db.getPlannedDay(TimeTools.toDay("2009-06-03"));
		
		assertNotSame(dayOne, dbDayOne);
		assertEquals(dayOne.getId(), dbDayOne.getId());
		assertEquals(dayTwo.getId(), dbDayTwo.getId());
		
		assertEquals(1, dayOne.getScheduleTasks().size());
		assertEquals(1, dayTwo.getScheduleTasks().size());
		
		ScheduleTask dbTaskOne = dbDayOne.getScheduleTasks().get(0);
		ScheduleTask dbTaskTwo = dbDayTwo.getScheduleTasks().get(0);
		
		assertEquals(ScheduleTask.ROLLOVER_TYPE_NEXT_DAY, dbTaskOne.getRolloverType());
		assertEquals(ScheduleTask.ROLLOVER_TYPE_PREVIOUS_DAY, dbTaskTwo.getRolloverType());
		
		//we're not testing which date here, is that ok?
		assertEquals("00:00", TimeTools.toTimeString(dbTaskOne.getEndTime()));
		assertEquals("00:00", TimeTools.toTimeString(dbTaskTwo.getStartTime()));
		
		//should the end date of task one be exactly the same as task two? 
		assertEquals(dbTaskOne.getEndTime(), dbTaskTwo.getStartTime());
	}
	
	public void testCreateDay()
	{	
		Day recDay = new Day(TimeTools.toDay("2009-05-23"));
		recDay.setPlanned(false);
		
		Day planDay = new Day(TimeTools.toDay("2009-05-23"));
		planDay.setPlanned(true);
		
		assertTrue(recDay.isRecorded());
		assertTrue(planDay.isPlanned());
		assertFalse(recDay.isPlanned());
		assertFalse(planDay.isRecorded());
		
		db.save(recDay);
		db.save(planDay);
		
		Day dbRecDay = db.getRecordedDay(TimeTools.toDay("2009-05-23"));
		Day dbPlanDay = db.getPlannedDay(TimeTools.toDay("2009-05-23"));
		
		assertNotSame(recDay, dbRecDay);
		assertNotSame(planDay, dbPlanDay);
		
		assertEquals(recDay.getId(), dbRecDay.getId());
		assertEquals(planDay.getId(), dbPlanDay.getId());
		
		assertTrue(dbRecDay.getId().longValue() != dbPlanDay.getId().longValue());
		
		assertTrue(dbRecDay.isRecorded());
		assertTrue(dbPlanDay.isPlanned());
	}
	
	private ScheduleTask buildTask(Day day, Goal goal, String startTime, String endTime)
	{
		ScheduleTask st = new ScheduleTask();
		st.setStartTime(TimeTools.toDateTime(day.getDate(), startTime));
		st.setEndTime(TimeTools.toDateTime(day.getDate(), endTime));
		
		Task t = new Task("Wake the wehwehweh");
		st.setTask(t);
		goal.addTask(t);
		
		return st;
	}
	
	
	public void testCreateBudget()
	{
		//cleanup:
		Date myDate = TimeTools.toDay("2010-08-13");
		Budget budget = db.getBudget(myDate);
		
		int tries = 0;
		int maxTries = 20;
		while (budget != null && budget.getId() != null)
		{
			db.deleteBudget(budget.getId());
			budget = db.getBudget(myDate);
			
			tries++;
			if (tries >= maxTries)
			{
				Log.e("DB","Could not delete budget from db!");
				break;
			}
		}
		
		
		//setup:
		Category cat = new Category("TestBudgetCat");
		cat.setColor(Color.rgb(100,155,255));
		Goal goalOne = new Goal("Make budget work");
		Goal goalTwo = new Goal("Make budget work twice");
		
		goalOne.setCategory(cat);
		goalTwo.setCategory(cat);
		
		Task taskOne = new Task("Write unittest for budget");
		Task taskTwo = new Task("Confirm test results");
		Task taskThree = new Task("Fix source code");
		
		goalOne.addTask(taskOne);
		goalOne.addTask(taskTwo);
		goalOne.addTask(taskThree);
		
		Task taskFour = new Task("Rinse and repeat");
		goalTwo.addTask(taskFour);
		
		db.save(goalOne);
		db.save(goalTwo); //should save tasks + category too
		
		//save dummy budget
		budget = new Budget(myDate);
		budget.setHours(taskOne, 3);
		budget.setHours(taskTwo, 4);
		budget.setHours(taskThree, 2);
		
		budget.setHours(taskFour, 90);
		
		db.save(budget);
		
		assertNotNull(budget.getId());
		
		//retrieve and test
		Budget dbBudget = db.getBudget(TimeTools.toDay("2010-08-13"));
		assertNotNull(dbBudget); //budget exists in db
		assertEquals(budget.getId(), dbBudget.getId());
		
		//test hours
		assertEquals(2, dbBudget.getAllGoals().size());
		assertEquals(4, dbBudget.getAllTasks().size());
		
		float delta = 0.01f;
		
		assertEquals(9, budget.getHours(goalOne), delta); //sum of tasks of goal one
		assertEquals(90, budget.getHours(goalTwo), delta); //sum of tasks of goal one
		
		for (Task task : dbBudget.getAllTasks())
		{
			assertTrue(budget.getAllTasks().contains(task));
			assertEquals(budget.getHours(task), dbBudget.getHours(task), delta);
		}
	}
	
}
