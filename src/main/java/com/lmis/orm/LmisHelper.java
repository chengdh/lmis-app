/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.lmis.orm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.lmis.Lmis;
import com.lmis.LmisArguments;
import com.lmis.LmisDomain;
import com.lmis.LmisVersionException;
import com.lmis.base.ir.Ir_model;
import com.lmis.orm.LmisFieldsHelper.OERelationData;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisDate;
import com.lmis.util.PreferenceManager;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class LmisHelper extends Lmis {
    public static final String TAG = "LmisHelper";
    Context mContext = null;
    LmisDatabase mDatabase = null;
    LmisUser mUser = null;
    PreferenceManager mPref = null;
    int mAffectedRows = 0;
    List<Long> mResultIds = new ArrayList<Long>();
    List<LmisDataRow> mRemovedRecordss = new ArrayList<LmisDataRow>();

    @Override
    protected String getAuthToken() {
        if (mUser == null)
            return null;

        return mUser.getAuthentication_token();
    }

    public LmisHelper(SharedPreferences pref) {
        super(pref);
        init();
    }

    public LmisHelper(Context context, String host)
            throws ClientProtocolException, JSONException, IOException,
            LmisVersionException {
        super(host);
        mContext = context;
        init();
    }

    public LmisHelper(Context context, LmisUser data, LmisDatabase lmisDatabase)
            throws JSONException, IOException,
            LmisVersionException {
        super(data.getHost());
        Log.d(TAG, "LmisHelper->LmisHelper(Context, LmisUser, LmisDatabase)");
        Log.d(TAG, "Called from LmisDatabase->getLmisInstance()");
        mContext = context;
        mDatabase = lmisDatabase;
        mUser = data;
        init();
        /*
         * Required to login with server.
		 */
        login(mUser.getUsername(), mUser.getPassword(),
                mUser.getHost());
    }

    private void init() {
        Log.d(TAG, "LmisHelper->init()");
        mPref = new PreferenceManager(mContext);
    }

    public LmisUser login(String username, String password, String serverURL) {
        LmisUser userObj = null;
        try {
            JSONObject rs = this.authenticate(username, password);
            int userId = 0;
            if (rs.get("id") instanceof Integer) {
                userId = rs.getInt("id");

                userObj = new LmisUser();
                userObj.setHost(serverURL);
                userObj.setIsactive(true);
                userObj.setAndroidName(rs.getString("real_name"));
                userObj.setReal_name(rs.getString("real_name"));
                userObj.setUser_id(userId);
                userObj.setUsername(username);
                userObj.setPassword(password);
                userObj.setDefault_org_id(rs.getInt("default_org_id"));
                userObj.setAuthentication_token(rs.getString("authentication_token"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userObj;
    }

    public LmisUser getUser() {
        return mUser;
    }

    private String androidName(String username, String database) {
        StringBuffer android_name = new StringBuffer();
        android_name.append(username);
        android_name.append("[");
        android_name.append(database);
        android_name.append("]");
        return android_name.toString();
    }

    public boolean syncWithServer() {
        return syncWithServer(false, null, null, false, -1, false);
    }

    public boolean syncWithServer(boolean removeLocalIfNotExists) {
        return syncWithServer(false, null, null, false, -1,
                removeLocalIfNotExists);
    }

    public boolean syncWithServer(LmisDomain domain,
                                  boolean removeLocalIfNotExists) {
        return syncWithServer(false, domain, null, false, -1,
                removeLocalIfNotExists);
    }

    public boolean syncWithServer(LmisDomain domain) {
        return syncWithServer(false, domain, null, false, -1, false);
    }

    public boolean syncWithServer(boolean twoWay, LmisDomain domain,
                                  List<Object> ids) {
        return syncWithServer(twoWay, domain, ids, false, -1, false);
    }

    public int getAffectedRows() {
        return mAffectedRows;
    }

    public List<LmisDataRow> getRemovedRecords() {
        return mRemovedRecordss;
    }

    public List<Integer> getAffectedIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (Long id : mResultIds) {
            ids.add(Integer.parseInt(id.toString()));
        }
        return ids;
    }

    public boolean syncWithMethod(String method, LmisArguments args) {
        return syncWithMethod(method, args, false);
    }

    public boolean syncWithMethod(String method, LmisArguments args,
                                  boolean removeLocalIfNotExists) {
        Log.d(TAG, "LmisHelper->syncWithMethod()");
        Log.d(TAG, "Model: " + mDatabase.getModelName());
        Log.d(TAG, "User: " + mUser.getAndroidName());
        Log.d(TAG, "Method: " + method);
        boolean synced = false;
        LmisFieldsHelper fields = new LmisFieldsHelper(
                mDatabase.getDatabaseColumns());
        try {
            JSONObject result = callMethod(mDatabase.getModelName(), method, args.getArray(), null);

            Log.d(TAG, "result: " + result);
            if (result.getJSONArray("result").length() > 0)
                mAffectedRows = result.getJSONArray("result").length();

            synced = handleResultArray(fields, result.getJSONArray("result"), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return synced;
    }

    public boolean syncWithServer(boolean twoWay, LmisDomain domain,
                                  List<Object> ids, boolean limitedData, int limits,
                                  boolean removeLocalIfNotExists) {
        boolean synced = false;
        Log.d(TAG, "LmisHelper->syncWithServer()");
        Log.d(TAG, "Model: " + mDatabase.getModelName());
        Log.d(TAG, "User: " + mUser.getAndroidName());
        LmisFieldsHelper fields = new LmisFieldsHelper(mDatabase.getDatabaseColumns());
        try {
            if (domain == null) {
                domain = new LmisDomain();
            }
            if (ids != null) {
                domain.add("id_in", ids);
            }
            if (limitedData) {
                int data_limit = mPref.getInt("sync_data_limit", 60);
                domain.add("created_at_gte", LmisDate.getDateBefore(data_limit));
            }

            if (limits == -1) {
                limits = 50;
            }
            JSONArray result = search_read(mDatabase.getModelName(), fields.get(), domain.get(), 0, limits, null, null);
            mAffectedRows = result.length();
            synced = handleResultArray(fields, result, removeLocalIfNotExists);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, mDatabase.getModelName() + " synced");
        return synced;
    }

    private boolean handleResultArray(LmisFieldsHelper fields, JSONArray results,
                                      boolean removeLocalIfNotExists) {
        boolean flag = false;
        try {
            fields.addAll(results);
            // Handling many2many and many2one records
            // 通过id从服务器端获取many2many many2one one2many表的相关值
            List<OERelationData> rel_models = fields.getRelationData();
            for (OERelationData rel : rel_models) {
                LmisHelper oe = rel.getDb().getLmisInstance();
                oe.syncWithServer(false, null, rel.getIds(), false, 0, false);
            }
            List<Long> result_ids = mDatabase.createORReplace(fields.getValues(), removeLocalIfNotExists);
            mResultIds.addAll(result_ids);
            mRemovedRecordss.addAll(mDatabase.getRemovedRecords());
            if (result_ids.size() > 0) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


    public List<LmisDataRow> search_read_remain() {
        Log.d(TAG, "LmisHelper->search_read_remain()");
        return search_read(true);
    }

    private LmisDomain getLocalIdsDomain(String operator) throws JSONException {
        LmisDomain domain = new LmisDomain();
        JSONArray ids = new JSONArray();
        for (LmisDataRow row : mDatabase.select()) {
            ids.put(row.getInt("id"));
        }
        domain.add("id_" + operator, ids);
        return domain;
    }

    private List<LmisDataRow> search_read(boolean getRemain) {
        List<LmisDataRow> rows = new ArrayList<LmisDataRow>();
        try {
            LmisFieldsHelper fields = new LmisFieldsHelper(
                    mDatabase.getDatabaseServerColumns());
            JSONObject domain = null;
            if (getRemain)
                domain = getLocalIdsDomain("not in").get();
            JSONArray result = search_read(mDatabase.getModelName(), fields.get(), domain, 0, 100, null, null);
            for (int i = 0; i < result.length(); i++) {
                JSONObject record = result.getJSONObject(i);
                LmisDataRow row = new LmisDataRow();
                row.put("id", record.getInt("id"));
                for (LmisColumn col : mDatabase.getDatabaseServerColumns()) {
                    row.put(col.getName(), record.get(col.getName()));
                }
                rows.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<LmisDataRow> search_read() {
        Log.d(TAG, "LmisHelper->search_read()");
        return search_read(false);
    }

    public void delete(int id) {
        Log.d(TAG, "LmisHelper->delete()");
        try {
            destroy(mDatabase.getModelName(), id);
            mDatabase.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object call_kw(String method, LmisArguments arguments) {
        return call_kw(method, arguments, new JSONObject());
    }

    public Object call_kw(String method, LmisArguments arguments,
                          JSONObject context) {
        return call_kw(null, method, arguments, context);
    }

    public Object call_kw(String model, String method, LmisArguments arguments, JSONObject context) {
        Log.d(TAG, "LmisHelper->call_kw()");
        JSONObject result = null;
        if (model == null) {
            model = mDatabase.getModelName();
        }
        try {
            result = callMethod(model, method, arguments.getArray(), null);
            return result.get("result");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer create(LmisValues values) {
        Log.d(TAG, "LmisHelper->create()");
        Integer newId = null;
        try {
            JSONObject result = createNew(mDatabase.getModelName(), generateArguments(values));
            newId = result.getInt("result");
            values.put("id", newId);
            mDatabase.create(values);
            return newId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newId;
    }

    public Boolean update(LmisValues values, Integer id) {
        Log.d(TAG, "LmisHelper->update()");
        Boolean flag = false;
        try {
            flag = updateValues(mDatabase.getModelName(),
                    generateArguments(values), id);
            if (flag)
                mDatabase.update(values, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    private JSONArray generateArguments(LmisValues values) {
        Log.d(TAG, "LmisHelper->generateArguments()");
        JSONArray ret = null;
        JSONObject arguments = new JSONObject();
        try {
            for (String key : values.keys()) {
                if (values.get(key) instanceof LmisM2MIds) {
                    LmisM2MIds m2mIds = (LmisM2MIds) values.get(key);
                    JSONArray m2mArray = new JSONArray();
                    m2mArray.put(6);
                    m2mArray.put(false);
                    m2mArray.put(m2mIds.getJSONIds());
                    arguments.put(key, new JSONArray("[" + m2mArray.toString()
                            + "]"));
                } else {
                    arguments.put(key, values.get(key));
                }
            }
            ret.put(arguments);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


}
