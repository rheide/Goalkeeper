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
import com.example.android.apis.graphics.ColorPickerDialog;
import com.example.android.apis.graphics.ColorPickerDialog.OnColorChangedListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

public class GoalEditor extends MainMenuActivity implements OnClickListener, OnItemSelectedListener, OnDateSetListener, OnColorChangedListener
{
	private static final int PICK_CATEGORY_COLOR = 7021;
	
	private EditText mNameText;
	private EditText mDescText;
	private TextView mDeadlineText;
	private TextView mCategoryText;
	private Spinner mCategorySpinner;
	private CheckBox mDeadlineCheck;

	static final int DATE_DIALOG_ID = 0;

	private Goal mGoal;

	private List<Category> mCategories;
	private ArrayAdapter<Category> mCatAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.goaleditor);
		
		mNameText = (EditText) findViewById(R.id.goalName);
		mDescText = (EditText) findViewById(R.id.goalDesc);
		mDeadlineText = (TextView) findViewById(R.id.goalDeadline);
		mCategoryText = (TextView) findViewById(R.id.goalCategoryText);
		mDeadlineCheck = (CheckBox) findViewById(R.id.goalDeadlineCheck);
		mCategorySpinner = (Spinner) findViewById(R.id.goalCategorySpinner);

		findViewById(R.id.butClose).setOnClickListener(this);
		findViewById(R.id.butDeleteCategory).setOnClickListener(this);
		findViewById(R.id.butChangeDeadline).setOnClickListener(this);
		findViewById(R.id.butCategoryColor).setOnClickListener(this);

		//retrieve goal to display!
		Bundle extras = this.getIntent().getExtras();
		long goalId = extras.getLong("goal_id");
		mGoal = mdb.getGoal(goalId);

		//load categories
		mCatAdapter = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item);
		HashMap<String, Category> cats = mdb.getCategories();
		this.mCategories = new ArrayList<Category>(cats.values());

		Category newCat = new Category("New category..");
		newCat.setId(-1l);
		mCategories.add(newCat);

		for (Category cat : this.mCategories)
			mCatAdapter.add(cat);

		mCategorySpinner.setAdapter(mCatAdapter);
		mCategorySpinner.setOnItemSelectedListener(this);

		loadGoal(mGoal);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		//category was selected
		Log.i("GoalEditor", "Category selected: " + pos);
		Category cat = mCategories.get(pos);
		if (cat.getId() < 0)
		{
			//show new category dialog here
			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.VERTICAL);

			TextView tv = new TextView(this);
			tv.setText("Type a name for the new category");

			ll.addView(tv);

			EditText et = new EditText(this);
			ll.addView(et);

			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Add category");
			adb.setView(ll);
			CategoryClickListener ccl = new CategoryClickListener(et);
			adb.setPositiveButton("OK", ccl);
			adb.setNegativeButton("Cancel", null);
			AlertDialog ad = adb.create();

			ad.show();
		}
		else
			mGoal.setCategory(cat);
	}

	private class CategoryClickListener implements DialogInterface.OnClickListener
	{
		private EditText mCatNameText;

		public CategoryClickListener(EditText catNameText)
		{
			this.mCatNameText = catNameText;
		}

		public void onClick(DialogInterface dialog, int which)
		{
			if (which == DialogInterface.BUTTON_POSITIVE)
			{
				//create new category and select it
				Category cat = new Category(mCatNameText.getText().toString());
				mdb.save(cat);

				mCategories.add(mCategories.size() - 1, cat); //add before new category item
				mCatAdapter.insert(cat, mCategories.size() - 2); //index to add the item BEFORE
				mCatAdapter.notifyDataSetChanged();

				updateCategory(cat);
			}
			else
			{
				//roll back to previous category
				int ix = mCategories.indexOf(mGoal.getCategory());
				mCategorySpinner.setSelection(ix);
			}
		}
	}

	private void updateCategory(Category cat)
	{
		mGoal.setCategory(cat);
		mdb.save(mGoal);

		int ix = mCategories.indexOf(cat);
		mCategorySpinner.setSelection(ix);
	}

	public void onNothingSelected(AdapterView<?> parent)
	{
	
	}

	public void onClick(View src)
	{
		switch (src.getId())
		{
			case R.id.butClose:
				//close window!
				save();
				this.setResult(RESULT_OK);
				this.finish();
				break;
			case R.id.butDeleteCategory :
				deleteCategory();
				break;
			case R.id.butChangeDeadline :
				showDialog(DATE_DIALOG_ID);
				break;
			case R.id.butCategoryColor : 
				pickColor();
				break; 
		}
	}
	
	private void pickColor()
	{
		 new ColorPickerDialog(this, this, mGoal.getCategory().getColor()).show();
	}
	
	public void colorChanged(int color)
	{
		Category cat = mGoal.getCategory();
		cat.setColor(color);
		mdb.save(cat);
		updateCategoryText(cat);
	}
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
			case DATE_DIALOG_ID :
				
				Calendar cal = GregorianCalendar.getInstance();
				if (mGoal.getDeadline() != null)
					cal.setTime(mGoal.getDeadline());
				
				return new DatePickerDialog(this, this, 
						cal.get(Calendar.YEAR), 
						cal.get(Calendar.MONTH),
						cal.get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}
	
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		mDeadlineText.setText(TimeTools.toDayString(cal.getTime()));
		mDeadlineCheck.setChecked(true);
	}

	private void save()
	{
		try
		{
			mGoal.setName(mNameText.getText().toString());
			mGoal.setDescription(mDescText.getText().toString());
			mGoal.setCategory(mCategories.get(mCategorySpinner.getSelectedItemPosition()));

			if (mDeadlineCheck.isChecked())
			{
				mGoal.setDeadline(TimeTools.toDay(mDeadlineText.getText().toString()));
			}
			else
				mGoal.setDeadline(null);
			mdb.save(mGoal);			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(this, "Could not save goal", Toast.LENGTH_SHORT).show();
		}
	}

	private void deleteCategory()
	{
		/* 
		 * check that:
		 * - No other active goal is using this category
		 * - Another category exists to switch to
		 */
		Category delCat = mGoal.getCategory();

		if (mCategories.size() == 2) //own category + addnewcat item
		{
			//unable to delete, tell user
			Toast.makeText(this, "You cannot delete the last category", Toast.LENGTH_SHORT).show();
		}
		else
		{
			boolean otherActiveGoalExists = false;
			List<Goal> goals = mdb.getGoals();
			for (Goal goal : goals)
			{
				if (goal.equals(mGoal))
					continue; //ignore our own goal
				if (goal.getCategory().equals(delCat))
				{
					otherActiveGoalExists = true;
					break;
				}
			}

			if (otherActiveGoalExists)
			{
				//notify user, can't delete
				Toast.makeText(this, "Cannot delete category: other goals are using it.", Toast.LENGTH_SHORT).show();
			}
			else
			{
				//delete category and switch current goal's category to other one
				mdb.deleteCategory(delCat.getName());

				mCategories.remove(delCat);
				mCatAdapter.remove(delCat);
				mCatAdapter.notifyDataSetChanged();

				Category repCat = mCategories.get(0);

				updateCategory(repCat);
			}
		}
	}

	private void loadGoal(Goal goal)
	{
		mNameText.setText(goal.getName());
		mDescText.setText(goal.getDescription());

		if (goal.getDeadline() != null)
		{
			mDeadlineCheck.setChecked(true);
			mDeadlineText.setText(TimeTools.toDayString(goal.getDeadline()));
		}
		else
		{
			mDeadlineCheck.setChecked(false);
			mDeadlineText.setText("0000-00-00");
		}

		Category cat = goal.getCategory();
		int ix = mCategories.indexOf(cat);
		if (ix >= 0)
			mCategorySpinner.setSelection(ix);
		
		updateCategoryText(cat);
	}
	
	private void updateCategoryText(Category cat)
	{	
		mCategoryText.setBackgroundColor(cat.getColor());
		mCategoryText.setText("Category: "+cat.getName());
	}

}
