package com.lmis.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by chengdh on 2017/4/23.
 */

/**
 * Created by chengdh on 16/2/27.
 */
public class NetworkUtil {
    public static boolean isNetWorkAvailable(ConnectivityManager cm) {

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
