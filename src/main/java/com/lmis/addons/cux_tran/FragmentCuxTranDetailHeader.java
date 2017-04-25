package com.lmis.addons.cux_tran;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FragmentCuxTranDetailHeader extends BaseFragment {

    public static final String TAG = "FragmentCuxTranDetailHeader";


    View mView = null;
    LmisDataRow mCuxTranData;

    //单据类型
    @InjectView(R.id.txv_business_type)
    TextView mTxvBusinessType;
    //计划编号
    @InjectView(R.id.txv_require_number)
    TextView mTxvRequireNumber;
    //计划状态
    @InjectView(R.id.txv_require_status)
    TextView mTxvRequireStatus;

    @InjectView(R.id.txv_project_name)
    TextView mTxvProjectName;

    //计划来源
    @InjectView(R.id.txv_categorie)
    TextView mTxvCategorie;

    //计划类型
    @InjectView(R.id.txv_require_type)
    TextView mTxvRequireType;

    //提报部门
    @InjectView(R.id.txv_require_deparment)
    TextView mTxvRequireDeparment;

    //申请时间
    @InjectView(R.id.txv_require_date)
    TextView mTxvRequireDate;

    //创建时间
    @InjectView(R.id.txv_creation_date)
    TextView mTxvCreationDate;


    //需求人员
    @InjectView(R.id.txv_require_person)
    TextView mTxvRequirePerson;

    //备注
    @InjectView(R.id.txv_remark)
    TextView mTxvRemark;


    //预算总额
    @InjectView(R.id.txv_bugdet_balance)
    TextView mTxvBugdetBalance;

    //可用预算
    @InjectView(R.id.txv_bugdet_total)
    TextView mTxvBugdetTotal;

    //本次领用
    @InjectView(R.id.txv_header_bugdet)
    TextView mTxvHeaderBugdet;


    public LmisDataRow getmCuxTranData() {
        return mCuxTranData;
    }

    public void setmCuxTranData(LmisDataRow mCuxTranData) {
        this.mCuxTranData = mCuxTranData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_cux_tran_detail_header, container, false);
        ButterKnife.inject(this, mView);
        initView();
        return mView;
    }

    private void initView() {
        //设置主表内容

        String businessType = mCuxTranData.getString("business_type");
        mTxvBusinessType.setText(CuxTranList.mMapBusinessType.get(businessType));

        String requireNumber = mCuxTranData.getString("require_number");
        mTxvRequireNumber.setText(requireNumber);
        String requireStatus = mCuxTranData.getString("require_status");
        mTxvRequireStatus.setText(requireStatus);
        String projectName = mCuxTranData.getString("name");
        mTxvProjectName.setText(projectName);

        String categorie = mCuxTranData.getString("categorie");
        mTxvCategorie.setText(categorie);
        String requireType = mCuxTranData.getString("require_type");
        mTxvRequireType.setText(requireType);

        String requireDeparment = mCuxTranData.getString("require_deparment");
        mTxvRequireDeparment.setText(requireDeparment);
        String requireDate = mCuxTranData.getString("require_date");
        mTxvCreationDate.setText(requireDate);

        String requirePerson = mCuxTranData.getString("require_person");
        mTxvRequirePerson.setText(requirePerson);
        String creationDate = mCuxTranData.getString("creation_date");
        mTxvCreationDate.setText(creationDate);

        String remark = mCuxTranData.getString("remark");
        mTxvRemark.setText(remark);

        //预算总额
        String bugdetBalance = mCuxTranData.getString("bugdet_balance");
        mTxvBugdetBalance.setText(bugdetBalance);

        //可用预算
        String bugdetDemandTotal = mCuxTranData.getString("bugdet_total");
        mTxvBugdetTotal.setText(bugdetDemandTotal);

        //本次领用
        String headerBugdet = mCuxTranData.getString("header_bugdet");
        mTxvHeaderBugdet.setText(headerBugdet);

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
