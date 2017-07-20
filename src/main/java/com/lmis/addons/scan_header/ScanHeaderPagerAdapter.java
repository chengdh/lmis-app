package com.lmis.addons.scan_header;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lmis.orm.LmisDataRow;
import com.lmis.util.barcode_scan_header.BarcodeParser;

/**
 * Created by chengdh on 2017/7/12.
 */

public class ScanHeaderPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "ScanHeaderPagerAdapter";

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
        switch (position) {
            case 0:
                FragmentScanBarcode f = new FragmentScanBarcode();
                Bundle arg = new Bundle();
                arg.putString("type",mOpType);
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

    @Override
    public int getCount() {
        return 2;
    }
}
