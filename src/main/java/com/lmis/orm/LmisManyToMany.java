package com.lmis.orm;

public class LmisManyToMany {
    LmisDBHelper mDb = null;

    public LmisManyToMany(LmisDBHelper db) {
        mDb = db;
    }

    public LmisDBHelper getDBHelper() {
        return mDb;
    }
}
