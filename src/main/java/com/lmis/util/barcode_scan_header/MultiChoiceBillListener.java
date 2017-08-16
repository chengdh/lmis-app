package com.lmis.util.barcode_scan_header;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.Toast;

import com.j256.ormlite.stmt.query.In;
import com.lmis.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chengdh on 14-8-4.
 */
public class MultiChoiceBillListener implements AbsListView.MultiChoiceModeListener {
    Context mContext;
    List<String> mMultiSelectedRows = new ArrayList<>();
    int mSelectedCounter = 0;
    BarcodeParser mBarcodeParser;
    List<Object> mBillObjects = null;


    public MultiChoiceBillListener(Context context, List<Object> billObjects, BarcodeParser barcodeParser) {
        mContext = context;
        mBarcodeParser = barcodeParser;
        mBillObjects = billObjects;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked) {

            mMultiSelectedRows.add(Integer.toString(position));
            mSelectedCounter++;
        } else {
            mMultiSelectedRows.remove(Integer.toString(position));
            mSelectedCounter--;
        }
        if (mSelectedCounter != 0) {
            mode.setTitle(mSelectedCounter + "");
        }

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_fragment_inventory_move_bill_list_context, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_inventory_out_bill_choice_delete:
                deleteSelectedBills();
                actionMode.finish();
                return true;
            default:
                return false;

        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mSelectedCounter = 0;
    }

    /**
     * 删除选中的条形码.
     *
     * @return the int
     */
    private int deleteSelectedBills() {
        int ret = 0;
        for (String position : mMultiSelectedRows) {
            try {
                GoodsInfo gi = (GoodsInfo) mBillObjects.get(Integer.parseInt(position));
                String billNo = gi.getmBarcode();
                mBarcodeParser.removeBill(billNo);
                ret++;
                Toast.makeText(mContext, "运单扫码记录已删除!", Toast.LENGTH_LONG).show();
            } catch (InvalidBarcodeException ex) {
                Toast.makeText(mContext, "条码格式错误!", Toast.LENGTH_LONG).show();
            } catch (BarcodeNotExistsException ex) {
                Toast.makeText(mContext, "条码不存在!", Toast.LENGTH_LONG).show();
            }
        }
        mMultiSelectedRows.clear();
        return ret;
    }
}
