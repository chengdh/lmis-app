package com.lmis.addons.inventory;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 14-6-16.
 * 扫描明细
 */
public class InventoryLineDB extends LmisDatabase {

    public InventoryLineDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "LoadListWithBarcodeLine";
    }

    @Override
    public List<LmisColumn> getModelColumns() {

        List<LmisColumn> cols = new ArrayList<LmisColumn>();
        //主表id
        cols.add(new LmisColumn("load_list_with_barcode_id","Master Inventory", LmisFields.integer(20)));
        //扫描的barcode
        cols.add(new LmisColumn("barcode","barcode", LmisFields.varchar(20)));
        cols.add(new LmisColumn("state", "state", LmisFields.varchar(20)));
        //手动全部出入库标记,用于标明用户是否手动将该条码关联的运单货物全部入库
        cols.add(new LmisColumn("manual_set_all","manual_set_all", LmisFields.integer(),false));

        return cols;
    }
}
