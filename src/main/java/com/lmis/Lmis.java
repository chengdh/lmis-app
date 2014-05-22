package com.lmis;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import static us.monoid.web.Resty.content;

public class Lmis {

    public static final String TAG = "lmis.Lmis";
    private boolean debugMode;
    public String _base_url;
    public final String DESC = "DESC";
    public final String ASC = "ASC";
    private String kwargs;
    private static SharedPreferences pref;
    protected String _base_location;
    protected String _port;

    public Lmis(SharedPreferences pref) {
        debugMode = false;
        _base_url = null;
        kwargs = null;
        _base_location = null;
        _port = null;
        _base_url = stripURL(pref.getString("base_url", null));
    }

    public Lmis(String base_url, long port)
            throws JSONException, IOException, LmisVersionException {
        debugMode = false;
        _base_url = null;
        kwargs = null;
        _base_location = null;
        _port = null;
        _port = String.valueOf(port);
        _base_url = (new StringBuilder(String.valueOf(stripURL(base_url)))).append(":").append(_port).toString();
    }

    public Lmis(String base_url)
            throws ClientProtocolException, JSONException, IOException, LmisVersionException {
        debugMode = false;
        _base_url = null;
        kwargs = null;
        _base_location = null;
        _port = null;
        _base_url = stripURL(base_url);
    }

    public Lmis(String base_url, boolean isnetwork)
            throws JSONException, IOException, LmisVersionException {
        debugMode = false;
        _base_url = null;
        kwargs = null;
        _base_location = null;
        _port = null;
        _base_url = stripURL(base_url);
    }

    public void debugMode(boolean on) {
        debugMode = on;
    }


    private synchronized JSONObject callHTTP(String RequestURL, JSONObject params)
            throws IOException, JSONException {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            android.os.StrictMode.ThreadPolicy policy = (new android.os.StrictMode.ThreadPolicy.Builder()).permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (params == null)
            params = new JSONObject();

        JSONObject ret = new Resty().json(RequestURL, content(params)).object();
        if (debugMode) {
            Log.i("LMIS_POST_URL", RequestURL);
            Log.i("LMIS_POST", params.toString());
            Log.i("LMIS_RESPONSE", ret.toString());
        }
        return ret;
    }

    public JSONObject testConnection() throws JSONException, IOException {

        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/api/v1/tokens/test_connect.json").toString();
        return callHTTP(req_url, null);
    }


    public JSONObject authenticate(String Username, String password)
            throws JSONException, IOException {
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/api/v1/tokens.json").toString();
        JSONObject obj = null;
        JSONObject params = new JSONObject();
        params.put("username", Username);
        params.put("password", password);
        obj = callHTTP(req_url, params);
        return obj.getJSONObject("result");
    }

    public JSONObject search_count(String model, JSONArray args) throws JSONException, IOException {
        return call_kw(model, "search_count", args);
    }

    public JSONObject search_read(String model, JSONObject fieldsAccumulates, JSONObject domainAccumulates, int offset, int limit, String sortField, String sortType)
            throws JSONException, IOException {
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/api/v1/dataset/search_read").toString();
        JSONObject obj = null;
        JSONObject params = new JSONObject();
        params.put("model", model);
        if (fieldsAccumulates != null) {
            fieldsAccumulates.accumulate("fields", "id");
            params.put("fields", fieldsAccumulates.get("fields"));
        } else {
            params.put("fields", new JSONArray());
        }
        if (domainAccumulates != null)
            params.put("domain", domainAccumulates.get("domain"));
        else
            params.put("domain", new JSONArray());
        params.put("offset", offset);
        params.put("limit", limit);
        if (sortField != null && sortType != null)
            params.put("sort", (new StringBuilder(String.valueOf(sortField))).append(" ").append(sortType).toString());
        else
            params.put("sort", "");

        obj = callHTTP(req_url, params);
        return obj.getJSONObject("result");
    }

    public JSONObject search_read(String model, JSONObject fieldsAccumulates, JSONObject domainAccumulates)
            throws JSONException, IOException {
        return search_read(model, fieldsAccumulates, domainAccumulates, 0, 0, null, null);
    }

    public JSONObject search_read(String model, JSONObject fieldsAccumulates)
            throws JSONException, IOException {
        return search_read(model, fieldsAccumulates, null, 0, 0, null, null);
    }

    private JSONObject createWriteParams(String modelName, String methodName, JSONObject args, Integer id)
            throws JSONException {
        JSONObject params = new JSONObject();
        params.put("model", modelName);
        params.put("method", methodName);
        JSONArray _args = null;
        if (id != null && args != null)
            _args = new JSONArray((new StringBuilder("[[")).append(String.valueOf(id)).append("], ").append(args.toString()).append("]").toString());
        else if (id == null && args != null)
            _args = new JSONArray((new StringBuilder("[")).append(args.toString()).append("]").toString());
        else if (id != null && args == null)
            _args = new JSONArray((new StringBuilder("[[")).append(String.valueOf(id)).append("]]").toString());
        params.put("args", _args);
        JSONObject kwargs = new JSONObject();
        params.put("kwargs", kwargs);
        return params;
    }

