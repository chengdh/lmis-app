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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode_scan_header.BarcodeParser;
import com.lmis.util.barcode_scan_header.BarcodeQueryListener;
import com.lmis.util.barcode_scan_header.GoodsInfo;
import com.lmis.util.barcode_scan_header.GoodsInfoChangeEvent;
import com.lmis.util.barcode_scan_header.MultiChoiceBillListener;
import com.lmis.util.barcode_scan_header.ScandedBarcodeChangeEventForScanHeader;
import com.lmis.util.controls.GoodsStatus;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-9-14.
 */
public class FragmentBillList extends BaseFragment implements AdapterView.OnItemLongClickListener {
    public static final String TAG = "FragmentBillList";

    View mView = null;

    @Inject
    Bus mBus;

    //已扫描的票据列表
    @InjectView(R.id.lst_view_bills)
    ListView mLstBills;

    LmisListAdapter mBillsAdapter = null;

    //已扫描的票据列表
    List<Object> mBillsObjects = null;

    //条码解析器
    BarcodeParser mBarcodeParser;

    SearchView mSearchViewBillList = null;

    public BarcodeParser getmBarcodeParser() {
        return mBarcodeParser;
    }

    public void setmBarcodeParser(BarcodeParser mBarcodeParser) {
        this.mBarcodeParser = mBarcodeParser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_scan_header_bill_list, container, false);
        ButterKnife.inject(this, mView);
        initBillsListTab();
        return mView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_scan_header_bill_list, menu);
        mSearchViewBillList = (SearchView) menu.findItem(R.id.menu_scan_header_bill_list_search).getActionView();
        mSearchViewBillList.setOnQueryTextListener(new BarcodeQueryListener(mBillsAdapter));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        return false;
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


    /**
     * 初始化票据列表tab.
     */
    private void initBillsListTab() {
        Log.d(TAG, "FragmentBillList#initBillListTab");
//        if (mBarcodeParser.getmId() > 0) {
//            ScanHeaderDB db = new ScanHeaderDB(scope.context());
//            LmisDataRow scanHeader = db.select(mBarcodeParser.getmId());
//            List<LmisDataRow> lines = scanHeader.getO2MRecord("scan_lines").browseEach();
//            for (LmisDataRow l : lines) {
//                GoodsInfo gs = new GoodsInfo(scope.context());
//                gs.setmBarcode(l.getString("barcode"));
//                gs.setmID(l.getInt("carrying_bill_id"));
//                gs.setmScanedQty(l.getInt("qty"));
//                mBarcodeParser.addGoodsInfo(gs);
//            }
//
//        }
        mBillsObjects = new ArrayList<Object>(mBarcodeParser.getmScanedBarcode());
        mBillsAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_scan_header_list_bills_item, mBillsObjects) {
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
                GoodsInfo gs = (GoodsInfo) mBillsObjects.get(position);
                String billNo = gs.getmBillNo();
                int count = gs.getmScanedQty();
                int goodsStatusType = gs.getmGoodsStatusType();
                String goodsStatusNote = gs.getmGoodsStatusNote();
                holder.txvBillNo.setText(billNo);
                holder.txvGoodsStatusType.setText(GoodsStatus.statusList().get(goodsStatusType));
                holder.txvGoodsStatusNote.setText(goodsStatusNote);
                holder.txvBarcodeCount.setText("已扫" + count + "件");

                return mView;
            }

        };
        mLstBills.setAdapter(mBillsAdapter);
        mLstBills.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mLstBills.setOnItemLongClickListener(this);
        mLstBills.setMultiChoiceModeListener(new MultiChoiceBillListener(scope.context(), mBillsObjects, mBarcodeParser));
    }

    @Subscribe
    public void onGoodsInfoChangedEvent(GoodsInfoChangeEvent evt) {
        //通知billListTab数据变化
        mBillsObjects.clear();
        mBillsObjects.addAll(mBarcodeParser.getmScanedBarcode());
        mBillsAdapter.notifiyDataChange(mBillsObjects);
        //清除选中状态
        mLstBills.clearChoices();
    }

    @Subscribe
    public void onScanedBarcodeChangedEventForScanHeader(ScandedBarcodeChangeEventForScanHeader evt) {
        //通知billListTab数据变化
        mBillsObjects.clear();
        mBillsObjects.addAll(mBarcodeParser.getmScanedBarcode());
        mBillsAdapter.notifiyDataChange(mBillsObjects);
        //清除选中状态
        mLstBills.clearChoices();
    }


    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.unregister(this);
    }
}
