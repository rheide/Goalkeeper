package com.br.timetool;

import java.util.LinkedList;
import java.util.Queue;

import com.br.timetool.core.MainMenuActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Recorder extends MainMenuActivity implements OnClickListener {
	// add a Queue here to keep track of the ET timeslices we're supposed to account for when
	// the user gives us focus
	static Queue<Integer> mTimesliceQueue = new LinkedList<Integer> ();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("Recorder", "entered onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);

		// bind our buttons
		Button butRecordYes = (Button) findViewById(R.id.butRecordYes);
		Button butRecordNo = (Button) findViewById(R.id.butRecordNo);
		butRecordYes.setOnClickListener(this);
		butRecordNo.setOnClickListener(this);
	}

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.butRecordYes:
			// TODO: log to DB
			showToast("Excellent work!");
			break;
		case R.id.butRecordNo:
			// TODO: log to DB
			showToast("Aww.. c'mon, you can do it!");
			break;
		}
		finish();
	}
	
	private void showToast(CharSequence inStr) {
		Toast.makeText(this, inStr, Toast.LENGTH_SHORT).show();
    }
	
	protected static int getTimesliceCount() {
		Log.i("Recorder", "" + mTimesliceQueue.size());
		return mTimesliceQueue.size();
	}
	
	protected static void addTimeslice(int in) {
		mTimesliceQueue.add(in);
		Log.i("Recorder", "" + mTimesliceQueue.size());
	}
}
