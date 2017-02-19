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
import com.lmis.addons.cux_demand.CuxDemandPlatformHeaderDB;
import com.lmis.addons.wf_notification.WfNotificationDB;
import com.lmis.auth.LmisAccountManager;
import com.lmis.dagger_module.ServiceModule;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisHelper;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisNotificationHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 2017/2/17.
 */

public class CuxDemandSyncService extends InjectingService implements PerformSync {

    public final String TAG = "CuxDemandSyncService";


    @Inject
    @InjectingServiceModule.Service
    SyncAdapterImpl mSyncAdapter;

    @Override
    public void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "CuxDemandSyncService->performSync()");
        if (extras == null || extras.size() == 0) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(SyncFinishReceiver.SYNC_FINISH);
        LmisUser user = LmisAccountManager.getAccountDetail(context, account.name);
        try {
            WfNotificationDB wfDB = new WfNotificationDB(context);
            CuxDemandPlatformHeaderDB db = new CuxDemandPlatformHeaderDB(context);
            wfDB.setAccountUser(user);
            db.setAccountUser(user);
            LmisHelper lmis = db.getLmisInstance();
            if (lmis == null) {
                return;
            }

            //获取wfDB中status=OPEN的数据
            String[] whereArgs = {};
            List<LmisDataRow> wfNotifications = wfDB.select("status='OPEN'", whereArgs);
            if (wfNotifications.size() == 0 && user.getAndroidName().equals(account.name)) {
                context.sendBroadcast(intent);
                return;
            }

            JSONArray itemKeys = new JSONArray();
            //获取item_key数组
            for (LmisDataRow wf : wfNotifications) {
                itemKeys.put(wf.getString("item_key"));
            }


            //获取wf_notificationDB中的item_key
            LmisArguments arguments = new LmisArguments();
            arguments.add(itemKeys);

            //数据库中原有的数据也需要更新
            List<Integer> ids = db.ids();
            if (lmis.syncWithMethod("bills_by_wf_itemkeys", arguments)) {
                int affected_rows = lmis.getAffectedRows();
                Log.d(TAG, "CuxDemandSyncService[arguments]:" + arguments.toString());
                Log.d(TAG, "CuxDemandSyncService->affected_rows:" + affected_rows);
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

                    String notify_title = context.getResources().getString(R.string.cux_demand_sync_notify_title);
                    notify_title = String.format(notify_title, affected_rows);

                    String notify_body = context.getResources().getString(R.string.cux_demand_sync_notify_body);
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

