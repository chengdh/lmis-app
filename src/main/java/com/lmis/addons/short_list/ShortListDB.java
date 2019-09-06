package com.lmis.addons.short_list;

import android.content.Context;

import com.lmis.Lmis;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDataRow;
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
 * 分理处短驳单
 * Created by chengdh on 2017/7/12.
 */

public class ShortListDB extends LmisDatabase {
    public ShortListDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "ShortList";
    }

    @Override
    public List<LmisColumn> getModelColumns() {

        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        //车牌号
        cols.add(new LmisColumn("vehicle_no", "vehicle number", LmisFields.varchar(30)));

        //司机姓名
        cols.add(new LmisColumn("driver", "driver name", LmisFields.varchar(30)));
       //司机电话
        cols.add(new LmisColumn("mobile", "mobile", LmisFields.varchar(30)));
        //发货地
        cols.add(new LmisColumn("from_org_id", "From Org", LmisFields.manyToOne(new OrgDB(mContext))));
        //到货地
        cols.add(new LmisColumn("to_org_id", "To Org", LmisFields.manyToOne(new OrgDB(mContext))));

        cols.add(new LmisColumn("bill_date", "bill_date", LmisFields.varchar(20)));
        cols.add(new LmisColumn("bill_no", "bill_no", LmisFields.varchar(20)));

        cols.add(new LmisColumn("user_id", "user_id", LmisFields.integer(20)));
        cols.add(new LmisColumn("state", "state", LmisFields.varchar(20)));


        //备注
        cols.add(new LmisColumn("note", "note", LmisFields.varchar(200)));

        //是否已上传
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //上传时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));
        //明细
        cols.add(new LmisColumn("scan_lines", "Scan Lines", LmisFields.oneToMany(new ShortListLineDB(mContext))));
        //货物件数
        cols.add(new LmisColumn("sum_goods_count", "sum goods count", LmisFields.integer(), false));

        //运单数量
        cols.add(new LmisColumn("sum_bills_count", "bills count", LmisFields.integer(), false));

        //服务端id
        cols.add(new LmisColumn("server_id", "server id", LmisFields.integer(), false));

        return cols;
    }

    /**
     * 保存单条数据到服务器.
     *
     * @param id the id
     */
    public void save2server(int id) throws JSONException, IOException {
        LmisDataRow row = select(id);
        JSONObject json = row.exportAsJSON();
        String clazz = json.getString("op_type");
        delUnusedAttrs(json);

        JSONArray args = new JSONArray();
        args.put(json);
        Lmis instance = getLmisInstance();

        json.remove("id");
        JSONObject result = instance.callMethod(clazz, "create_and_process", args, null);
        JSONObject sh = result.getJSONObject("result");
        int serverId = sh.getInt("id");

        LmisValues v = new LmisValues();
        v.put("server_id", serverId);
        v.put("processed", true);
        v.put("process_datetime", new Date());
        update(v, id);
    }

    /**
     * 处理装车操作.
     *
     * @param id the id
     * @throws JSONException the json exception
     * @throws IOException   the io exception
     */
    public void processLoad(int id) throws JSONException, IOException {
        LmisDataRow row = select(id);
        JSONObject json = row.exportAsJSON();
        String clazz = json.getString("op_type");
        delUnusedAttrs(json);

        JSONArray args = new JSONArray();
        args.put(json);
        Lmis instance = getLmisInstance();

        json.remove("id");
        JSONObject result = instance.callMethod(clazz, "create_and_process", args, null);
        JSONObject sh = result.getJSONObject("result");
        int serverId = sh.getInt("id");

        LmisValues v = new LmisValues();
        v.put("server_id", serverId);
        v.put("processed", "loaded");
        v.put("process_datetime", new Date());
        update(v, id);

    }


    /**
     * 处理发车操作.
     *
     * @param id the id
     */
    public void processShip(int id) throws JSONException, IOException {
        LmisDataRow row = select(id);

        Lmis instance = getLmisInstance();

        String clazz = row.getString("op_type");
        int serverId = row.getInt("server_id");
        JSONArray args = new JSONArray();

        JSONObject result = instance.callMethod(clazz, "ship", args, serverId);
        LmisValues v = new LmisValues();
        v.put("processed", "shipped");
        v.put("process_datetime", new Date());
        update(v, id);

    }

    /**
     * 删除不需要的属性.
     *
     * @param json the json
     */
    private void delUnusedAttrs(JSONObject json) throws JSONException {
        json.remove("processed");
        json.remove("process_datetime");
        json.remove("op_type");
        json.remove("sum_bills_count");
        json.remove("sum_goods_count");
        JSONArray arr = json.getJSONArray("scan_lines_attributes");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject line = (JSONObject) arr.get(i);
            line.remove("scan_header_id");
            line.remove("barcode");
        }

    }
}
