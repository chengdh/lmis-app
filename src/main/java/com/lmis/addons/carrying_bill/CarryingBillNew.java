package com.lmis.addons.carrying_bill;

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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisValues;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-9.
 */
public class CarryingBillNew extends BaseFragment {

    public static final String TAG = "CarryingBillNew";

    View mView = null;

    @InjectView(R.id.spinner_to_org)
    Spinner mSpinnerToOrg;

    @InjectView(R.id.edt_from_customer_name)
    EditText mEdtFromCustomerName;

    @InjectView(R.id.edt_from_customer_mobile)
    EditText mEdtFromCustomerMobile;

    @InjectView(R.id.edt_to_customer_name)
    EditText mEdtToCustomerName;


    @InjectView(R.id.edt_to_customer_mobile)
    EditText mEdtToCustomerMobile;

    @InjectView(R.id.edt_goods_info)
    EditText mEdtGoodsInfo;


    @InjectView(R.id.spin_pay_type)
    Spinner mSpinnerPayType;

    @InjectView(R.id.edt_carrying_fee)
    EditText mEdtCarryingFee;


    @InjectView(R.id.edt_goods_fee)
    EditText mEdtGoodsFee;


    @InjectView(R.id.edt_goods_num)
    EditText mEdtGoodsNum;


    @InjectView(R.id.edt_insured_fee)
    EditText mEdtInsuredFee;


    @InjectView(R.id.edt_from_short_carrying_fee)
    EditText  mEdtFromShortCarryingFee;


    @InjectView(R.id.edt_to_short_carrying_fee)
    EditText mEdtToShortCarryingFee;


    @InjectView(R.id.edt_note)
    EditText mEdtNote;

    SaveTask mSaveTask = null;
    UploadTask mUploadTask = null;

    /**
     * 当前运单的id.
     */
    int mCarryingBillID = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_bill_form, container, false);
        ButterKnife.inject(this, mView);
        initControls();
        return mView;
    }


    private void initControls() {

    }


    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_carrying_bill_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_carrying_bill_save):
                if (validate()) {
                    mSaveTask = new SaveTask();
                    mSaveTask.execute((Void) null);
                    return true;
                } else
                    return false;
            case (R.id.menu_carrying_bill_upload):
                if (validate()) {
                    mUploadTask = new UploadTask();
                    mUploadTask.execute((Void) null);
                    return true;
                } else
                    return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 验证数据是否符合要求.
     *
     * @return the boolean
     */
    private Boolean validate() {
        Boolean ret = true;
        String fromCustomerName = mEdtFromCustomerName.getText().toString();
        if (fromCustomerName != null && fromCustomerName.length() == 0) {
            mEdtFromCustomerName.setError("发货人不可为空!");
            mEdtFromCustomerName.requestFocus();
            ret = false;
        }
        String fromCustomerMobile = mEdtFromCustomerMobile.getText().toString();
        if (fromCustomerMobile != null && fromCustomerMobile.length() == 0) {
            mEdtFromCustomerMobile.setError("发货人手机不可为空!");
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
        if (toCustomerMobile != null && toCustomerMobile.length() == 0) {
            mEdtToCustomerMobile.setError("收货人手机不可为空!");
            mEdtToCustomerMobile.requestFocus();
            ret = false;
        }

        String goodsInfo = mEdtGoodsInfo.getText().toString();
        if (goodsInfo != null && goodsInfo.length() == 0) {
            mEdtGoodsInfo.setError("货物信息不可为空!");
            mEdtGoodsInfo.requestFocus();
            ret = false;
        }

        return ret;

    }

    private Boolean save2DB() {
        LmisValues vals = new LmisValues();
        LmisUser currentUser = scope.currentUser();
        vals.put("from_org_id", currentUser.getDefault_org_id());
        LmisDataRow toOrg = (LmisDataRow) mSpinnerToOrg.getSelectedItem();
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

        vals.put("note", mEdtNote.getText());
        Map.Entry<String, String> payType = (Map.Entry<String, String>) mSpinnerPayType.getSelectedItem();
        vals.put("pay_type", payType.getKey());

        CarryingBillDB db = (CarryingBillDB) databaseHelper(scope.context());
        if (mCarryingBillID == -1) {
            mCarryingBillID = (int) db.create(vals);
        } else {
            db.update(vals, mCarryingBillID);
        }
        return true;
    }

    private class SaveTask extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在保存数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return save2DB();
        }

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

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在上传数据...");
            pdialog.show();
        }

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
                LmisDataRow bill = ((CarryingBillDB)db()).select(mCarryingBillID);
                CarryingBillPrint.print(bill,false);


            } else {
                Toast.makeText(scope.context(), "上传运单数据失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mUploadTask= null;
        }
    }
}
