package com.lmis.addons.inventory;

import android.content.Context;

import com.lmis.Lmis;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisValues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-6-16.
 * 条码扫描出库数据主表
 */
public class InventoryMoveDB extends LmisDatabase {

    public InventoryMoveDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "LoadListWithBarcode";
    }

    @Override
    public List<LmisColumn> getModelColumns() {

        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        //发货地
        cols.add(new LmisColumn("from_org_id", "From Org", LmisFields.manyToOne(new OrgDB(mContext))));
        //到货地
        cols.add(new LmisColumn("to_org_id", "To Org", LmisFields.manyToOne(new OrgDB(mContext))));

        cols.add(new LmisColumn("bill_no", "bill_no", LmisFields.varchar(20)));

        cols.add(new LmisColumn("bill_date", "bill_date", LmisFields.varchar(20)));

        cols.add(new LmisColumn("note", "note", LmisFields.varchar(20), false));
        cols.add(new LmisColumn("user_id", "user_id", LmisFields.integer(20)));
        cols.add(new LmisColumn("state", "state", LmisFields.varchar(20)));

        cols.add(new LmisColumn("confirm_id", "confirm_id", LmisFields.integer(20)));
        cols.add(new LmisColumn("confirm_date", "confirm_date", LmisFields.varchar(20)));
        //是否已上传
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //上传时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));
        //出入库类别
        cols.add(new LmisColumn("type", "type", LmisFields.varchar(20), false));
        //明细
        cols.add(new LmisColumn("load_list_with_barcode_lines", "Move Lines", LmisFields.oneToMany(new InventoryLineDB(mContext)), false));
        //货物件数
        cols.add(new LmisColumn("sum_goods_count", "sum goods count", LmisFields.integer(), false));
        //运单数量
        cols.add(new LmisColumn("sum_bills_count", "bills count", LmisFields.integer(), false));

        return cols;
    }

    /**
     * 保存单条数据到服务器.
     *
     * @param id the id
     */
    public void save2server(int id) throws JSONException, IOException {
        JSONObject json = select(id).exportAsJSON(false);
        delUnusedAttr(json);

        JSONArray args = new JSONArray();
        args.put(json);
        Lmis instance = getLmisInstance();
        instance.callMethod("LoadListWithBarcode", "create", args, null);
        LmisValues v = new LmisValues();
        v.put("processed",true);
        v.put("process_datetime",new Date());
        update(v,id);
    }

    /**
     * 删除不需要的属性.
     *
     * @param json the json
     */
    private void delUnusedAttr(JSONObject json) throws JSONException {
        json.remove("processed");
        json.remove("process_datetime");
        json.remove("sum_goods_count");
        json.remove("sum_bills_count");
        JSONArray arr = json.getJSONArray("load_list_with_barcode_lines_attributes");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject line = (JSONObject) arr.get(i);
            line.remove("load_list_with_barcode_id");
            line.remove("manual_set_all");

        }

    }

}
