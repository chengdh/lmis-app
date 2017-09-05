package com.lmis.addons.scan_header;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.lmis.orm.LmisDataRow;
import com.lmis.util.barcode_scan_header.BarcodeParser;
import com.lmis.util.barcode_scan_header.ScanHeaderOpType;

/**
 * Created by chengdh on 2017/7/12.
 * 参考https://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
 */

public class ScanHeaderPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "ScanHeaderPagerAdapter";

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    BarcodeParser mBarcodeParser = null;
    LmisDataRow mScanHeader = null;
    String mOpType = "";

    public ScanHeaderPagerAdapter(FragmentManager fm, BarcodeParser parser, LmisDataRow scanHeader, String opType) {
        super(fm);
        mBarcodeParser = parser;
        mScanHeader = scanHeader;
        mOpType = opType;
    }

    public String getmOpType() {
        return mOpType;
    }

    public void setmOpType(String mOpType) {
        this.mOpType = mOpType;
    }

    @Override
    public Fragment getItem(int position) {
        if (mOpType.equals(ScanHeaderOpType.LOAD_OUT)) {
            switch (position) {
                case 0:
                    FragmentVehicleForm f0 = new FragmentVehicleForm();
                    Bundle arg0 = new Bundle();
                    arg0.putString("type", mOpType);
                    f0.setArguments(arg0);

                    f0.setmBarcodeParser(mBarcodeParser);
                    f0.setmScanHeader(mScanHeader);
                    return f0;
                case 1:
                    FragmentScanBarcode f1 = new FragmentScanBarcode();
                    Bundle arg1 = new Bundle();
                    arg1.putString("type", mOpType);
                    f1.setArguments(arg1);
                    f1.setmBarcodeParser(mBarcodeParser);
                    f1.setmScanHeader(mScanHeader);
                    return f1;
                case 2:
                    FragmentBillList f2 = new FragmentBillList();
                    f2.setmBarcodeParser(mBarcodeParser);
                    return f2;
                default:
                    FragmentScanBarcode f3 = new FragmentScanBarcode();
                    f3.setmBarcodeParser(mBarcodeParser);
                    f3.setmScanHeader(mScanHeader);
                    return f3;

            }
        } else {
            switch (position) {
                case 0:
                    FragmentScanBarcode f = new FragmentScanBarcode();
                    Bundle arg = new Bundle();
                    arg.putString("type", mOpType);
                    f.setArguments(arg);
                    f.setmBarcodeParser(mBarcodeParser);
                    f.setmScanHeader(mScanHeader);
                    return f;
                case 1:
                    FragmentBillList f1 = new FragmentBillList();
                    f1.setmBarcodeParser(mBarcodeParser);
                    return f1;
                default:
                    FragmentScanBarcode f3 = new FragmentScanBarcode();
                    f3.setmBarcodeParser(mBarcodeParser);
                    f3.setmScanHeader(mScanHeader);
                    return f3;
            }
        }
    }


    @Override
    public int getCount() {
        if (mOpType.equals(ScanHeaderOpType.LOAD_OUT)) {
            return 3;
        } else
            return 2;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }


}
