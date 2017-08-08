package com.lmis.util.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.addons.carrying_bill.PayType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by chengdh on 2017/8/8.
 */

public class GoodsStatusSpinner extends Spinner {
    ArrayAdapter<Map.Entry> mAdapter = null;

    List<Map.Entry> mGoodsStatus = null;


    public GoodsStatusSpinner(final Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);

        mGoodsStatus = new ArrayList<Map.Entry>(GoodsStatus.statusList().entrySet());
        mAdapter = new ArrayAdapter<Map.Entry>(context, android.R.layout.simple_spinner_dropdown_item, mGoodsStatus) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null);
                Map.Entry theEntry = mGoodsStatus.get(position);
                textView.setText(theEntry.getValue().toString());
                textView.setPadding(5, 5, 5, 5);
                return textView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_item, null);
                Map.Entry theEntry = mGoodsStatus.get(position);
                textView.setText(theEntry.getValue().toString());
                return textView;
            }
        };
        setAdapter(mAdapter);
    }

}
