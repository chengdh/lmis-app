package com.lmis.util.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.fizzbuzz.android.dagger.Injector;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chengdh on 14-6-18.
 * 显示银行的下拉列表
 */
public class BanksSpinner extends Spinner {
    List<String> mAllBanks = Arrays.asList(
            "",
            "招商银行",
            "建设银行",
            "交通银行",
            "邮储银行",
            "工商银行",
            "农业银行",
            "中国银行",
            "中信银行",
            "光大银行",
            "华夏银行",
            "民生银行",
            "广发银行",
            "平安银行");

    ArrayAdapter<String> mAdapter = null;

    public BanksSpinner(Context context) {
        super(context);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, mAllBanks);
        setAdapter(mAdapter);
    }

    public BanksSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, mAllBanks);
        setAdapter(mAdapter);
    }
    public int setSelectionBank(String bankName){
        int pos = mAllBanks.indexOf(bankName);
        if(pos >=0){
            setSelection(pos);
        }
        return pos;

    }
}
