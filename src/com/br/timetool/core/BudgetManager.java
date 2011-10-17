package com.br.timetool.core;

import java.util.List;

import com.br.timetool.entity.Budget;

public class BudgetManager
{
	public BudgetManager()
	{
		
	}
	
	/**
	 * Checks that the time assigned to all items in the Budget matches up to the 
	 * available time. (and other rules too)
	 * 
	 * @param budget
	 * @return a list of problems with the budget
	 */
	public List<String> validateBudget(Budget budget)
	{
		//TODO should we make a separate class for (BudgetError)s?
		return null;
	}
	
}
