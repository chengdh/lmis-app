package com.lmis.orm;
import java.util.List;

//one to many record
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
		return mDatabase.selectO2M(o2m.getDBHelper(), mDatabase.simpTableName()
				+ "_id = ?", new String[] { mId + "" });
	}

	public LmisDataRow browseAt(int index) {
		List<LmisDataRow> list = browseEach();
		if (list.size() == 0) {
			return null;
		}
		return list.get(index);
	}
}
