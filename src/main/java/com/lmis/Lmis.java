package com.lmis;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;


public class Lmis {


    public static final String TAG = "lmis.Lmis";
    public static DefaultHttpClient httpclient = new DefaultHttpClient();
    private boolean debugMode;
    public String _base_url;
    public final String DESC = "DESC";
    public final String ASC = "ASC";
    private String kwargs;
    private static SharedPreferences pref;
    protected String _base_location;
    protected String _port;

    public static DefaultHttpClient getThreadSafeClient() {
        httpclient = new DefaultHttpClient();
        return httpclient;
    }

    public Lmis(SharedPreferences pref) {
        debugMode = false;
        _base_url = null;
        kwargs = null;
        _base_location = null;
        _port = null;
        _base_url = stripURL(pref.getString("base_url", null));
    }

    public Lmis(String base_url, long port)
            throws ClientProtocolException, JSONException, IOException, LmisVersionException {
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
            throws ClientProtocolException, JSONException, IOException, LmisVersionException {
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

    private synchronized JSONObject callHTTP(String RequestURL, String jsonString)
            throws IOException, JSONException {
        debugMode(true);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            android.os.StrictMode.ThreadPolicy policy = (new android.os.StrictMode.ThreadPolicy.Builder()).permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        HttpPost httppost = new HttpPost(RequestURL);
        StringEntity se = new StringEntity(jsonString);
        JSONObject obj = null;
        httppost.setEntity(se);
        httppost.setHeader("Content-type", "application/json");
        HttpResponse httpresponse = null;
        httpresponse = httpclient.execute(httppost);
        if (httpresponse != null) {
            String a = "";
            try {
                InputStream in = httpresponse.getEntity().getContent();
                a = convertStreamToString(in);
                obj = new JSONObject(a);
                in.close();
                httpresponse.getEntity().consumeContent();
            } catch (JSONException jexe) {
                return null;
            }
        }
        if (debugMode) {
            Log.i("LMIS_POST_URL", RequestURL);
            Log.i("LMIS_POST", jsonString);
            Log.i("LMIS_RESPONSE", obj.toString());
        }
        return obj;
    }

    public JSONObject testConnection() throws JSONException, IOException {

        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/api/v1/tokens/test_connect.json").toString();
        JSONObject obj;
        JSONObject params = new JSONObject();
        String jsonString = generate_json_request(params);
        obj = callHTTP(req_url, jsonString);
        return obj;
    }


    public JSONObject authenticate(String Username, String password)
            throws JSONException, IOException {
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/api/v1/tokens.json").toString();
        JSONObject obj = null ;
        JSONObject params = new JSONObject();
        params.put("username", Username);
        params.put("password", password);
        String jsonString = generate_json_request(params);
        obj = callHTTP(req_url, jsonString);
        return obj.getJSONObject("result");
    }

    public JSONObject search_count(String model, JSONArray args) throws JSONException, IOException {
        return call_kw(model, "search_count", args);
    }

    public JSONObject search_read(String model, JSONObject fieldsAccumulates, JSONObject domainAccumulates, int offset, int limit, String sortField, String sortType)
            throws JSONException,  IOException {
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/web/dataset/search_read").toString();
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
        String jsonString = generate_json_request(params);
        obj = callHTTP(req_url, jsonString);
        return obj.getJSONObject("result");
    }

    public JSONObject search_read(String model, JSONObject fieldsAccumulates, JSONObject domainAccumulates)
            throws JSONException, ClientProtocolException, IOException {
        return search_read(model, fieldsAccumulates, domainAccumulates, 0, 0, null, null);
    }

    public JSONObject search_read(String model, JSONObject fieldsAccumulates)
            throws JSONException, ClientProtocolException, IOException {
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
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/web/dataset/call_kw/").append(modelName).append(":").append(methodName).toString();
        JSONObject params = new JSONObject();
        params = createWriteParams(modelName, methodName, args, id);
        String jsonString = generate_json_request(params);
        JSONObject response = callHTTP(req_url, jsonString);
        return response;
    }

    public JSONObject createNew(String modelName, JSONObject arguments)
            throws JSONException, ClientProtocolException, IOException {
        return callMethod(modelName, "create", arguments, null);
    }

    public boolean updateValues(String modelName, JSONObject arguments, Integer id)
            throws ClientProtocolException, JSONException, IOException {
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

    private String generate_json_request(JSONObject params)
            throws JSONException {
        //JSONObject postObj = new JSONObject();
        //postObj.put("jsonrpc", "2.0");
        //postObj.put("method", "call");
        //postObj.put("params", params);
        return params.toString();
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
            throws JSONException, ClientProtocolException, IOException {
        JSONObject response = null;
        String req_url = (new StringBuilder(String.valueOf(_base_url))).append("/web/dataset/call_kw").toString();
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
        String jsonString = generate_json_request(params);
        response = callHTTP(req_url, jsonString);
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

