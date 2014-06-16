/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.lmis.base.about;

import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.util.controls.LmisTextView;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AboutFragment extends BaseFragment {

    View rootView = null;
    @InjectView(R.id.txvVersionName)
    LmisTextView versionName;

    @InjectView(R.id.line2)
    LmisTextView aboutLine2;

    @InjectView(R.id.line3)
    LmisTextView aboutLine3;

    @InjectView(R.id.line4)
    LmisTextView aboutLine4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle("About");

        rootView = inflater.inflate(com.lmis.R.layout.fragment_about_company, container, false);
        ButterKnife.inject(this, rootView);

        try {
            // setting version name from manifest file
            String version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            versionName.setText("Version " + version);

            // setting link in textview
            if (aboutLine2 != null) {
                aboutLine2.setMovementMethod(LinkMovementMethod.getInstance());
            }
            if (aboutLine3 != null) {
                aboutLine3.setMovementMethod(LinkMovementMethod.getInstance());
            }
            if (aboutLine4 != null) {
                aboutLine4.setMovementMethod(LinkMovementMethod.getInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scope.main().getActionBar().setDisplayHomeAsUpEnabled(true);
        scope.main().getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }
}
