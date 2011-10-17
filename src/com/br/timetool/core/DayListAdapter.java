package com.br.timetool.core;

import com.br.timetool.R;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.ScheduleTask;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class DayListAdapter extends ArrayAdapter<ScheduleTask>
{
	private Day mDay;
	
	public DayListAdapter(Context context, Day day)
	{
		super(context, android.R.layout.simple_list_item_1, day.getScheduleTasks());
		this.mDay = day;
	}
	
	
	
}
