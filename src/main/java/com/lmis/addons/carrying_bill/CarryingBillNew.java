package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.lmis.CurrentOrgChangeEvent;
import com.lmis.Lmis;
import com.lmis.R;
import com.lmis.addons.il_config.IlConfigDB;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisValues;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.controls.ExcludeAccessOrgSearchableSpinner;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;
import com.rajasharan.widget.SearchableSpinner;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-8-9.
 */
public class CarryingBillNew extends BaseFragment implements SearchableSpinner.OnSelectionChangeListener {

    /**
     * The constant TAG.
     */
    public static final String TAG = "CarryingBillNew";

    /**
     * The M bus.
     */
    @Inject
    Bus mBus;
    /**
     * The M view.
     */
    View mView = null;

    /**
     * The M spinner to org.
     */
    //@InjectView(R.id.spinner_to_org)
    //Spinner mSpinnerToOrg;

    @InjectView(R.id.search_spinner_to_org)
    ExcludeAccessOrgSearchableSpinner mSearchSpinnerToOrg;

    /**
     * The M edt from customer name.
     */
    @InjectView(R.id.edt_from_customer_name)
    EditText mEdtFromCustomerName;

    /**
     * The M edt from customer mobile.
     */
    @InjectView(R.id.edt_from_customer_mobile)
    EditText mEdtFromCustomerMobile;

    /**
     * The M edt to customer name.
     */
    @InjectView(R.id.edt_to_customer_name)
    EditText mEdtToCustomerName;


    /**
     * The M edt to customer mobile.
     */
    @InjectView(R.id.edt_to_customer_mobile)
    EditText mEdtToCustomerMobile;

    /**
     * The M edt goods info.
     */
    @InjectView(R.id.edt_goods_info)
    EditText mEdtGoodsInfo;


    /**
     * The M spinner pay type.
     */
    @InjectView(R.id.spin_pay_type)
    Spinner mSpinnerPayType;

    /**
     * The M edt carrying fee.
     */
    @InjectView(R.id.edt_carrying_fee)
    EditText mEdtCarryingFee;


    /**
     * The M edt goods fee.
     */
    @InjectView(R.id.edt_goods_fee)
    EditText mEdtGoodsFee;


    /**
     * The M edt goods num.
     */
    @InjectView(R.id.edt_goods_num)
    EditText mEdtGoodsNum;


    /**
     * The M edt insured fee.
     */
    @InjectView(R.id.edt_insured_fee)
    EditText mEdtInsuredFee;


    /**
     * The M edt from short carrying fee.
     */
    @InjectView(R.id.edt_from_short_carrying_fee)
    EditText mEdtFromShortCarryingFee;


    /**
     * The M edt to short carrying fee.
     */
    @InjectView(R.id.edt_to_short_carrying_fee)
    EditText mEdtToShortCarryingFee;


    /**
     * The M edt note.
     */
    @InjectView(R.id.edt_note)
    EditText mEdtNote;

    /**
     * The M edt customer no.
     */
    @InjectView(R.id.edt_customer_no)
    EditText mEdtCustomerNo;

    /**
     * The M btn search customer.
     */
    @InjectView(R.id.btn_search_customer)
    ImageButton mBtnSearchCustomer;

    /**
     * The M btn remove customer.
     */
    @InjectView(R.id.btn_remove_customer)
    ImageButton mBtnRemoveCustomer;

    /**
     * The M edt customer iD.
     */
    @InjectView(R.id.edt_customer_id)
    EditText mEdtCustomerID;

    /**
     * 数据保存处理.
     */
    SaveTask mSaveTask = null;
    /**
     * 数据上传 task.
     */
    UploadTask mUploadTask = null;

    /**
     * The M search customer task.
     */
    SearchCustomerTask mSearchCustomerTask = null;

    /**
     * 当前运单的id.
     */
    int mCarryingBillID = -1;
    LmisDataRow mCurOrg;

