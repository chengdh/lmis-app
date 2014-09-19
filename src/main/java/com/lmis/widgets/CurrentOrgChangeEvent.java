package com.lmis.widgets;

import com.lmis.orm.LmisDataRow;

/**
 * Created by chengdh on 14-9-19.
 */
public class CurrentOrgChangeEvent {
   LmisDataRow mOrg;

    public CurrentOrgChangeEvent(LmisDataRow mOrg) {
        this.mOrg = mOrg;
    }

    public LmisDataRow getmOrg() {
        return mOrg;
    }
}
