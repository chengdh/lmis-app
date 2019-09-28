package com.lmis.services;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by chengdh on 14-6-15.
 */

public class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapterImpl";

    /**
     * The m context.
     */
    private Context mContext;

    /**
     * Instantiates a new sync adapter impl.
     *
     * @param context the context
     */
    public SyncAdapterImpl(Context context) {
        super(context, true);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String str, ContentProviderClient providerClient, SyncResult syncResult) {
        Log.d(TAG,"onPerformSync execute");
        if (account != null) {
            ((PerformSync) mContext).performSync(mContext, account, bundle, str, providerClient, syncResult);
        }
    }
}