    /**
     * On create view.
     *
     * @param inflater           the inflater
     * @param container          the container
     * @param savedInstanceState the saved instance state
     * @return the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_bill_form, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        initControls();
        return mView;
    }


    /**
     * Init controls.
     */
    private void initControls() {
        LmisUser currentUser = scope.currentUser();
        Integer default_org_id = currentUser.getDefault_org_id();
        OrgDB orgDB = new OrgDB(scope.context());
        mCurOrg = orgDB.select(default_org_id);

        mEdtInsuredFee.setEnabled(false);
        mEdtCustomerID.setVisibility(View.GONE);
        mSearchSpinnerToOrg.setOnSelectionChangeListener(this);
        ;
        mSpinnerPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                reCalToShortCarryingFee();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mEdtCarryingFee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                reCalToShortCarryingFee();
                //重新计算保险费
                reCalInsuredFee(mSearchSpinnerToOrg.getSelectedOrg());
            }
        });


        mBtnSearchCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customerCode = mEdtCustomerNo.getText().toString();
                if (customerCode != null && customerCode.length() > 0) {
                    mSearchCustomerTask = new SearchCustomerTask(customerCode);
                    mSearchCustomerTask.execute((Void) null);
                } else {
                    Toast.makeText(scope.context(), "请输入客户编号!", Toast.LENGTH_SHORT).show();
                    mEdtCustomerNo.requestFocus();
                }

            }
        });
        mBtnRemoveCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtCustomerNo.setText("");
                mEdtFromCustomerName.setText("");
                mEdtFromCustomerMobile.setText("");
                mEdtCustomerID.setText("");
                mEdtCustomerNo.setEnabled(true);
                mEdtFromCustomerName.setEnabled(true);
                mEdtFromCustomerMobile.setEnabled(true);
                mEdtCustomerNo.requestFocus();
            }
        });
        //计算初始保险费
        reCalInsuredFee(mCurOrg);
    }


    /**
     * Database helper.
     *
     * @param context the context
     * @return the object
     */
    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    /**
     * Drawer menus.
     *
     * @param context the context
     * @return the list
     */
    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    /**
     * On create options menu.
     *
     * @param menu     the menu
     * @param inflater the inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_carrying_bill_new, menu);
    }

    /**
     * On options item selected.
     *
     * @param item the item
     * @return the boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;
        switch (item.getItemId()) {
            case (R.id.menu_carrying_bill_save):
                if (validate()) {
                    mSaveTask = new SaveTask();
                    mSaveTask.execute((Void) null);
                    ret = true;
                } else
                    ret = false;
                break;
            case (R.id.menu_carrying_bill_upload):
                if (validate()) {
                    mUploadTask = new UploadTask();
                    mUploadTask.execute((Void) null);
                    ret = true;
                } else
                    ret = false;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return ret;
    }

    /**
     * 验证数据是否符合要求.
     *
     * @return the boolean
     */
    private Boolean validate() {
        Boolean ret = true;
//        String customerID = mEdtCustomerID.getText().toString();
//        if (customerID != null && customerID.length() == 0) {
//            mEdtCustomerNo.setError("客户编号不可为空!");
//            mEdtCustomerNo.requestFocus();
//            ret = false;
//        }
        String fromCustomerName = mEdtFromCustomerName.getText().toString();
        if (fromCustomerName != null && fromCustomerName.length() == 0) {
            mEdtFromCustomerName.setError("发货人不可为空!");
            mEdtFromCustomerName.requestFocus();
            ret = false;
        }
        String fromCustomerMobile = mEdtFromCustomerMobile.getText().toString();
        if (fromCustomerMobile.length() != 11) {
            mEdtFromCustomerMobile.setError("发货人手机不正确!");
            mEdtFromCustomerMobile.requestFocus();
            ret = false;
        }
        String toCustomerName = mEdtToCustomerName.getText().toString();
        if (toCustomerName != null && toCustomerName.length() == 0) {
            mEdtToCustomerName.setError("收货人不可为空!");
            mEdtToCustomerName.requestFocus();
            ret = false;
        }
        String toCustomerMobile = mEdtToCustomerMobile.getText().toString();
        if (toCustomerMobile.length() != 11) {
            mEdtToCustomerMobile.setError("收货人手机不正确!");
            mEdtToCustomerMobile.requestFocus();
            ret = false;
        }

        String goodsNum = mEdtGoodsNum.getText().toString();
        if (goodsNum != null && goodsNum.length() == 0) {
            mEdtGoodsNum.setError("货物件数不可为空!");
            mEdtGoodsNum.requestFocus();
            ret = false;
        }
        String goodsInfo = mEdtGoodsInfo.getText().toString();
        if (goodsInfo != null && goodsInfo.length() == 0) {
            mEdtGoodsInfo.setError("货物信息不可为空!");
            mEdtGoodsInfo.requestFocus();
            ret = false;
        }
        String carryingFee = mEdtCarryingFee.getText().toString();
        if (carryingFee != null && carryingFee.length() == 0) {
            mEdtCarryingFee.setError("运费要大于0!");
            mEdtToCustomerMobile.requestFocus();
            ret = false;
        }
        String goodsFee = mEdtGoodsFee.getText().toString();
        if (goodsFee != null && goodsFee.length() == 0) {
            mEdtGoodsFee.setError("代收货款要输入数字!");
            mEdtGoodsFee.setText("0");
        }

        String fromShortCarryingFee = mEdtFromShortCarryingFee.getText().toString();
        if (fromShortCarryingFee != null && fromShortCarryingFee.length() == 0) {
            mEdtFromShortCarryingFee.setError("接货费要输入数字!");
            mEdtGoodsFee.setText("0");
        }

        String toShortCarryingFee = mEdtToShortCarryingFee.getText().toString();
        if (toShortCarryingFee != null && toShortCarryingFee.length() == 0) {
            mEdtToShortCarryingFee.setError("送货费要输入数字!");
            mEdtToShortCarryingFee.setText("0");
        }
        return ret;

    }

    /**
     * Save 2 dB.
     *
     * @return the boolean
     */
    private Boolean save2DB() {
        LmisValues vals = new LmisValues();
        LmisUser currentUser = scope.currentUser();
        vals.put("from_org_id", currentUser.getDefault_org_id());
//        LmisDataRow toOrg = (LmisDataRow) mSpinnerToOrg.getSelectedItem();
        LmisDataRow toOrg = mSearchSpinnerToOrg.getSelectedOrg();
        vals.put("to_org_id", toOrg.getInt("id"));
        vals.put("from_customer_name", mEdtFromCustomerName.getText());
        vals.put("from_customer_mobile", mEdtFromCustomerMobile.getText());

        vals.put("to_customer_name", mEdtToCustomerName.getText());
        vals.put("to_customer_mobile", mEdtToCustomerMobile.getText());


        vals.put("goods_info", mEdtGoodsInfo.getText());
        vals.put("goods_num", mEdtGoodsNum.getText());

        vals.put("carrying_fee", mEdtCarryingFee.getText());
        vals.put("goods_fee", mEdtGoodsFee.getText());

        vals.put("insured_fee", mEdtInsuredFee.getText());

        vals.put("from_short_carrying_fee", mEdtFromShortCarryingFee.getText());
        vals.put("to_short_carrying_fee", mEdtToShortCarryingFee.getText());

        String fromCustomerID = mEdtCustomerID.getText().toString();
        String fromCustomerCode = mEdtCustomerNo.getText().toString();
        if (fromCustomerID != null && fromCustomerID.length() > 0) {
            vals.put("from_customer_id", fromCustomerID);
            vals.put("from_customer_code", fromCustomerCode);
        }

        vals.put("note", mEdtNote.getText() + "[手机开票]");
        Map.Entry<String, String> payType = (Map.Entry<String, String>) mSpinnerPayType.getSelectedItem();
        vals.put("pay_type", payType.getKey());

        vals.put("user_id", scope.currentUser().getUser_id());

        CarryingBillDB db = (CarryingBillDB) databaseHelper(scope.context());
        if (mCarryingBillID == -1) {
            mCarryingBillID = (int) db.create(vals);
        } else {
            db.update(vals, mCarryingBillID);
        }
        return true;
    }


    /**
     * Parse fee.
     *
     * @param edt the edt
     * @return the int
     */
    private int parseFee(EditText edt) {

        String strFee = edt.getText().toString();
        int fee = 0;
        if (strFee != null && strFee.length() > 0) {
            fee = Integer.parseInt(strFee);
        }
        return fee;
    }

    /**
     * 根据系统设置计算到货短途.
     *
     * @return the int
     */
    private int reCalToShortCarryingFee() {
        Map.Entry<String, String> payTypeEntry = (Map.Entry<String, String>) mSpinnerPayType.getSelectedItem();
        String payType = payTypeEntry.getKey();
        //如果不是提货付,则不产生到货短途
        if (!payType.equals(PayType.PAY_TYPE_TH)) {
            mEdtToShortCarryingFee.setText("0");
            return 0;
        }
        int carryingFee = parseFee(mEdtCarryingFee);
        LmisDataRow toOrg = mSearchSpinnerToOrg.getSelectedOrg();
        OrgDB orgDB = new OrgDB(scope.context());
        int toShortCarryingFee = orgDB.getConfigToShortCarryingFee(toOrg.getInt("id"), carryingFee);
        mEdtToShortCarryingFee.setText(toShortCarryingFee + "");
        return toShortCarryingFee;
    }

    /**
     * 发货地变化时,重新计算保险费.
     *
     * @return the int
     */
    private int reCalInsuredFee(LmisDataRow curOrg) {
        OrgDB orgDB = new OrgDB(scope.context());
        IlConfigDB configDB = new IlConfigDB(scope.context());
        int setInsuredFee = 0;
        int carryingFeeGetOnInsuredFee = orgDB.getCarryingFeeGteOnInsuredFee(curOrg.getInt("id"));
        int configInsuredFee = configDB.getInsuredFee();
        int carryingFee = parseFee(mEdtCarryingFee);
        if (carryingFeeGetOnInsuredFee == 0 || carryingFee >= carryingFeeGetOnInsuredFee) {
            setInsuredFee = configInsuredFee;
        }
        mEdtInsuredFee.setText(setInsuredFee + "");

        return setInsuredFee;
    }

    /**
     * 发货地变化时的，重新计算保价费.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onCurOrgChangeEvent(CurrentOrgChangeEvent evt) {
        mCurOrg = evt.getmOrg();
        //reCalInsuredFee(mCurOrg);
    }


    @Override
    public void onSelectionChanged(String s) {
        reCalToShortCarryingFee();
        reCalInsuredFee(mSearchSpinnerToOrg.getSelectedOrg());
    }


    /**
     * The type Save task.
     */
    private class SaveTask extends AsyncTask<Void, Void, Boolean> {

        /**
         * The Pdialog.
         */
        LmisDialog pdialog;

        /**
         * On pre execute.
         */
        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在保存数据...");
            pdialog.show();
        }

        /**
         * Do in background.
         *
         * @param voids the voids
         * @return the boolean
         */
        @Override
        protected Boolean doInBackground(Void... voids) {
            return save2DB();
        }

        /**
         * On post execute.
         *
         * @param success the success
         */
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mSaveTask.cancel(true);
                Toast.makeText(scope.context(), "保存运单成功!", Toast.LENGTH_SHORT).show();
                DrawerListener drawer = scope.main();
                drawer.refreshDrawer(CarryingBillList.TAG);

            } else {
                Toast.makeText(scope.context(), "保存运单失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mSaveTask = null;
        }
    }

    /**
     * 上传运单数据到服务器.
     */
    private class UploadTask extends AsyncTask<Void, Void, Boolean> {

        /**
         * The Pdialog.
         */
        LmisDialog pdialog;

        /**
         * On pre execute.
         */
        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在上传数据...");
            pdialog.show();
        }

        /**
         * Do in background.
         *
         * @param voids the voids
         * @return the boolean
         */
        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean ret = true;
            ret = save2DB();
            try {
                ((CarryingBillDB) db()).save2server(mCarryingBillID);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ret = false;
            }
            return ret;
        }

        /**
         * On post execute.
         *
         * @param success the success
         */
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mUploadTask.cancel(true);
                Toast.makeText(scope.context(), "上传运单数据成功!", Toast.LENGTH_SHORT).show();
                DrawerListener drawer = scope.main();
                drawer.refreshDrawer(CarryingBillList.TAG);
                //返回已处理界面
                CarryingBillList list = new CarryingBillList();
                Bundle arg = new Bundle();
                arg.putString("type", "processed");
                list.setArguments(arg);
                scope.main().startMainFragment(list, true);
                //打印小票
                LmisDataRow bill = db().select(mCarryingBillID);
                CarryingBillPrint.print(bill, scope.currentUser(), false);


            } else {
                Toast.makeText(scope.context(), "上传运单数据失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mUploadTask = null;
        }
    }

    /**
     * 客户资料查询task.
     */
    private class SearchCustomerTask extends AsyncTask<Void, Void, Boolean> {

        /**
         * The Pdialog.
         */
        LmisDialog pdialog;
        /**
         * The M vip code.
         */
        String mVipCode = "";
        /**
         * The Result.
         */
        JSONObject result = null;

        /**
         * Instantiates a new Search customer task.
         *
         * @param vipCode the vip code
         */
        private SearchCustomerTask(String vipCode) {
            mVipCode = vipCode;
        }

        /**
         * On pre execute.
         */
        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "查询中...");
            pdialog.show();
        }

        /**
         * Do in background.
         *
         * @param voids the voids
         * @return the boolean
         */
        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean ret = true;
            JSONArray args = new JSONArray();
            args.put(mVipCode);
            args.put("audited");
            Lmis instance = db().getLmisInstance();
            try {
                result = instance.callMethod("vip", "find_by_code_and_vip_state", args, null);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ret = false;
            }
            return ret;
        }

        /**
         * On post execute.
         *
         * @param success the success
         */
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mSearchCustomerTask.cancel(true);
                try {
                    if (result.get("result").toString() == "null") {
                        Toast.makeText(scope.context(), "未查到客户信息!", Toast.LENGTH_SHORT).show();
                    } else {
                        JSONObject customer = result.getJSONObject("result");
                        mEdtFromCustomerName.setText(customer.getString("name"));
                        mEdtFromCustomerMobile.setText(customer.getString("mobile"));
                        mEdtCustomerID.setText(customer.getInt("id") + "");
                        mEdtCustomerNo.setEnabled(false);
                        mEdtFromCustomerName.setEnabled(false);
                        mEdtFromCustomerMobile.setEnabled(false);
                        Toast.makeText(scope.context(), "已查到客户信息!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(scope.context(), "查找客户资料失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mSearchCustomerTask = null;
        }
    }
}
