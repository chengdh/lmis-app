package com.lmis.services;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.fizzbuzz.android.dagger.InjectingService;
import com.lmis.LmisArguments;
import com.lmis.auth.LmisAccountManager;
import com.lmis.base.user_org.UserOrgDB;
import com.lmis.dagger_module.ServiceModule;
import com.lmis.orm.LmisHelper;
import com.lmis.support.LmisUser;

import java.util.List;

import javax.inject.Inject;

import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-6-15.
 */
public class UserOrgSyncService extends InjectingService implements PerformSync {

    public final String TAG = "UserOrgSyncService";

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

        Log.d(TAG, "UserOrgSyncService->performSync()");
        LmisUser user = LmisAccountManager.getAccountDetail(context, account.name);
        try {
            UserOrgDB orgDB = new UserOrgDB(context);
            orgDB.setAccountUser(user);
            LmisHelper lmis = orgDB.getLmisInstance();
            if (lmis == null) {
                return;
            }

            //同步参数: user_id:当前用户id,is_select : true
            LmisArguments arguments = new LmisArguments();
            JSONObject conditionWraper = new JSONObject();
            JSONObject conditions = new JSONObject();
            conditions.put("user_id", user.getUser_id());
            conditions.put("is_select", true);
            conditionWraper.put("conditions", conditions);
            arguments.add(conditionWraper);

            if (lmis.syncWithMethod("all", arguments)) {
                int affected_rows = lmis.getAffectedRows();
                Log.d(TAG, "OrgSyncService[arguments]:" + arguments.toString());
                Log.d(TAG, "OrgSyncService->affected_rows:" + affected_rows);
            }
            //更新数据库中已存在的expense信息
            //List<Integer> updated_ids = updateOldExpenses(expense_db, oe, user, ids);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Object> getModules() {
        List<Object> ret = super.getModules();
        ret.add(new ServiceModule(this, this));
        return ret;
    }
}
