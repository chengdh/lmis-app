package com.lmis.addons.scan_header;

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

public class ScanHeaderDetail extends BaseFragment {
    public static final String TAG = "ScanHeaderDetail";

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
    ListView mListScanedBills;
    LmisListAdapter mScanedBillsAdapter = null;
    List<Object> mScanedBillsObjects = null;

    View mView = null;
    Menu mMenu;
    Integer mId = null;
    LmisDataRow mScanHeader = null;

    SearchView mSearchViewBarcodeList;

    String mOpType = null;

    ProcessSendder mProcessSenderAsync = null;


    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_scan_header_detail, container, false);

        ButterKnife.inject(this, mView);
        initData();
        return mView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_scan_header_detail, menu);
        mMenu = menu;
        mSearchViewBarcodeList = (SearchView) menu.findItem(R.id.menu_scan_header_detail_search).getActionView();
        mSearchViewBarcodeList.setOnQueryTextListener(new BarcodeQueryListener(mScanedBillsAdapter));


        MenuItem item = menu.findItem(R.id.menu_scan_header_detail_send);
        String state = mScanHeader.getString("processed");
        if ((mOpType.equals(ScanHeaderOpType.LOAD_OUT) || mOpType.equals(ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT) || mOpType.equals(ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT)) && state.equals("true")) {
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }
    }

    private void setShipMenuItemVisible(boolean visible) {
        MenuItem item = mMenu.findItem(R.id.menu_scan_header_detail_send);
        item.setVisible(visible);
    }

    /**
     * 初始化数据.
     */
    private void initData() {
        Log.d(TAG, "ScanHeaderDetail#initData");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOpType = bundle.getString("type");
            mId = bundle.getInt("scan_header_id");
            mScanHeader = new ScanHeaderDB(scope.context()).select(mId);
            LmisDataRow fromOrg = mScanHeader.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = mScanHeader.getM2ORecord("to_org_id").browse();
            String fromOrgName = "";
            String toOrgName = "";
            if (fromOrg != null) {
                fromOrgName = mScanHeader.getM2ORecord("from_org_id").browse().getString("name");
            }
            if (toOrg != null) {
                toOrgName = mScanHeader.getM2ORecord("to_org_id").browse().getString("name");
            }

            Integer goodsCount = mScanHeader.getInt("sum_goods_count");
            Integer billsCount = mScanHeader.getInt("sum_bills_count");
            String describe = String.format("共%d票%d件", billsCount, goodsCount);
            String billDate = mScanHeader.getString("bill_date");
            String fromTo = String.format("%s 至 %s", fromOrgName, toOrgName);
            String vNo = mScanHeader.getString("v_no");
            String driverName = mScanHeader.getString("driver_name");
            String mobile = mScanHeader.getString("mobile");
            String idNo = mScanHeader.getString("id_no");
            mTxvTitle.setText(fromTo);
            mTxvBillDate.setText(billDate);
            mTxvSubTitle.setText(describe);
            mTxvVNo.setText(vNo);
            mTxvDriverName.setText(String.format("%s(%s)", driverName, mobile));
            if (mOpType.equals(ScanHeaderOpType.SUB_BRANCH) || mOpType.equals(ScanHeaderOpType.LOAD_OUT) || mOpType.equals(ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT) || mOpType.equals(ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT)) {
                mLayoutSubtitle.setVisibility(View.VISIBLE);
            } else {
                mLayoutSubtitle.setVisibility(View.GONE);
            }

            mScanedBillsObjects = new ArrayList<Object>(mScanHeader.getO2MRecord("scan_lines").browseEach());
            mScanedBillsAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_scan_header_list_bills_item, mScanedBillsObjects) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View mView = convertView;
                    ViewHolderForBillsList holder;
                    if (mView == null) {
                        mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_scan_header_list_bills_item, parent, false);
                        holder = new ViewHolderForBillsList(mView);
                        mView.setTag(holder);
                    } else {
                        holder = (ViewHolderForBillsList) mView.getTag();
                    }
                    LmisDataRow line = (LmisDataRow) mScanedBillsObjects.get(position);
                    String billNo = line.getString("barcode");
                    int qty = line.getInt("qty");


                    int goodsStatusType = 0;
                    if (line.get("goods_status_type") != null) {
                        goodsStatusType = line.getInt("goods_status_type");
                    }
                    String goodsStatusNote = "";
                    if (line.get("goods_status_note") != null) {
                        goodsStatusNote = line.getString("goods_status_note");
                    }
                    holder.txvBillNo.setText(billNo);
                    holder.txvGoodsStatusType.setText(GoodsStatus.statusList().get(goodsStatusType));
                    holder.txvGoodsStatusNote.setText(goodsStatusNote);
                    holder.txvBillNo.setText(billNo);
                    holder.txvBarcodeCount.setText(qty + "件");
                    return mView;
                }

            };
            mListScanedBills.setAdapter(mScanedBillsAdapter);
            mListScanedBills.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
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
        @InjectView(R.id.txv_barcode_count)
        TextView txvBarcodeCount;

        @InjectView(R.id.txv_goods_status_type)
        TextView txvGoodsStatusType;

        @InjectView(R.id.txv_goods_status_note)
        TextView txvGoodsStatusNote;

        public ViewHolderForBillsList(View view) {
            ButterKnife.inject(this, view);
        }
    }


    @Override
    public Object databaseHelper(Context context) {
        return new ScanHeaderDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan_header_detail_send:
                mProcessSenderAsync = new ProcessSendder();
                mProcessSenderAsync.execute((Void) null);
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
                ((ScanHeaderDB) db()).processShip(mId);
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
                drawer.refreshDrawer(mOpType);
                //返回已处理界面
                ScanHeaderList list = new ScanHeaderList();
                Bundle arg = new Bundle();
                arg.putString("type", mOpType);
                arg.putString("state", "shipped");
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
