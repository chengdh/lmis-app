package com.lmis.orm;

public class LmisM2ORecord {
    private LmisColumn mCol = null;
    private String mValue = null;

    public LmisM2ORecord(LmisColumn col, String value) {
        mCol = col;
        mValue = value;
    }

    public LmisDataRow browse() {
        LmisManyToOne m2o = (LmisManyToOne) mCol.getType();
        LmisDatabase db = (LmisDatabase) m2o.getDBHelper();
        if (mValue.equals("false")) {
            return null;
        }
        return db.select(Integer.parseInt(mValue));
    }
}
