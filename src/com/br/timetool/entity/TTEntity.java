package com.br.timetool.entity;

import android.content.ContentValues;

public abstract class TTEntity
{
	public abstract Long getId();
	
	public abstract void setId(Long id);
	
	public abstract ContentValues getContentValues();
	
	@Override
	public final int hashCode()
	{
		if (getId() == null)
			return super.hashCode();
		else
			return getId().hashCode();
	}
	
	@Override
	public final boolean equals(Object that)
	{
		//ensure that two objects qualify as equal, 
		//even if they are different objects in memory, 
		//but the same object (same id) in the database
		
		if (that == null) return false;
		
		if (!(that instanceof TTEntity)) return false;
		
		if (this.getId() == null)
			return super.equals(that);
		
		return this.getClass().equals(that.getClass()) && 
				this.getId().equals(((TTEntity)that).getId());
	}
}
