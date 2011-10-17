package com.br.timetool.action;

import com.br.timetool.db.TTDatabase;

/**
 * Generic action interface for actions related to TT entities.
 * <p>Activities should keep a full list of undoable actions for themselves.</p>
 * <p>TTActions do not necessarily have to affect the db. A lot of actions should
 * happen only in memory first, and then later be persisted to the db all at once after
 * having been validated.</p>
 *
 */
public interface TTAction
{
	public void init(TTDatabase db);
	
	public void execute();
	
	public void undo();
}
