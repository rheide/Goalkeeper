package com.br.timetool.core;

import com.br.timetool.InfoManagerBudgetEditor;
import com.br.timetool.InfoManagerGoalViewer;
import com.br.timetool.Launcher;
import com.br.timetool.R;
import com.br.timetool.ScheduleViewTester;
import com.br.timetool.db.TTDatabase;
import com.br.timetool.db.TTDatabaseImpl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Base class for items that need to display the main menu
 *
 */
public class MainMenuActivity extends Activity
{
	protected TTDatabase mdb;
	
	public MainMenuActivity()
	{
		super();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{	
		super.onCreate(savedInstanceState);
		initDatabase();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//will re-activate the db if not active
		initDatabase();
	}
	
	protected boolean onMainMenuItemSelected(int itemId)
	{
		Intent intent = new Intent();
		
		//prevent duplicate activities from running - always reuse the old activity if available
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		
		switch (itemId)
		{
			case R.id.mainmenu_info :
				//TODO open current task screen
				break;
			case R.id.mainmenu_eggtimer :
				intent.setClass(this, Launcher.class);
				startActivity(intent);
				return true;
			case R.id.mainmenu_budget :
				intent.setClass(this, InfoManagerBudgetEditor.class);
				startActivity(intent);
				return true;
			case R.id.mainmenu_goals :
				intent.setClass(this, InfoManagerGoalViewer.class);
				startActivity(intent);
				return true;
			case R.id.mainmenu_schedule :
				intent.setClass(this, ScheduleViewTester.class);
				startActivity(intent);
				return true;
			case R.id.mainmenu_review :
				//TODO
				break;
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return onMainMenuItemSelected(item.getItemId());
	}
	
	@Override
	protected void onResume()
	{
		Log.i("MMActivity", "MMActivity "+getClass().getName()+" resumed. Database: "+mdb);
		initDatabase();
		super.onResume();
	}
	
	/**
	 * Creates a database connection if not currently connected to the db
	 */
	protected void initDatabase()
	{	
		if (mdb == null)
		{
			mdb = new TTDatabaseImpl();
			((TTDatabaseImpl)mdb).init(getApplicationContext());
		}
	}
	
	private void closeDatabase()
	{
		if (mdb != null)
		{
			((TTDatabaseImpl)mdb).close();
			mdb = null;
		}
	}
	
	@Override
	protected void onStop()
	{	
		Log.i("MMActivity", "MMActivity "+getClass().getName()+" stopped. Database: "+mdb);
		closeDatabase();
		super.onStop();
	}
}
