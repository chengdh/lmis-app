package com.lmis.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lmis.addons.il_config.IlConfigDB;
import com.lmis.base.org.OrgDB;
import com.lmis.base.user_org.UserOrgDB;
import com.lmis.config.ModulesConfig;
import com.lmis.support.Module;
import com.lmis.support.fragment.FragmentHelper;

import java.util.ArrayList;
import java.util.List;

public class LmisSQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = LmisSQLiteHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "LmisSQLite.db";
    public static final int DATABASE_VERSION = 2;

    //此处的mContext实际是MainActivity
    Context mContext;
    ModulesConfig mModuleConfig = new ModulesConfig();
    List<String> mDBTables = new ArrayList<String>();

    public LmisSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public List<LmisDBHelper> baseModels() {
        List<LmisDBHelper> baseModels = new ArrayList<LmisDBHelper>();
        baseModels.add(new OrgDB(mContext));
        baseModels.add(new UserOrgDB(mContext));
        baseModels.add(new IlConfigDB(mContext));
        return baseModels;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SQLHelper sqlHelper = new SQLHelper();
        for (LmisDBHelper db_helper : baseModels()) {
            List<String> sqlQueries = sqlHelper.createTable(db_helper);
            for (String query : sqlQueries) {
                db.execSQL(query);
            }
        }
        for (Module module : mModuleConfig.modules()) {
            FragmentHelper model = (FragmentHelper) module.getModuleInstance();
            LmisDBHelper model_db = (LmisDBHelper) model.databaseHelper(mContext);
            if (model_db != null) {
                List<String> sqlQueries = sqlHelper.createTable(model_db);
                for (String query : sqlQueries) {
                    db.execSQL(query);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SQLHelper sqlHelper = new SQLHelper();
        for (Module module : mModuleConfig.modules()) {
            FragmentHelper model = (FragmentHelper) module.getModuleInstance();
            LmisDBHelper model_db = (LmisDBHelper) model.databaseHelper(mContext);
            List<String> sqlQueries = sqlHelper.dropTable(model_db);
            for (String query : sqlQueries) {
                db.execSQL(query);
            }
        }
    }

    private void setDBTables() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query("sqlite_master", new String[]{"name"},
                "type = ?", new String[]{"table"}, null, null, null);
        if (cr.moveToFirst()) {
            do {
                String table = cr.getString(0);
                if (!table.equals("android_metadata")
                        && !table.equals("sqlite_sequence")) {
                    mDBTables.add(table);
                }
            } while (cr.moveToNext());
        }
        cr.close();
        db.close();
    }

    public boolean hasTable(String table_or_model) {
        if (mDBTables.size() == 0)
            setDBTables();
        String table = table_or_model;
        if (table_or_model.contains(".")) {
            table = table_or_model.replaceAll("\\.", "_");
        }
        if (mDBTables.contains(table)) {
            return true;
        }
        return false;
    }

    public boolean cleanUserRecords(String account_name) {
        Log.d(TAG, "cleanUserRecords()");
        if (mDBTables.size() == 0)
            setDBTables();
        SQLiteDatabase db = getWritableDatabase();
        for (String table : mDBTables) {
            int total = 0;
            total = db.delete(table, "oea_name = ?",
                    new String[]{account_name});
            Log.v(TAG, total + " cleaned from " + table);
        }
        db.close();
        Log.i(TAG, account_name + " records cleaned");
        return true;
    }
}
