package com.lmis.addons.cux_demand;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.drawer.DrawerItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 2017/2/19.
 */

public class CuxDemandDetail extends BaseFragment implements DialogAudit.NoticeDialogListener,DialogAuditReject.NoticeDialogListener {

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

    @Override
    public void onDialogPositiveClick(DialogAudit dialog) {
        dialog.dismiss();

    }

    @Override
    public void onDialogNegativeClick(DialogAudit dialog) {

    }

    @Override
    public void onRejectDialogPositiveClick(DialogAuditReject dialog) {
        dialog.dismiss();
    }

    @Override
    public void onRejectDialogNegativeClick(DialogAuditReject dialog) {

    }

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class LineViewHolder {
        @InjectView(R.id.txv_line_number)
        TextView txvLineNumber;
        @InjectView(R.id.txv_item_description)
        TextView txvItemDescription;
        @InjectView(R.id.txv_item_spec)
        TextView txvItemSpec;

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
            holder.txvLineNumber.setText(lineNumber);
            holder.txvItemDescription.setText(itemDescription);
            holder.txvItemSpec.setText(itemSpec);
            holder.txvItemPrice.setText(itemPrice);
            holder.txvDemandQuantiry.setText(demandQuantiry);
            holder.txvLineBugdet.setText(lineBugdet);
        }

        return mView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_cux_demand_detail, menu);
//        setMenuVisible(menu, !mProcessed);
    }

    //workflow处理完毕后,需要禁用审核按钮
    private void setMenuVisible(Menu menu, Boolean visible) {
//        MenuItem item_ok = menu.findItem(R.id.menu_expense_detail_audit);
//        MenuItem item_cancel = menu.findItem(R.id.menu_expense_detail_cancel);
//        item_ok.setVisible(visible);
//        item_cancel.setVisible(visible);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
//        setMenuVisible(menu, !mProcessed);
        super.onPrepareOptionsMenu(menu);
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
                // 编写cancel代码

                showRejectAuditDialog();
//                mWorkflowOperation = new WorkflowOperation("refuse");
//                mWorkflowOperation.execute();
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
//    public class WorkflowOperation extends AsyncTask<Void, Void, Boolean> {
//        boolean isConnection = true;
//        OEHelper mOE = null;
//        ProgressDialog mProgressDialog = null;
//        String mSignal = null;
//
//        public WorkflowOperation(String signal) {
//            mSignal = signal;
//            mOE = db().getOEInstance();
//            if (mOE == null)
//                isConnection = false;
//
//            String working_text = scope.main().getResources().getString(R.string.working_text);
//            mProgressDialog = new ProgressDialog(getActivity());
//            mProgressDialog.setMessage(working_text);
//            if (isConnection) {
//                mProgressDialog.show();
//            }
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            Boolean execSuccess = false;
//            if (!isConnection) {
//                return false;
//            }
//            OEArguments arguments = new OEArguments();
//            // Param 1 : model_name
//            String modelName = "hr.expense.expense";
//            // Param 2 : res_id
//            Integer resId = mExpenseId;
//            //params 3 : signal
//            try {
//                mOE.exec_workflow(modelName, resId, mSignal);
//                execSuccess = true;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            // Creating Local Database Requirement Values
//            OEValues values = new OEValues();
//            String value = (execSuccess) ? "true" : "false";
//            mProcessed = true;
//            values.put("processed", value);
//
//            if (execSuccess) {
//                try {
//                    db().update(values, mExpenseId);
//                } catch (Exception e) {
//                }
//            }
//            return execSuccess;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (result) {
//                //刷新菜单栏
//                scope.main().supportInvalidateOptionsMenu();
//
//                //重新同步数据
//                scope.main().requestSync(ExpenseProvider.AUTHORITY);
//
//                DrawerListener drawer = (DrawerListener) getActivity();
//                drawer.refreshDrawer(Expense.TAG);
//
//                String toast_text = scope.main().getResources().getString(R.string.expense_processed_text);
//                Toast.makeText(getActivity(), toast_text, Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(getActivity(), "No connection", Toast.LENGTH_LONG).show();
//            }
//            mProgressDialog.dismiss();
//        }
//    }


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

