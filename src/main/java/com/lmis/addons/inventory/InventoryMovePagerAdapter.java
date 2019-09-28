package com.lmis.addons.inventory;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import com.lmis.orm.LmisDataRow;
import com.lmis.util.barcode.BarcodeParser;

import java.util.List;

/**
 * Created by chengdh on 14-9-14.
 */
public class InventoryMovePagerAdapter extends FragmentStatePagerAdapter{
    public static final String TAG = "InventoryOutPagerAdapter";

    BarcodeParser mBarcodeParser = null;
    LmisDataRow mInventoryOut = null;

    public InventoryMovePagerAdapter(FragmentManager fm, BarcodeParser parser, LmisDataRow inventoryOut) {
        super(fm);
        mBarcodeParser = parser;
        mInventoryOut = inventoryOut;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                FragmentInventoryMoveVehicleForm f0 = new FragmentInventoryMoveVehicleForm();
                f0.setmBarcodeParser(mBarcodeParser);
                f0.setmInventoryMove(mInventoryOut);
                return f0;
            case 1:
                FragmentScanBarcode f1 = new FragmentScanBarcode();
                f1.setmBarcodeParser(mBarcodeParser);
                f1.setmInventoryMove(mInventoryOut);
                return f1;
            case 2:
                FragmentBillList f2 = new FragmentBillList();
                f2.setmBarcodeParser(mBarcodeParser);
                return f2;

            case 3:
                FragmentBarcodeList f3 = new FragmentBarcodeList();
                f3.setmBarcodeParser(mBarcodeParser);
                return f3;
            default:
                FragmentScanBarcode f4 = new FragmentScanBarcode();
                f4.setmBarcodeParser(mBarcodeParser);
                f4.setmInventoryMove(mInventoryOut);
                return f4;

        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
