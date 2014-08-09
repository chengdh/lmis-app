package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by chengdh on 14-8-9.
 */
public class CarryingBillNew extends BaseFragment {

    public static final String TAG = "CarryingBillNew";

    View mView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_bill_form, container, false);

        //ButterKnife.inject(this, mView);
        return mView;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }
}
