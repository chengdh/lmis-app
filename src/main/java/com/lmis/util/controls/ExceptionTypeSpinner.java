package com.lmis.util.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.orm.LmisDataRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chengdh on 14-9-4.
 */
public class ExceptionTypeSpinner extends Spinner {
    static List mExceptionTypes =  null;
    ArrayAdapter<Object> mAdapter = null;


    public ExceptionTypeSpinner(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new ArrayAdapter<Object>(context, android.R.layout.simple_spinner_dropdown_item, exceptionTypes()) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null);
                String[] row = (String[])exceptionTypes().get(position);
                textView.setText(row[1]);
                textView.setPadding(12, 12, 12, 12);
                return textView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_item, null);
                String[] row = (String[])exceptionTypes().get(position);
                textView.setText(row[1]);
                return textView;
            }
        };
        setAdapter(mAdapter);
    }
    //提供货物异常类别列表
    public static List exceptionTypes() {
        if(mExceptionTypes == null) {
            mExceptionTypes = new ArrayList();
            mExceptionTypes.add(new String[] {"SH","丢却"});
            mExceptionTypes.add(new String[] {"DA","破损"});
        }
        return mExceptionTypes;
    }
}
