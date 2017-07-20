package com.lmis.util.barcode_scan_header;

import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

/**
 * Created by chengdh on 14-8-3.
 */
public class BarcodeQueryListener implements SearchView.OnQueryTextListener {
    /**
     * The list search adapter.
     */
    private ArrayAdapter<Object> mListSearchAdapter;
    Boolean mIsSearched = false;

    public BarcodeQueryListener(ArrayAdapter<Object> listAdapter) {
        mListSearchAdapter = listAdapter;
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            newText = "";
            if (mIsSearched && mListSearchAdapter != null) {
                mListSearchAdapter.getFilter().filter(null);
            }

        } else {
            mIsSearched = true;
            mListSearchAdapter.getFilter().filter(newText);
        }

        return false;
    }
}
