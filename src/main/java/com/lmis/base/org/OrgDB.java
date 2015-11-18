package com.lmis.base.org;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * The type Org dB.
 */
public class OrgDB extends LmisDatabase {

    /**
     * Instantiates a new Org dB.
     *
     * @param context the context
     */
    public OrgDB(Context context) {
        super(context);
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

    /**
     * Gets model name.
     *
     * @return the model name
     */
    @Override
    public String getModelName() {
        return "org";
    }

    /**
     * Gets model columns.
     *
     * @return the model columns
     */
    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        cols.add(new LmisColumn("name", "Name", LmisFields.varchar(128)));
        cols.add(new LmisColumn("simp_name", "Simple Name", LmisFields.varchar(128)));
        cols.add(new LmisColumn("parent_id", "Parent Org", LmisFields.integer()));
        cols.add(new LmisColumn("phone", "Phone", LmisFields.varchar(20)));
        cols.add(new LmisColumn("manager", "Manager", LmisFields.varchar(40)));
        cols.add(new LmisColumn("location", "Location", LmisFields.varchar(60)));
        cols.add(new LmisColumn("code", "Code", LmisFields.varchar(20)));
        cols.add(new LmisColumn("is_summary", "Is Summary", LmisFields.varchar(20)));
        cols.add(new LmisColumn("is_yard", "Is Yard", LmisFields.varchar(20)));
        cols.add(new LmisColumn("carrying_fee_gte_on_insured_fee", "carrying_fee_gte_on_insured_fee", LmisFields.integer()));
        cols.add(new LmisColumn("is_visible", "Is Visible", LmisFields.varchar(20)));
        cols.add(new LmisColumn("is_active", "Is Active", LmisFields.varchar(20)));
        cols.add(new LmisColumn("auto_generate_to_short_carrying_fee", "auto generate to short carrying_fee", LmisFields.varchar(20)));
        cols.add(new LmisColumn("agtscf_rate", "agtscf_rate", LmisFields.varchar(20)));
        cols.add(new LmisColumn("fixed_to_short_carrying_fee", "fixed to short carrying fee", LmisFields.varchar(20)));
        return cols;
    }

    /**
     * Get children orgs.
     *
     * @param parentID the parent iD
     * @return the list
     */
    public List<LmisDataRow> getChildrenOrgs(int parentID) {
        String where = "parent_id = ?";
        String[] wherArgs = {parentID + ""};
        return this.select(where, wherArgs);
    }

    /**
     * Get carrying fee gte on insured fee.
     *
     * @param orgID the org iD
     * @return the int
     */
    public int getCarryingFeeGteOnInsuredFee(int orgID) {
        int ret = 0;
        String where = "id = ?";
        String[] whereArgs = {orgID + ""};
        List<LmisDataRow> rows = select(where, whereArgs, null, null, null);
        if (rows != null && rows.size() > 0) {
            String fee = rows.get(0).getString("carrying_fee_gte_on_insured_fee");
            if (fee != null && !fee.equals("null"))
                ret = Integer.parseInt(fee);
        }
        return ret;
    }

    public int getConfigToShortCarryingFee(int toOrgId, int carryingFee) {
        int ret = 0;
        if (carryingFee == 0) {
            return 0;
        }
        String where = "id = ?";
        String[] whereArgs = {toOrgId + ""};
        List<LmisDataRow> rows = select(where, whereArgs, null, null, null);
        String isGenerate = rows.get(0).getString("auto_generate_to_short_carrying_fee");
        if (isGenerate.equals("true")) {
            double agtscfRate = Double.parseDouble(rows.get(0).getString("agtscf_rate"));
            double fixedToShortCarryingFee = Double.parseDouble(rows.get(0).getString("fixed_to_short_carrying_fee"));
            if (agtscfRate > 0) {
                ret = (int) (carryingFee * agtscfRate);
            } else {
                ret = (int) fixedToShortCarryingFee;
            }

        }

        return ret;

    }
}
