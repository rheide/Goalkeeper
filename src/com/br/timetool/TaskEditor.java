package com.br.timetool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.br.timetool.core.MainMenuActivity;
import com.br.timetool.db.TTDatabaseImpl;
import com.br.timetool.entity.Category;
import com.br.timetool.entity.Goal;
import com.br.timetool.entity.Task;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class TaskEditor extends MainMenuActivity implements OnClickListener
{	
	private EditText mNameText;
	private EditText mDescText;
	
	private TextView mTaskIndexText;
	
	private Goal mGoal;
	private Task mTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taskeditor);
		
		mNameText = (EditText) findViewById(R.id.taskName);
		mDescText = (EditText) findViewById(R.id.taskDesc);
		mTaskIndexText = (TextView) findViewById(R.id.taskIndexText);
		
		((Button) findViewById(R.id.butCloseTask)).setOnClickListener(this);
		((Button) findViewById(R.id.butPrevTask)).setOnClickListener(this);
		((Button) findViewById(R.id.butNextTask)).setOnClickListener(this);
		
		//retrieve goal to display!
		Bundle extras = this.getIntent().getExtras();
		long taskId = extras.getLong("task_id");
		long goalId = extras.getLong("goal_id");
		
		mGoal = mdb.getGoal(goalId); //this also loads other tasks of the same goal but that's ok
		for (Task task : mGoal.getTasks())
		{
			if (task.getId().longValue() == taskId)
			{
				this.mTask = task;
				break;
			}
		}
		
		if (mTask == null)
		{
			Log.e("TaskEditor", "Could not find task "+taskId+" in goal "+goalId);
		}
		
		loadTask(mTask);
	}

	public void onClick(View src)
	{
		int ix;
		switch (src.getId())
		{
			case R.id.butCloseTask :
				//close window!
				save();
				this.setResult(RESULT_OK);
				this.finish();
				break;
			case R.id.butPrevTask : 
				ix = mGoal.getTasks().indexOf(mTask);
				if (ix - 1 >= 0)
				{
					save(); //current task
					loadTask(mGoal.getTasks().get(ix-1));
				}
				break;
			case R.id.butNextTask :
				ix = mGoal.getTasks().indexOf(mTask);
				if (ix + 1 < mGoal.getTasks().size())
				{
					save(); //current task
					loadTask(mGoal.getTasks().get(ix+1));
				}
				break;
		}
	}

	private void save()
	{
		try
		{
			mTask.setName(mNameText.getText().toString());
			mTask.setDescription(mDescText.getText().toString());
			mdb.save(mTask);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(this, "Could not save task", Toast.LENGTH_SHORT).show();
		}
	}

	private void loadTask(Task task)
	{
		this.mTask = task;
		int ix = mGoal.getTasks().indexOf(task) + 1;
		
		mTaskIndexText.setText("Task "+ix+"/"+mGoal.getTasks().size());
		
		mNameText.setText(task.getName());
		mDescText.setText(task.getDescription());
	}

}
