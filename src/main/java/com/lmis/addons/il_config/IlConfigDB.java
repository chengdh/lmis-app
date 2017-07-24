package com.lmis.addons.il_config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDBHelper;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisM2MIds;
import com.lmis.orm.LmisValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chengdh on 14/11/26.
 */
public class IlConfigDB extends LmisDatabase {

    /**
     * The constant SYSTEM_TITILE.
     */
    public static final String SYSTEM_TITILE = "system_title";
    /**
     * The constant CLIENT_NAME.
     */
    public static final String CLIENT_NAME = "client_name";
    /**
     * The constant INSURED_FEE.
     */
    public static final String INSURED_FEE = "insured_fee";
    /**
     * The constant CARRYING_FEE_GTE_ON_INSURED_FEE.
     */
    public static final String CARRYING_FEE_GTE_ON_INSURED_FEE = "carrying_fee_gte_on_insured_fee";
    /**
     * The constant MAX_HAND_FEE.
     */
    public static final String MAX_HAND_FEE = "max_hand_fee";

    /**
     * Instantiates a new Il config dB.
     *
     * @param context the context
     */
    public IlConfigDB(Context context) {
        super(context);
    }

    /**
     * Gets model name.
     *
     * @return the model name
     */
    @Override
    public String getModelName() {
        return "IlConfig";
    }

    /**
     * Gets model columns.
     *
     * @return the model columns
     */
    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        cols.add(new LmisColumn("key", "Key", LmisFields.varchar(30)));
        cols.add(new LmisColumn("title", "Title", LmisFields.varchar(30)));
        cols.add(new LmisColumn("value", "Value", LmisFields.varchar(30)));
        return cols;
    }

    /**
     * Get int value.
     *
     * @param key         the key
     * @param default_val the default _ val
     * @return the int
     */
    private int getIntValue(String key, int default_val) {
        int ret = default_val;
        String where = "key = ?";
        String[] whereArgs = {key};
        List<LmisDataRow> rows = select(where, whereArgs, null, null, null);
        if (rows.size() > 0) {
            ret = Integer.parseInt(rows.get(0).getString("value"));

        }
        return ret;
    }

    /**
     * Get insured fee.
     *
     * @return the float
     */
    public int getInsuredFee() {

        return getIntValue(INSURED_FEE, 2);
    }

    @Override
    public int count(String where, String[] whereArgs) {
        int count = 0;
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

    @Override
    public List<LmisDataRow> select(String where, String[] whereArgs, String groupBy, String having, String orderBy) {
        List<LmisDataRow> rows = new ArrayList<LmisDataRow>();
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

    @Override
    public int update(LmisValues values, String where, String[] whereArgs) {
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

}
