package com.br.timetool.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.br.timetool.entity.Category;
import com.br.timetool.entity.Goal;
import com.br.timetool.entity.Task;

public class GoalListAdapter extends BaseExpandableListAdapter
{
	private Context mContext;
	private List<Goal> mGoals;
	
	private Goal newGoal;
	
	public GoalListAdapter(Context context, List<Goal> goals)
	{
		super();
		this.mContext = context;
		this.mGoals = new ArrayList<Goal>(goals);
		
		Goal.sortByCategory(mGoals);
		
		newGoal = new Goal("New goal..");
		newGoal.setId(-1l);
		Category newCat = new Category("");
		newGoal.setCategory(newCat);
		newCat.setColor(Color.rgb(220,220,220));
		mGoals.add(newGoal);
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
	
	public void removeGoal(Goal goal)
	{
		this.mGoals.remove(goal);
		notifyDataSetChanged();
	}
	
	public void addGoal(Goal goal)
	{
		//insert at second-to-last position to keep the new goal item at the bottom
		this.mGoals.add(mGoals.size()-1, goal); 
		notifyDataSetChanged();
	}

	private View buildView(Goal goal)
	{
		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		
		
		// Layout parameters for the ExpandableListView
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 36);

		TextView textView = new TextView(this.mContext);
		textView.setLayoutParams(lp);
		// Center the text vertically
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		// Set the text starting position
		textView.setPadding(36, 0, 0, 0);
		
		
		AbsListView.LayoutParams cp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 16);

		TextView textViewCat = new TextView(this.mContext);
		textViewCat.setLayoutParams(cp);
		// Center the text vertically
		textViewCat.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		textViewCat.setTextSize(11);
		// Set the text starting position
		textViewCat.setPadding(16, 0, 0, 0);
		
		textView.setTextColor(Color.rgb(0, 0, 0));
		textViewCat.setTextColor(Color.rgb(0, 0, 0));
		
		ll.setBackgroundColor(goal.getCategory().getColor());
		
		if (goal.getId() != null && goal.getId() < 0) //new goal item
		{
			textView.setText(goal.getName());
			
		}
		else
		{
			textView.setText("["+goal.getTasks().size()+"] "+goal.getName());
		}
		
		if (goal.getCategory() != null)
			textViewCat.setText(goal.getCategory().getName());
		
		ll.addView(textViewCat);
		ll.addView(textView);
		
		return ll;
	}
	
	
	private View buildView(Task task)
	{
		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		
		// Layout parameters for the ExpandableListView
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 32);

		TextView textView = new TextView(this.mContext);
		textView.setLayoutParams(lp);
		// Center the text vertically
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		// Set the text starting position
		textView.setPadding(36, 0, 0, 0);
		
		textView.setTextColor(Color.rgb(0, 0, 0));
		
		//use slightly brighter color for tasks
		int color = task.getGoal().getCategory().getColor();
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] -= 0.15f;
		if (hsv[1] < 0.0f) hsv[1] = 0.0f;
		color = Color.HSVToColor(hsv);
		ll.setBackgroundColor(color);
		
		
		textView.setText(task.getName());
		
		ll.addView(textView);
		
		return ll;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		Goal goal = mGoals.get(groupPosition);
		return buildView(goal.getTasks().get(childPosition));
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
		//TODO use convertview!
		Goal goal = mGoals.get(groupPosition);
		if (goal != null)
			return buildView(goal);
		else
		{
			TextView tv = new TextView(mContext);
			tv.setText("INVALID GOAL: "+goal);
			return tv;
		}
	}

	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return groupPosition < mGoals.size() - 1; //'new goal' item's children are not selectable
	}

	public boolean hasStableIds()
	{
		return true;
	}

}
