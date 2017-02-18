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

public class CuxDemandPlatformLineDB  extends LmisDatabase{

    public CuxDemandPlatformLineDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "CuxDemandPlatformLinesA";
    }

    @Override
    public List<LmisColumn> getModelColumns() {

        List<LmisColumn> cols = new ArrayList<>();
        cols.add(new LmisColumn("id", "id", LmisFields.integer(16), true));
        cols.add(new LmisColumn("cux_demand_id", "cux demand id", LmisFields.integer(16), true));
        cols.add(new LmisColumn("line type", "line type", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("apply_number", "apply number", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("item_number", "item number", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("item_description", "item description", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("item_spec", "item spec", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("item_price", "item price", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("demand_quantiry", "demand quantiry", LmisFields.varchar(30), true));
        cols.add(new LmisColumn("line_bugdet", "line bugdet", LmisFields.varchar(30), true));
        return cols;
    }
}
