package com.lmis.addons.cux_demand;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 2017/2/17.
 */

public class CuxDemandPlatformHeaderDB extends LmisDatabase {

    public CuxDemandPlatformHeaderDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "CuxDemand";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<>();

        cols.add(new LmisColumn("apply_number", "apply number", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("ou_name", "org name", LmisFields.varchar(30), true, true));
        cols.add(new LmisColumn("org_id", "org id", LmisFields.integer(16), true, true));

        //编制时间
        cols.add(new LmisColumn("apply_date", "apply date", LmisFields.varchar(20), true, true));


        //计划来源
        cols.add(new LmisColumn("apply_source", "apply source", LmisFields.varchar(60), true, true));

        //计划类型
        cols.add(new LmisColumn("apply_type", "apply type", LmisFields.varchar(60), true, true));

        //提交日期
        cols.add(new LmisColumn("submit_date", "submit date", LmisFields.varchar(20), true, true));

        cols.add(new LmisColumn("apply_deparment", "apply department", LmisFields.varchar(40), true, true));
        cols.add(new LmisColumn("applier_user", "applier", LmisFields.varchar(30), true, true));
        cols.add(new LmisColumn("remark", "remark", LmisFields.varchar(30), true, true));

        cols.add(new LmisColumn("project_number", "project number", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("project_name", "project name", LmisFields.varchar(60), true, true));

        cols.add(new LmisColumn("task_number", "task number", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("task_name", "task name", LmisFields.varchar(60), true, true));

        //是否紧急
        cols.add(new LmisColumn("urge_flag", "urge flag", LmisFields.varchar(60), true, true));
        //联系方式
        cols.add(new LmisColumn("attribute1", "linker", LmisFields.varchar(60), true, true));

        cols.add(new LmisColumn("wip_entity_id", "wip_entity_id", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("wip_entity_name", "wip_entity_name", LmisFields.varchar(60), true, true));

        //计划状态
        cols.add(new LmisColumn("apply_status", "apply status", LmisFields.varchar(40), true, true));


        cols.add(new LmisColumn("bugdet_total", "bugdet_total", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("bugdet_demand_total", "bugdet_demand_total", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("header_bugdet", "header_bugdet", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("bugdet_balance", "bugdet_balance", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("actual_cost", "actual_cost", LmisFields.varchar(60), true, true));

        cols.add(new LmisColumn("wf_itemkey", "wf itemkey", LmisFields.varchar(60), true, true));
//        cols.add(new LmisColumn("cux_demand_lines", "line_ids", LmisFields.oneToMany(new CuxDemandPlatformLineDB(mContext) )));

        //是否已查看
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false, true));
        //查看时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false, true));

        return cols;
    }
}