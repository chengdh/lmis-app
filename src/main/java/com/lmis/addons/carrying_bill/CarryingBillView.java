package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-19.
 */
public class CarryingBillView extends BaseFragment {

    public final static String TAG = "CarryingBillView";

    @InjectView(R.id.txv_from_org)
    TextView mTxvFromOrg;

    @InjectView(R.id.txv_to_org)
    TextView mTxvToOrg;

    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

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

    int mCarryingBillId = -1;
    LmisDataRow mCarryingBill = null;


    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_carrying_bill_view, container, false);

        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    private void init() {
        Log.d(TAG, "CarryingBillView#init");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCarryingBillId = bundle.getInt("carrying_bill_id");
            mCarryingBill = new CarryingBillDB(scope.context()).select(mCarryingBillId);
            LmisDataRow fromOrg = mCarryingBill.getM2ORecord("from_org_id").browse();
            mTxvFromOrg.setText(fromOrg.getString("name"));

            LmisDataRow toOrg = mCarryingBill.getM2ORecord("to_org_id").browse();
            mTxvToOrg.setText(toOrg.getString("name"));

            String billNo = mCarryingBill.getString("bill_no");
            mTxvBillNo.setText(billNo);
            String goodsNo = mCarryingBill.getString("goods_no");
            mTxvGoodsNo.setText(goodsNo);
            //String billDate = mCarryingBill.getString("bill_date");
            //mTxvBillDate.setText(billDate);
            String fromCustomerName = mCarryingBill.getString("from_customer_name");
            mTxvFromCustomerName.setText(fromCustomerName);
            String fromCustomerMobile = mCarryingBill.getString("from_customer_mobile");
            mTxvFromCustomerMobile.setText(fromCustomerMobile);
            String toCustomerName = mCarryingBill.getString("to_customer_name");
            mTxvToCustomerName.setText(toCustomerName);
            String toCustomerMobile = mCarryingBill.getString("to_customer_mobile");
            mTxvToCustomerMobile.setText(toCustomerMobile);

            mTxvFromCustomerMobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:+" + ((TextView) view).getText().toString().trim()));
                    startActivity(callIntent);

                }
            });

            mTxvToCustomerMobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:+" + ((TextView) view).getText().toString().trim()));
                    startActivity(callIntent);

                }
            });



            String payType = mCarryingBill.getString("pay_type");
            String payTypeDes = PayType.payTypes().get(payType);
            mTxvPayType.setText(payTypeDes);

            int carryingFee = mCarryingBill.getInt("carrying_fee");
            mTxvCarryingFee.setText(carryingFee + "");
            int goodsFee = mCarryingBill.getInt("goods_fee");
            mTxvGoodsFee.setText(goodsFee + "");
            int goodsNum = mCarryingBill.getInt("goods_num");
            mTxvGoodsNum.setText(goodsNum + "");
            int insuredFee = mCarryingBill.getInt("insured_fee");
            mTxvInsuredFee.setText(insuredFee + "");
            int fromShortCarryingFee = mCarryingBill.getInt("from_short_carrying_fee");
            mTxvFromShortCarryingFee.setText(fromShortCarryingFee + "");
            int toShortCarryingFee = mCarryingBill.getInt("to_short_carrying_fee");
            mTxvToShortCarryingFee.setText(toShortCarryingFee + "");
            String goodsInfo = mCarryingBill.getString("goods_info");
            mTxvGoodsInfo.setText(goodsInfo);
            String note = mCarryingBill.getString("note");
            mTxvNote.setText(note);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_carrying_bill_view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_carrying_bill_print):
                CarryingBillPrint.print(mCarryingBill,scope.currentUser(), true);
                break;
            case (R.id.menu_carrying_bill_print_barcode):
                CarryingBillPrint.testPrintBarcode();
                break;
            case (R.id.menu_carrying_bill_print_label):
                LabelPrint.print(mCarryingBill);
                break;

            default:
                super.onOptionsItemSelected(item);
                break;
        }
        return true;
    }

}
