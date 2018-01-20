package com.lmis.addons.scan_header;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.barcode_scan_header.BarcodeParser;
import com.lmis.util.barcode_scan_header.ScanHeaderOpType;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 录入车辆信息
 * Created by chengdh on 14-9-14.
 */
public class FragmentVehicleForm extends BaseFragment {

    public static final String TAG = "FragmentVehicleForm";

    String mOpType = ScanHeaderOpType.LOAD_OUT;

    @Inject
    Bus mBus;

    @InjectView(R.id.edt_v_no)
    EditText mEdtVNo;

    @InjectView(R.id.edt_note)
    EditText mEdtNote;


    @InjectView(R.id.edt_driver_name)
    EditText mEdtDriverName;

    @InjectView(R.id.edt_mobile)
    EditText mEdtMobile;

    @InjectView(R.id.edt_id_no)
    EditText mEdtIdNo;

    @InjectView(R.id.spinner_load_org_select)
    Spinner mSpinnerLoadOrgSelect;

    View mView = null;

    LmisDataRow mScanHeader = null;
    BarcodeParser mBarcodeParser = null;

    public LmisDataRow getmScanHeader() {
        return mScanHeader;
    }

    public void setmScanHeader(LmisDataRow mScanHeader) {
        this.mScanHeader = mScanHeader;
    }

    public BarcodeParser getmBarcodeParser() {
        return mBarcodeParser;
    }

    public void setmBarcodeParser(BarcodeParser mBarcodeParser) {
        this.mBarcodeParser = mBarcodeParser;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new ScanHeaderDB(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_scan_header_form, container, false);
        ButterKnife.inject(this, mView);
        Bundle args = getArguments();
        if (args != null && args.containsKey("type"))
            mOpType = args.getString("type");
        mBus.register(this);
        initData();
        return mView;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    private void initData() {
        Log.d(TAG, "FragmentVehicleForm#initData");
        if (mScanHeader != null) {
            //设置to_org_id spinner
            int toOrgID = mScanHeader.getInt("to_org_id");

            SpinnerAdapter adp = mSpinnerLoadOrgSelect.getAdapter();
            for (int i = 0; i < adp.getCount(); i++) {
                LmisDataRow r = (LmisDataRow) adp.getItem(i);
                if (r.getInt("id") == toOrgID) {
                    mSpinnerLoadOrgSelect.setSelection(i);
                }

            }
            mEdtDriverName.setText(mScanHeader.getString("driver_name"));
            mEdtVNo.setText(mScanHeader.getString("v_no"));
            mEdtMobile.setText(mScanHeader.getString("mobile"));
            mEdtIdNo.setText(mScanHeader.getString("id_no"));
        }
        mEdtDriverName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBarcodeParser.setmDriverName(mEdtDriverName.getText().toString());

            }
        });
        mEdtIdNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBarcodeParser.setmIdNo(mEdtIdNo.getText().toString());

            }
        });
        mEdtMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBarcodeParser.setmMobile(mEdtMobile.getText().toString());

            }
        });
        mEdtVNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBarcodeParser.setmVNo(mEdtVNo.getText().toString());

            }
        });
        mEdtNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBarcodeParser.setmNote(mEdtNote.getText().toString());
            }
        });

        mSpinnerLoadOrgSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LmisDataRow org = (LmisDataRow) mSpinnerLoadOrgSelect.getItemAtPosition(position);
                mBarcodeParser.setmToOrgID(org.getInt("id"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public boolean validateBeforeUpload() {

        boolean success = true;
        if (mOpType.equals(ScanHeaderOpType.LOAD_OUT)) {
            String vNo = mEdtVNo.getText().toString();
            String driverName = mEdtDriverName.getText().toString();
            String mobile = mEdtMobile.getText().toString();
            if (vNo.equals("")) {
                success = false;
                mEdtVNo.setError("请输入车牌号!");
                mEdtVNo.requestFocus();

            }
            if (driverName.equals("")) {
                success = false;

                mEdtDriverName.setError("请输入司机姓名!");
                mEdtDriverName.requestFocus();

            }
            if (mobile.equals("")) {
                success = false;
                mEdtMobile.setError("请输入司机手机!");
                mEdtMobile.requestFocus();

            }
        }
        return success;
    }

}
