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
 * Created by chengdh on 14-7-13.
 */
public class SummaryOrgSpinner extends Spinner {
    @Inject
    @OrgModule.SummaryOrgs
    List<LmisDataRow> mSummaryOrgs;
    ArrayAdapter<LmisDataRow> mAdapter = null;

    public SummaryOrgSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<LmisDataRow>(context, android.R.layout.simple_spinner_dropdown_item, mSummaryOrgs);
        setAdapter(mAdapter);
    }
}
