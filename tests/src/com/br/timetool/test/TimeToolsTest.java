package com.br.timetool.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.br.timetool.TimeTools;
import com.br.timetool.entity.Day;
import com.br.timetool.entity.ScheduleTask;

import junit.framework.TestCase;

public class TimeToolsTest extends TestCase
{	
	private SimpleDateFormat sdf = 
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testTimeRounding() throws ParseException
	{
		Date d1 = sdf.parse("2010-08-13 19:36:39");
		Date rd1 = TimeTools.toNearestMinute(d1, 15);
		assertEquals("2010-08-13 19:30:00", sdf.format(rd1));
		
		
		Date d2 = sdf.parse("2010-08-15 19:41:39");
		Date rd2 = TimeTools.toNearestMinute(d2, 15);
		assertEquals("2010-08-15 19:45:00", sdf.format(rd2));
	}

}
