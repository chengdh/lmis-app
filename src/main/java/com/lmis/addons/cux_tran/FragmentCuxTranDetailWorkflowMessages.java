package com.lmis.addons.cux_tran;

import android.content.Context;

import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

/**
 * Created by chengdh on 2017/2/25.
 */

public class FragmentCuxTranDetailWorkflowMessages extends BaseFragment {
    public static final String TAG = "FragmentCuxTranDetailWorkflowMessages";

    LmisDataRow mCuxTranData;

    public LmisDataRow getmCuxTranData() {
        return mCuxTranData;
    }

    public void setmCuxTranData(LmisDataRow mCuxTranData) {
        this.mCuxTranData = mCuxTranData;
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
