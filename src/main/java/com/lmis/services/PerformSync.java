package com.lmis.services;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by chengdh on 14-6-15.
 */
public interface PerformSync {
      public void performSync(Context context, Account account, Bundle extras,
                            String authority, ContentProviderClient provider,
                            SyncResult syncResult);

}
