package com.lmis.services;

import android.accounts.Account;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.fizzbuzz.android.dagger.InjectingService;
import com.lmis.LmisArguments;
import com.lmis.MainActivity;
import com.lmis.R;
import com.lmis.addons.message.MessageDB;
import com.lmis.addons.wf_notification.WfNotificationDB;
import com.lmis.auth.LmisAccountManager;
import com.lmis.dagger_module.ServiceModule;
import com.lmis.orm.LmisHelper;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisNotificationHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by chengdh on 2017/2/17.
 */

public class WfNotificationSyncService extends InjectingService implements PerformSync {

    public final String TAG = "WfNotificationSyncService";


    @Inject
    @InjectingServiceModule.Service
    SyncAdapterImpl mSyncAdapter;

    @Override
    public void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "WfNotificationSyncService->performSync()");
        Intent intent = new Intent();
        intent.setAction(SyncFinishReceiver.SYNC_FINISH);
        LmisUser user = LmisAccountManager.getAccountDetail(context, account.name);
        try {
            WfNotificationDB notificationDB = new WfNotificationDB(context);
            notificationDB.setAccountUser(user);
            LmisHelper lmis = notificationDB.getLmisInstance();
            if (lmis == null) {
                return;
            }

            LmisArguments arguments = new LmisArguments();
            arguments.add(user.getUser_id());

            //数据库中原有的数据也需要更新
            List<Integer> ids = notificationDB.ids();
            if (lmis.syncWithMethod("unread", arguments)) {
                int affected_rows = lmis.getAffectedRows();
                Log.d(TAG, "WfNotifySyncService[arguments]:" + arguments.toString());
                Log.d(TAG, "WfNotifySyncService->affected_rows:" + affected_rows);
                List<Integer> affected_ids = lmis.getAffectedIds();
                boolean notification = true;
                ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo.getPackageName().equalsIgnoreCase("com.lmis")) {
                    notification = false;
                }
                if (notification && affected_rows > 0) {
                    LmisNotificationHelper mNotification = new LmisNotificationHelper();
                    Intent mainActiivty = new Intent(context, MainActivity.class);
                    mNotification.setResultIntent(mainActiivty, context);

                    String notify_title = context.getResources().getString(R.string.wf_notification_sync_notify_title);
                    notify_title = String.format(notify_title, affected_rows);

                    String notify_body = context.getResources().getString(R.string.wf_notification_sync_notify_body);
                    notify_body = String.format(notify_body, affected_rows);

                    mNotification.showNotification(context, notify_title, notify_body, authority, R.drawable.ic_oe_notification);
                }
                intent.putIntegerArrayListExtra("new_ids", (ArrayList<Integer>) affected_ids);


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user.getAndroidName().equals(account.name)) {
            context.sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = getSyncAdapter().getSyncAdapterBinder();
        return ret;
    }

    public SyncAdapterImpl getSyncAdapter() {

        return mSyncAdapter;
    }

    @Override
    protected List<Object> getModules() {
        List<Object> ret = super.getModules();
        ret.add(new ServiceModule(this, this));
        return ret;
    }

}

