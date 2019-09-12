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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.BarcodeQueryListener;
import com.lmis.util.barcode.MultiChoiceBillListener;
import com.lmis.util.barcode.ScandedBarcodeChangeEvent;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        mView = inflater.inflate(R.layout.fragment_inventory_move_bill_list, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        initBillsListTab();
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_inventory_move_bill_list, menu);
        mSearchViewBillList = (SearchView) menu.findItem(R.id.menu_inventory_out_bill_list_search).getActionView();
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

        public ViewHolderForBillsList(View view) {
            ButterKnife.inject(this, view);
        }
    }


    /**
     * 初始化票据列表tab.
     */
    private void initBillsListTab() {
        Log.d(TAG, "FragmentBillList#initBillListTab");
        mBillsObjects = new ArrayList<Object>(mBarcodeParser.getBillsList());
        mBillsAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_inventory_move_list_bills_item, mBillsObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolderForBillsList holder;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_inventory_move_list_bills_item, parent, false);
                    holder = new ViewHolderForBillsList(mView);
                    mView.setTag(holder);
                } else {
                    holder = (ViewHolderForBillsList) mView.getTag();
                }
                Map.Entry billHash = (Map.Entry) mBillsObjects.get(position);
                String billNo = billHash.getKey().toString();
                String count = billHash.getValue().toString();
                holder.txvBillNo.setText(billNo);
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
    public void onScanedBarcodeChangedEvent(ScandedBarcodeChangeEvent evt) {
        //通知billListTab数据变化
        mBillsObjects.clear();
        mBillsObjects.addAll(mBarcodeParser.getBillsList());
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

}
