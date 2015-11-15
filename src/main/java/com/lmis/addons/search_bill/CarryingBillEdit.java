package com.lmis.addons.search_bill;

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
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.Lmis;
import com.lmis.MainActivity;
import com.lmis.R;
import com.lmis.addons.carrying_bill.CarryingBillDB;
import com.lmis.addons.carrying_bill.PayType;
import com.lmis.addons.il_config.IlConfigDB;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 9/19/15.
 */
public class CarryingBillEdit extends BaseFragment {
    public static final String TAG = "CarryingBillEdit";
    /**
     * The M view.
     */
    View mView = null;

    /**
     * 发货地.
     */
    @InjectView(R.id.txv_from_org)
    TextView mTxvFromOrg;

    /**
     * 到货地.
     */
    @InjectView(R.id.txv_to_org)
    TextView mTxvToOrg;

    /**
     * 货号.
     */
    @InjectView(R.id.txv_goods_no)
    TextView mTxvGoodsNo;

    /**
     * 票号.
     */
    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

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
     * 数据上传 task.
     */
    UpdateTask mUpdateTask = null;

    /**
     * The M search customer task.
     */
    SearchCustomerTask mSearchCustomerTask = null;

    /**
     * 自运单显示界面传过来的运单对象.
     */
    JSONObject mJsonBill = null;

