package com.lmis.orm;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.support.LmisUser;
import com.lmis.util.Inflector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class LmisDatabase extends LmisSQLiteHelper implements LmisDBHelper {
    public static final String TAG = "LmisDatabase";

    protected Context mContext;

    protected LmisUser mUser;

    protected LmisDBHelper mDBHelper = null;
    List<LmisDataRow> mRemovedRecords = new ArrayList<LmisDataRow>();

    public LmisDatabase(Context context) {
        super(context);
        mUser = LmisUser.current(context);
        mContext = context;
        mDBHelper = this;
    }

    public String modelName() {
        return mDBHelper.getModelName();
    }

    public String tableName() {
        return Inflector.tableize(modelName());
    }

    public void setAccountUser(LmisUser user) {
        mUser = user;
    }

    public int count() {
        return count(null, null);
    }

    public int count(String where, String[] whereArgs) {
        int count = 0;
        if (where == null) {
            where = " oea_name = ?";
            whereArgs = new String[]{mUser.getAndroidName()};
        } else {
            where += " and oea_name = ?";
            List<String> tmpWhereArgs = new ArrayList<String>();
            tmpWhereArgs.addAll(Arrays.asList(whereArgs));
            tmpWhereArgs.add(mUser.getAndroidName());
            whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query(tableName(), new String[]{"count(*) as total"},
                where, whereArgs, null, null, null);
        if (cr.moveToFirst()) {
            count = cr.getInt(0);
        }
        cr.close();
        db.close();
        return count;
    }

    public int update(LmisValues values, int id) {
        int ret = update(values, "id = ?", new String[]{id + ""});
        if (ret > 0)
            broadcastInfo(id);
        return ret;
    }

    public void updateManyToManyRecords(String column, LmisM2MIds.Operation operation, int id, int rel_id) {
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(rel_id);
        updateManyToManyRecords(column, operation, id, ids);
    }

    public void updateManyToManyRecords(String column, LmisM2MIds.Operation operation, int id, List<Integer> ids) {
        LmisDBHelper m2mObj = findFieldModel(column);
        manageMany2ManyRecords(m2mObj, operation, (long) id, ids);
    }

    public int update(LmisValues values, String where, String[] whereArgs) {
        if (where == null) {
            where = " oea_name = ?";
            whereArgs = new String[]{mUser.getAndroidName()};
        } else {
            where += " and oea_name = ?";
            List<String> tmpWhereArgs = new ArrayList<String>();
            tmpWhereArgs.addAll(Arrays.asList(whereArgs));
            tmpWhereArgs.add(mUser.getAndroidName());
            whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
        }
        if (!values.contains("oea_name")) {
            values.put("oea_name", mUser.getAndroidName());
        }
        SQLiteDatabase db = getWritableDatabase();
        HashMap<String, Object> res = getContentValues(values);
        ContentValues cValues = (ContentValues) res.get("cValues");
        int count = db.update(tableName(), cValues, where, whereArgs);
        db.close();
        if (res.containsKey("m2mObjects")) {
            @SuppressWarnings("unchecked")
            List<HashMap<String, Object>> objectList = (List<HashMap<String, Object>>) res.get("m2mObjects");
            for (HashMap<String, Object> obj : objectList) {
                LmisDBHelper m2mDb = (LmisDBHelper) obj.get("m2mObject");
                for (LmisDataRow row : select(where, whereArgs, null, null, null)) {
                    manageMany2ManyRecords(m2mDb, LmisM2MIds.Operation.REPLACE, row.getInt("id"), obj.get("m2mRecordsObj"));
                }
            }
        }
        return count;

    }

    public List<Long> createORReplace(List<LmisValues> listValues) {
        return createORReplace(listValues, false);
    }

    public List<Long> createORReplace(List<LmisValues> listValues, boolean canDeleteLocalIfNotExists) {
        List<Long> ids = new ArrayList<Long>();
        for (LmisValues values : listValues) {
            long id = values.getInt("id");
            if (id == -1)
                continue;
            int count = count("id = ?", new String[]{values.getString("id")});
            if (count == 0) {
                ids.add(id);
                create(values);
            } else {
                ids.add(id);
                update(values, values.getInt("id"));
            }
        }
        if (canDeleteLocalIfNotExists) {
            mRemovedRecords = new ArrayList<LmisDataRow>();
            for (LmisDataRow row : select()) {
                if (!ids.contains(Long.parseLong(row.getString("id")))) {
                    delete(row.getInt("id"));
                    mRemovedRecords.add(row);
                }
            }
        }
        return ids;
    }

    public List<LmisDataRow> getRemovedRecords() {
        return mRemovedRecords;
    }

    public long create(LmisValues values) {
        long newId = 0;
        if (!values.contains("oea_name")) {
            values.put("oea_name", mUser.getAndroidName());
        }
        SQLiteDatabase db = getWritableDatabase();
        HashMap<String, Object> res = getContentValues(values);
        ContentValues cValues = (ContentValues) res.get("cValues");
        newId = db.insert(tableName(), null, cValues);
        try {
            newId = cValues.getAsInteger("id");
        } catch (Exception ex) {
            Log.d(TAG, "cValues not give a id");
        }
        broadcastInfo(newId);
        db.close();
        //插入many2many表的值
        if (res.containsKey("m2mObjects")) {
            @SuppressWarnings("unchecked")
            List<HashMap<String, Object>> objectList = (List<HashMap<String, Object>>) res.get("m2mObjects");
            for (HashMap<String, Object> obj : objectList) {
                LmisDBHelper m2mDb = (LmisDBHelper) obj.get("m2mObject");
                manageMany2ManyRecords(m2mDb, LmisM2MIds.Operation.ADD, newId, obj.get("m2mRecordsObj"));
            }
        }
        return newId;
    }

    protected HashMap<String, Object> getContentValues(LmisValues values) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        ContentValues cValues = new ContentValues();
        List<HashMap<String, Object>> m2mObjectList = new ArrayList<HashMap<String, Object>>();
        List<LmisColumn> cols = mDBHelper.getModelColumns();
        cols.addAll(getDefaultCols());
        for (LmisColumn col : cols) {
            String key = col.getName();
            if (values.contains(key)) {
                if (values.get(key) instanceof LmisM2MIds) {
                    HashMap<String, Object> m2mObjects = new HashMap<String, Object>();
                    LmisDBHelper m2mDb = findFieldModel(key);
                    m2mObjects.put("m2mObject", m2mDb);
                    m2mObjects.put("m2mRecordsObj", values.get(key));
                    m2mObjectList.add(m2mObjects);
                    continue;
                }
                //如果是o2m字段,则直接跳过
                if (values.get(key) instanceof LmisO2MIds) {
                    continue;
                }
                //处理blob字段
                Object val = values.get(key);
                if (val instanceof byte[])
                    cValues.put(key, (byte[]) val);
                else
                    cValues.put(key, values.get(key).toString());
            }
        }
        result.put("m2mObjects", m2mObjectList);
        result.put("cValues", cValues);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected void manageMany2ManyRecords(LmisDBHelper relDb, LmisM2MIds.Operation operation,
                                          long id, Object idsObj) {
        String first_table = tableName();
        String second_table = Inflector.tableize(relDb.getModelName());
        String rel_table = first_table + "_" + second_table;
        List<Integer> ids = new ArrayList<Integer>();
        if (idsObj instanceof LmisM2MIds) {
            LmisM2MIds idsObject = (LmisM2MIds) idsObj;
            operation = idsObject.getOperation();
            ids = idsObject.getIds();
        }
        if (idsObj instanceof List) {
            ids = (List<Integer>) idsObj;
        }
        SQLiteDatabase db = null;
        String col_first = Inflector.getIdName(first_table);
        String col_second = Inflector.getIdName(second_table);
        if (operation == LmisM2MIds.Operation.REPLACE) {
            db = getWritableDatabase();
            db.delete(rel_table, col_first + " = ? AND oea_name = ?",
                    new String[]{id + "", mUser.getAndroidName()});
            db.close();
        }
        for (Integer rId : ids) {
            ContentValues values = new ContentValues();
            values.put(col_first, id);
            values.put(col_second, rId);
            values.put("oea_name", mUser.getAndroidName());
            switch (operation) {
                case ADD:
                case APPEND:
                case REPLACE:
                    Log.d(TAG,
                            "manageMany2ManyRecords() ADD, APPEND, REPLACE called");
                    if (!hasRecord(rel_table, col_first + " = ? AND " + col_second
                            + " = ? AND oea_name = ?", new String[]{id + "",
                            rId + "", mUser.getAndroidName()})) {
                        db = getWritableDatabase();
                        db.insert(rel_table, null, values);
                        db.close();
                    }
                    break;
                case REMOVE:
                    Log.d(TAG, "createMany2ManyRecords() REMOVE called");
                    db = getWritableDatabase();
                    db.delete(rel_table, col_first + " = ? AND " + col_second
                            + " = ? AND oea_name = ?", new String[]{id + "",
                            rId + "", mUser.getAndroidName()});
                    db.close();
                    break;
            }
        }
    }

    private boolean hasRecord(String table, String where, String[] whereArgs) {
        boolean flag = false;
        if (where == null) {
            where = " oea_name = ?";
            whereArgs = new String[]{mUser.getAndroidName()};
        } else {
            where += " and oea_name = ?";
            List<String> tmpWhereArgs = new ArrayList<String>();
            tmpWhereArgs.addAll(Arrays.asList(whereArgs));
            tmpWhereArgs.add(mUser.getAndroidName());
            whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query(table, new String[]{"count(*) as total"},
                where, whereArgs, null, null, null);
        cr.moveToFirst();
        int count = cr.getInt(0);
        cr.close();
        db.close();
        if (count > 0) {
            flag = true;
        }
        return flag;
    }

    private LmisDBHelper findFieldModel(String field) {
        for (LmisColumn col : mDBHelper.getModelColumns()) {
            if (field.equals(col.getName()) && col.getType() instanceof LmisManyToMany) {
                LmisManyToMany m2m = (LmisManyToMany) col.getType();
                return m2m.getDBHelper();
            }
        }
        return null;
    }

    public int delete() {
        return delete(null, null);
    }

    public int delete(String table) {
        return delete(table, null, null);
    }

    public int delete(int id) {
        return delete("id = ?", new String[]{id + ""});
    }

    public int delete(String where, String[] whereArgs) {
        return delete(tableName(), where, whereArgs);
    }

    private int delete(String table, String where, String[] whereArgs) {
        if (where == null) {
            where = "oea_name = ?";
            whereArgs = new String[]{mUser.getAndroidName()};
        } else {
            where += " AND oea_name = ?";
            List<String> tmpWhereArgs = new ArrayList<String>();
            tmpWhereArgs.addAll(Arrays.asList(whereArgs));
            tmpWhereArgs.add(mUser.getAndroidName());
            whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
        }

        int count = 0;
        if (deleteMany2ManyRecord(select(where, whereArgs))) {
            SQLiteDatabase db = getWritableDatabase();
            count = db.delete(table, where, whereArgs);
            db.close();
        }
        return count;
    }

    private boolean deleteMany2ManyRecord(List<LmisDataRow> records) {
        for (LmisDataRow rec : records) {
            int id = rec.getInt("id");
            for (LmisColumn col : getModelColumns()) {
                if (col.getType() instanceof LmisManyToMany) {
                    LmisDatabase m2mDB = (LmisDatabase) ((LmisManyToMany) col
                            .getType()).getDBHelper();
                    List<Integer> idsObj = new ArrayList<Integer>();
                    for (LmisDataRow m2mRec : rec.getM2MRecord(col.getName())
                            .browseEach())
                        idsObj.add(m2mRec.getInt("id"));
                    manageMany2ManyRecords(m2mDB, LmisM2MIds.Operation.REMOVE, id, idsObj);
                }
            }
        }
        return true;
    }

    public List<LmisDataRow> select() {
        return select(null, null, null, null, null);
    }

    public LmisDataRow select(int id) {
        List<LmisDataRow> rows = select("id = ?", new String[]{id + ""}, null, null, null);
        if (rows.size() > 0) {
            return rows.get(0);
        }
        return null;
    }

    public List<LmisDataRow> select(String where, String[] whereArgs) {
        return select(where, whereArgs, null, null, null);
    }

    public List<Integer> ids() {
        List<Integer> ids = new ArrayList<Integer>();
        for (LmisDataRow row : select()) {
            ids.add(row.getInt("id"));
        }
        return ids;
    }

    public List<Integer> ids(String where, String[] whereArgs) {
        List<Integer> ids = new ArrayList<Integer>();
        for (LmisDataRow row : select(where, whereArgs)) {
            ids.add(row.getInt("id"));
        }
        return ids;
    }

    public List<LmisDataRow> select(String where, String[] whereArgs, String groupBy, String having, String orderBy) {
        if (where == null) {
            where = "oea_name = ?";
            whereArgs = new String[]{mUser.getAndroidName()};
        } else {
            where += " AND oea_name = ?";
            List<String> tmpWhereArgs = new ArrayList<String>();
            tmpWhereArgs.addAll(Arrays.asList(whereArgs));
            tmpWhereArgs.add(mUser.getAndroidName());
            whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
        }
        List<LmisDataRow> rows = new ArrayList();
        SQLiteDatabase db = getReadableDatabase();
        String[] cols = getColumns();
        Cursor cr = db.query(tableName(), cols, where, whereArgs, groupBy, having, orderBy);
        List<LmisColumn> mCols = mDBHelper.getModelColumns();
        mCols.addAll(getDefaultCols());
        if (cr.moveToFirst()) {
            do {
                LmisDataRow row = new LmisDataRow();
                for (LmisColumn col : mCols) {
                    row.put(col.getName(), createRowData(col, cr));
                }
                rows.add(row);
            } while (cr.moveToNext());
        }
        cr.close();
        db.close();
        return rows;
    }

    public List<LmisDataRow> selectM2M(LmisDBHelper rel_db, String where, String[] whereArgs) {
        if (where == null) {
            where = "oea_name = ?";
            whereArgs = new String[]{mUser.getAndroidName()};
        } else {
            where += " AND oea_name = ?";
            List<String> tmpWhereArgs = new ArrayList<String>();
            tmpWhereArgs.addAll(Arrays.asList(whereArgs));
            tmpWhereArgs.add(mUser.getAndroidName());
            whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
        }
        List<LmisDataRow> rows = new ArrayList<LmisDataRow>();
        HashMap<String, Object> mRelObj = relTableColumns(rel_db);
        @SuppressWarnings("unchecked")
        List<LmisColumn> mCols = (List<LmisColumn>) mRelObj.get("columns");
        List<String> cols = new ArrayList<String>();
        for (LmisColumn col : mCols) {
            cols.add(col.getName());
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query(mRelObj.get("rel_table").toString(),
                cols.toArray(new String[cols.size()]), where, whereArgs, null,
                null, null);
        LmisDatabase rel_db_obj = (LmisDatabase) rel_db;
        String rel_col_name = Inflector.getIdNameByCamel(rel_db_obj.getModelName());
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex(rel_col_name));
                rows.add(rel_db_obj.select(id));
            } while (cr.moveToNext());
        }
        cr.close();
        db.close();
        return rows;
    }

    /**
     * 获取o2m字段的值
     *
     * @param rel_db    the rel _ db
     * @param where     the where
     * @param whereArgs the where args
     * @return the list
     */
    public List<LmisDataRow> selectO2M(LmisDBHelper rel_db, String where, String[] whereArgs) {
        if (where == null) {
            where = "oea_name = ?";
            whereArgs = new String[]{mUser.getAndroidName()};
        } else {
            where += " AND oea_name = ?";
            List<String> tmpWhereArgs = new ArrayList<String>();
            tmpWhereArgs.addAll(Arrays.asList(whereArgs));
            tmpWhereArgs.add(mUser.getAndroidName());
            whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
        }
        List<LmisDataRow> rows = ((LmisDatabase) rel_db).select(where, whereArgs, null, null, null);
        return rows;
    }

    protected Object createRowData(LmisColumn col, Cursor cr) {
        if (col.getType() instanceof String) {
            if (col.getType().equals(LmisFields.blob()))
                return cr.getBlob(cr.getColumnIndex(col.getName()));
            else
                return cr.getString(cr.getColumnIndex(col.getName()));
        }
        if (col.getType() instanceof LmisManyToOne) {
            return new LmisM2ORecord(col, cr.getString(cr.getColumnIndex(col
                    .getName())));
        }
        if (col.getType() instanceof LmisManyToMany) {
            return new LmisM2MRecord(this, col,
                    cr.getInt(cr.getColumnIndex("id")));
        }
        if (col.getType() instanceof LmisOneToMany) {
            return new LmisO2MRecord(this, col,
                    cr.getInt(cr.getColumnIndex("id")));
        }

        return null;
    }

    protected String[] getColumns() {
        List<String> cols = new ArrayList<String>();
        cols.add("id");
        for (LmisColumn col : mDBHelper.getModelColumns()) {
            if (col.getType() instanceof String
                    || col.getType() instanceof LmisManyToOne) {
                cols.add(col.getName());
            }
        }
        cols.add("oea_name");
        return cols.toArray(new String[cols.size()]);
    }

    public LmisHelper getLmisInstance() {
        LmisHelper lmis = null;
        try {
            lmis = new LmisHelper(mContext, mUser, this);
        } catch (Exception e) {
            Log.d(TAG, "LmisDatabase->getLmisInstance()");
            Log.e(TAG, e.getMessage() + ". No connection with Lmis server");
        }
        return lmis;
    }


    public boolean truncateTable(String table) {
        if (delete(table) > 0) {
            return true;
        }
        return false;
    }

    public boolean truncateTable() {
        if (delete() > 0) {
            return true;
        }
        return false;
    }

    public boolean isEmptyTable() {
        boolean flag = true;
        if (count() > 0) {
            flag = false;
        }
        return flag;
    }

    public HashMap<String, Object> relTableColumns(LmisDBHelper relDB) {
        List<LmisColumn> mCols = new ArrayList<LmisColumn>();
        HashMap<String, Object> res = new HashMap<String, Object>();
        String main_table = tableName();
        String ref_table = Inflector.tableize(relDB.getModelName());
        String rel_table = main_table + "_" + ref_table;
        res.put("rel_table", rel_table);
        mCols.add(new LmisColumn(Inflector.getIdName(main_table), "Main ID", LmisFields.integer()));
        mCols.add(new LmisColumn(Inflector.getIdName(ref_table), "Ref ID", LmisFields.integer()));
        mCols.add(new LmisColumn("oea_name", "Android name", LmisFields.text()));
        res.put("columns", mCols);
        return res;
    }

    public List<LmisColumn> getDefaultCols() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();
        cols.add(new LmisColumn("id", "id", LmisFields.integer()));
        cols.add(new LmisColumn("oea_name", "android name", LmisFields.varchar(50),
                false));
        return cols;
    }

    public List<LmisColumn> getDatabaseColumns() {
        return mDBHelper.getModelColumns();
    }

    public List<LmisColumn> getDatabaseServerColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();
        for (LmisColumn col : mDBHelper.getModelColumns()) {
            if (col.canSync()) {
                cols.add(col);
            }
        }
        return cols;
    }

    public int lastId() {
        int last_id = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query(tableName(), new String[]{"MAX(id) as id"},
                "oea_name = ?", new String[]{mUser.getAndroidName()}, null,
                null, null);
        if (cr.moveToFirst())
            last_id = cr.getInt(0);
        cr.close();
        db.close();
        return last_id;
    }

    private void broadcastInfo(long newId) {
        Intent intent = new Intent();
        intent.setAction(DataSetChangeReceiver.DATA_CHANGED);
        intent.putExtra("id", String.valueOf(newId));
        intent.putExtra("model", modelName());
        mContext.sendBroadcast(intent);
    }
}
