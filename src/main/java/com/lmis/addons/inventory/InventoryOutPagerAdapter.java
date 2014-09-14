package com.lmis.addons.inventory;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lmis.orm.LmisDataRow;
import com.lmis.util.barcode.BarcodeParser;

/**
 * Created by chengdh on 14-9-14.
 */
public class InventoryOutPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "InventoryOutPagerAdapter";

    BarcodeParser mBarcodeParser = null;
    LmisDataRow mInventoryOut = null;

    public InventoryOutPagerAdapter(FragmentManager fm, BarcodeParser parser, LmisDataRow inventoryOut) {
        super(fm);
        mBarcodeParser = parser;
        mInventoryOut = inventoryOut;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FragmentScanBarcode f = new FragmentScanBarcode();
                f.setmBarcodeParser(mBarcodeParser);
                f.setmInventoryOut(mInventoryOut);
                return f;
            case 1:
                FragmentBillList f1 = new FragmentBillList();
                f1.setmBarcodeParser(mBarcodeParser);
                return f1;

            case 2:
                FragmentBarcodeList f2 = new FragmentBarcodeList();
                f2.setmBarcodeParser(mBarcodeParser);
                return f2;
            default:
                FragmentScanBarcode f3 = new FragmentScanBarcode();
                f3.setmBarcodeParser(mBarcodeParser);
                f3.setmInventoryOut(mInventoryOut);
                return f3;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
