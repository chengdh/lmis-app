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
 * Created by chengdh on 14-8-9.
 */
public class ExcludeAccessOrgSpinner extends Spinner {
    @Inject
    @OrgModule.ExcludeAccessOrgs
    List<LmisDataRow> mAccessOrgs;
    ArrayAdapter<LmisDataRow> mAdapter = null;

    public ExcludeAccessOrgSpinner(Context context) {
        super(context);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<LmisDataRow>(context, android.R.layout.simple_spinner_dropdown_item, mAccessOrgs);
        setAdapter(mAdapter);
    }

    public ExcludeAccessOrgSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<LmisDataRow>(context, android.R.layout.simple_spinner_dropdown_item, mAccessOrgs);
        setAdapter(mAdapter);
    }

}
