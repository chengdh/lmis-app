/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.lmis.support;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.lmis.Lmis;
import com.lmis.LmisVersionException;
import com.lmis.auth.LmisAccountManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * The Class LmisServerConnection.
 */
public class LmisServerConnection {

    public static final String TAG = "LmisServerConnection";
    /**
     * The openerp.
     */
    public Lmis openerp = null;

    /**
     * Test connection.
     *
     * @param context   the context
     * @param serverURL the server url
     * @return true, if successful
     * @throws com.lmis.LmisVersionException
     */
    public boolean testConnection(Context context, String serverURL)
            throws LmisVersionException {
        Log.d(TAG, "LmisServerConnection->testConnection()");
        if (TextUtils.isEmpty(serverURL)) {
            return false;
        }
        try {
            openerp = new Lmis(serverURL);
            openerp.getDatabaseList();
        } catch (LmisVersionException version) {
            throw new LmisVersionException(version.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Gets the databases.
     *
     * @param context   the context
     * @param serverURL the server url
     * @return the databases
     * @throws com.lmis.LmisVersionException
     */
    public JSONArray getDatabases(Context context, String serverURL)
            throws LmisVersionException {
        JSONArray dbList = null;
        if (this.testConnection(context, serverURL)) {
            try {
                dbList = openerp.getDatabaseList();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return dbList;
    }

    /**
     * Checks if is network available.
     *
     * @param context the context
     * @return true, if is network available
     * @throws com.lmis.LmisVersionException
     */
    public static boolean isNetworkAvailable(Context context)
            throws LmisVersionException {
        boolean outcome = false;

        LmisServerConnection osc = new LmisServerConnection();
        outcome = osc.testConnection(context, LmisAccountManager
                .currentUser(context).getHost());

        return outcome;
    }

    /**
     * Checks if is network available.
     *
     * @param context the context
     * @param url     the url
     * @return true, if is network available
     * @throws com.lmis.LmisVersionException
     */
    public static boolean isNetworkAvailable(Context context, String url)
            throws LmisVersionException {
        boolean outcome = false;

        LmisServerConnection osc = new LmisServerConnection();
        outcome = osc.testConnection(context, url);

        return outcome;
    }
}
