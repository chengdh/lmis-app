package com.lmis;

import java.util.Iterator;
import java.util.List;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class LmisDomain {

    public LmisDomain() {
        mDomain = new JSONObject();
    }

    public void add(String key, Object value) throws JSONException {
        if (value instanceof List)
            mDomain.put(key,listToArray(value));
        else
            mDomain.put(key,value);
    }

    public JSONArray listToArray(Object ids) {
        JSONArray jIds = new JSONArray();
        List object = (List) ids;
        try {
            Object obj;
            for (Iterator iterator = object.iterator(); iterator.hasNext(); jIds.put(obj))
                obj = iterator.next();

        } catch (Exception exception) {
        }
        return jIds;
    }

    public JSONObject get() {
        return mDomain;
    }

    JSONObject mDomain;
}
