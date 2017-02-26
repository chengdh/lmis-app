package com.lmis.addons.cux_tran;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lmis.addons.cux_demand.FragmentCuxDemandDetailHeader;
import com.lmis.addons.cux_demand.FragmentCuxDemandDetailLines;
import com.lmis.addons.cux_demand.FragmentCuxDemandDetailWorkflowMessages;
import com.lmis.orm.LmisDataRow;

/**
 * Created by chengdh on 2017/2/25.
 */

public class CuxTranDetailPageAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "CuxTranDetailPageAdapter ";

    LmisDataRow mCuxTranDetail = null;

    public CuxTranDetailPageAdapter(FragmentManager fm, LmisDataRow cuxDemand) {
        super(fm);
        mCuxTranDetail = cuxDemand;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FragmentCuxTranDetailHeader f = new FragmentCuxTranDetailHeader();
                f.setmCuxTranData(mCuxTranDetail);
                return f;
            case 1:
                FragmentCuxTranDetailLines f1 = new FragmentCuxTranDetailLines();
                f1.setmCuxTranData(mCuxTranDetail);
                return f1;
            case 2:
                FragmentCuxTranDetailWorkflowMessages f2 = new FragmentCuxTranDetailWorkflowMessages();
                return f2;
            default:
                FragmentCuxDemandDetailHeader f3 = new FragmentCuxDemandDetailHeader();
                f3.setmCuxDemandData(mCuxTranDetail);

                return f3;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
