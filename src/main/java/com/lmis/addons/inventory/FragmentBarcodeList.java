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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.BarcodeQueryListener;
import com.lmis.util.barcode.GoodsInfo;
import com.lmis.util.barcode.GoodsInfoAddSuccessEvent;
import com.lmis.util.barcode.MultiChoiceBarcodeListener;
import com.lmis.util.barcode.ScandedBarcodeChangeEvent;
import com.lmis.util.barcode.ScandedBarcodeConfirmChangeEvent;
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
public class FragmentBarcodeList extends BaseFragment implements AdapterView.OnItemLongClickListener {
    public static final String TAG = "FragmentBarcoeList";

    @Inject
    Bus mBus;

    //已扫描的条码列表
    @InjectView(R.id.lst_view_barcodes)
    ListView mListBarcodes;

    LmisListAdapter mBarcodesAdapter = null;
    List<Object> mBarcodesObjects = null;
    View mView = null;

    BarcodeParser mBarcodeParser = null;

    SearchView mSearch = null;

    public BarcodeParser getmBarcodeParser() {
        return mBarcodeParser;
    }

    public void setmBarcodeParser(BarcodeParser mBarcodeParser) {
        this.mBarcodeParser = mBarcodeParser;
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

        @InjectView(R.id.img_state)
        ImageView imgState;

        public ViewHolderForBarcodesList(View view) {
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_inventory_out_barcode_list, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        initBarcodesListTab();
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_inventory_move_barcode_list, menu);
        mSearch = (SearchView) menu.findItem(R.id.menu_inventory_out_barcode_list_search).getActionView();
        mSearch.setOnQueryTextListener(new BarcodeQueryListener(mBarcodesAdapter));

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 初始化条码列表.
     */
    private void initBarcodesListTab() {
        Log.d(TAG, "FragmentBarcodeList#initBarcodesListTab");
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
                if(gs.getmState().equals("draft")){
                    holder.imgState.setVisibility(View.GONE);
                }
                else
                {
                    holder.imgState.setVisibility(View.VISIBLE);
                }
                if (position > 0) {
                    GoodsInfo prevGs = (GoodsInfo) mBarcodesObjects.get(position - 1);
                    if (prevGs.getmBillNo().equals(billNo))
                        holder.txvBillNo.setText("");
                }
                return mView;
            }

        };
        mListBarcodes.setAdapter(mBarcodesAdapter);
        mListBarcodes.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListBarcodes.setOnItemLongClickListener(this);
        mListBarcodes.setMultiChoiceModeListener(new MultiChoiceBarcodeListener(scope.context(), mBarcodesObjects, mBarcodeParser));
    }

    @Subscribe
    public void onScanedBarcodeChangedEvent(ScandedBarcodeChangeEvent evt) {
        //通知barcodesListTab数据变化
        mBarcodesObjects.clear();
        mBarcodesObjects.addAll(mBarcodeParser.getmScanedBarcode());
        mBarcodesAdapter.notifiyDataChange(mBarcodesObjects);
        //清除选中状态
        mListBarcodes.clearChoices();
    }

    @Subscribe
    public void onScanedConfirmBarcodeChangedEvent(ScandedBarcodeConfirmChangeEvent evt) {
        //通知barcodesListTab数据变化
        mBarcodesObjects.clear();
        mBarcodesObjects.addAll(mBarcodeParser.getmScanedBarcode());
        mBarcodesAdapter.notifiyDataChange(mBarcodesObjects);
        //清除选中状态
        mListBarcodes.clearChoices();
    }


    @Subscribe
    public void onGoodsInfoConfirmSuccessEvent(GoodsInfoAddSuccessEvent evt){

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
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }
}
