package com.br.timetool.core;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.br.timetool.InfoManagerBudgetEditor;
import com.br.timetool.entity.Budget;
import com.br.timetool.entity.Goal;
import com.br.timetool.entity.Task;

public class BudgetListAdapter extends BaseExpandableListAdapter
{
	private NumberFormat nf;
	
	private Context mContext;
	private List<Goal> mGoals;
	private Budget mBudget;
	
	private Task selectedTask = null;
		
	private MyView mSelectedView;
	
	private InfoManagerBudgetEditor mEditor;
	
	public BudgetListAdapter(InfoManagerBudgetEditor editor, Budget budget)
	{
		super();
		this.mContext = editor;
		this.mBudget = budget;
		this.mEditor = editor;
		this.mGoals = new ArrayList<Goal>(budget.getAllGoals());
		
		this.mSelectedView = new MyView(mContext);
		mSelectedView.setBackgroundColor(Color.rgb(255, 255, 200));
		mSelectedView.main.setTextColor(Color.BLACK);
		mSelectedView.right.setTextColor(Color.BLACK);
		
		
		this.nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);
		
		Goal.sortByCategory(mGoals);
	}

	public Object getChild(int groupPosition, int childPosition)
	{
		Goal goal = mGoals.get(groupPosition);
		return goal.getTasks().get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	public int getChildrenCount(int groupPosition)
	{
		return mGoals.get(groupPosition).getTasks().size();
	}

	public void setSelectedTask(Task task)
	{
		this.selectedTask = task;
		notifyDataSetChanged();
	}
	
	public Task getSelectedTask()
	{
		return this.selectedTask;
	}
	
	private class MyView extends RelativeLayout
	{
		private TextView main;
		private TextView right;
		
		public MyView(Context c)
		{
			super(c);
			
			main = new TextView(c);
			main.setText("---");
			main.setPadding(36, 0, 0, 0);
			main.setGravity(Gravity.LEFT);
			
			right = new TextView(c);
			right.setText("(--)");
			right.setTextColor(Color.rgb(200, 255, 200));
			right.setTextSize(14);
			right.setGravity(Gravity.RIGHT);
			right.setPadding(0, 0, 16, 0);
			
			LinearLayout rightLayout = new LinearLayout(c);
			rightLayout.setOrientation(LinearLayout.HORIZONTAL);
			
			
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 36);
			
			this.setLayoutParams(lp);
			
			
			RelativeLayout.LayoutParams rlpMain = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,36);
			rlpMain.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			
			RelativeLayout.LayoutParams rlpRight = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,36);
			rlpRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			
			main.setId(8); //nooo magic number
			right.setId(9); //yeeees
			
			rlpMain.addRule(RelativeLayout.LEFT_OF, right.getId());
			
			main.setLayoutParams(rlpMain);
			right.setLayoutParams(rlpRight);
			
			this.addView(main);
			this.addView(right);
		}
		
	}
	

	public Object getGroup(int groupPosition)
	{
		return mGoals.get(groupPosition);
	}

	public int getGroupCount()
	{
		return mGoals.size();
	}

	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{	
		Goal goal = mGoals.get(groupPosition);
	
		MyView mv = null;
		if (convertView == null || !(convertView instanceof MyView) || convertView == mSelectedView)
		{
			mv = new MyView(mContext);
		}
		else
			mv = (MyView)convertView;
		
		mv.main.setText(goal.getName());
		mv.right.setText(nf.format(mBudget.getHours(goal)));
		return mv;
	}

	
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		Goal goal = mGoals.get(groupPosition);
		Task task = goal.getTasks().get(childPosition);
		
		if (selectedTask == task)
		{
			MyView msv = mSelectedView;
			
			msv.main.setText(task.getName());
			msv.right.setText(nf.format(mBudget.getHours(task)));
			
			return msv;
		}
		else
		{
			MyView mv = null;
			if (convertView == null || !(convertView instanceof MyView) || convertView == mSelectedView)
			{
				mv = new MyView(mContext);
			}
			else
				mv = (MyView)convertView;
			
			mv.main.setText(task.getName());
			mv.right.setText(nf.format(mBudget.getHours(task)));
			
			return mv;
		}
	}
		
	public void taskSelected(int group, int task)
	{
		Log.i("BLA", "Task selected: "+group+" - "+task);
		if (group >= 0 && group < mGoals.size())
		{
			Goal goal = mGoals.get(group);
			if (task >= 0 && task < goal.getTasks().size())
			{
				selectedTask = goal.getTasks().get(task);
			}
			else
				selectedTask = null;
		}
		else
			selectedTask = null;
		
		notifyDataSetChanged();
	}
	
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return groupPosition < mGoals.size();
	}

	public boolean hasStableIds()
	{
		return true;
	}

}
