package com.lmis.addons.cux_tran;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 2017/2/17.
 * 领料单明细
 */

public class CuxTranLineDB extends LmisDatabase{


    public CuxTranLineDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "CuxTranLine";
    }

    @Override
    public List<LmisColumn> getModelColumns() {

        List<LmisColumn> cols = new ArrayList<>();
        cols.add(new LmisColumn("cux_tran_id", "cux tran id", LmisFields.integer(16), true, false));
        cols.add(new LmisColumn("require_id", "require_id", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("line_number", "line number", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("organization_id", "organization id", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("inventory_item_id", "item id", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("item_spc", "item spec", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("uom", "unit measure", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("subinventory", "subinventory", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("required_qty", "required qty", LmisFields.integer(16), true, true));

        cols.add(new LmisColumn("project_number", "project number", LmisFields.varchar(30), true, true));
        cols.add(new LmisColumn("task_number", "task number", LmisFields.varchar(30), true, true));
        cols.add(new LmisColumn("apply_number", "apply_number", LmisFields.varchar(30), true, true));
        cols.add(new LmisColumn("apply_line_num", "apply line num", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("apply_qty", "apply qty", LmisFields.integer(16), true, true));

        //物料编码
        cols.add(new LmisColumn("item_number", "item number", LmisFields.varchar(30), true, true));
        //物料名称
        cols.add(new LmisColumn("item_dec", "item dec", LmisFields.varchar(30), true, true));


        //备注
        cols.add(new LmisColumn("remark", "remark", LmisFields.varchar(60), true, true));


        cols.add(new LmisColumn("project_id", "project id", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("task_id", "task id", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("expense_type", "expense type", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("cost", "cost", LmisFields.varchar(60), true, true));


        cols.add(new LmisColumn("wip_entity_name", "wip entiry name", LmisFields.varchar(60), true, true));
        cols.add(new LmisColumn("operation_seq_num", "operation seq num", LmisFields.integer(16), true, true));
        cols.add(new LmisColumn("canceled_qty", "canceled qty", LmisFields.integer(16), true, true));

        return cols;
    }
}
