package com.br.timetool.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;

public class Category extends TTEntity
{
	public static final String TABLE = "category c";
	public static final String FIELDS = "c.id, c.name, c.color, c.status";
	
	public static final int DEFAULT_COLOR = Color.rgb(170, 220, 255);
	
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_DELETED = 1;
	
	private Long id;
	
	private String name;
	
	private int color = DEFAULT_COLOR;
	
	private int status = STATUS_ACTIVE;
	
	public Category()
	{
		
	}
	
	public Category(String name)
	{
		this.name = name;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public String toString()
	{
		return name;
	}
	
	@Override
	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		
		cv.put("id", id);
		cv.put("name", name);
		cv.put("color", color);
		cv.put("status", status);
		
		return cv;
	}
	
	@Override
	public Long getId()
	{
		return id;
	}
	
	@Override
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public int getColor()
	{
		return color;
	}
	
	public void setColor(int color)
	{
		this.color = color;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	/**
	 * Creates a new Category from a database cursor. 
	 * 
	 * @param insertCat An existing Category object to modify, or null to create a new one. 
	 * @param cursor
	 * @param index The column index to start reading the category from
	 * @return
	 */
	public static Category buildFromCursor(Category insertCat, Cursor cursor, int index)
	{
		Category cat = insertCat;
		if (cat == null)
			cat = new Category();
		
		cat.setId(cursor.getLong(index++));
		cat.setName(cursor.getString(index++));
		cat.setColor(cursor.getInt(index++));
		cat.setStatus(cursor.getInt(index++));
		
		return cat;
	}
}
