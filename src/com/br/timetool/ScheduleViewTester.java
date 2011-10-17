package com.br.timetool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.br.timetool.core.MainMenuActivity;
import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.ScheduleTask;

public class ScheduleViewTester extends MainMenuActivity {
	private final int mTIMESLICE = 15; // how many minutes between each entry in schedule listing
	private final int mNUMROWS = 24 * 60 / mTIMESLICE; // 24 hours divided into X slices
	
	// if you change the XML, you'll perhaps need to change one of these
	private final int mROWHEIGHT = 20; // this is the default height (px) of the time row
	private final int mROWWIDTH = 40; // width (px) of time TextView (with margins)
	private final int mMARGINSIZE = 1; // android:layout_marginBottom="1px" for TaskText

	TableLayout mTL; // the root of the view we return
	int mDayColWidth; // dynamically determined based on screen real-estate available
	
	private final int mCOLOR_SCHEDULED_TASK = android.graphics.Color.RED;
	private final int mCOLOR_RECORDED_TASK = android.graphics.Color.GREEN;

	// add colors for diff backgrounds

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("ScheduleView", "entered onCreate()");
		super.onCreate(savedInstanceState);

		// Connect to DB so we can extract info [R: use the mdb field to access the db]
		
		// BEGIN: HARDCODED TESTING
		Date myDate = TimeTools.toDay("2010-08-11"); // (recorded day) pre-populated by Unit Test
		//Day day = db.getPlannedDay(myDate); // returns a Day flagged as "planned"  
		Day day = mdb.getRecordedDay(myDate);
		
		// there could just be a method db.getDay() to call if we don't know if the Day is planned or not?

		myDate = TimeTools.toDay("2010-08-12");  // (empty day)
		Day day2 = mdb.getPlannedDay(myDate);
		//Day day2 = new Day();
		
		myDate = TimeTools.toDay("2010-08-13"); // (planned day)
		Day day3 = mdb.getPlannedDay(myDate);

		List<Day> dayList = new ArrayList<Day>();
		dayList.add(day);
		dayList.add(day2);
		dayList.add(day3);
		// END: HARDCODED TESTING

