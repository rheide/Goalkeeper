package com.br.timetool;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.br.timetool.core.DayListAdapter;
import com.br.timetool.core.GoalListAdapter;
import com.br.timetool.core.MainMenuActivity;
import com.br.timetool.db.TTDatabase;
import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.Category;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.Goal;
import com.br.timetool.entity.ScheduleTask;
import com.br.timetool.entity.Task;

/* from http://developer.android.com/resources/tutorials/views/hello-datepicker.html */
public class InfoManagerGoalViewer extends MainMenuActivity
{	
	private ExpandableListView mGoalList;
	
	private GoalListAdapter mGoalListAdapter;

	public static final int ACTION_DELETE_GOAL = 127;
	public static final int ACTION_DELETE_TASK = 128;
	public static final int ACTION_EDIT_GOAL = 129;
	public static final int ACTION_EDIT_TASK = 130;
	public static final int ACTION_ADD_TASK = 131;
	
	private Task activeTask = null;
	private Goal activeGoal = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imgoal);
		
		// capture our View elements
		mGoalList = (ExpandableListView) findViewById(R.id.goalList);
		
		MyListener ml = new MyListener();
		mGoalList.setOnGroupClickListener(ml);
		mGoalList.setOnCreateContextMenuListener(ml);
				
		loadGoals();
	}
	
	private void loadGoals()
	{	
		List<Goal> goals = mdb.getGoals();
		
		if (goals.size() == 0)
			goals.add(new Goal("Dummy goal"));
		
		GoalListAdapter gla = new GoalListAdapter(this, goals);
		mGoalList.setAdapter(gla);
		this.mGoalListAdapter = gla;
		gla.notifyDataSetChanged();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		Log.i("GoalViewer", "Context menu item selected: "+item+" - "+item.getItemId());
		switch (item.getItemId())
		{
			case ACTION_ADD_TASK : 
				Log.i("GoalViewer", "Context menu: add task to goal: "+activeGoal);
				addTaskToActiveGoal();
				break;
			case ACTION_DELETE_GOAL : 
				Log.i("GoalViewer", "Context menu: delete goal - "+activeGoal);
				deleteActiveGoal();
				break;
			case ACTION_DELETE_TASK : 
				Log.i("GoalViewer", "Context menu: delete task - "+activeTask);
				deleteActiveTask();
				break;
			case ACTION_EDIT_GOAL : 
				Log.i("GoalViewer", "Context menu: edit goal - "+activeGoal);
				editActiveGoal();
				break;
			case ACTION_EDIT_TASK : 
				Log.i("GoalViewer", "Context menu: edit task - "+activeTask);
				editActiveTask();
				break;
		}
		return super.onContextItemSelected(item);
	}
	
	private void addTaskToActiveGoal()
	{
		if (activeGoal == null) return;
		
		Task task = new Task("New task ("+activeGoal.getTasks().size()+")");
		activeGoal.addTask(task);
		mdb.save(task);
		
		mGoalListAdapter.notifyDataSetChanged();
	}
	
	private void deleteActiveGoal()
	{
		if (activeGoal == null) return;
		
		if (activeGoal.getId() < 0) return; //can't delete the "Add new goal.." item
		
		mdb.deleteGoal(activeGoal);
		mGoalListAdapter.removeGoal(activeGoal);
	}
	
	private void deleteActiveTask()
	{
		if (activeTask == null) return;
		
		activeGoal.removeTask(activeTask);
		mdb.deleteTask(activeTask);
		
		mGoalListAdapter.notifyDataSetChanged();
	}
	
	private void editActiveGoal()
	{
		if (activeGoal == null) return;
		
		Intent intent = new Intent(this, GoalEditor.class);
		intent.putExtra("goal_id", activeGoal.getId());
		startActivityForResult(intent, ACTION_EDIT_GOAL);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i("GoalViewer", "Activity result: "+requestCode+" - "+resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) return;
		
		if (requestCode == ACTION_EDIT_GOAL ||
			requestCode == ACTION_EDIT_TASK)
		{
			//update data from database!
			loadGoals();
		}
		
	}
	
	private void editActiveTask()
	{
		if (activeTask == null) return;
		
		Intent intent = new Intent(this, TaskEditor.class);
		intent.putExtra("goal_id", activeTask.getGoal().getId());
		intent.putExtra("task_id", activeTask.getId());
		startActivityForResult(intent, ACTION_EDIT_TASK);
	}
	
	private void addNewGoal()
	{
		Log.i("GoalViewer", "Adding new goal");
		
		Goal goal = new Goal("New goal ("+mGoalList.getCount()+")");
		
		//which category? let's just get the first one (very inefficient)
		Map<String,Category> cats = mdb.getCategories();
		if (cats.size() > 0)
			goal.setCategory(cats.values().iterator().next());
		else
			goal.setCategory(new Category("New category"));
		
		mdb.save(goal);
		mGoalListAdapter.addGoal(goal);
	}
		
	private class MyListener implements OnCreateContextMenuListener, OnGroupClickListener
	{
		public MyListener()
		{
			super();
		}
		
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
		{
			Goal goal = (Goal)mGoalListAdapter.getGroup(groupPosition);
			Log.i("GoalViewer", "GroupClick: "+goal.getName());
			
			if (goal.getId() < 0)
			{
				//new goal item, add a new goal
				addNewGoal();
				return true;
			}
			else			
				return false;
		}
		
		public void onCreateContextMenu(ContextMenu menu, View v, 
				ContextMenuInfo menuInfo)
		{
			if (!(menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo)) return;
			
			Log.i("GoalViewer", "CreateContextMenu! "+menuInfo);
			
			ExpandableListContextMenuInfo cmi = (ExpandableListContextMenuInfo)menuInfo;
			
			int posType = ExpandableListView.getPackedPositionType(cmi.packedPosition);
			if (posType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) //goal
			{
				Goal goal = (Goal)mGoalListAdapter.getGroup(
						ExpandableListView.getPackedPositionGroup(cmi.packedPosition));
				
				createGoalMenu(goal, menu, v);
			}
			else if (posType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) //task
			{
				Goal goal = (Goal)mGoalListAdapter.getGroup(
						ExpandableListView.getPackedPositionGroup(cmi.packedPosition));
				
				Task task = goal.getTasks().get(ExpandableListView.getPackedPositionChild(cmi.packedPosition));
				
				createTaskMenu(task, menu, v);
			}
		}
		
		private void createGoalMenu(Goal goal, ContextMenu menu, View v)
		{
			Log.i("GoalViewer", "GoalMenu! "+goal.getName());
			if (goal.getId() >= 0) //don't show menu for the add new goal item
			{			
				menu.setHeaderTitle(goal.getName());
				menu.add(Menu.NONE, ACTION_DELETE_GOAL, 0, "Delete");
				menu.add(Menu.NONE, ACTION_EDIT_GOAL, 0, "Edit");
				menu.add(Menu.NONE, ACTION_ADD_TASK, 0, "Add task");			
				menu.add(Menu.NONE, 0, 0, "Cancel");
			
				activeGoal = goal;
			}
		}
		
		private void createTaskMenu(Task task, ContextMenu menu, View v)
		{
			Log.i("GoalViewer", "TaskMenu! "+task.getName());
			
			menu.setHeaderTitle(task.getName());
			menu.add(Menu.NONE, ACTION_DELETE_TASK, 0, "Delete");
			menu.add(Menu.NONE, ACTION_EDIT_TASK, 0, "Edit");
			menu.add(Menu.NONE, 0, 0, "Cancel");
			
			activeGoal = task.getGoal();
			activeTask = task;
		}
	}
	
}
