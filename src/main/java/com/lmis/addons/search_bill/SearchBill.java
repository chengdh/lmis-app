package com.lmis.addons.search_bill;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.Lmis;
import com.lmis.R;
import com.lmis.addons.carrying_bill.CarryingBillDB;
import com.lmis.addons.carrying_bill.CarryingBillPrint;
import com.lmis.addons.carrying_bill.PayType;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.util.drawer.DrawerItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-8-25.
 */
public class SearchBill extends BaseFragment {

    public final static String TAG = "SearchBill";
    public final static Map<String, String> statesMap = new HashMap<String, String>() {
        {
            put("billed", "已开票");
            put("loaded", "已装车");
            put("shipped", "已发货");
            put("reached", "已到货");
            put("distributed", "已分货");
            put("deliveried", "已提货");
            put("settlemented", "已日结");
            put("refunded", "已返款");
            put("refunded_confirmed", "返款已确认");
            put("payment_listed", "准备支付");
            put("paid", "货款已付");
            put("posted", "已过帐");

            //外部中转单
            put("transited", "已中转");

            //内部中转单
            put("transit_reached", "已中转至货场");
            put("transit_shipped", "中转货场已发出");
            put("transit_refunded_confirmed", "中转返款已确认");

            put("invalided", "无效");
            put("canceled", "注销");
        }
    };


    @InjectView(R.id.layout_blank)
    LinearLayout mLayoutBlank;
    @InjectView(R.id.txv_from_org)
    TextView mTxvFromOrg;

    @InjectView(R.id.txv_to_org)
    TextView mTxvToOrg;

    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

    @InjectView(R.id.txv_state_des)
    TextView mTxvStateDes;


    @InjectView(R.id.txv_goods_no)
    TextView mTxvGoodsNo;

    @InjectView(R.id.txv_pay_type)
    TextView mTxvPayType;

    @InjectView(R.id.txv_carrying_fee)
    TextView mTxvCarryingFee;

    @InjectView(R.id.txv_goods_fee)
    TextView mTxvGoodsFee;

    @InjectView(R.id.txv_insured_fee)
    TextView mTxvInsuredFee;

    @InjectView(R.id.txv_from_customer_name)
    TextView mTxvFromCustomerName;

    @InjectView(R.id.txv_from_customer_mobile)
    TextView mTxvFromCustomerMobile;

    @InjectView(R.id.txv_to_customer_name)
    TextView mTxvToCustomerName;

    @InjectView(R.id.txv_to_customer_mobile)
    TextView mTxvToCustomerMobile;

    @InjectView(R.id.txv_from_short_carrying_fee)
    TextView mTxvFromShortCarryingFee;

    @InjectView(R.id.txv_to_short_carrying_fee)
    TextView mTxvToShortCarryingFee;

    @InjectView(R.id.txv_goods_info)
    TextView mTxvGoodsInfo;

    @InjectView(R.id.txv_goods_num)
    TextView mTxvGoodsNum;

    @InjectView(R.id.txv_note)
    TextView mTxvNote;

    View mView = null;

    Menu mMenu;

    //自服务器端传入的运单json对象
    JSONObject mJsonBill = null;

    SearchView mSearchView = null;
    MenuItem mMenuSearch = null;
    MyQueryTextLisener mQueryTextListener = null;

    BillSearcher mSearcher = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_bill_search, container, false);
        ButterKnife.inject(this, mView);
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenu = menu;
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_bill_search, menu);
        mMenuSearch = menu.findItem(R.id.menu_bill_search_search);
        mSearchView = (SearchView) mMenuSearch.getActionView();
        mSearchView.setQueryHint("输入运单号或扫描条码");
        mSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        mQueryTextListener = new MyQueryTextLisener();
        mSearchView.setOnQueryTextListener(mQueryTextListener);
        mMenuSearch.expandActionView();
        init();

    }


    private void init() {
        Bundle args = getArguments();
        if (args != null) {
            String billNo = args.getString("bill_no");
            mSearchView.setQuery(billNo, true);
        }
    }

    /**
     * 显示运单信息.
     */
    private void handleView(JSONObject jsonBill) throws JSONException {

        String fromOrgName = jsonBill.getString("from_org_name");
        mTxvFromOrg.setText(fromOrgName);
        jsonBill.put("from_org_name", fromOrgName);

        String toOrgName = jsonBill.getString("to_org_name");
        mTxvToOrg.setText(toOrgName);
        jsonBill.put("to_org_name", toOrgName);

        String billNo = jsonBill.getString("bill_no");
        mTxvBillNo.setText(billNo);
        String goodsNo = jsonBill.getString("goods_no");
        mTxvGoodsNo.setText(goodsNo);

        String state = jsonBill.getString("state");
        String stateDes = statesMap.get(state);
        mTxvStateDes.setText(stateDes);

        String fromCustomerName = jsonBill.getString("from_customer_name");
        mTxvFromCustomerName.setText(fromCustomerName);
        String fromCustomerMobile = jsonBill.getString("from_customer_mobile");
        mTxvFromCustomerMobile.setText(fromCustomerMobile);
        String toCustomerName = jsonBill.getString("to_customer_name");
        mTxvToCustomerName.setText(toCustomerName);
        String toCustomerMobile = jsonBill.getString("to_customer_mobile");
        mTxvToCustomerMobile.setText(toCustomerMobile);

        String payTypeDes = jsonBill.getString("pay_type_des");
        mTxvPayType.setText(payTypeDes);

        String carryingFee = jsonBill.getString("carrying_fee");
        mTxvCarryingFee.setText(carryingFee);
        String goodsFee = jsonBill.getString("goods_fee");
        mTxvGoodsFee.setText(goodsFee);
        String goodsNum = jsonBill.getString("goods_num");
        mTxvGoodsNum.setText(goodsNum);
        String insuredFee = jsonBill.getString("insured_fee");
        mTxvInsuredFee.setText(insuredFee);
        String fromShortCarryingFee = jsonBill.getString("from_short_carrying_fee");
        mTxvFromShortCarryingFee.setText(fromShortCarryingFee);
        String toShortCarryingFee = jsonBill.getString("to_short_carrying_fee");
        mTxvToShortCarryingFee.setText(toShortCarryingFee + "");
        String goodsInfo = jsonBill.getString("goods_info");
        mTxvGoodsInfo.setText(goodsInfo);
        String note = jsonBill.getString("note");
        mTxvNote.setText(note);
    }

    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
