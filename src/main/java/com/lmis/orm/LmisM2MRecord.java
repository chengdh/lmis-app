package com.lmis.orm;

import com.lmis.util.Inflector;

import java.util.List;

public class LmisM2MRecord {
    LmisColumn mCol = null;
    int mId = 0;
    LmisDatabase mDatabase = null;

    public LmisM2MRecord(LmisDatabase lmisDatabase, LmisColumn col, int id) {
        mDatabase = lmisDatabase;
        mCol = col;
        mId = id;
    }

    public List<LmisDataRow> browseEach() {
        LmisManyToMany m2m = (LmisManyToMany) mCol.getType();

        String foreignColName = Inflector.getIdNameByCamel(mDatabase.modelName());
        return mDatabase.selectM2M(m2m.getDBHelper(), foreignColName + " = ?" , new String[]{mId + ""});
    }

    public LmisDataRow browseAt(int index) {
        List<LmisDataRow> list = browseEach();
        if (list.size() == 0) {
            return null;
        }
        return list.get(index);
    }
}
