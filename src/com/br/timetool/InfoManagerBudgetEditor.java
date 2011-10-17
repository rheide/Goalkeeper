package com.br.timetool;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.br.timetool.core.BudgetListAdapter;
import com.br.timetool.core.MainMenuActivity;
import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.Budget;
import com.br.timetool.entity.Goal;
import com.br.timetool.entity.Task;

public class InfoManagerBudgetEditor extends MainMenuActivity implements OnClickListener, OnChildClickListener, OnGroupClickListener, OnSeekBarChangeListener
{
	private final static float HOUR_INCREMENT = 0.25f;
	
	private final static float TOTAL_GOAL_HOURS = 168.00f;
	
	private NumberFormat nf;
	
	private ExpandableListView mBudgetGoalList;

	private TextView mBudgetDateText;
	private TextView mBudgetTotalText;
	
	private TextView mBudgetSelectedTimeText;
	private SeekBar mTimeBar;

	private BudgetListAdapter mBudgetListAdapter;

	private Budget mBudget = null;

	private Budget mTodaysBudget = null;
	
	private View mEditView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imbudget);

		Log.i("BudgetEditor", "BudgetEditor onCreate - "+System.identityHashCode(this));
		
		mBudgetGoalList = (ExpandableListView) findViewById(R.id.budgetGoalList);
		mBudgetDateText = (TextView) findViewById(R.id.budgetDateText);
		mBudgetTotalText = (TextView) findViewById(R.id.budgetTotalTimeText);
		mTimeBar = (SeekBar) findViewById(R.id.budgetTimeSlider);
		mBudgetSelectedTimeText = (TextView) findViewById(R.id.budgetSelectedTimeText);
		mEditView = (View)findViewById(R.id.budgetTimeEditView);
		
		mEditView.setVisibility(View.GONE); //GORRRRRN

		mTimeBar.setOnSeekBarChangeListener(this);
		
		mBudgetGoalList.setItemsCanFocus(true);
		mBudgetGoalList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		mBudgetGoalList.setOnChildClickListener(this);
		mBudgetGoalList.setOnGroupClickListener(this);
		
		this.nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);

		((Button) findViewById(R.id.butBudgetDateNext)).setOnClickListener(this);
		((Button) findViewById(R.id.butBudgetDatePrev)).setOnClickListener(this);
		((Button) findViewById(R.id.butBudgetSave)).setOnClickListener(this);
		((Button) findViewById(R.id.butBudgetTimeMinus)).setOnClickListener(this);
		((Button) findViewById(R.id.butBudgetTimePlus)).setOnClickListener(this);

		//get the latest budget and display it
		Budget budget = mdb.getBudget();

		Date today = TimeTools.toDay(new Date());
		if (today.equals(TimeTools.toDay(budget.getDate())))
		{
			mTodaysBudget = budget;
			loadBudget(budget); //today's budget exists in db
		}
		else
		{
			//build new budget for today
			buildNewBudgetForToday();
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		if (!fromUser)
			return;
		
		Task selectedTask = mBudgetListAdapter.getSelectedTask();
		if (selectedTask == null) return;
		
		float hours = ((float)progress) * HOUR_INCREMENT;
		
		Log.i("BudgetEditor","Progress change: "+progress+" - "+hours);
		
		mBudget.setHours(selectedTask, hours);
		
		setSelectedHours(hours);
		mBudgetListAdapter.notifyDataSetChanged();
		calculateTotal();
	}
	
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		
	}
	
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		
	}
	
	private void calculateTotal()
	{
		float total = mBudget.getTotalTime();
		mBudgetTotalText.setText("Total: "+nf.format(total)+"/168 hours");
	}
	
	
	
	
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
	{
		//should only intercept clicks if budget is editable (eg. today's budget)
		if (mBudget.getDate().equals(mTodaysBudget.getDate()))
		{
			mBudgetListAdapter.taskSelected(groupPosition, childPosition);
			mBudgetGoalList.invalidate();
			loadTask(mBudgetListAdapter.getSelectedTask());
			return true;
		}
		return false; //not editable
	}

	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
	{	
		mBudgetListAdapter.setSelectedTask(null);
		mEditView.setVisibility(View.GONE); //GORRRRRN
		return false; 
	}
	
	private void loadTask(Task task)
	{	
		float taskHours = mBudget.getHours(task);
		float freeHours = (TOTAL_GOAL_HOURS - mBudget.getTotalTime()) + taskHours;
		setSelectedHours(taskHours);
		
		int maxProgress = (int)(freeHours / HOUR_INCREMENT);
		int curProgress = (int)(taskHours / HOUR_INCREMENT);
		
		mTimeBar.setMax(maxProgress);
		mTimeBar.setProgress(curProgress);
		
		mEditView.setVisibility(View.VISIBLE); //unGORRRRRN
	}

	private void buildNewBudgetForToday()
	{
		if (mTodaysBudget == null)
		{
			Budget budget = new Budget(TimeTools.toDay(new Date()));

			List<Goal> tmpGoals = mdb.getGoals();
			for (Goal goal : tmpGoals)
			{	
				for (Task task : goal.getTasks())
					budget.setHours(task, 0.0f);
			}
			mTodaysBudget = budget;
		}
		loadBudget(mTodaysBudget);
	}

	public void onClick(View src)
	{
		switch (src.getId())
		{
			case R.id.butBudgetDateNext :
				loadNextBudget();
				break;
			case R.id.butBudgetDatePrev :
				loadPreviousBudget();
				break;
			case R.id.butBudgetSave : 
				saveBudget();
				break;
			case R.id.butBudgetTimeMinus :
				decrementSelectedItem();
				break;
			case R.id.butBudgetTimePlus : 
				incrementSelectedItem();
				break;
		}
	}
	
	private void saveBudget()
	{
		mdb.save(mBudget);
		Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show();
	}

	private void loadNextBudget()
	{
		Date date = mBudget != null ? mBudget.getDate() : new Date();
		Budget budget = mdb.getBudgetAfter(date);
		if (budget != null)
			loadBudget(budget);
		else
		//this is the latest budget in the database
		{
			//if the latest budget in the db is not from today, display a new budget for today
			if (mBudget != null &&
					!mBudget.getDate().equals(TimeTools.toDay(new Date())))
			{
				buildNewBudgetForToday();
			}
		}
	}

	private void loadPreviousBudget()
	{
		Date date = mBudget != null ? mBudget.getDate() : new Date();
		Budget budget = mdb.getBudgetBefore(date);
		if (budget != null)
			loadBudget(budget);
	}

	private void loadBudget(Budget budget)
	{
		this.mBudget = budget;
		
		mEditView.setVisibility(View.GONE); //GORRRRRN
		
		mBudgetListAdapter = new BudgetListAdapter(this, budget);
		mBudgetGoalList.setAdapter(mBudgetListAdapter);
		mBudgetDateText.setText(TimeTools.toDayString(budget.getDate()));
		
		calculateTotal();
	}

	private void incrementSelectedItem()
	{
		Task selectedTask = mBudgetListAdapter.getSelectedTask();
		if (selectedTask == null) return;
		
		if (mBudget.getTotalTime() == TOTAL_GOAL_HOURS) return; //can't increment any further
		
		float newHours = 0;
		newHours = mBudget.getHours(selectedTask) + HOUR_INCREMENT;
		mBudget.setHours(selectedTask, newHours);
		
		setSelectedHours(newHours);
		refreshSeekBar(newHours);
		calculateTotal();
	}
	
	private void decrementSelectedItem()
	{
		Task selectedTask = mBudgetListAdapter.getSelectedTask();
		if (selectedTask == null) return;
		
		float newHours = 0;
		
		newHours = mBudget.getHours(selectedTask) - HOUR_INCREMENT;
		if (newHours < 0) 
			newHours = 0;
		
		mBudget.setHours(selectedTask, newHours);
		setSelectedHours(newHours);
		refreshSeekBar(newHours);
		calculateTotal();
	}
	
	private void setSelectedHours(float newHours)
	{	
		String txt = nf.format(newHours)+"h";
		this.mBudgetSelectedTimeText.setText(txt);
	}
	
	private void refreshSeekBar(float hours)
	{
		int seekBarValue = (int)(hours / HOUR_INCREMENT);
		mTimeBar.setProgress(seekBarValue);
	}
}
