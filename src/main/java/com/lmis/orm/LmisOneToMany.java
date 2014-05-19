package com.lmis.orm;

public class LmisOneToMany {
	LmisDBHelper mDb = null;

	public LmisOneToMany(LmisDBHelper db) {
		mDb = db;
	}

	public LmisDBHelper getDBHelper() {
		return mDb;
	}
}