		ScrollView scrollv = new ScrollView(this);
		View schedv = getScheduleView(dayList, 0);
		scrollv.addView(schedv);
		setContentView(scrollv);
	}

	private View getScheduleView(List<Day> days, int flags) {
		Log.i("ScheduleViewTester", "entered getScheduleView()");

		// currently all schedules will display from 00:00 to 24:00 (scrollable)
		// show all days LtoR in the order they're given to us in the List arg

		// given a list of days, one day is a column in our table
		// really, we'll manually build a table with one row with at least 2 cols
		// col 1 is always the list of times 00:00 to 24:00
		// col 2 will be the first Day (at least one needed)
		// more cols are optional

		// how many pixels wide any Day column should be, after accounting for Time col
		Display display = getWindowManager().getDefaultDisplay(); 
		mDayColWidth = (display.getWidth() - mROWWIDTH) / days.size();
		Log.i("mDayColWidth calc", "display.getWidth():"+display.getWidth()+" mROWWIDTH:"+mROWWIDTH+" days.size():"+days.size());

		mTL = new TableLayout(this);
		mTL.setMinimumWidth(display.getWidth());
		
		TableRow tr = new TableRow(this); // we make one row and add all Time and Day cols to it
		tr.setMinimumWidth(display.getWidth());
		tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		tr.addView(buildOneTimeCol());
		for (int i=0; i<days.size(); i++) {
			tr.addView(buildOneDayCol(days.get(i)));
		}

		mTL.addView(tr);
		return mTL;
	}

	private View buildOneTimeCol() {
		// from 00:00 to 24:00 in mTIMESLICE increments
		Log.i("ScheduleViewTester", "entered buildOneTimeCol()");

		View fromXML;
		LayoutInflater inflater = getLayoutInflater();
		TableLayout tl = new TableLayout(this);
		TableRow tr;
		TextView time = null;

		Calendar currTime = Calendar.getInstance(); // easier to add time
		currTime.set(0, 0, 0, 0, 0, 0);

		for (int i=0; i<mNUMROWS; i++) {
			fromXML = inflater.inflate(R.layout.sv1col, null, false); // .inflate returns a new copy
			tr = (TableRow) fromXML.findViewById(R.id.oneTaskRow);

			time = (TextView) fromXML.findViewById(R.id.oneTaskRowTimeText);
			time.setText(TimeTools.toTimeString( currTime.getTime() )); // HH:mm
			
			time.setOnClickListener(mClickListener);
			currTime.add(Calendar.MINUTE, mTIMESLICE);

			tl.addView(tr);
		}

		return tl;
	}

	private View buildOneDayColCompact(Day day) {
		// THIS IS ANCIENT, NEEDS TO BE UPDATED IF INTENDING TO SHOW COMPACT VIEW
		// one row for each task
		Log.i("ScheduleViewTester", "entered buildOneDayCol()");

		View fromXML;
		LayoutInflater inflater = getLayoutInflater();
		TableLayout tl = new TableLayout(this);
		TableRow tr;
		TextView task;

		List<ScheduleTask> lTask = day.getScheduleTasks();
		// dynamically build table rows, one for each Task in Day
		for (int i=0; i<lTask.size(); i++) {
			ScheduleTask currTask = lTask.get(i);
			fromXML = inflater.inflate(R.layout.sv1col2, null, false); // .inflate returns a new copy
			tr = (TableRow) fromXML.findViewById(R.id.oneTaskRow);

			task = (TextView) fromXML.findViewById(R.id.oneTaskRowTaskText);
			task.setText(currTask.getTask().getName());

			tl.addView(tr);
		}

		return tl;
	}

	private View buildOneDayCol(Day day) {
		Log.i("ScheduleViewTester", "entered buildOneDayCol()");

		View fromXML;
		LayoutInflater inflater = getLayoutInflater();
		TableLayout tl = new TableLayout(this);
		tl.setMinimumWidth(mDayColWidth);
		tl.setColumnStretchable(0, true);
		
		TableRow tr;
		TextView task;
		ScheduleTask currTask = null;
		List<ScheduleTask> lTask = day.getScheduleTasks();

		Calendar currTime = Calendar.getInstance(); // easier to add time
		currTime.set(0, 0, 0, 0, 0, 0);

		if (lTask.size() > 0) { currTask = lTask.remove(0); } // load first task
		for (int i=0; i<mNUMROWS; i++) {
			//Log.i("for loop:", "i:"+i+" currTime:"+currTime.getTime().getHours()+":"+currTime.getTime().getMinutes());
			// TODO: this is really slow... but getting faster by inflating only necessary TextViews 
			fromXML = inflater.inflate(R.layout.sv1col2, null, false); // .inflate returns a new copy
			tr = (TableRow) fromXML.findViewById(R.id.oneTaskRow);
			task = (TextView) fromXML.findViewById(R.id.oneTaskRowTaskText);

			//if (currTask != null) {
			/*
				if ( isSameHHMM(currTask.getEndTime(), currTime.getTime())) {
					task.setText("end "+currTask.getTask().getName()); // over-written by a new startTime
					//if (lTask.size() > 0) { currTask = lTask.remove(0); } // done with this one, get next Task
				}
			 */
			if ( (currTask != null) && isSameHHMM(currTask.getStartTime(), currTime.getTime())) {
				// make Task name long to test wrapping VVV
				//task.setText(currTask.getTask().getName() + "... and so on, could be longer!");
				task.setText(currTask.getTask().getName());
				Log.i("XXX", "day.isPlanned():"+day.isPlanned()+" day.isRecorded():"+day.isRecorded());
				if (day.isPlanned()) { task.setBackgroundColor(mCOLOR_SCHEDULED_TASK); }
				else { task.setBackgroundColor(mCOLOR_RECORDED_TASK); }

				task.setFocusable(true);
				task.setFocusableInTouchMode(true);
				task.setOnClickListener(mClickListener);

				// adjust for # of rows needed once this task's TextView is expanded
				int mins = (int) (currTask.getDuration() * 60); // convert to minutes
				int numSlicesUsed = (int) (mins / mTIMESLICE); // how many timeslices is this task?
				//Log.i("Hours, slices: ", ""+mins+" "+numSlicesUsed);

				int rowHeight = (numSlicesUsed * mROWHEIGHT) - mMARGINSIZE;
				task.setMinHeight(rowHeight); // usable _before_ drawing (not .setHeight())
				task.setMaxHeight(rowHeight); // prevents long text from wrapping, pushing down view
				task.setMinWidth(mDayColWidth);
				task.setMaxWidth(mDayColWidth); // prevents long text from pushing view right
				task.setWidth(mDayColWidth);

				// skip drawing all of the other rows this Task now occupies (-1 because end of for loops adds 1)
				i += (numSlicesUsed-1);
				currTime.add(Calendar.MINUTE, mTIMESLICE * (numSlicesUsed-1));

				// we need to account for one Task's endingTime being the next Task's startTime
				if (lTask.size() > 0) { currTask = lTask.remove(0); } // done with this one, get next Task
			}
			else {
				task.setText(" "); // need to setText or they'll never be displayed
				task.setOnClickListener(mClickListener);
			}
			currTime.add(Calendar.MINUTE, mTIMESLICE);
			tl.addView(tr);
		}

		return tl;
	}

	// this listener is assigned to all rows of Day columns, whether filled or empty
	// TODO: launch dialog that allows view/editing of Tasks
	private OnClickListener mClickListener = new OnClickListener() {
		public void onClick(View src) {
			src.requestFocus();
			//TextView src2 = (TextView) src;
			Log.i("TextView clicked!", "h:"+src.getHeight()+" w:"+src.getWidth()); 
		}
	};

	private boolean isSameHHMM(Date date1, Date date2) {
		date1 = TimeTools.toNearestMinute( date1, mTIMESLICE );
		date2 = TimeTools.toNearestMinute( date2, mTIMESLICE );
		return (date1.getHours() == date2.getHours()) && (date1.getMinutes() == date2.getMinutes());
	}
}
