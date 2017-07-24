package com.lmis.addons.scan_header;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode_scan_header.BarcodeParser;
import com.lmis.util.barcode_scan_header.BarcodeQueryListener;
import com.lmis.util.drawer.DrawerItem;

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

    //已扫描的条码列表
    @InjectView(R.id.listview_bills)
    ListView mListScanedBills;
    LmisListAdapter mScanedBillsAdapter = null;
    List<Object> mScanedBillsObjects = null;

    View mView = null;
    Integer mId = null;
    LmisDataRow mScanHeader = null;

    SearchView mSearchViewBarcodeList;

    String mOpType = null;


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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_scan_header_detail, menu);
        mSearchViewBarcodeList = (SearchView) menu.findItem(R.id.menu_scan_header_detail_search).getActionView();
        mSearchViewBarcodeList.setOnQueryTextListener(new BarcodeQueryListener(mScanedBillsAdapter));
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
            mTxvTitle.setText(fromTo);
            mTxvBillDate.setText(billDate);
            mTxvSubTitle.setText(describe);

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

        public ViewHolderForBillsList(View view) {
            ButterKnife.inject(this, view);
        }
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
