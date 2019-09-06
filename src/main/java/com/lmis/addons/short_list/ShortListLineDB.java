package com.lmis.addons.short_list;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 2017/7/12.
 */

public class ShortListLineDB extends LmisDatabase {
    public ShortListLineDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "ShortListLine";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        cols.add(new LmisColumn("short_list_id", "short list", LmisFields.integer()));
        cols.add(new LmisColumn("carrying_bill_id", "carrying_bill_id", LmisFields.integer()));
        cols.add(new LmisColumn("from_org_id", "from org id", LmisFields.integer()));
        cols.add(new LmisColumn("to_org_id", "to org id", LmisFields.integer()));
        cols.add(new LmisColumn("qty", "Quantity", LmisFields.integer()));
        cols.add(new LmisColumn("goods_status_type", "goods status type", LmisFields.integer()));
        cols.add(new LmisColumn("goods_status_note", "goods status note", LmisFields.varchar(60)));
        cols.add(new LmisColumn("barcode", "barcode", LmisFields.varchar(20),false));
        return cols;
    }
}
