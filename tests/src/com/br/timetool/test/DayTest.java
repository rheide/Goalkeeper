package com.br.timetool.test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.br.timetool.TimeTools;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.ScheduleTask;

import junit.framework.TestCase;

public class DayTest extends TestCase
{
	private Day day;

	protected void setUp() throws Exception
	{
		super.setUp();
		day = new Day(new Date());
		ScheduleTask t1 = new ScheduleTask(day);
		t1.setStartTime(TimeTools.toDateTime(day.getDate(), "09:00"));
		t1.setEndTime(TimeTools.toDateTime(day.getDate(), "10:30"));
		day.addScheduleTask(t1);
		ScheduleTask t2 = new ScheduleTask(day);
		t2.setStartTime(TimeTools.toDateTime(day.getDate(), "13:00"));
		t2.setEndTime(TimeTools.toDateTime(day.getDate(), "13:30"));
		day.addScheduleTask(t2);
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
		day = null;
	}

	public void testAddTask()
	{
		//ensure that task is added at the right position
		//and that both linked-list and arraylist are updated correctly
		
		ScheduleTask newTask = new ScheduleTask(day);
		newTask.setStartTime(TimeTools.toDateTime(day.getDate(), "11:00"));
		newTask.setEndTime(  TimeTools.toDateTime(day.getDate(), "11:30"));
		
		day.addScheduleTask(newTask);
				
		//test arraylist consistency
		assertEquals(3, day.getScheduleTasks().size());		
		assertEquals(newTask, day.getScheduleTasks().get(1)); //middle element should be our task
		
		//test linked list consistency
		ScheduleTask firstTask = day.getScheduleTasks().get(0);
		assertNull(firstTask.getPreviousTask());
		assertEquals(newTask, firstTask.getNextTask());
		assertEquals(firstTask, newTask.getPreviousTask());
		ScheduleTask thirdTask = day.getScheduleTasks().get(2);
		assertEquals(thirdTask, newTask.getNextTask());
		assertNull(thirdTask.getNextTask());
	}

	public void testListEquality()
	{
		/*
		 * This test tests whether two different Days in memory
		 * but with the same ID in the database will show up
		 * correctly when looking them up in an ArrayList or Map
		 */
		
		Day one = new Day(new Date());
		Day two = new Day(one.getDate());
		Day three = new Day(new Date());
		
		one.setId(5l);
		two.setId(5l);
		three.setId(9l);
		
		Set<Day> days = new HashSet<Day>();
		days.add(one);
		
		assertTrue(days.contains(two));
		
		days.clear();
		days.add(three);
		assertFalse(days.contains(two));
		
		assertEquals(one, two); //identical objects
		assertNotSame(one, two); //but not the same memory address
		
		
	}
	
	public void testRemoveTask()
	{	
		ScheduleTask newTask = new ScheduleTask(day);
		newTask.setStartTime(TimeTools.toDateTime(day.getDate(), "11:00"));
		newTask.setEndTime(  TimeTools.toDateTime(day.getDate(), "11:30"));
		day.addScheduleTask(newTask);
		
		assertEquals(3, day.getScheduleTasks().size());
		//(the addTask test method tests if the task was added correctly)
		
		day.removeScheduleTask(newTask);
		assertEquals(2, day.getScheduleTasks().size());
		
		ScheduleTask firstTask = day.getScheduleTasks().get(0);
		ScheduleTask secondTask = day.getScheduleTasks().get(1);
		
		assertNull(firstTask.getPreviousTask());
		assertEquals(secondTask, firstTask.getNextTask());
		assertEquals(firstTask, secondTask.getPreviousTask());
		assertNull(secondTask.getNextTask());
	}

	public void testSetPlanned()
	{	
		day.setPlanned(true);
		
		assertTrue(day.isPlanned());
		assertFalse(day.isRecorded());
		
		
		day.setPlanned(false);
		
		assertFalse(day.isPlanned());
		assertTrue(day.isRecorded());
		
	}

	
	public void testScheduleValidator()
	{
		fail("Test not written yet");
	}
	
}
