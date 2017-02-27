package com.lmis.addons.cux_tran;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.Toast;

import com.lmis.LmisArguments;
import com.lmis.R;
import com.lmis.addons.cux_demand.CuxDemandList;
import com.lmis.addons.shared.DialogAudit;
import com.lmis.addons.shared.DialogAuditReject;
import com.lmis.addons.wf_notification.WfNoticicationList;
import com.lmis.addons.wf_notification.WfNotificationDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisHelper;
import com.lmis.orm.LmisValues;
import com.lmis.providers.cux_tran.CuxTranProvider;
import com.lmis.providers.wf_notification.WfNotificationProvider;
import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisUser;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 2017/2/19.
 */

public class CuxTranDetail extends BaseFragment implements TabHost.OnTabChangeListener, DialogAudit.NoticeDialogListener, DialogAuditReject.NoticeDialogListener {

    public static final String TAG = "CuxTranDetail";
    View mView = null;
    Integer mCuxTranId = null;
    LmisDataRow mCuxTranData = null;

    //是否已操作
    Boolean mProcessed = false;

    //工作流审批pass
    WorkflowOperation wfPassOperator = null;

    //工作流审批拒绝
    WorkflowOperation wfRejectOperator = null;

    CuxTranDetailPageAdapter mPageAdapter;

    @InjectView(R.id.tab_host_cux_tran_detail)
    TabHost mTabHost;
    @InjectView(R.id.pager_cux_tran_detail)
    ViewPager mPager;

    @Override
    public void onDialogPositiveClick(DialogAudit dialog) {
        if (wfPassOperator != null) {
            wfPassOperator.cancel(true);
            wfPassOperator = null;
        }
        String auditNote = dialog.getAuditNote();
        wfPassOperator = new WorkflowOperation("0", auditNote);

        wfPassOperator.execute((Void) null);

    }

    @Override
    public void onDialogNegativeClick(DialogAudit dialog) {

        dialog.dismiss();

    }

    @Override
    public void onRejectDialogPositiveClick(DialogAuditReject dialog) {
        if (wfRejectOperator != null) {
            wfRejectOperator.cancel(true);
            wfRejectOperator = null;
        }

        String auditNote = dialog.getAuditNote();
        wfRejectOperator = new WorkflowOperation("1", auditNote);

        wfRejectOperator.execute((Void) null);
    }

