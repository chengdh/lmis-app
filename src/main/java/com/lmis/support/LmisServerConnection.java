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

/**
 * The Class LmisServerConnection.
 */
public class LmisServerConnection {

    public static final String TAG = "LmisServerConnection";
    /**
     * The openerp.
     */
    public Lmis lmis = null;

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
            lmis = new Lmis(serverURL);
            lmis.testConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
