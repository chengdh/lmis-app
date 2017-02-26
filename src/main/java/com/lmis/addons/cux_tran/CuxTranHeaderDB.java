package com.lmis.addons.cux_tran;

import android.content.Context;

import com.lmis.addons.cux_demand.CuxDemandPlatformLineDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 2017/2/17.
 * 领料单
 */

public class CuxTranHeaderDB extends LmisDatabase {

    public CuxTranHeaderDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "CuxTran";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<>();

        cols.add(new LmisColumn("require_number", "require number", LmisFields.varchar(60), true));
        cols.add(new LmisColumn("require_deparment", "require deparment", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("require_status", "require status", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("require_person", "require person", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("require_person_id", "require_person_id", LmisFields.integer(16), true));
        cols.add(new LmisColumn("require_date", "require date", LmisFields.varchar(20), true));

        cols.add(new LmisColumn("last_update_date", "last update date", LmisFields.varchar(20), true));
        cols.add(new LmisColumn("last_update_by", "last update by", LmisFields.integer(16), true));

        cols.add(new LmisColumn("creation_date", "creation date", LmisFields.varchar(20), true));
        cols.add(new LmisColumn("created_by", "created by", LmisFields.integer(16), true));

        cols.add(new LmisColumn("last_update_login", "last_update_login", LmisFields.integer(16), true));

        cols.add(new LmisColumn("require_status_code", "require status code", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("require_type", "require type", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("require_type_id", "require type id", LmisFields.integer(16), true));

        cols.add(new LmisColumn("wip_entity_id", "wip_entiry_id", LmisFields.integer(16), true));
        cols.add(new LmisColumn("categorie", "categorie", LmisFields.varchar(60), true));

        cols.add(new LmisColumn("remark", "remark", LmisFields.varchar(100), true));

        cols.add(new LmisColumn("trans_deparment", "trans deparment", LmisFields.varchar(60), true));

        cols.add(new LmisColumn("bugdet_balance", "bugdet balance", LmisFields.varchar(60), true));
        cols.add(new LmisColumn("header_bugdet", "header bugdet", LmisFields.varchar(60), true));
        cols.add(new LmisColumn("bugdet_demand_total", "bugdet demand total", LmisFields.varchar(60), true));
        cols.add(new LmisColumn("bugdet_total", "bugdet total", LmisFields.varchar(60), true));

        cols.add(new LmisColumn("project_id", "project id", LmisFields.integer(16), true));
        //项目名称
        cols.add(new LmisColumn("name", "project name", LmisFields.varchar(60), true));
        //项目编号
        cols.add(new LmisColumn("segment1", "project number", LmisFields.varchar(60), true));
        cols.add(new LmisColumn("project_type", "project type", LmisFields.varchar(60), true));

        //工作流关联ID
        cols.add(new LmisColumn("wf_itemkey", "workflow item key", LmisFields.varchar(60), true));


        cols.add(new LmisColumn("cux_tran_lines", "line_ids", LmisFields.oneToMany(new CuxTranLineDB(mContext) )));

        //是否已查看
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //查看时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));

        return cols;
    }
}