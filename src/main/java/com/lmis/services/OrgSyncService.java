package com.lmis.services;

import android.accounts.Account;
import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
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
import com.lmis.auth.LmisAccountManager;
import com.lmis.base.org.OrgDB;
import com.lmis.dagger_module.ServiceModule;
import com.lmis.orm.LmisHelper;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisNotificationHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by chengdh on 14-6-9.
 */
public class OrgSyncService extends InjectingService implements PerformSync {

    public final String TAG = "OrgSyncService";

    @Inject
    @InjectingServiceModule.Service
    SyncAdapterImpl mSyncAdapter;


    @Override
    public IBinder onBind(Intent intent) {

        IBinder ret = getSyncAdapter().getSyncAdapterBinder();
        return ret;
    }

    public SyncAdapterImpl getSyncAdapter() {

        return mSyncAdapter;
    }

    /**
     * Perform sync.
     *
     * @param context    the context
     * @param account    the account
     * @param extras     the extras
     * @param authority  the authority
     * @param provider   the provider
     * @param syncResult the sync result
     */
    public void performSync(Context context, Account account, Bundle extras,
                            String authority, ContentProviderClient provider,
                            SyncResult syncResult) {

        Log.d(TAG, "OrgSyncService->performSync()");
        Intent intent = new Intent();
        Intent updateWidgetIntent = new Intent();
        updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setAction(SyncFinishReceiver.SYNC_FINISH);
        LmisUser user = LmisAccountManager.getAccountDetail(context, account.name);
        try {
            OrgDB orgDB = new OrgDB(context);
            orgDB.setAccountUser(user);
            LmisHelper lmis = orgDB.getLmisInstance();
            if (lmis == null) {
                return;
            }

            LmisArguments arguments = new LmisArguments();

            //数据库中原有的数据也需要更新
            List<Integer> ids = orgDB.ids();
            if (lmis.syncWithMethod("all", arguments)) {
                int affected_rows = lmis.getAffectedRows();
                Log.d(TAG, "OrgSyncService[arguments]:" + arguments.toString());
                Log.d(TAG, "OrgSyncService->affected_rows:" + affected_rows);
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

                    String notify_title = context.getResources().getString(R.string.orgs_sync_notify_title);
                    notify_title = String.format(notify_title, affected_rows);

                    String notify_body = context.getResources().getString(R.string.orgs_sync_notify_body);
                    notify_body = String.format(notify_body, affected_rows);

                    mNotification.showNotification(context, notify_title, notify_body, authority,
                            R.drawable.ic_oe_notification);
                }
                intent.putIntegerArrayListExtra("new_ids", (ArrayList<Integer>) affected_ids);
            }
            //更新数据库中已存在的expense信息
            //List<Integer> updated_ids = updateOldExpenses(expense_db, oe, currentUser, ids);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user.getAndroidName().equals(account.name)) {
            context.sendBroadcast(intent);
        }
    }

    @Override
    protected List<Object> getModules() {
        List<Object> ret = super.getModules();
        ret.add(new ServiceModule(this, this));
        return ret;
    }
}
