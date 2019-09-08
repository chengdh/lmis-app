package com.lmis.addons.short_list;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.lmis.orm.LmisDataRow;

/**
 * Created by chengdh on 2017/7/12.
 * 参考https://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
 */

public class ShortListPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "ShortListPagerAdapter";

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    LmisDataRow mShortList = null;

    public ShortListPagerAdapter(FragmentManager fm, LmisDataRow shortList) {
        super(fm);
        mShortList = shortList;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fg = null;
        switch (position) {
            case 0:
                fg = new FragmentVehicleForm();
                Bundle arg0 = new Bundle();
                fg.setArguments(arg0);

                ((FragmentVehicleForm)fg).setmShortList(mShortList);
                break;
            case 1:
                fg = new FragmentBillList();
                ((FragmentBillList)fg).setmShortList(mShortList);
                break;

        }
        return fg;
    }


    @Override
    public int getCount() {
        return 2;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

}
