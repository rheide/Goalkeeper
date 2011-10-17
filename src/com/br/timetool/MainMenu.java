package com.br.timetool;

import com.br.timetool.core.MainMenuActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends MainMenuActivity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("MainMenu", "entered onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//add click handler to buttons:
		findViewById(R.id.mainmenu_info).setOnClickListener(this);
		findViewById(R.id.mainmenu_eggtimer).setOnClickListener(this);
		findViewById(R.id.mainmenu_goals).setOnClickListener(this);
		findViewById(R.id.mainmenu_budget).setOnClickListener(this);
		findViewById(R.id.mainmenu_schedule).setOnClickListener(this);
		findViewById(R.id.mainmenu_review).setOnClickListener(this);
		
	}

	public void onClick(View src) {
		
		super.onMainMenuItemSelected(src.getId());
		
	}
}