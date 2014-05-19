package com.lmis.orm;

public class LmisManyToOne {
	LmisDBHelper mDb = null;

	public LmisManyToOne(LmisDBHelper db) {
		mDb = db;
	}

	public LmisDBHelper getDBHelper() {
		return mDb;
	}
}
