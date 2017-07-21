package com.lmis.addons.scan_header;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 2017/7/12.
 */

public class ScanLineDB extends LmisDatabase {
    public ScanLineDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "ScanLine";
    }

    @Override
    public List<LmisColumn> getModelColumns() {

        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        cols.add(new LmisColumn("scan_header_id", "Scan Header", LmisFields.integer()));
        cols.add(new LmisColumn("carrying_bill_id", "carrying_bill_id", LmisFields.integer()));
        cols.add(new LmisColumn("qty", "Quantity", LmisFields.integer()));
        cols.add(new LmisColumn("barcode", "barcode", LmisFields.varchar(20),false));
        return cols;
    }
}
