package com.lmis.addons.cux_demand;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.lmis.LmisArguments;
import com.lmis.R;
import com.lmis.addons.wf_notification.WfNoticicationList;
import com.lmis.addons.wf_notification.WfNotificationDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisHelper;
import com.lmis.orm.LmisValues;
import com.lmis.providers.cux_demand.CuxDemandProvider;
import com.lmis.providers.wf_notification.WfNotificationProvider;
import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisUser;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.controls.ExceptionTypeSpinner;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 2017/2/19.
 */

public class CuxDemandDetail extends BaseFragment implements DialogAudit.NoticeDialogListener, DialogAuditReject.NoticeDialogListener {

    public static final String TAG = "CuxDemandDetail";
    View mView = null;
    Integer mCuxDemandId = null;
    LmisDataRow mCuxDemandData = null;
    LmisListAdapter mCuxDemandLinesAdapter = null;

    @InjectView(R.id.txv_project_name)
    TextView mTxvProjectName;
    @InjectView(R.id.txv_apply_date)
    TextView mTxvApplyDate;
    @InjectView(R.id.txv_applier_user)
    TextView mTxvApplierUser;
    @InjectView(R.id.txv_apply_deparment)
    TextView mTxvApplyDeparment;
    @InjectView(R.id.txv_header_bugdet)
    TextView mTxvHeaderBugdet;
    @InjectView(R.id.lst_cux_demand_lines)
    ListView mCuxDemandLinesView;


    //是否已操作
    Boolean mProcessed = false;
    //费用单明细
    List<Object> mCuxDemandLines = new ArrayList<Object>();

    //工作流审批pass
    WorkflowOperation wfPassOperator = null;

    //工作流审批拒绝
    WorkflowOperation wfRejectOperator = null;

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

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class LineViewHolder {
        //        @InjectView(R.id.txv_line_number)
//        TextView txvLineNumber;
        @InjectView(R.id.txv_item_description)
        TextView txvItemDescription;
//        @InjectView(R.id.txv_item_spec)
//        TextView txvItemSpec;

        @InjectView(R.id.txv_item_price)
        TextView txvItemPrice;

        @InjectView(R.id.txv_demand_quantiry)
        TextView txvDemandQuantiry;
        @InjectView(R.id.txv_line_bugdet)
        TextView txvLineBugdet;


        public LineViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_cux_demand_detail, container, false);
        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    private void init() {
        Log.d(TAG, "CuxDemandDetail->init()");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCuxDemandId = bundle.getInt("cux_demand_id");
            mCuxDemandData = db().select(mCuxDemandId);
            //mProcessed = mCuxDemandData.getBoolean("processed");

            List<LmisDataRow> lines = mCuxDemandData.getO2MRecord("cux_demand_lines").browseEach();
            for (Object l : lines) {
                mCuxDemandLines.add(l);
            }
            initControls();
        }
    }

    private void initControls() {
        //设置主表内容

        String projectName = mCuxDemandData.getString("project_name");
        String applyNumber = mCuxDemandData.getString("apply_number");
        String applierUser = mCuxDemandData.getString("applier_user");
        String applyDate = mCuxDemandData.getString("apply_date");
        String headerBugdet = mCuxDemandData.getString("header_bugdet");
        String applyDeparment = mCuxDemandData.getString("apply_deparment");
        mProcessed = mCuxDemandData.getBoolean("processed");

        mTxvProjectName.setText(projectName + "[" + applyNumber + "]");
        mTxvApplierUser.setText(applierUser);
        mTxvApplyDate.setText(applyDate.substring(0, 10));
        mTxvApplyDeparment.setText(applyDeparment);
        mTxvHeaderBugdet.setText("SUM:" + headerBugdet);

        //设置子表内容
        mCuxDemandLinesAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_cux_demand_detail_line_item, mCuxDemandLines) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getActivity().getLayoutInflater().inflate(getResource(), parent, false);
                    view.setTag(new LineViewHolder(view));
                }
                view = handleRowView(view, position);
                return view;
            }
        };
        mCuxDemandLinesView.setAdapter(mCuxDemandLinesAdapter);
    }

    private View handleRowView(View mView, final int position) {
        LineViewHolder holder = (LineViewHolder) mView.getTag();
        if (mCuxDemandLines.size() > 0) {
            LmisDataRow line = (LmisDataRow) mCuxDemandLines.get(position);

            String lineNumber = line.getString("line_number");
            String itemDescription = line.getString("item_description");
            String itemSpec = line.getString("item_spec");
            String itemPrice = line.getString("item_price");
            String demandQuantiry = line.getString("demand_quantiry");
            String lineBugdet = line.getString("line_bugdet");
//            holder.txvLineNumber.setText(lineNumber);
            holder.txvItemDescription.setText(itemSpec);
//            holder.txvItemSpec.setText(itemSpec);
            holder.txvItemPrice.setText(itemPrice);
            holder.txvDemandQuantiry.setText(demandQuantiry);
            holder.txvLineBugdet.setText(lineBugdet);
        }

        return mView;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // handle item selection
        switch (item.getItemId()) {
            case R.id.menu_audit_pass:
                Log.d(TAG, "CuxDemandDetail#onOptionsItemSelected#pass");
                // 编写审批代码
                showPassAuditDialog();
                return true;
            case R.id.menu_audit_reject:
                Log.d(TAG, "CuxDemandDetail#onOptionsItemSelected#reject");
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
                if (model.equals("CuxDemand") && mCuxDemandId == Integer.parseInt(id)) {
                    Log.d(TAG, "CuxDemandDetail->datasetChangeReceiver@onReceive");
                    LmisDataRow row = db().select(Integer.parseInt(id));
                    mCuxDemandData = row;
                    //更新界面上state的显示
//                    String name = mExpenseData.getString("name");
//                    String state = mExpenseData.getString("state");
//                    String status = getStatus(state);
//                    TextView txvName = (TextView) mView.findViewById(R.id.txvExpenseName);
//
//                    txvName.setText(name + "(" + status + ")");
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
        inflater.inflate(R.menu.menu_fragment_cux_demand_detail, menu);
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
        LmisHelper mLmisHelper = null;
        WfNotificationDB wfDB = null;
        ProgressDialog mProgressDialog = null;
        String mSignal = null;
        String mAuditNote = null;

        public WorkflowOperation(String auditSignal, String auditNote) {
            mSignal = auditSignal;
            mAuditNote = auditNote;
            mLmisHelper = db().getLmisInstance();
            wfDB = new WfNotificationDB(scope.context());
            if (mLmisHelper == null)
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
            String wfItemkey = mCuxDemandData.getString("wf_itemkey");
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
                JSONObject ret = (JSONObject) mLmisHelper.call_kw("audit", arguments);
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
                    db().update(values, mCuxDemandId);
                    wfDB.update(values,wfNotificationId);
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
                scope.main().requestSync(CuxDemandProvider.AUTHORITY);
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
        return new CuxDemandPlatformHeaderDB(context);
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

