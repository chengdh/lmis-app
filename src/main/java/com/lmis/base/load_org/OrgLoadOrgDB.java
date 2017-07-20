package com.lmis.base.load_org;

import android.content.Context;

import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 2017/7/20.
 */

public class OrgLoadOrgDB extends LmisDatabase {

    Context mContext;

    public OrgLoadOrgDB(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        //分拣组id
        cols.add(new LmisColumn("org_load_id", "org_load_id", LmisFields.integer()));
        cols.add(new LmisColumn("org_id", "Org ", LmisFields.manyToOne(new OrgDB(mContext))));
        return cols;
    }

    @Override
    public String getModelName() {

        return "OrgLoadOrg";
    }

}

