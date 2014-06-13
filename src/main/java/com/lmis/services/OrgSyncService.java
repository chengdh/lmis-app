package com.lmis.services;

import android.accounts.Account;
import android.app.ActivityManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.lmis.LmisArguments;
import com.lmis.MainActivity;
import com.lmis.R;
import com.lmis.addons.expense.ExpenseDBHelper;
import com.lmis.auth.LmisAccountManager;
import com.lmis.orm.LmisHelper;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisNotificationHelper;

import java.util.ArrayList;
import java.util.List;

import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-6-9.
 */
public class OrgSyncService extends Service {

    public final String TAG = "OrgSyncService";

    private static SyncAdapterImpl sSyncAdapter = null;

    Context mContext = null;

    public OrgSyncService() {
        super();
        mContext = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = null;
        ret = getSyncAdapter().getSyncAdapterBinder();
        return ret;
    }

    public SyncAdapterImpl getSyncAdapter() {

        if (sSyncAdapter == null) {
            sSyncAdapter = new SyncAdapterImpl(this);
        }
        return sSyncAdapter;
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

        Log.d(TAG, "ExpenseSyncService->performSync()");
        Intent intent = new Intent();
        Intent updateWidgetIntent = new Intent();
        updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setAction(SyncFinishReceiver.SYNC_FINISH);
        LmisUser user = LmisAccountManager.getAccountDetail(context, account.name);
        try {
            ExpenseDBHelper expense_db = new ExpenseDBHelper(context);
            expense_db.setAccountUser(user);
            LmisHelper oe = expense_db.getLmisInstance();
            if (oe == null) {
                return;
            }
            int user_id = user.getUser_id();

            // Updating User Context for OE-JSON-RPC
            JSONObject newContext = new JSONObject();
            //newContext.put("default_model", "res.users");
            //newContext.put("default_res_id", user_id);

            LmisArguments arguments = new LmisArguments();
            // Param 1 : domain
            //LmisDomain domain = new LmisDomain();
            //arguments.add(domain);
            // Param 2 : context

            //数据库中原有的数据也需要更新
            List<Integer> ids = expense_db.ids();
            if (oe.syncWithMethod("get_waiting_audit_expenses", arguments)) {
                int affected_rows = oe.getAffectedRows();
                Log.d(TAG, "ExpenseSyncService[arguments]:" + arguments.toString());
                Log.d(TAG, "ExpenseSyncService->affected_rows:" + affected_rows);
                List<Integer> affected_ids = oe.getAffectedIds();
                boolean notification = true;
                ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo.getPackageName().equalsIgnoreCase("com.openerp")) {
                    notification = false;
                }
                if (notification && affected_rows > 0) {
                    LmisNotificationHelper mNotification = new LmisNotificationHelper();
                    Intent mainActiivty = new Intent(context, MainActivity.class);
                    mNotification.setResultIntent(mainActiivty, context);

                    String notify_title = context.getResources().getString(R.string.expenses_sync_notify_title);
                    notify_title = String.format(notify_title, affected_rows);

                    String notify_body = context.getResources().getString(R.string.expenses_sync_notify_body);
                    notify_body = String.format(notify_body, affected_rows);

                    mNotification.showNotification(context, notify_title, notify_body, authority,
                            R.drawable.ic_oe_notification);
                }
                intent.putIntegerArrayListExtra("new_ids", (ArrayList<Integer>) affected_ids);
            }
            //更新数据库中已存在的expense信息
            //List<Integer> updated_ids = updateOldExpenses(expense_db, oe, user, ids);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user.getAndroidName().equals(account.name)) {
            context.sendBroadcast(intent);
            //context.sendBroadcast(updateWidgetIntent);
        }
    }


    /**
     * The Class SyncAdapterImpl.
     */
    public class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

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
            if (account != null) {
                new OrgSyncService().performSync(mContext, account, bundle, str, providerClient, syncResult);
            }
        }
    }
}
