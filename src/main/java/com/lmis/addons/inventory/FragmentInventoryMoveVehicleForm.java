package com.lmis.addons.inventory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.barcode.BarcodeParser;
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
public class FragmentInventoryMoveVehicleForm extends BaseFragment {

    public static final String TAG = "FragmentInvMoveVehicleForm";

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

    View mView = null;

    LmisDataRow mInventoryMove = null;

    public BarcodeParser getmBarcodeParser() {
        return mBarcodeParser;
    }

    public void setmBarcodeParser(BarcodeParser mBarcodeParser) {
        this.mBarcodeParser = mBarcodeParser;
    }

    BarcodeParser mBarcodeParser = null;
    public LmisDataRow getmInventoryMove() {
        return mInventoryMove;
    }

    public void setmInventoryMove(LmisDataRow mInventoryMove) {
        this.mInventoryMove = mInventoryMove;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new InventoryMoveDB(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_inventory_move_vehicle_form, container, false);
        ButterKnife.inject(this, mView);
        Bundle args = getArguments();
        mBus.register(this);
        initData();
        return mView;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @SuppressLint("LongLogTag")
    private void initData() {
        Log.d(TAG, "FragmentInventoryMoveVehicleForm#initData");
        if (mInventoryMove != null) {
            mEdtDriverName.setText(mInventoryMove.getString("driver"));
            mEdtVNo.setText(mInventoryMove.getString("vehicle_no"));
            mEdtMobile.setText(mInventoryMove.getString("mobile"));
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

                mBarcodeParser.setDriver(s.toString());
                if (mInventoryMove != null) {
                    mInventoryMove.put("driver", s.toString());
                }

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

                if (mInventoryMove != null) {
                    mInventoryMove.put("mobile", s.toString());
                }
                mBarcodeParser.setMobile(s.toString());

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

                if (mInventoryMove != null) {
                    mInventoryMove.put("vehicle_no", s.toString());
                }

                mBarcodeParser.setmVehicleNo(s.toString());

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

                if (mInventoryMove != null) {
                    mInventoryMove.put("note", mEdtNote.getText().toString());
                }
                mBarcodeParser.setNote(s.toString());
            }
        });

    }

    public boolean validateBeforeUpload() {

        boolean success = true;
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
        return success;
    }

}
