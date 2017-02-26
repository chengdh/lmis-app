package com.lmis.addons.cux_demand;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 2017/2/25.
 */

public class FragmentCuxDemandDetailHeader extends BaseFragment {

    public static final String TAG = "FragmentCuxDemandDetailHeader";


    View mView = null;
    LmisDataRow mCuxDemandData;
    //计划编号
    @InjectView(R.id.txv_apply_number)
    TextView mTxvApplyNumber;
    //计划状态
    @InjectView(R.id.txv_apply_status)
    TextView mTxvApplyStatus;

    @InjectView(R.id.txv_project_name)
    TextView mTxvProjectName;

    //计划来源
    @InjectView(R.id.txv_apply_source)
    TextView mTxvApplySource;

    //计划类型
    @InjectView(R.id.txv_apply_type)
    TextView mTxvApplyType;

    //提报部门
    @InjectView(R.id.txv_apply_deparment)
    TextView mTxvApplyDeparment;

    //编制时间
    @InjectView(R.id.txv_apply_date)
    TextView mTxvApplyDate;

    //需求人员
    @InjectView(R.id.txv_applier_user)
    TextView mTxvApplierUser;

    //提交时间
    @InjectView(R.id.txv_submit_date)
    TextView mTxvSubmitDate;

    //备注
    @InjectView(R.id.txv_remark)
    TextView mTxvRemark;

    //预算总额
    @InjectView(R.id.txv_bugdet_total)
    TextView mTxvBugdetTotal;

    //已审批额
    @InjectView(R.id.txv_bugdet_demand_total)
    TextView mTxvBugdetDemandTotal;

    //实际成本
    @InjectView(R.id.txv_actual_cost)
    TextView mTxvBugdetActualCost;

    //需求额
    @InjectView(R.id.txv_header_bugdet)
    TextView mTxvHeaderBugdet;
    //余额
    @InjectView(R.id.txv_bugdet_balance)
    TextView mTxvBugdetBalance;

    //未审批额
    @InjectView(R.id.txv_left_bugdet_demand_total)
    TextView mTxvLeftBugdetDemandTotal;


    public LmisDataRow getmCuxDemandData() {
        return mCuxDemandData;
    }

    public void setmCuxDemandData(LmisDataRow mCuxDemandData) {
        this.mCuxDemandData = mCuxDemandData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_cux_demand_detail_header, container, false);
        ButterKnife.inject(this, mView);
        initView();
        return mView;
    }

    private void initView() {
        //设置主表内容

        String applyNumber = mCuxDemandData.getString("apply_number");
        mTxvApplyNumber.setText(applyNumber);
        String applyStatus = mCuxDemandData.getString("apply_status");
        mTxvApplyStatus.setText(applyStatus);
        String projectName = mCuxDemandData.getString("project_name");
        mTxvProjectName.setText(projectName);

        String applySource = mCuxDemandData.getString("apply_source");
        mTxvApplySource.setText(applySource);
        String applyType = mCuxDemandData.getString("apply_type");
        mTxvApplyType.setText(applyType);

        String applyDeparment = mCuxDemandData.getString("apply_deparment");
        mTxvApplyDeparment.setText(applyDeparment);
        String applyDate = mCuxDemandData.getString("apply_date");
        mTxvApplyDate.setText(applyDate.substring(0, 15));

        String applierUser = mCuxDemandData.getString("applier_user");
        mTxvApplierUser.setText(applierUser);
        String submitDate = mCuxDemandData.getString("submit_date");
        mTxvSubmitDate.setText(submitDate);

        String applyRemark = mCuxDemandData.getString("remark");
        mTxvRemark.setText(applyRemark);
        //预算总额

        String bugdetTotal = mCuxDemandData.getString("bugdet_total");
        mTxvBugdetTotal.setText(bugdetTotal);

        //已审批额
        String bugdetDemandTotal = mCuxDemandData.getString("bugdet_demand_total");
        mTxvBugdetDemandTotal.setText(bugdetDemandTotal);

        //实际成本

        String bugdetActualCost = mCuxDemandData.getString("bugdet_actual_cost");
        mTxvBugdetActualCost.setText(bugdetActualCost);

        //需求额

        String headerBugdet = mCuxDemandData.getString("header_bugdet");
        mTxvHeaderBugdet.setText(headerBugdet);
        //余额

        String bugdetB1alance = mCuxDemandData.getString("bugdet_balance");
        mTxvBugdetBalance.setText(bugdetB1alance);

        //未审批额
        //FIXME 算法
        String leftBalanceDemandTotal = "0";
        mTxvLeftBugdetDemandTotal.setText(leftBalanceDemandTotal);

    }

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }
}
