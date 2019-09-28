package com.lmis.util.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.dagger_module.OrgModule;
import com.lmis.orm.LmisDataRow;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by chengdh on 14-6-18.
 * 显示所有外转机构的下拉列表
 */
public class AllAreasSpinner extends Spinner {
    @Inject
    @OrgModule.AllAreas
    List<LmisDataRow> mAllAreas;
    ArrayAdapter<LmisDataRow> mAdapter = null;

    public AllAreasSpinner(Context context) {
        super(context);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<LmisDataRow>(context, android.R.layout.simple_spinner_dropdown_item, mAllAreas);
        setAdapter(mAdapter);
    }

    public AllAreasSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<LmisDataRow>(context, android.R.layout.simple_spinner_dropdown_item, mAllAreas);
        setAdapter(mAdapter);
    }
}
