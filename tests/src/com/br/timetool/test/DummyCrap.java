package com.br.timetool.test;

import java.util.Date;

import com.br.timetool.TimeTools;
import com.br.timetool.entity.*;

public class DummyCrap
{
	public Goal killBrianGoal = new Goal("Kill Brian with a rake");
	public Goal stealGoldGoal = new Goal("Steal gold bar from Izu");
	
	public Task buyRakeTask = new Task("Buy rake");
	public Task hitBrianTask = new Task("Hit Brian on head");
	public Task laughTask = new Task("Laugh");
	
	public Task cycleToIzuTask = new Task("Cycle to Izu");
	public Task killGuardTask = new Task("Kill guard");
	public Task stealGoldTask = new Task("Steal gold");
	public Task whatTask = new Task("???");
	public Task profittTask = new Task("Profitt");
	
	public Budget budget = new Budget(TimeTools.toDay("2010-08-13"));
	
	public Day day = new Day(TimeTools.toDay("2010-08-13"));
	
	public ScheduleTask morningTask = new ScheduleTask(day);
	public ScheduleTask afternoonTask = new ScheduleTask(day);
	
	public DummyCrap()
	{	
		killBrianGoal.addTask(buyRakeTask);
		killBrianGoal.addTask(hitBrianTask);
		killBrianGoal.addTask(laughTask);
				
		stealGoldGoal.addTask(cycleToIzuTask);
		stealGoldGoal.addTask(killGuardTask);
		stealGoldGoal.addTask(stealGoldTask);
		stealGoldGoal.addTask(whatTask);
		stealGoldGoal.addTask(profittTask);
		
		budget.setHours(buyRakeTask, 2);
		budget.setHours(hitBrianTask, 2);
		budget.setHours(laughTask, 2);
		
		budget.setHours(cycleToIzuTask, 1);
		budget.setHours(killGuardTask, 1.5f);
		budget.setHours(stealGoldTask, 1.25f);
		budget.setHours(whatTask, 3);
		budget.setHours(profittTask, 2);
				
		afternoonTask.setTask(hitBrianTask);
		afternoonTask.setStartTime(TimeTools.toDateTime(day.getDate(), "13:00"));
		afternoonTask.setEndTime(  TimeTools.toDateTime(day.getDate(), "17:00"));
		day.addScheduleTask(afternoonTask);
		
		morningTask.setTask(cycleToIzuTask);
		morningTask.setStartTime(TimeTools.toDateTime(day.getDate(), "09:00"));
		morningTask.setEndTime(  TimeTools.toDateTime(day.getDate(), "12:00"));
		day.addScheduleTask(morningTask);
	}
}
