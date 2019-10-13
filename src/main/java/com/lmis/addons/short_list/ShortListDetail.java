package com.lmis.addons.short_list;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode_scan_header.BarcodeQueryListener;
import com.lmis.util.barcode_scan_header.ScanHeaderOpType;
import com.lmis.util.controls.GoodsStatus;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 2017/7/12.
 */

public class ShortListDetail extends BaseFragment {
    public static final String TAG = "ShortListDetail";

    @InjectView(R.id.txv_title)
    TextView mTxvTitle;

    @InjectView(R.id.txv_bill_date)
    TextView mTxvBillDate;

    @InjectView(R.id.txv_sub_title)
    TextView mTxvSubTitle;

    @InjectView(R.id.txv_vno)
    TextView mTxvVNo;

    @InjectView(R.id.txv_driver_name)
    TextView mTxvDriverName;

    @InjectView(R.id.linlayout_subtitle)
    LinearLayout mLayoutSubtitle;


    //已扫描的条码列表
    @InjectView(R.id.listview_bills)
    ListView mListBills;
    LmisListAdapter mBillsAdapter = null;
    List<Object> mBillsObjects = null;

    View mView = null;
    Menu mMenu;
    Integer mId = null;
    LmisDataRow mShortList = null;

    SearchView mSearchViewBarcodeList;

    ProcessSendder mProcessSenderAsync = null;


    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_short_list_detail, container, false);

        ButterKnife.inject(this, mView);
        initData();
        initBillsList();
        return mView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_short_list_detail, menu);
        mMenu = menu;
        setShipMenuItemVisible(false);
    }

    private void setShipMenuItemVisible(boolean visible) {
        MenuItem item = mMenu.findItem(R.id.menu_short_list_detail_ship);
        item.setVisible(visible);
    }

    /**
     * 初始化数据.
     */
    private void initData() {
        Log.d(TAG, "ShortListDetail#initData");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mId = bundle.getInt("short_list_id");
            mShortList = new ShortListDB(scope.context()).select(mId);
            LmisDataRow fromOrg = mShortList.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = mShortList.getM2ORecord("to_org_id").browse();
            String fromOrgName = "";
            String toOrgName = "";
            if (fromOrg != null) {
                fromOrgName = mShortList.getM2ORecord("from_org_id").browse().getString("name");
            }
            if (toOrg != null) {
                toOrgName = mShortList.getM2ORecord("to_org_id").browse().getString("name");
            }

            Integer goodsCount = mShortList.getInt("sum_goods_count");
            Integer billsCount = mShortList.getInt("sum_bills_count");
            String describe = String.format("共%d票%d件", billsCount, goodsCount);
            String billDate = mShortList.getString("bill_date");
            String fromTo = String.format("%s 至 %s", fromOrgName, toOrgName);
            String vNo = mShortList.getString("vehicle_no");
            String driverName = mShortList.getString("driver");
            String mobile = mShortList.getString("mobile");
            mTxvTitle.setText(fromTo);
            mTxvBillDate.setText(billDate);
            mTxvSubTitle.setText(describe);
            mTxvVNo.setText(vNo);
            mTxvDriverName.setText(String.format("%s(%s)", driverName, mobile));


        }
    }

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolderForBillsList {
        //发货地
        @InjectView(R.id.txv_bill_no)
        TextView txvBillNo;
        //描述信息
        @InjectView(R.id.txv_goods_num)
        TextView txvGoodsNum;

        @InjectView(R.id.txv_goods_info)
        TextView txvGoodsInfo;

        @InjectView(R.id.txv_from_to)
        TextView txvFromTo;


        public ViewHolderForBillsList(View view) {
            ButterKnife.inject(this, view);
        }
    }


    /**
     * 初始化票据列表.
     */
    private void initBillsList() {
        Log.d(TAG, "FragmentBillList#initBillList");


        mBillsObjects = new ArrayList<Object>(mShortList.getO2MRecord("carrying_bills").browseEach());

        mBillsAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_short_list_new_list_bills_item, mBillsObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolderForBillsList holder;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_short_list_new_list_bills_item, parent, false);
                    holder = new ViewHolderForBillsList(mView);
                    mView.setTag(holder);
                } else {
                    holder = (ViewHolderForBillsList) mView.getTag();
                }


                LmisDataRow bill = (LmisDataRow) mBillsObjects.get(position);
                try {
                    String billNo = bill.getString("bill_no");
                    String fromOrg = bill.getString("from_org_name");
                    String toOrg = bill.getString("to_org_name");
                    String carryingFee = bill.getString("carrying_fee");
                    String goodsFee = bill.getString("goods_fee");
                    String goodsInfo = bill.getString("goods_info");
                    String goodsNum = bill.getString("goods_num");

                    String fromTo = String.format("%s 至 %s", fromOrg, toOrg);

                    holder.txvBillNo.setText(billNo);
                    holder.txvFromTo.setText(fromTo);
                    holder.txvGoodsInfo.setText(goodsInfo);
                    holder.txvGoodsNum.setText(goodsNum);
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                }

                return mView;
            }

        };
        mListBills.setAdapter(mBillsAdapter);
        mListBills.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
    }


    @Override
    public Object databaseHelper(Context context) {
        return new ShortListDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_short_list_detail_ship:
                mProcessSenderAsync = new ProcessSendder();
                mProcessSenderAsync.execute((Void) null);
                return true;
            case R.id.menu_short_list_detail_print:
                ShortListPrintCpcl.printShortListCpcl(scope.context(),mShortList,ShortListPrintCpcl.PRINTER_NAME);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 上传数据.
     */
    private class ProcessSendder extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在处理发车数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ((ShortListDB) db()).processShip(mId);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mProcessSenderAsync.cancel(true);
                Toast.makeText(scope.context(), "发车处理成功!", Toast.LENGTH_SHORT).show();
                setShipMenuItemVisible(false);
                DrawerListener drawer = scope.main();
                //返回已处理界面
                ShortListList list = new ShortListList();
                Bundle arg = new Bundle();
                arg.putString("state", "loaded");
                list.setArguments(arg);
                scope.main().startMainFragment(list, true);

            } else {
                Toast.makeText(scope.context(), "发车处理失败!", Toast.LENGTH_SHORT).show();
                setShipMenuItemVisible(true);
            }

            pdialog.dismiss();
        }
    }
}
