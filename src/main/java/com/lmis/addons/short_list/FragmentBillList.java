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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lmis.Lmis;
import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode_scan_header.BarcodeQueryListener;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-9-14.
 */
public class FragmentBillList extends BaseFragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    public static final String TAG = "FragmentBillList";

    View mView = null;

    @Inject
    Bus mBus;

    //已扫描的票据列表
    @InjectView(R.id.lst_view_bills)
    PullToRefreshListView mLstBills;

    LmisListAdapter mBillsAdapter = null;

    //状态为草稿的票据列表
    List<Object> mBillsObjects = new ArrayList<>();

    //
    List<Object> mSelectedBillsObjects = new ArrayList<>();

    BillsLoader mLoader = null;

    LmisDataRow mShortList = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_short_list_new_bill_list, container, false);
        ButterKnife.inject(this, mView);
        initBillsList();
        return mView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_short_list_new_bill_list, menu);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d(TAG,"onItemClick");
        return false;
    }

    //点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject row = (JSONObject) mLstBills.getRefreshableView().getItemAtPosition(position);

    }

    public LmisDataRow getmShortList() {
        return mShortList;
    }

    public void setmShortList(LmisDataRow mShortList) {
        this.mShortList = mShortList;
    }

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolderForBillsList {
        public CheckBox getCbxSelectBill() {
            return cbxSelectBill;
        }

        public void setCbxSelectBill(CheckBox cbxSelectBill) {
            this.cbxSelectBill = cbxSelectBill;
        }

        @InjectView(R.id.cbx_select_bill)
        CheckBox cbxSelectBill;
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

        @InjectView(R.id.txv_carrying_fee_total)
        TextView txvCarryingFeeTotal;



        public ViewHolderForBillsList(View view) {
            ButterKnife.inject(this, view);
        }
    }


    /**
     * 初始化票据列表.
     */
    private void initBillsList() {
        Log.d(TAG, "FragmentBillList#initBillList");
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


                final JSONObject bill = (JSONObject) mBillsObjects.get(position);
                try {
                    String billNo = bill.getString("bill_no");
                    String goodsNum = bill.getString("goods_num");
                    String goodsInfo = bill.getString("goods_info");
                    String fromOrg = bill.getString("from_org_name");
                    String toOrg = bill.getString("to_org_name");
                    String carryingFeeTotal = bill.getString("carrying_fee_total");

                    String fromTo = String.format("%s->%s", fromOrg, toOrg);

                    holder.txvBillNo.setText(billNo);
                    holder.txvFromTo.setText(fromTo);
                    holder.txvGoodsInfo.setText(goodsInfo);
                    holder.txvGoodsNum.setText(goodsNum);
                    holder.txvCarryingFeeTotal.setText(carryingFeeTotal);
                }
                catch(Exception ex){
                    Log.d(TAG,ex.toString());
                }

                //设置选择框事件
                holder.getCbxSelectBill().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cbx = (CheckBox)v;
                        if(cbx.isChecked()){
                            mSelectedBillsObjects.add(bill);

                        }
                        else{
                            mSelectedBillsObjects.remove(bill);
                        }
                        mShortList.put("bills",mSelectedBillsObjects);
                    }
                });

                return mView;
            }

        };
        mLstBills.setAdapter(mBillsAdapter);
        mLstBills.getRefreshableView().setOnItemClickListener(this);
        mLoader = new BillsLoader();
        mLoader.execute((Void) null);

//        mLstBills.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
//        mLstBills.setMultiChoiceModeListener(new MultiChoiceBillListener(scope.context(), mBillsObjects, mBarcodeParser));
        mLstBills.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
                if(mLoader != null){
                    mLoader.cancel(true);
                    mLoader = null;
                }

                mLoader = new BillsLoader();
                mLoader.execute((Void) null);
            }
        });
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new ShortListDB(context);
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

    //全选/不选所有单据
    private void selectAllBills(boolean checked){
        mSelectedBillsObjects.clear();
        for (int i = 0;i<mBillsObjects.size();i++){
            View itemView = mLstBills.getRefreshableView().getChildAt(i);
            CheckBox cbx = (CheckBox) itemView.findViewById(R.id.cbx_select_bill);
            cbx.setChecked(checked);
            if(checked){
                mSelectedBillsObjects.add(mBillsObjects.get(i));
            }
        }

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//         switch (item.getItemId()) {
//            case (R.id.menu_short_list_new_select_all):
//                Log.d(TAG, "switch Menu select");
//                selectAllBills(true);
//                return true;
//             case (R.id.menu_short_list_new_select_none):
//                Log.d(TAG, "switch Menu select");
//                selectAllBills(false);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    /**
     * 上传数据.
     */
    private class BillsLoader extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "获取数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                getBillsFromServer();

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            }
            return true;
        }

        private void getBillsFromServer() throws JSONException, IOException {

            LmisUser currentUser = scope.currentUser();
            JSONArray args = new JSONArray();
            args.put(currentUser.getDefault_org_id());
            Lmis instance = db().getLmisInstance();

            JSONObject result = instance.callMethod("ComputerBill", "waitting_short_list_load", args, null);
            JSONArray bills = result.getJSONArray("result");
            mBillsObjects.clear();
            mSelectedBillsObjects.clear();

            for (int i = 0; i < bills.length(); i++) {
                mBillsObjects.add(bills.get(i));
                mSelectedBillsObjects.add(bills.get(i));
            }
            mShortList.put("bills",mSelectedBillsObjects);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mLoader.cancel(true);
                Toast.makeText(scope.context(), "获取数据成功!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(scope.context(), "获取数据失败!", Toast.LENGTH_SHORT).show();
            }
            mBillsAdapter.notifiyDataChange(mBillsObjects);
            mLstBills.onRefreshComplete();
            pdialog.dismiss();
        }

    }
}
