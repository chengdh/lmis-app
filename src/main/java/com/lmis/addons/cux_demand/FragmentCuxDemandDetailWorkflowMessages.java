package com.lmis.addons.cux_demand;

import android.content.Context;

import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

/**
 * Created by chengdh on 2017/2/25.
 */

public class FragmentCuxDemandDetailWorkflowMessages extends BaseFragment {
    public static final String TAG = "FragmentCuxDemandDetailWorkflowMessages";

    LmisDataRow mCuxDemandData;

    public LmisDataRow getmCuxDemandData() {
        return mCuxDemandData;
    }

    public void setmCuxDemandData(LmisDataRow mCuxDemandData) {
        this.mCuxDemandData = mCuxDemandData;
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
