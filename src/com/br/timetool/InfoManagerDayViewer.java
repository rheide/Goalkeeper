package com.br.timetool;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.br.timetool.core.DayListAdapter;
import com.br.timetool.core.MainMenuActivity;
import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.ScheduleTask;

/* from http://developer.android.com/resources/tutorials/views/hello-datepicker.html */
public class InfoManagerDayViewer extends MainMenuActivity
{
	private TextView mDateDisplay;
	private Button mPickDate;
	private ListView mScheduleTaskView;

	private int mYear;
	private int mMonth;
	private int mDay;

	static final int DATE_DIALOG_ID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imday);
		
		// capture our View elements
		mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
		mPickDate = (Button) findViewById(R.id.pickDate);
		mScheduleTaskView = (ListView) findViewById(R.id.scheduleTaskList);

		// add a click listener to the button
		mPickDate.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showDialog(DATE_DIALOG_ID);
			}
		});

		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		// display the current date (this method is below)
		updateDisplay();
	}

	// updates the date in the TextView
	private void updateDisplay()
	{
		String dayStr = (mMonth + 1) + "-" + mDay + "-" + mYear;
		mDateDisplay.setText(dayStr);
		
		Date date = TimeTools.toDay(dayStr);
		Day day = mdb.getPlannedDay(date);
		if (day.getId() == null)
		{
			//DEBUG: let's add some dummy tasks
			ScheduleTask st1 = new ScheduleTask();
			st1.setStartTime(TimeTools.toDateTime(day.getDate(), "09:00"));
			st1.setEndTime(TimeTools.toDateTime(day.getDate(), "11:00"));
			day.addScheduleTask(st1);
			
			ScheduleTask st2 = new ScheduleTask();
			st2.setStartTime(TimeTools.toDateTime(day.getDate(), "12:00"));
			st2.setEndTime(TimeTools.toDateTime(day.getDate(), "13:00"));
			day.addScheduleTask(st2);
		}
		
		Log.i("InfoManager","Loading day: "+dayStr+" - "+day.getId()+" - "+day.getScheduleTasks().size()+" tasks");
		DayListAdapter dla = new DayListAdapter(this, day);
		mScheduleTaskView.setAdapter(dla);
		mScheduleTaskView.refreshDrawableState();
		dla.notifyDataSetChanged();
	}

	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
	{
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
		{
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
			case DATE_DIALOG_ID :
				return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		}
		return null;
	}
}
