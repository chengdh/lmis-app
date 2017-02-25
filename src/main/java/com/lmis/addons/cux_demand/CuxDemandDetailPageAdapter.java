package com.lmis.addons.cux_demand;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lmis.orm.LmisDataRow;

/**
 * Created by chengdh on 2017/2/25.
 */

public class CuxDemandDetailPageAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "CuxDemandDetailPageAdapter ";

    LmisDataRow mCuxDetail = null;

    public CuxDemandDetailPageAdapter(FragmentManager fm, LmisDataRow cuxDemand) {
        super(fm);
        mCuxDetail = cuxDemand;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FragmentCuxDemandDetailHeader f = new FragmentCuxDemandDetailHeader();
                f.setmCuxDemandData(mCuxDetail);
                return f;
            case 1:
                FragmentCuxDemandDetailLines f1 = new FragmentCuxDemandDetailLines();
                f1.setmCuxDemandData(mCuxDetail);
                return f1;
            case 2:
                FragmentCuxDemandDetailWorkflowMessages f2 = new FragmentCuxDemandDetailWorkflowMessages();
                return f2;
            default:
                FragmentCuxDemandDetailHeader f3 = new FragmentCuxDemandDetailHeader();
                f3.setmCuxDemandData(mCuxDetail);

                return f3;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
