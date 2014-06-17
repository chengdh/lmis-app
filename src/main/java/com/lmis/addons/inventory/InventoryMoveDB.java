package com.lmis.addons.inventory;

import android.content.Context;

import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

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
        return "InventoryMove";
    }

    @Override
    public List<LmisColumn> getModelColumns() {

        List<LmisColumn> cols = new ArrayList<LmisColumn>();
        //发货地
        cols.add(new LmisColumn("from_org_id", "From Org", LmisFields.manyToOne(new OrgDB(mContext)), false));
        //到货地
        cols.add(new LmisColumn("to_org_id", "To Org", LmisFields.manyToOne(new OrgDB(mContext)), false));
        cols.add(new LmisColumn("bill_date", "bill_date", LmisFields.varchar(20), false));
        //是否已上传
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //上传时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));
        //出入库类别
        cols.add(new LmisColumn("type", "type", LmisFields.varchar(20), false));
        //明细
        cols.add(new LmisColumn("line_ids","Move Lines", LmisFields.oneToMany(new InventoryLineDB(mContext)),false));
        //货物件数
        cols.add(new LmisColumn("sum_goods_count","sum goods count", LmisFields.integer(),false));
        //运单数量
        cols.add(new LmisColumn("sum_bills_count","bills count", LmisFields.integer(),false));

        return cols;
    }

    /**
     * Get sum count.
     * 获取货物总数量
     *
     * @return the integer
     */
    public static Integer getSumCount(){
        return 100;
    }

    /**
     * Get sum bills.
     * 获取装车总票数
     *
     * @return the integer
     */
    public static Integer getSumBills(){
        return 23;
    }
}
