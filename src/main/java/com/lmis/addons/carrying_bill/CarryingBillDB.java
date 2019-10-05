package com.lmis.addons.carrying_bill;

import android.content.Context;

import com.lmis.Lmis;
import com.lmis.base.area.AreaDB;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisValues;
import com.lmis.util.StringHelper;

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

    /**
     * Instantiates a new Carrying bill dB.
     *
     * @param context the context
     */
    public CarryingBillDB(Context context) {
        super(context);
    }

    /**
     * Gets model name.
     *
     * @return the model name
     */
    @Override
    public String getModelName() {
        return "ComputerBill";
    }

    /**
     * Gets model columns.
     *
     * @return the model columns
     */
    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        //运单号
        LmisColumn colBillNo = new LmisColumn("bill_no", "BillNo", LmisFields.varchar(20));
        cols.add(colBillNo);

        //货号
        LmisColumn colGoodsNo = new LmisColumn("goods_no", "GoodsNo", LmisFields.varchar(20));
        cols.add(colGoodsNo);
        //运单日期
        LmisColumn colBillDate = new LmisColumn("bill_date", "BillDate", LmisFields.varchar(20));
        cols.add(colBillDate);


        //单据类型
        LmisColumn colType = new LmisColumn("type", "Type", LmisFields.varchar(20));
        cols.add(colType);


        //中转地
        cols.add(new LmisColumn("transit_org_id", "transit Org", LmisFields.manyToOne(new OrgDB(mContext))));
        cols.add(new LmisColumn("transit_org_name", "transit Org", LmisFields.varchar(60),false));


        //外转到货地区
        cols.add(new LmisColumn("area_id", "area", LmisFields.manyToOne(new AreaDB(mContext))));
        //发货地
        cols.add(new LmisColumn("area_name", "area name", LmisFields.varchar(60),false));


        //发货地
        cols.add(new LmisColumn("from_org_id", "From Org", LmisFields.manyToOne(new OrgDB(mContext))));

        //到货地
        cols.add(new LmisColumn("to_org_id", "To Org", LmisFields.manyToOne(new OrgDB(mContext))));

        //发货地
        cols.add(new LmisColumn("from_org_name", "From Org", LmisFields.varchar(60),false));

        //到货地
        cols.add(new LmisColumn("to_org_name", "To Org", LmisFields.varchar(60),false));


        //发货人id
        LmisColumn colFromCustomerID = new LmisColumn("from_customer_id", "From Customer ID", LmisFields.integer());
        cols.add(colFromCustomerID);

        //发货人代码
        LmisColumn colFromCustomerCode = new LmisColumn("from_customer_code", "From Customer Code", LmisFields.varchar(20), false);
        cols.add(colFromCustomerCode);

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

        //服务备注
        LmisColumn colServiceNote= new LmisColumn("service_note", "service_note", LmisFields.varchar(30),false);
        cols.add(colServiceNote);

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
        LmisColumn colGoodsVolume = new LmisColumn("goods_volume", "Goods Volume", LmisFields.varchar(20));
        cols.add(colGoodsVolume);

        //重量
        LmisColumn colGoodsWeight = new LmisColumn("goods_weight", "package", LmisFields.varchar(20));
        cols.add(colGoodsWeight);


        //包装
        LmisColumn colPackage = new LmisColumn("package", "package", LmisFields.varchar(60));
        cols.add(colPackage);


        //加急与回单
        LmisColumn isUrgent = new LmisColumn("is_urgent", "is urgent", LmisFields.varchar(20),false);
        LmisColumn isReceipt = new LmisColumn("is_receipt", "is receipt", LmisFields.varchar(20),false);
        cols.add(isUrgent);
        cols.add(isReceipt);


        //备注
        LmisColumn colNote = new LmisColumn("note", "Note", LmisFields.text());
        cols.add(colNote);

        //创建时间
        LmisColumn colCreatedAtStr= new LmisColumn("created_at_str", "created_at_str", LmisFields.varchar(40),false);
        cols.add(colCreatedAtStr);

        //银行
        LmisColumn colBankName = new LmisColumn("bank_name", "bank name", LmisFields.varchar(40));
        cols.add(colBankName);

        //卡号
        LmisColumn colCardNo = new LmisColumn("card_no", "card no", LmisFields.varchar(40));
        cols.add(colCardNo);

        //录入人员
        LmisColumn colUser = new LmisColumn("user_id", "User", LmisFields.integer());
        cols.add(colUser);
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
     * @throws JSONException the jSON exception
     * @throws JSONException the jSON exception
     */
    public void save2server(int id) throws JSONException, IOException {
        JSONObject json = select(id).exportAsJSON(true);
        String billType = json.getString("type");

        JSONObject billAttributes = new JSONObject();
        if(json.getString("is_urgent").equals("true")){
            billAttributes.put("customerable_id",id);
            billAttributes.put("customerable_type","CarryingBill");
            billAttributes.put("is_urgent",1);
        }
        if(json.getString("is_receipt").equals("true")){
            billAttributes.put("is_receipt",1);
            billAttributes.put("customerable_id",id);
            billAttributes.put("customerable_type","CarryingBill");
        }
        json.put("bill_association_object_attributes",billAttributes);
        delUnusedAttr(json);


        JSONArray args = new JSONArray();
        args.put(json);
        Lmis instance = getLmisInstance();

        JSONObject response = instance.callMethod(billType, "create", args, null).getJSONObject("result");
        //FIXME 判断是否返回了id,rails model create 方法在失败时,会返回对应的model对象，但是valid?方法返回false
        //参考http://stackoverflow.com/questions/23975835/ruby-on-rails-active-record-return-value-when-create-fails
        if (response.getString("id").equals("null"))
            throw new JSONException("保存运单时出现错误!");


        LmisValues v = new LmisValues();
        v.put("processed", true);
        v.put("process_datetime", new Date());
        v.put("bill_no", response.getString("bill_no"));
        v.put("goods_no", response.getString("goods_no"));
        v.put("bill_date", response.getString("bill_date"));


        json.put("bill_association_object_attributes",billAttributes);

        update(v, id);
    }

    /**
     * 向服务器端更新运单数据.
     *
     * @param server_id the server id
     * @param vals      the vals
     * @throws JSONException the json exception
     * @throws IOException   the io exception
     */
    public void update2server(int server_id, JSONObject vals) throws JSONException, IOException {
        JSONArray args = new JSONArray();
        args.put(server_id);
        args.put(vals);
        Lmis instance = getLmisInstance();
        JSONObject response = instance.callMethod("ComputerBill", "update", args, null).getJSONObject("result");
    }


    /**
     * 删除不需要的属性.
     *
     * @param json the json
     * @throws JSONException the jSON exception
     */
    private void delUnusedAttr(JSONObject json) throws JSONException {

        json.remove("from_org_name");
        json.remove("to_org_name");
        json.remove("transit_org_name");
        json.remove("area_name");
        json.remove("from_customer_code");
        json.remove("is_urgent");
        json.remove("is_receipt");
        json.remove("processed");
        json.remove("created_at_str");
        json.remove("process_datetime");
        json.remove("type");
    }

    /**
     * 获取保险费设置.
     * 系统设置中 有保险费金额
     * 另外 组织机构设置中 有关于保险费的运费的金额设置
     *
     * @param orgID       the org iD
     * @param carryingFee the carrying fee
     * @return the int
     */
    public float getInsuredFee(int orgID, float carryingFee) {
        Lmis instance = getLmisInstance();

        return 2;
    }

    /**
     * 得到条码标签列表.
     *
     * @param bill the bill
     * @return the list
     */
    public static List<String> getLabels(LmisDataRow bill) {
        List<String> ret = new ArrayList<String>();
        String sToOrgId = StringHelper.addZero(bill.getM2ORecord("to_org_id").browse().getInt("id"), 3);
        String billNo = bill.getString("bill_no");
        int goodsNum = bill.getInt("goods_num");
        String sGoodsNum = StringHelper.addZero(bill.getInt("goods_num"), 3);
        String barcode;
        for (int i = 1; i <= goodsNum; i++) {
            String sSeq = StringHelper.addZero(i, 3);
            barcode = sToOrgId + billNo + sGoodsNum + sSeq;
            ret.add(barcode);
        }
        return ret;
    }

    /**
     * 生成条码标签列表.
     *
     * @param bill the bill
     * @return the labels
     */
    public static List<String> getLabels(JSONObject bill) {
        List<String> ret = new ArrayList<String>();
        try {
            String sToOrgId = StringHelper.addZero(bill.getInt("to_org_id"), 3);
            String billNo = bill.getString("bill_no");
            int goodsNum = bill.getInt("goods_num");
            String sGoodsNum = StringHelper.addZero(bill.getInt("goods_num"), 3);
            String barcode;
            for (int i = 1; i <= goodsNum; i++) {
                String sSeq = StringHelper.addZero(i, 3);
                barcode = sToOrgId + billNo + sGoodsNum + sSeq;
                ret.add(barcode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }


}
