package com.br.timetool;

import java.util.Calendar;

import com.br.timetool.core.MainMenuActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Launcher extends MainMenuActivity implements OnClickListener {
	int mRepeatInterval = 20 * 1000; // How long between alarms in milliseconds.
	PendingIntent mAlarmIntent; // Used for setting/cancelling the periodic alarm

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("Launcher", "entered onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher);

		// bind our buttons
		Button butStartTimer = (Button) findViewById(R.id.butStartTimer);
		Button butStopTimer = (Button) findViewById(R.id.butStopTimer);
		butStartTimer.setOnClickListener(this);
		butStopTimer.setOnClickListener(this);
	}

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.butStartTimer:
			// TODO: if we already have an alarm, update it
			setAlarmManager();
			showToast("Alarm activated");
			break;
		case R.id.butStopTimer:
			stopAlarmManager();
			showToast("Alarm disabled");
			break;
		}
	}

	public void setAlarmManager() {
		Calendar cal = Calendar.getInstance(); // get a Calendar object with current time
		cal.add(Calendar.MILLISECOND, mRepeatInterval); // to make this a time in the future
		Intent intent = new Intent(this, AlarmReceiver.class);
		int requestCode = 0; // reserved for future use in Android
		mAlarmIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		// in setRepeating() below, interval is in milliseconds
		am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), mRepeatInterval, mAlarmIntent);
	}

	public void stopAlarmManager() {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(mAlarmIntent); // no worries if it hasn't been instantiated yet
	}
	
	public void showToast(CharSequence inStr) {
		Toast.makeText(this, inStr, Toast.LENGTH_SHORT).show();
	}
}
