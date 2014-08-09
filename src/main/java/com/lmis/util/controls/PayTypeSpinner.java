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
 * Created by chengdh on 14-8-9.
 */
public class PayTypeSpinner extends Spinner {

    ArrayAdapter<Map.Entry> mAdapter = null;

    List<Map.Entry> mPayTypes = null;

    public PayTypeSpinner(final Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);

        mPayTypes = new ArrayList<Map.Entry>(PayType.payTypes().entrySet());
        mAdapter = new ArrayAdapter<Map.Entry>(context, android.R.layout.simple_spinner_dropdown_item, mPayTypes) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null);
                Map.Entry theEntry = mPayTypes.get(position);
                textView.setText(theEntry.getValue().toString());
                return textView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_item, null);
                Map.Entry theEntry = mPayTypes.get(position);
                textView.setText(theEntry.getValue().toString());
                return textView;
            }
        };
        setAdapter(mAdapter);
    }
}
