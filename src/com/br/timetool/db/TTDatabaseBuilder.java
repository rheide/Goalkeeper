package com.br.timetool.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Generates SQLite database tables, indexes and keys 
 *
 */
public class TTDatabaseBuilder extends SQLiteOpenHelper
{
	private static final String DB_NAME = "goalkeeper";
	private static final int DB_VERSION = 13; //our data-model version

	public TTDatabaseBuilder(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.i("DB", "Creating new database");
		
		//core: categories, goals, tasks
		db.execSQL("CREATE TABLE IF NOT EXISTS `category` (`id` INTEGER PRIMARY KEY AUTOINCREMENT ,`name` VARCHAR(255) NOT NULL, `color` INT NOT NULL DEFAULT 0, `status` INT NOT NULL DEFAULT 0);");
		db.execSQL("CREATE TABLE IF NOT EXISTS `goal` (`id` INTEGER PRIMARY KEY AUTOINCREMENT ,`name` VARCHAR(255) NOT NULL ,`description` TEXT NULL ,`creation` INT NULL ,`deadline` INT NULL, `status` INT NOT NULL DEFAULT 0, `category_id` INT NOT NULL);");				
		db.execSQL("CREATE TABLE IF NOT EXISTS `task` (`id` INTEGER PRIMARY KEY AUTOINCREMENT ,`name` VARCHAR(255) NULL ,`description` TEXT NULL ,`status` INT NOT NULL DEFAULT 0, `goal_id` INT NOT NULL );");
		
		//budget
		db.execSQL("CREATE TABLE IF NOT EXISTS `budget` (`id` INTEGER PRIMARY KEY AUTOINCREMENT ,`date` INT NULL );");
		db.execSQL("CREATE TABLE IF NOT EXISTS `budget_task` (`budget_id` INT NOT NULL ,`task_id` INT NOT NULL ,`hours` DECIMAL(3,2) NOT NULL DEFAULT 0 ,PRIMARY KEY (`task_id`, `budget_id`) );");
		
		//schedule
		db.execSQL("CREATE TABLE IF NOT EXISTS `day` (`id` INTEGER PRIMARY KEY AUTOINCREMENT ,`date` INT NULL ,`planned` INT NOT NULL DEFAULT 1 );");
		db.execSQL("CREATE TABLE IF NOT EXISTS `schedule_task` (`id` INTEGER PRIMARY KEY AUTOINCREMENT ,`start_time` INT NULL ,`end_time` INT NULL ,`satisfaction` INT NOT NULL DEFAULT 0 , `do_not_disturb` INT NOT NULL DEFAULT 0, `rollover_type` INT NOT NULL DEFAULT 0, `day_id` INT NOT NULL ,`task_id` INT NOT NULL );");		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w("DB", "DB version mismatch. Old: " + oldVersion + ", New: " +
				newVersion + " - Dropping old data!");

		db.execSQL("DROP TABLE IF EXISTS category");
		db.execSQL("DROP TABLE IF EXISTS goal");
		db.execSQL("DROP TABLE IF EXISTS task");
		db.execSQL("DROP TABLE IF EXISTS schedule_task");
		db.execSQL("DROP TABLE IF EXISTS day");
		db.execSQL("DROP TABLE IF EXISTS budget");
		db.execSQL("DROP TABLE IF EXISTS budget_task");
		db.execSQL("DROP TABLE IF EXISTS budget_goal");

		onCreate(db);
	}

}
