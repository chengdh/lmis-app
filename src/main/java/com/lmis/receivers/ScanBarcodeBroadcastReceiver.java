package com.lmis.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ScanBarcodeBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ScanBarcodeBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "in ScanBarcodeBroadcastReceiver receive ");
    }
}
