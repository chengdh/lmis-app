package com.lmis.orm;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

public class LmisFieldsHelper {
    public static final String TAG = "LmisFieldsHelper";
    JSONObject mFields = new JSONObject();
    List<LmisValues> mValues = new ArrayList<LmisValues>();
    List<LmisColumn> mColumns = new ArrayList<LmisColumn>();
    OERelRecord mRelRecord = new OERelRecord();
    ValueWatcher mValueWatcher = null;

    public LmisFieldsHelper(String[] fields) {
        addAll(fields);
    }

    public LmisFieldsHelper(List<LmisColumn> cols) {
        addAll(cols);
        mColumns.addAll(cols);
        mColumns.add(new LmisColumn("id", "id", LmisFields.integer()));
    }

    public LmisFieldsHelper(JSONArray records) {
        addAll(records);
    }

    public void addAll(JSONArray records) {
        try {
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                LmisValues cValue = new LmisValues();
                for (LmisColumn col : mColumns) {
                    if (col.canSync()) {
                        String key = col.getName();
                        Object colValue = false;
                        if (record.has(key)) {
                            colValue  = record.get(key);
                        }
                        if (col.getmValueWatcher() != null) {
                            LmisValues values = col.getmValueWatcher().getValue(col, colValue);
                            cValue.setAll(values);
                        }
                        if (col.getType() instanceof LmisManyToOne) {
                            if (colValue instanceof JSONArray) {
                                JSONArray m2oRec = new JSONArray(colValue.toString());
                                colValue = m2oRec.get(0);
                                if ((Integer) colValue != 0) {
                                    LmisManyToOne m2o = (LmisManyToOne) col.getType();
                                    LmisDatabase db = (LmisDatabase) m2o.getDBHelper();
                                    mRelRecord.add(db, colValue);
                                } else {
                                    colValue = false;
                                }
                            }
                        } else if (col.getType() instanceof LmisManyToMany) {
                            if (colValue instanceof JSONArray) {
                                JSONArray m2mRec = new JSONArray(colValue.toString());
                                List<Integer> ids = getIdsList(m2mRec);
                                LmisM2MIds mIds = new LmisM2MIds(LmisM2MIds.Operation.REPLACE, ids);
                                colValue = mIds;
                                LmisManyToMany m2m = (LmisManyToMany) col.getType();
                                LmisDatabase db = (LmisDatabase) m2m.getDBHelper();
                                mRelRecord.add(db, ids);
                            }
                        }
                        //处理one to many字段
                        else if (col.getType() instanceof LmisOneToMany) {
                            if (colValue instanceof JSONArray) {
                                JSONArray o2mRec = new JSONArray(colValue.toString());
                                List<Integer> ids = getIdsList(o2mRec);
                                LmisO2MIds mIds = new LmisO2MIds(LmisO2MIds.Operation.REPLACE, ids);
                                colValue = mIds;
                                LmisOneToMany o2m = (LmisOneToMany) col.getType();
                                LmisDatabase db = (LmisDatabase) o2m.getDBHelper();
                                mRelRecord.add(db, ids);
                            }
                            continue;
                        }
                        cValue.put(key, colValue);
                    }
                }
                mValues.add(cValue);
            }
        } catch (Exception e) {
            Log.d(TAG, "LmisFieldsHelper->addAll(JSONArray records)");
            e.printStackTrace();
        }
    }

    private List<Integer> getIdsList(JSONArray array) {
        Log.d(TAG, "LmisFieldsHelper->getIdsList()");
        List<Integer> ids = new ArrayList<Integer>();
        try {
            int length = array.length();
            if (length > 50) {
                Log.i(TAG,"Many2Many or One2Many records more than 50... - Limiting to 50 records only");
                length = 50;
            }
            for (int i = 0; i < length; i++) {
                if (array.get(i) instanceof JSONArray)
                    ids.add(array.getJSONArray(i).getInt(0));
                else if (array.get(i) instanceof JSONObject) {
                    JSONObject rec = (JSONObject) array.get(i);
                    if (rec.has("id"))
                        ids.add(rec.getInt("id"));
                } else
                    ids.add(array.getInt(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    public void addAll(String[] fields) {
        try {
            for (int i = 0; i < fields.length; i++) {
                mFields.accumulate("fields", fields[i]);
            }
            if (fields.length == 1) {
                mFields.accumulate("fields", fields[0]);
            }
        } catch (Exception e) {
        }
    }

    public void addAll(List<LmisColumn> cols) {
        try {
            for (LmisColumn col : cols) {
                if (col.canSync()) {
                    mFields.accumulate("fields", col.getName());
                }
            }
            if (cols.size() == 1) {
                mFields.accumulate("fields", cols.get(0));
            }
        } catch (Exception e) {
        }
    }

    public void addManyToOneId(Object model, int id) {

    }

    public JSONObject get() {
        return mFields;
    }

    public List<LmisValues> getValues() {
        return mValues;
    }

    public List<OERelationData> getRelationData() {
        return mRelRecord.getAll();
    }

    class OERelRecord {
        private HashMap<String, Object> _models = new HashMap<String, Object>();
        private HashMap<String, List<Object>> _model_ids = new HashMap<String, List<Object>>();

        @SuppressWarnings("unchecked")
        public void add(LmisDatabase db, Object ids) {
            if (!_models.containsKey(db.getModelName())) {
                _models.put(db.getModelName(), db);
            }
            List<Object> _ids = new ArrayList<Object>();
            if (ids instanceof List) {
                _ids = (List<Object>) ids;
            }
            if (ids instanceof Integer) {
                _ids.add(ids);
            }
            if (_model_ids.containsKey(db.getModelName())) {
                if (!_model_ids.containsValue(_ids))
                    _model_ids.get(db.getModelName()).addAll(_ids);
            } else {
                _model_ids.put(db.getModelName(), _ids);
            }
        }

        public List<OERelationData> getAll() {
            List<OERelationData> datas = new ArrayList<LmisFieldsHelper.OERelationData>();
            Set<String> keys = _models.keySet();
            for (String key : keys) {
                LmisDatabase db = (LmisDatabase) _models.get(key);
                datas.add(new OERelationData(db, _model_ids.get(key)));
            }
            return datas;
        }
    }

    public class OERelationData {
        LmisDatabase db;
        List<Object> ids;

        public OERelationData(LmisDatabase db, List<Object> ids) {
            super();
            this.db = db;
            this.ids = ids;
        }

        public LmisDatabase getDb() {
            return db;
        }

        public void setDb(LmisDatabase db) {
            this.db = db;
        }

        public List<Object> getIds() {
            return ids;
        }

        public void setIds(List<Object> ids) {
            this.ids = ids;
        }

    }

    public interface ValueWatcher {
        public LmisValues getValue(LmisColumn col, Object value);
    }
}
