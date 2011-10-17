package com.br.timetool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	/* created once each time the AlarmManager broadcasts, then destroyed */
	final int NOTIFY_ID = 1; // arbitrary
	NotificationManager mNM;
	Context mContext;
	
	// EggTimer notification visual details
	CharSequence mTickerText = "EggTimer ringing!"; // quickly appears at top when alarm fires
	CharSequence mContentTitle = "EggTimer"; // appears when user pulls down notification
	
	CharSequence mContentText = "Are you working on what you planned?";
	int mIcon = R.drawable.icon; // default green Android
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("AlarmReceiver", "entered onReceive()");
		Recorder.addTimeslice(1);

		try {
			mContext = context;
			setNotificationManager();
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	public void setNotificationManager() {
		String ns = Context.NOTIFICATION_SERVICE;
		mNM = (NotificationManager) mContext.getSystemService(ns);
		
		// Instantiate the Notification:
		long when = System.currentTimeMillis();
		Notification n = new Notification(mIcon, mTickerText, when);
		n.flags |= Notification.FLAG_AUTO_CANCEL; // when the user selects the notification, it clears itself
		n.number = Recorder.getTimesliceCount();
		
		// Define the Notification's expanded message and Intent:
		Intent notificationIntent = new Intent(mContext, Recorder.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
		n.setLatestEventInfo(mContext, mContentTitle, mContentText, contentIntent);
		// TODO: blinkenlights, etc. (currently crashes on next line)
		// notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		
		// Pass the Notification to the NotificationManager:
		mNM.notify(NOTIFY_ID, n);	
    }
	
	protected void clearNotification() {
		// protected = callable from other Activities in same package
		mNM.cancel(NOTIFY_ID);
	}
}
