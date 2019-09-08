package com.lmis.addons.short_list;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
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

    @Inject
    Bus mBus;

    @InjectView(R.id.edt_v_no)
    EditText mEdtVNo;

    @InjectView(R.id.edt_note)
    EditText mEdtNote;


    @InjectView(R.id.edt_driver)
    EditText mEdtDriverName;

    @InjectView(R.id.edt_mobile)
    EditText mEdtMobile;

    @InjectView(R.id.spinner_yards_select)
    Spinner mSpinnerYardsOrgSelect;

    View mView = null;

    LmisDataRow mShortList = null;

    public LmisDataRow getmShortList() {
        return mShortList;
    }

    public void setmShortList(LmisDataRow mShortList) {
        this.mShortList = mShortList;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new ShortListDB(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_short_list_form, container, false);
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

    private void initData() {
        Log.d(TAG, "FragmentVehicleForm#initData");
        if (mShortList != null && mShortList.get("id") != null) {
            //设置to_org_id spinner
            int toOrgID = mShortList.getM2ORecord("to_org_id").browse().getInt("id");

            SpinnerAdapter adp = mSpinnerYardsOrgSelect.getAdapter();
            for (int i = 0; i < adp.getCount(); i++) {
                LmisDataRow r = (LmisDataRow) adp.getItem(i);
                if (r.getInt("id") == toOrgID) {
                    mSpinnerYardsOrgSelect.setSelection(i);
                    mSpinnerYardsOrgSelect.setEnabled(false);
                }

            }
        }
        mEdtDriverName.setText(mShortList.getString("driver_name"));
        mEdtVNo.setText(mShortList.getString("v_no"));
        mEdtMobile.setText(mShortList.getString("mobile"));
        mSpinnerYardsOrgSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerAdapter adp = mSpinnerYardsOrgSelect.getAdapter();
                LmisDataRow r = (LmisDataRow) adp.getItem(position);
                mShortList.put("to_org_id", r.getString("id"));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mEdtDriverName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mShortList.put("driver", s.toString());

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
                mShortList.put("vehicle_no", s.toString());

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
                mShortList.put("mobile", s.toString());
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
