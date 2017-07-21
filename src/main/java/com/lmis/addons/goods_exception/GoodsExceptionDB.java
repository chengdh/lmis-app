package com.lmis.addons.goods_exception;

import android.content.Context;

import com.lmis.Lmis;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisValues;
import com.lmis.util.controls.ExceptionTypeSpinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-9-4.
 */
public class GoodsExceptionDB extends LmisDatabase {
    public GoodsExceptionDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "goods_exception";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        cols.add(new LmisColumn("org_id", "Org", LmisFields.manyToOne(new OrgDB(mContext))));
        cols.add(new LmisColumn("op_org_id", "Operate Org", LmisFields.manyToOne(new OrgDB(mContext))));
        cols.add(new LmisColumn("carrying_bill_id", "Carrying Bill ID", LmisFields.integer()));
        cols.add(new LmisColumn("bill_no", "bill no", LmisFields.varchar(20)));
        cols.add(new LmisColumn("goods_no", "goods no", LmisFields.varchar(20)));
        cols.add(new LmisColumn("bill_date", "Bill Date", LmisFields.varchar(20)));
        cols.add(new LmisColumn("exception_type", "Exception type", LmisFields.varchar(20)));
        cols.add(new LmisColumn("except_num", "Exception Num", LmisFields.integer()));
        cols.add(new LmisColumn("note", "Note", LmisFields.text()));
        cols.add(new LmisColumn("photo", "Photo", LmisFields.blob()));

        //是否已上传
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //上传时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));

        return cols;
    }

    public void save2server(int id) throws JSONException, IOException {
        JSONObject json = select(id).exportAsJSON();
        json.put("photo_file_name","goods_exception_" + json.getString("bill_no") + ".png");
        json.remove("bill_no");
        json.remove("goods_no");
        json.remove("processed");
        json.remove("process_datetime");
        JSONArray args = new JSONArray();
        args.put(json);
        Lmis instance = getLmisInstance();
        JSONObject response = instance.callMethod("GoodsException", "create", args, null).getJSONObject("result");
        LmisValues v = new LmisValues();
        v.put("processed", true);
        v.put("process_datetime", new Date());
        update(v, id);
    }

    public static String getExceptionTypeDes(String ex_type) {
        List types = ExceptionTypeSpinner.exceptionTypes();
        for (Object t : types) {
            String[] el = (String[])t;

            if (el[0].equals(ex_type))
                return el[1];
        }
        return null;
    }
}