    @Override
    public void onRejectDialogNegativeClick(DialogAuditReject dialog) {

        dialog.dismiss();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_cux_tran_detail, container, false);
        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    private void init() {
        Log.d(TAG, "CuxTranDetail->init()");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCuxTranId = bundle.getInt("cux_tran_id");
            mCuxTranData = db().select(mCuxTranId);

            mProcessed = mCuxTranData.getBoolean("processed");
            initControls();
        }
    }

    /**
     * /**
     * 初始化控件.
     */
    private void initControls() {
        initTabs();
        initPager();
    }

    private void initPager() {
        mPageAdapter = new CuxTranDetailPageAdapter(getFragmentManager(), mCuxTranData);
        mPager.setAdapter(mPageAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 初始化tabs.
     */
    private void initTabs() {
        mTabHost.setup();
        //TODO 此处加上tab图标
        mTabHost.addTab(mTabHost.newTabSpec("TAB_CUX_TRAN_DETAIL_HEADER").setIndicator("单据表头").setContent(R.id.tab_not_use));
        mTabHost.addTab(mTabHost.newTabSpec("TAB_CUX_TRAN_DETAIL_LINES").setIndicator("单据明细").setContent(R.id.tab_not_use));
        mTabHost.addTab(mTabHost.newTabSpec("TAB_CUX_TRAN_DETAIL_WF_MESSAGES").setIndicator("审批记录").setContent(R.id.tab_not_use));
        mTabHost.setOnTabChangedListener(this);
    }


    /**
     * On tab changed.
     *
     * @param s the s
     */
    @Override
    public void onTabChanged(String s) {
        if (s.equals("TAB_CUX_TRAN_DETAIL_HEADER")) {
            mPager.setCurrentItem(0);
        } else if (s.equals("TAB_CUX_TRAN_DETAIL_LINES")) {
            mPager.setCurrentItem(1);
        } else if (s.equals("TAB_CUX_TRAN_DETAIL_WF_MESSAGES")) {
            mPager.setCurrentItem(2);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // handle item selection
        switch (item.getItemId()) {
            case R.id.menu_audit_pass:
                Log.d(TAG, "CuxTranDetail#onOptionsItemSelected#pass");
                // 编写审批代码
                showPassAuditDialog();
                return true;
            case R.id.menu_audit_reject:
                Log.d(TAG, "CuxTranDetail#onOptionsItemSelected#reject");
                showRejectAuditDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        scope.context().registerReceiver(datasetChangeReceiver, new IntentFilter(DataSetChangeReceiver.DATA_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        scope.context().unregisterReceiver(datasetChangeReceiver);
    }

    private DataSetChangeReceiver datasetChangeReceiver = new DataSetChangeReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String id = intent.getExtras().getString("id");
                String model = intent.getExtras().getString("model");
                if (model.equals("CuxTran") && mCuxTranId == Integer.parseInt(id)) {
                    Log.d(TAG, "CuxTranDetail->datasetChangeReceiver@onReceive");
                    LmisDataRow row = db().select(Integer.parseInt(id));
                    mCuxTranData = row;
                }
            } catch (Exception e) {
            }

        }
    };

    //workflow处理完毕后,需要禁用审核按钮
    private void setMenuVisible(Menu menu, Boolean visible) {
        MenuItem item_ok = menu.findItem(R.id.menu_audit_pass);
        MenuItem item_cancel = menu.findItem(R.id.menu_audit_reject);
        item_ok.setVisible(visible);
        item_cancel.setVisible(visible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_cux_tran_detail, menu);
        setMenuVisible(menu, !mProcessed);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        setMenuVisible(menu, !mProcessed);
        super.onPrepareOptionsMenu(menu);
    }

    private String getStatus(String state) {
        int id = scope.main().getResources().getIdentifier("state_" + state, "string", scope.main().getPackageName());
        String value = id == 0 ? "" : scope.main().getResources().getString(id);
        return value;
    }


    /*
     *工作流处理类,用于异步处理工作流,处理过程如下:
     *1 用户点击[通过]或[不通过]按钮
     *2 系统异步调用服务端的exec_workflow
     *3 系统更新db中的操作状态为已操作,同时从服务器获取expense的最后状态,并更新到本地
     *4 将审批通过按钮设置为disable
     *5 使用Toast提示用户操作完成
     *6 更新drawer的状态
     * */
    public class WorkflowOperation extends AsyncTask<Void, Void, Boolean> {
        boolean isConnection = true;
        LmisHelper lmisHelper = null;
        WfNotificationDB wfDB = null;
        ProgressDialog mProgressDialog = null;
        String mSignal = null;
        String mAuditNote = null;

        public WorkflowOperation(String auditSignal, String auditNote) {
            mSignal = auditSignal;
            mAuditNote = auditNote;
            lmisHelper = db().getLmisInstance();
            wfDB = new WfNotificationDB(scope.context());
            if (lmisHelper == null)
                isConnection = false;

            String working_text = "处理中...";
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(working_text);
            if (isConnection) {
                mProgressDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean execSuccess = false;
            if (!isConnection) {
                return false;
            }
            LmisUser user = scope.currentUser();
            LmisArguments arguments = new LmisArguments();
            //params 1 : user_id
            Integer userId = user.getUser_id();
            //Param 2  : user_name
            String username = user.getUsername();
            //param 3 : notification_id
            String wfItemkey = mCuxTranData.getString("wf_itemkey");
            String[] whereArgs = {wfItemkey};
            List<LmisDataRow> wfRows = wfDB.select("item_key = ?", whereArgs);
            Integer wfNotificationId = wfRows.get(0).getInt("id");
            //params 4: 审批结果：0：审批通过；1：审批拒绝

            //params 5: P_APP_RESULT_NOTE 审批人填写的审批意见
            arguments.add(userId);
            arguments.add(username);
            arguments.add(wfNotificationId);
            arguments.add(mSignal);
            arguments.add(mAuditNote);


            String retCode = "-1";
            String retMessage = "";
            try {
                JSONObject ret = (JSONObject) lmisHelper.call_kw("audit", arguments);
                retCode = ret.getString("x_ret_code");
                retMessage = ret.getString("x_ret_message");
                execSuccess = retCode.equals("0");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Creating Local Database Requirement Values
            LmisValues values = new LmisValues();
            String value = execSuccess ? "true" : "false";
            mProcessed = true;
            values.put("processed", value);

            if (execSuccess) {
                try {
                    db().update(values, mCuxTranId);
                    wfDB.update(values, wfNotificationId);
                } catch (Exception e) {
                }
            }
            return execSuccess;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                //刷新菜单栏
                scope.main().supportInvalidateOptionsMenu();

                //重新同步数据
                scope.main().requestSync(CuxTranProvider.AUTHORITY);
                scope.main().requestSync(WfNotificationProvider.AUTHORITY);

                DrawerListener drawer = (DrawerListener) getActivity();
                drawer.refreshDrawer(CuxDemandList.TAG);
                drawer.refreshDrawer(WfNoticicationList.TAG);

                String toast_text = scope.main().getResources().getString(R.string.cux_demand_processed_text);
                Toast.makeText(getActivity(), toast_text, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "No connection", Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }
    }


    @Override
    public Object databaseHelper(Context context) {
        return new CuxTranHeaderDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    public void showPassAuditDialog() {
        // Create an instance of the dialog fragment and show it
        DialogAudit dialog = new DialogAudit();
        dialog.setmListener(this);
        dialog.show(getFragmentManager(), "auditDialogPass");
    }

    public void showRejectAuditDialog() {
        // Create an instance of the dialog fragment and show it
        DialogAuditReject dialog = new DialogAuditReject();
        dialog.setmListener(this);
        dialog.show(getFragmentManager(), "auditDialogReject");
    }
}

