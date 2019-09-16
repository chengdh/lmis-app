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
import com.lmis.addons.inventory.InventoryMoveDB;
import com.lmis.auth.LmisAccountManager;
import com.lmis.dagger_module.ServiceModule;
import com.lmis.orm.LmisHelper;
import com.lmis.orm.LmisValues;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisNotificationHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * 出库单同步处理
 * Created by chengdh on 14-9-15.
 */
public class InventoryOutSyncService extends InjectingService implements PerformSync {
    public static final String TAG = "InventoryOutSyncService";
    @Inject
    @InjectingServiceModule.Service
    SyncAdapterImpl mSyncAdapter;

    @Override
    public void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.d(TAG, "InventoryOutSyncService->performSync()");
        Intent intent = new Intent();
        intent.setAction(SyncFinishReceiver.SYNC_FINISH);
        LmisUser user = LmisAccountManager.getAccountDetail(context, account.name);
        try {
            InventoryMoveDB db = new InventoryMoveDB(context);
            db.setAccountUser(user);
            LmisHelper lmis = db.getLmisInstance();
            if (lmis == null) {
                return;
            }

            LmisArguments arguments = new LmisArguments();
            arguments.add(user.getDefault_org_id());

            //数据库中原有的数据也需要更新
            String where = "to_org_id = ?";
            String[] whereArgs = new String[]{user.getDefault_org_id() + ""};
            List<Integer> oldIDs = db.ids(where, whereArgs);
            if (lmis.syncWithMethod("get_confirms_for_app", arguments)) {
                int affected_rows = lmis.getAffectedRows();
                Log.d(TAG, "InventoryOutSyncService[arguments]:" + arguments.toString());
                Log.d(TAG, "InventoryOutSyncService->affected_rows:" + affected_rows);
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

                    String notify_title = context.getResources().getString(R.string.inventory_move_sync_notify_title);
                    notify_title = String.format(notify_title, affected_rows);

                    String notify_body = context.getResources().getString(R.string.inventory_move_sync_notify_body);
                    notify_body = String.format(notify_body, affected_rows);

                    mNotification.showNotification(context, notify_title, notify_body, authority, R.drawable.ic_oe_notification);
                }
                intent.putIntegerArrayListExtra("new_ids", (ArrayList<Integer>) affected_ids);
            }
            //更新数据库中已存在的expense信息
            List<Integer> updatedIDs = updateOldInventoryMoves(db, lmis, user, oldIDs);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user.getAndroidName().equals(account.name)) {
            context.sendBroadcast(intent);
        }
    }

    //更新数据库中已有的InventoryMove信息
    private List<Integer> updateOldInventoryMoves(InventoryMoveDB db, LmisHelper lmis, LmisUser user, List<Integer> ids) {
        Log.d(TAG, "InventoryOutSyncServide->updateOldInventoryMoves()");
        List<Integer> updated_ids = new ArrayList<Integer>();
        try {
            JSONArray ids_array = new JSONArray();
            for (int id : ids)
                ids_array.put(id);

            JSONObject fields = new JSONObject();

            fields.accumulate("fields", "id");
            fields.accumulate("fields", "state");

            JSONObject domain = new JSONObject();
            domain.put("id_in", ids_array);
            JSONArray result = lmis.search_read("load_list_with_barcode", fields, domain);

            Log.d(TAG, "InventoryOutSyncService#updateOldInventoryMoves" + result);
            for (int j = 0; j < result.length(); j++) {
                JSONObject objRes = result.getJSONObject(j);
                int id = objRes.getInt("id");
                String state = objRes.getString("state");
                LmisValues values = new LmisValues();
                values.put("state", state);
                db.update(values, id);
                updated_ids.add(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updated_ids;
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
