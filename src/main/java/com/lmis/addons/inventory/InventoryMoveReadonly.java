package com.lmis.addons.inventory;

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
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.BarcodeQueryListener;
import com.lmis.util.barcode.GoodsInfo;
import com.lmis.util.drawer.DrawerItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-4.
 */
public class InventoryMoveReadonly extends BaseFragment {
    public static final String TAG = "InventoryOutReadonly";

    @InjectView(R.id.txv_title)
    TextView mTxvTitle;

    @InjectView(R.id.txv_bill_date)
    TextView mTxvBillDate;

    @InjectView(R.id.txv_sub_title)
    TextView mTxvSubTitle;

    //已扫描的条码列表
    @InjectView(R.id.lst_view_barcodes)
    ListView mListBarcodes;
    LmisListAdapter mBarcodesAdapter = null;
    List<Object> mBarcodesObjects = null;

    View mView = null;
    Integer mInventoryOutId = null;
    LmisDataRow mInventoryOut = null;

    SearchView mSearchViewBarcodeList;

    String mOpType = null;
    //条码解析器
    BarcodeParser mBarcodeParser;


    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_inventory_move_readonly, container, false);

        ButterKnife.inject(this, mView);
        initData();
        initBarcodesList();
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_inventory_move_readonly, menu);
        mSearchViewBarcodeList = (SearchView) menu.findItem(R.id.menu_inventory_out_readonly_search).getActionView();
        mSearchViewBarcodeList.setOnQueryTextListener(new BarcodeQueryListener(mBarcodesAdapter));
    }

    /**
     * 初始化数据.
     */
    private void initData() {
        Log.d(TAG, "inventory_out#initData");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOpType = bundle.getString("type");
            mInventoryOutId = bundle.getInt("inventory_out_id");
            mInventoryOut = new InventoryMoveDB(scope.context()).select(mInventoryOutId);
            LmisDataRow fromOrg = mInventoryOut.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = mInventoryOut.getM2ORecord("to_org_id").browse();
            mBarcodeParser = new BarcodeParser(scope.context(), mInventoryOutId, fromOrg.getInt("id"), toOrg.getInt("id"), false, mOpType);
        }

    }

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolderForBarcodesList {
        //发货地
        @InjectView(R.id.txv_bill_no)
        TextView txvBillNo;
        //描述信息
        @InjectView(R.id.txv_barcode)
        TextView txvBarcode;

        public ViewHolderForBarcodesList(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private void initBarcodesList() {
        String fromOrgName = mInventoryOut.getM2ORecord("from_org_id").browse().getString("name");
        String toOrgName = mInventoryOut.getM2ORecord("to_org_id").browse().getString("name");

        Integer goodsCount = mInventoryOut.getInt("sum_goods_count");
        Integer billsCount = mInventoryOut.getInt("sum_bills_count");
        String describe = String.format("共%d票%d件", billsCount, goodsCount);
        String billDate = mInventoryOut.getString("bill_date");
        String fromTo = String.format("%s 至 %s", fromOrgName, toOrgName);
        mTxvTitle.setText(fromTo);
        mTxvBillDate.setText(billDate);
        mTxvSubTitle.setText(describe);

        mBarcodesObjects = new ArrayList<Object>(mBarcodeParser.getmScanedBarcode());
        mBarcodesAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_inventory_move_list_barcodes_item, mBarcodesObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolderForBarcodesList holder;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_inventory_move_list_barcodes_item, parent, false);
                    holder = new ViewHolderForBarcodesList(mView);
                    mView.setTag(holder);
                } else {
                    holder = (ViewHolderForBarcodesList) mView.getTag();
                }
                GoodsInfo gs = (GoodsInfo) mBarcodesObjects.get(position);
                String billNo = gs.getmBillNo();
                String barcode = gs.getmBarcode();
                holder.txvBillNo.setText(billNo);
                holder.txvBarcode.setText(barcode);
                if (position > 0) {
                    GoodsInfo prevGs = (GoodsInfo) mBarcodesObjects.get(position - 1);
                    if (prevGs.getmBillNo().equals(billNo))
                        holder.txvBillNo.setText("");
                }
                return mView;
            }

        };
        mListBarcodes.setAdapter(mBarcodesAdapter);
        mListBarcodes.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
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