    public JSONObject callMethod(String modelName, String methodName, JSONObject args, Integer id)
            throws JSONException, IOException {
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/api/v1/dataset/call_kw/").toString();
        JSONObject params = new JSONObject();
        params = createWriteParams(modelName, methodName, args, id);
        JSONObject response = callHTTP(req_url, params);
        return response;
    }

    public JSONObject createNew(String modelName, JSONObject arguments)
            throws JSONException, IOException {
        return callMethod(modelName, "create", arguments, null);
    }

    public boolean updateValues(String modelName, JSONObject arguments, Integer id)
            throws JSONException, IOException {
        JSONObject response = null;
        response = callMethod(modelName, "write", arguments, id);
        return response.getBoolean("result");
    }

    public boolean unlink(String modelName, Integer id)
            throws JSONException, IOException {
        JSONObject response = callMethod(modelName, "unlink", null, id);
        return response.getBoolean("result");
    }

    public File getImage(Context context, String model, String field, int id)
            throws IOException {
        JSONObject fields = new JSONObject();
        try {
            fields.accumulate("fields", field);
            String imagestring = null;
            JSONArray domainarr = new JSONArray((new StringBuilder("[[\"id\",\"=\",")).append(id).append("]]").toString());
            JSONObject domain = new JSONObject();
            domain.accumulate("domain", domainarr);
            JSONObject res = search_read(model, fields, domain, 0, 1, null, null);
            JSONArray result = res.getJSONArray("records");
            if (result.getJSONObject(0).getString("image") != "false") {
                imagestring = result.getJSONObject(0).getString(field);
                byte imageAsBytes[] = Base64.decode(imagestring.getBytes(), 5);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                File imgFile = new File(context.getCacheDir(), (new StringBuilder("img")).append(String.valueOf(id)).toString());
                imgFile.createNewFile();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 0, bos);
                byte bitmapdata[] = bos.toByteArray();
                FileOutputStream fos = new FileOutputStream(imgFile);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
                return imgFile;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public Bitmap getBitmapImage(Context context, String model, String field, int id)
            throws IOException {
        JSONObject fields = new JSONObject();
        try {
            fields.accumulate("fields", field);
            String imagestring = null;
            JSONArray domainarr = new JSONArray((new StringBuilder("[[\"id\",\"=\",")).append(id).append("]]").toString());
            JSONObject domain = new JSONObject();
            domain.accumulate("domain", domainarr);
            JSONObject res = search_read(model, fields, domain, 0, 1, null, null);
            JSONArray result = res.getJSONArray("records");
            if (result.getJSONObject(0).getString("image") != "false") {
                imagestring = result.getJSONObject(0).getString(field);
                byte imageAsBytes[] = Base64.decode(imagestring.getBytes(), 5);
                return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = (new Scanner(is)).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static boolean putSessionData(Context context, String key, String value) {
        pref = context.getApplicationContext().getSharedPreferences("OpenERP_Preferences", 1);
        android.content.SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
        return true;
    }

    public JSONObject call_kw(String modelName, String methodName, JSONArray args)
            throws JSONException, IOException {
        JSONObject response = null;
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/api/v1/dataset/call_kw").toString();
        JSONObject params = new JSONObject();
        params.put("model", modelName);
        params.put("method", methodName);
        params.put("args", args);
        JSONObject kwargs = null;
        if (this.kwargs != null)
            kwargs = new JSONObject(this.kwargs);
        else
            kwargs = new JSONObject();
        params.put("kwargs", kwargs);
        response = callHTTP(req_url, params);
        return response;
    }

    public boolean updateKWargs(JSONObject newValues) throws JSONException {
        JSONObject kwargs = null;
        if (newValues != null) {
            if (this.kwargs != null)
                kwargs = new JSONObject(this.kwargs);
            else
                kwargs = new JSONObject();
            String key;
            for (Iterator iter = newValues.keys(); iter.hasNext(); kwargs.put(key, newValues.get(key)))
                key = (String) iter.next();

            this.kwargs = kwargs.toString();
        } else {
            this.kwargs = (new JSONObject()).toString();
        }
        return true;
    }

    public static String getSessionData(Context context, String key) {
        pref = context.getApplicationContext().getSharedPreferences("OpenERP_Preferences", 1);
        return pref.getString(key, null);
    }

    public static boolean generateSessions(Context context, Lmis openerp, JSONObject response)
            throws JSONException {
        putSessionData(context, "username", response.getString("username"));
        putSessionData(context, "user_context", response.getJSONObject("user_context").toString());
        putSessionData(context, "db", response.getString("db"));
        putSessionData(context, "uid", response.getString("uid"));
        putSessionData(context, "session_id", response.getString("session_id"));
        putSessionData(context, "base_url", openerp.getServerURL());
        return true;
    }

    public String getServerURL() {
        return _base_url;
    }

    public static SharedPreferences getSessions(Context context) {
        return context.getApplicationContext().getSharedPreferences("OpenERP_Preferences", 1);
    }

    private String cleanServerURL(String url) {
        StringBuffer newURL = new StringBuffer();
        if (url.charAt(url.length() - 1) == '\\') {
            String subStr = url.substring(0, url.length() - 1);
            newURL.append(subStr);
        } else {
            newURL.append(url);
        }
        return newURL.toString();
    }


    public String stripURL(String url) {
        if (url.endsWith("/"))
            return url.substring(0, url.lastIndexOf("/"));
        else
            return url;
    }
}

