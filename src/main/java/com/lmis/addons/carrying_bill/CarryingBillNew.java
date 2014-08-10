package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-9.
 */
public class CarryingBillNew extends BaseFragment {

    public static final String TAG = "CarryingBillNew";

    View mView = null;

    @InjectView(R.id.tabHost)
    TabHost mTabHost;

    @InjectView(R.id.edt_from_customer_name)
    EditText mEdtFromCustomerName;

    @InjectView(R.id.edt_from_customer_mobile)
    EditText mEdtFromCustomerMobile;

    @InjectView(R.id.edt_to_customer_name)
    EditText mEdtToCustomerName;


    @InjectView(R.id.edt_to_customer_mobile)
    EditText mEdtToCustomerMobile;

    @InjectView(R.id.spin_pay_type)
    Spinner mSpinnerPayType;

    @InjectView(R.id.seek_bar_carrying_fee)
    SeekBar mSeekBarCarryingFee;

    @InjectView(R.id.txv_carrying_fee_disp)
    TextView mTxvCarryingFeeDisp;


    @InjectView(R.id.seek_bar_goods_fee)
    SeekBar mSeekBarGoodsFee;

    @InjectView(R.id.txv_goods_fee_disp)
    TextView mTxvGoodsFeeDisp;


    @InjectView(R.id.number_picker_goods_num)
    NumberPicker mNumberPickerGoodsNum;


    @InjectView(R.id.seek_bar_insured_fee)
    SeekBar mSeekBarInsuredFee;

    @InjectView(R.id.txv_insured_fee_disp)
    TextView mTxvInsuredFeeDisp;


    @InjectView(R.id.seek_bar_manage_fee)
    SeekBar mSeekBarManageFee;


    @InjectView(R.id.seek_bar_from_short_carrying_fee)
    SeekBar mSeekBarFromShortCarryingFee;

    @InjectView(R.id.txv_from_short_carrying_fee_disp)
    TextView mTxvFromShortCarryingFeeDisp;


    @InjectView(R.id.seek_bar_to_short_carrying_fee)
    SeekBar mSeekBarToShortCarryingFee;

    @InjectView(R.id.txv_to_short_carrying_fee_disp)
    TextView mTxvToShortCarryingFeeDisp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_carrying_bill_form, container, false);
        ButterKnife.inject(this, mView);
        initControls();
        return mView;
    }
    private void initControls(){
        initTabs();
        mSeekBarCarryingFee.setMax(500);
        mSeekBarCarryingFee.setProgress(30);

        mSeekBarCarryingFee.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxvCarryingFeeDisp.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarGoodsFee.setMax(10000);
        mSeekBarGoodsFee.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxvGoodsFeeDisp.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBarCarryingFee.setProgress(0);


        mNumberPickerGoodsNum.setMinValue(1);
        mNumberPickerGoodsNum.setMaxValue(1000);
        mNumberPickerGoodsNum.setValue(1);

    }

    private void initTabs() {
        mTabHost.setup();
        //TODO 此处加上tab图标
        mTabHost.addTab(mTabHost.newTabSpec("TAB_BILL_FORM").setIndicator("必填").setContent(R.id.tab_bill_form));
        mTabHost.addTab(mTabHost.newTabSpec("TAB_BILLS_FORM_EX").setIndicator("可选").setContent(R.id.tab_bill_form_ex));
    }

    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }
}