    LmisDataRow mFromOrg;
    LmisDataRow mToOrg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_bill_form_edit, container, false);
        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    private void init() {
        Bundle args = getArguments();
        if (args == null) {
            Log.d(TAG, "未传入要修改的运单对象!");
            return;
        }
        String json_bill_str = args.getString("json_bill_str");
        if (json_bill_str == null) {
            Log.d(TAG, "未传入要修改的运单对象!");
            return;
        }
        try {
            mJsonBill = new JSONObject(json_bill_str);
            OrgDB orgDB = new OrgDB(scope.context());
            int from_org_id = mJsonBill.getInt("from_org_id");
            int to_org_id = mJsonBill.getInt("to_org_id");
            mFromOrg = orgDB.select(from_org_id);
            mToOrg = orgDB.select(to_org_id);
            mTxvFromOrg.setText(mFromOrg.getString("name"));
            mTxvToOrg.setText(mToOrg.getString("name"));
            mTxvBillNo.setText(mJsonBill.getString("bill_no"));
            mTxvGoodsNo.setText(mJsonBill.getString("goods_no"));
            mEdtFromCustomerName.setText(mJsonBill.getString("from_customer_name"));
            mEdtFromCustomerMobile.setText(mJsonBill.getString("from_customer_mobile"));
            mEdtToCustomerName.setText(mJsonBill.getString("to_customer_name"));
            mEdtToCustomerMobile.setText(mJsonBill.getString("to_customer_mobile"));


            String payType = mJsonBill.getString("pay_type");
            int payTypePosition = PayType.getPayTypeIndex(payType);
            mSpinnerPayType.setSelection(payTypePosition, true);
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
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    reCalToShortCarryingFee();
                    reCalInsuredFee(mFromOrg);

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

            int carryingFee = (int) Double.parseDouble(mJsonBill.getString("carrying_fee"));
            mEdtCarryingFee.setText(carryingFee + "");
            int goodsFee = (int) Double.parseDouble(mJsonBill.getString("goods_fee"));
            mEdtGoodsFee.setText(goodsFee + "");
            int goodsNum = (int) Double.parseDouble(mJsonBill.getString("goods_num"));
            mEdtGoodsNum.setText(goodsNum + "");
            int insuredFee = (int) Double.parseDouble(mJsonBill.getString("insured_fee"));
            mEdtInsuredFee.setText(insuredFee + "");
            int fromShortCarryingFee = (int) Double.parseDouble(mJsonBill.getString("from_short_carrying_fee"));
            mEdtFromShortCarryingFee.setText(fromShortCarryingFee + "");
            int toShortCarryingFee = (int) Double.parseDouble(mJsonBill.getString("to_short_carrying_fee"));
            mEdtToShortCarryingFee.setText(toShortCarryingFee + "");
            String goodsInfo = mJsonBill.getString("goods_info");
            mEdtGoodsInfo.setText(goodsInfo);
            String note = mJsonBill.getString("note");
            mEdtNote.setText(note);

        } catch (Exception ex) {
            Log.d(TAG, "未传入要修改的运单对象!");
            return;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_carrying_bill_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_carrying_bill_edit_upload):
                if (validate()) {
                    mUpdateTask = new UpdateTask();
                    mUpdateTask.execute((Void) null);
                    return true;
                } else
                    return false;
            default:
                return super.onOptionsItemSelected(item);
        }

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
        OrgDB orgDB = new OrgDB(scope.context());
        int toShortCarryingFee = orgDB.getConfigToShortCarryingFee(mToOrg.getInt("id"), carryingFee);
        int oldToShortCarryingFee = parseFee(mEdtToShortCarryingFee);
        if (toShortCarryingFee > oldToShortCarryingFee) {
            mEdtToShortCarryingFee.setText(toShortCarryingFee + "");
        }
        return toShortCarryingFee;
    }

    /**
     * 发货地变化时,重新计算保险费.
     *
     * @return the int
     */
    private int reCalInsuredFee(LmisDataRow curOrg) {
        //计算保险费和到货短途
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

    private int parseFee(EditText edt) {


        String strFee = edt.getText().toString();
        int fee = 0;
        if (strFee != null && strFee.length() > 0) {
            fee = Integer.parseInt(strFee);
        }
        return fee;
    }

    private Boolean validate() {
        Boolean ret = true;
        String fromCustomerName = mEdtFromCustomerName.getText().toString();
        if (fromCustomerName != null && fromCustomerName.length() == 0) {
            mEdtFromCustomerName.setError("发货人不可为空!");
            mEdtFromCustomerName.requestFocus();
            ret = false;
        }
        String fromCustomerMobile = mEdtFromCustomerMobile.getText().toString();
        if (fromCustomerMobile.length() < 11) {
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
        if (toCustomerMobile.length() < 11) {
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

    public JSONObject getUpdateJson() throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("from_customer_name", mEdtFromCustomerName.getText().toString());
        ret.put("from_customer_mobile", mEdtFromCustomerMobile.getText().toString());

        ret.put("to_customer_name", mEdtToCustomerName.getText().toString());
        ret.put("to_customer_mobile", mEdtToCustomerMobile.getText().toString());

        Map.Entry<String, String> payTypeEntry = (Map.Entry<String, String>) mSpinnerPayType.getSelectedItem();
        String payType = payTypeEntry.getKey();
        ret.put("pay_type", payType);

        ret.put("goods_info", mEdtGoodsInfo.getText().toString());
        ret.put("goods_num", mEdtGoodsNum.getText().toString());
        ret.put("carrying_fee", mEdtCarryingFee.getText().toString());
        ret.put("insured_fee", mEdtInsuredFee.getText().toString());
        ret.put("from_short_carrying_fee", mEdtFromShortCarryingFee.getText().toString());
        ret.put("to_short_carrying_fee", mEdtToShortCarryingFee.getText().toString());
        ret.put("goods_fee", mEdtGoodsFee.getText().toString());
        ret.put("note", mEdtNote.getText().toString());

        return ret;
    }


    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    /**
     * 上传运单数据到服务器.
     */
    private class UpdateTask extends AsyncTask<Void, Void, Boolean> {

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
            try {
                ((CarryingBillDB) db()).update2server(mJsonBill.getInt("id"), getUpdateJson());
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
                mUpdateTask.cancel(true);
                Toast.makeText(scope.context(), "更新运单数据成功!", Toast.LENGTH_SHORT).show();
                //返回已处理界面
                try {
                    showCarryingBill();
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            } else {
                Toast.makeText(scope.context(), "更新运单数据失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mUpdateTask = null;
        }
    }

    private void showCarryingBill() throws JSONException {
        SearchBill searchBill = new SearchBill();
        Bundle args = new Bundle();
        args.putString("bill_no", mJsonBill.getString("bill_no"));
        searchBill.setArguments(args);
        ((MainActivity) getActivity()).startMainFragment(searchBill, true);

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
            Lmis instance = db().getLmisInstance();
            try {
                result = instance.callMethod("vip", "find_by_code", args, null);
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
