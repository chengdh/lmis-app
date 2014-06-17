package com.lmis.orm;
import com.lmis.util.Inflector;

import java.util.List;

/**
 * The type Lmis o 2 m record.
 * 1对多数据结构
 */
public class LmisO2MRecord {
	private LmisColumn mCol = null;
	private String mValue = null;
	private int mId = 0;
	private LmisDatabase mDatabase = null;

	public LmisO2MRecord(LmisDatabase lmisDatabase, LmisColumn col, int id) {
		mDatabase = lmisDatabase;
		mCol = col;
		mId = id;
	}

	public List<LmisDataRow> browseEach() {

		LmisOneToMany o2m = (LmisOneToMany) mCol.getType();
        String foreignColName = Inflector.getIdNameByCamel(mDatabase.modelName());

		return mDatabase.selectO2M(o2m.getDBHelper(), foreignColName + " = ?", new String[] { mId + "" });
	}

	public LmisDataRow browseAt(int index) {
		List<LmisDataRow> list = browseEach();
		if (list.size() == 0) {
			return null;
		}
		return list.get(index);
	}

}
