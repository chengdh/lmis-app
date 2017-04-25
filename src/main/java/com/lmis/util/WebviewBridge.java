package com.lmis.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.JavascriptInterface;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 与webview绑定的javascript
 * Created by chengdh on 15/12/8.
 */
public class WebviewBridge {
    HashMap<String, JSONObject> mObjectsFromJS = new HashMap<String, JSONObject>();
    HashMap<String, JSONArray> mArrayFromJS = new HashMap<String, JSONArray>();
    HashMap<String, String> mStringFromJS = new HashMap<String, String>();
    Context mContext;

    public WebviewBridge(Context c) {
        mContext = c;

    }

    @JavascriptInterface
    public void passObject(String name, String json) {
        try {
            mObjectsFromJS.put(name, new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void passArray(String name, String jsonArray) {
        try {
            mArrayFromJS.put(name, new JSONArray(jsonArray));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void passString(String name, String str) {
        mStringFromJS.put(name, str);
    }


    public JSONObject getJSONObject(String name) {
        JSONObject obj = mObjectsFromJS.get(name);
        return obj;
    }

    public JSONArray getJSONArray(String name) {
        JSONArray obj = mArrayFromJS.get(name);
        return obj;
    }

    public String getString(String name) {
        String obj = mStringFromJS.get(name);
        return obj;
    }


    /**
     * 获取登录用户id,如用户未登录则返回-1
     *
     * @return the int
     */
    @JavascriptInterface
    public int getUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getInt("user_id", -1);

    }

    /**
     * 判断登录用户是否admin.
     *
     * @return the user is admin
     */
    @JavascriptInterface
    public boolean getUserIsAdmin() {
        if (getUserId() == -1)
            return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isAdmin = prefs.getBoolean("user_is_admin", false);
        return isAdmin;
    }

    /**
     * 获取sharedPreferences中的键值.
     *
     * @param key the key
     * @return the pref string
     */
    @JavascriptInterface
    public String getPrefString(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String val = prefs.getString(key, "2016-02-01 00:00");
        return val;
    }

    /**
     * 获取sharedPreferences中的键值.
     *
     * @param key the key
     * @return the pref string
     */
    @JavascriptInterface
    public Boolean putPrefString(String key, String val) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, val);
        return editor.commit();
    }
}
