package com.lmis.addons.carrying_bill;

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
 * 运单对象
 * Created by chengdh on 14-8-9.
 */
public class CarryingBillDB extends LmisDatabase {

    public CarryingBillDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "ComputerBill";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        //运单号
        LmisColumn colBillNo = new LmisColumn("bill_no", "BillNo", LmisFields.varchar(7));
        cols.add(colBillNo);

        //货号
        LmisColumn colGoodsNo = new LmisColumn("goods_no", "GoodsNo", LmisFields.varchar(20));
        cols.add(colGoodsNo);
        //运单日期
        LmisColumn colBillDate = new LmisColumn("bill_date", "BillDate", LmisFields.varchar(20));
        cols.add(colBillDate);

        //发货地
        cols.add(new LmisColumn("from_org_id", "From Org", LmisFields.manyToOne(new OrgDB(mContext))));
        //到货地
        cols.add(new LmisColumn("to_org_id", "To Org", LmisFields.manyToOne(new OrgDB(mContext))));

        //发货人
        LmisColumn colFromCustomerName = new LmisColumn("from_customer_name", "From Customer", LmisFields.varchar(20));
        cols.add(colFromCustomerName);

        //发货人手机
        LmisColumn colFromCustomerMobile = new LmisColumn("from_customer_mobile", "From Customer Mobile", LmisFields.varchar(20));
        cols.add(colFromCustomerMobile);


        //收货人
        LmisColumn colToCustomerName = new LmisColumn("to_customer_name", "To Customer", LmisFields.varchar(20));
        cols.add(colToCustomerName);

        //收货人手机
        LmisColumn colToCustomerMobile = new LmisColumn("to_customer_mobile", "To Customer Mobile", LmisFields.varchar(20));
        cols.add(colToCustomerMobile);

        //支付方式
        LmisColumn colPayType = new LmisColumn("pay_type", "Pay Type", LmisFields.varchar(20));
        cols.add(colPayType);


        //运费
        LmisColumn colCarryingFee = new LmisColumn("carrying_fee", "Carrying fee", LmisFields.integer());
        cols.add(colCarryingFee);


        //代收货款
        LmisColumn colGoodsFee = new LmisColumn("goods_fee", "Goods fee", LmisFields.integer());
        cols.add(colGoodsFee);


        //发货短途
        LmisColumn colFromShortCarryingFee = new LmisColumn("from_short_carrying_fee", "From Short Carrying fee", LmisFields.integer());
        cols.add(colFromShortCarryingFee);


        //到货短途
        LmisColumn colToShortCarryingFee = new LmisColumn("to_short_carrying_fee", "To Short Carrying fee", LmisFields.integer());
        cols.add(colToShortCarryingFee);

        //保价费
        LmisColumn colInsuredFee = new LmisColumn("insured_fee", "Insured fee", LmisFields.integer());
        cols.add(colInsuredFee);

        //管理费
        LmisColumn colManageFee = new LmisColumn("manage_fee", "Manage fee", LmisFields.integer());
        cols.add(colManageFee);

        //货物名称
        LmisColumn colGoodsInfo = new LmisColumn("goods_info", "Goods Info", LmisFields.varchar(60));
        cols.add(colGoodsInfo);

        //件数
        LmisColumn colGoodsNum = new LmisColumn("goods_num", "Goods Num", LmisFields.integer());
        cols.add(colGoodsNum);

        //体积
        LmisColumn colGoodsVolume = new LmisColumn("goods_volume", "Goods Volume", LmisFields.integer());
        cols.add(colGoodsVolume);

        //type
        LmisColumn colType = new LmisColumn("type", "Type", LmisFields.varchar(20));
        cols.add(colType);

        //备注
        LmisColumn colNote = new LmisColumn("note", "Note", LmisFields.text());
        cols.add(colNote);

        //是否已上传
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //上传时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));

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
        JSONObject response = instance.callMethod("ComputerBill", "create", args, null).getJSONObject("result");
        LmisValues v = new LmisValues();
        v.put("processed",true);
        v.put("process_datetime",new Date());
        v.put("bill_no",response.getString("bill_no"));
        v.put("goods_no",response.getString("goods_no"));
        v.put("bill_date",response.getString("bill_date"));
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
    }
}