////        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
//
//        Bundle args = new Bundle();
//        args.putInt("no_use", 1);
//        SearchBill fragment = new SearchBill();
//        fragment.setArguments(args);
//        drawerItems.add(new DrawerItem(TAG, "运单查询", 0, R.drawable.ic_action_archive, fragment));
//        return drawerItems;
        return null;
    }

    private class MyQueryTextLisener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String s) {
            if (s != null && s.length() > 0) {
                mSearcher = new BillSearcher(s);
                mSearcher.execute((Void) null);
                MenuItem item = mMenu.findItem(R.id.menu_bill_search_search);
                item.collapseActionView();
                return true;
            } else
                return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    }

    //设置修改 作废菜单是否可见
    private void setMenuItemToggle(boolean show) {
        mMenu.findItem(R.id.menu_bill_edit).setVisible(show);
        mMenu.findItem(R.id.menu_bill_cancel).setVisible(show);
        mMenu.findItem(R.id.menu_bill_print).setVisible(show);
    }

    private void editBill() {
        if (mJsonBill == null)
            return;

        Bundle arg = new Bundle();
        arg.putString("json_bill_str", mJsonBill.toString());
        Fragment frag = new CarryingBillEdit();
        frag.setArguments(arg);
        scope.main().startMainFragment(frag, true);

    }

    private void printBill() {
        if (mJsonBill == null)
            return;

        CarryingBillPrint.printForJson(mJsonBill, scope.currentUser(), true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bill_edit:
                editBill();
                break;
            case R.id.menu_bill_cancel:
                break;
            case R.id.menu_bill_print:
                printBill();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    private class BillSearcher extends AsyncTask<Void, Void, Boolean> {

        LmisDialog dialog;
        String mQueryText = "";
        JSONObject ret = null;

        public BillSearcher(String queryText) {
            mQueryText = queryText;
        }

        @Override
        protected void onPreExecute() {
            dialog = new LmisDialog(getActivity(), false, "正在查找...");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            JSONArray args = new JSONArray();
            args.put(mQueryText);
            Lmis instance = ((CarryingBillDB) databaseHelper(scope.context())).getLmisInstance();
            OrgDB orgDB = new OrgDB(scope.context());
            int defaulOrgID = scope.currentUser().getDefault_org_id();
            List<LmisDataRow> childOrgs = orgDB.getChildrenOrgs(defaulOrgID);
            JSONArray orgIDS = new JSONArray();
            orgIDS.put(defaulOrgID);
            for (LmisDataRow childOrg : childOrgs) {
                orgIDS.put(childOrg.getInt("id"));
            }
            args.put(orgIDS);


            try {
                ret = instance.callMethod("carrying_bill", "find_by_bill_no_and_from_org_id", args, null);

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                try {
                    if (ret.get("result").toString() == "null") {
                        setMenuItemToggle(false);
                        Toast.makeText(scope.context(), "未查到符合条件的运单!", Toast.LENGTH_SHORT).show();
                        mLayoutBlank.setVisibility(View.VISIBLE);
                    } else {
                        try{
                            mJsonBill  = ret.getJSONObject("result");
                        }
                        catch(Exception ex){
                           ex.printStackTrace();
                        }

                        if (mJsonBill.getString("state").equals("billed")) {
                            setMenuItemToggle(true);
                        }
                        mLayoutBlank.setVisibility(View.GONE);
                        handleView(mJsonBill);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            dialog.dismiss();
            mSearcher.cancel(true);
            mSearcher = null;
        }
    }
}
