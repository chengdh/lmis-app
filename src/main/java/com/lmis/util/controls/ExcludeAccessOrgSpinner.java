package com.lmis.util.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

    public ExcludeAccessOrgSpinner(final Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);
        mAdapter = new ArrayAdapter<LmisDataRow>(context, android.R.layout.simple_spinner_dropdown_item, mAccessOrgs) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null);
                LmisDataRow row = mAccessOrgs.get(position);
                textView.setText(row.getString("name"));
                textView.setPadding(12, 12, 12, 12);
                return textView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) View.inflate(context, android.R.layout.simple_spinner_item, null);
                LmisDataRow row = mAccessOrgs.get(position);
                textView.setText(row.getString("name"));
                return textView;
            }
        };
        setAdapter(mAdapter);
    }

}
