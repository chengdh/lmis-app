package com.lmis.util.barcode;

import android.content.Context;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.addons.inventory.InventoryLineDB;
import com.lmis.addons.inventory.InventoryMoveDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisValues;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.util.Log;
import javax.inject.Inject;

/**
 * Created by chengdh on 14-6-18.
 * 条码处理器,前端扫描条码时,对条码进行解析
 * 同时判断条码是否正确
 */
public class BarcodeParser {

    public final static String TAG = "com.lmis.util.barcode.BarcodeParser";

    /**
     * event bus.
     */
    @Inject
    Bus mBus;

    /**
     * The M context.
     */
    Context mContext;

    @Inject
    InventoryMoveDB mInventoryOutDB;
    @Inject
    InventoryLineDB mInventoryLineDB;
    /**
     * 已扫描的条码列表.
     */
    List<GoodsInfo> mScanedBarcode;

    public int getmFromOrgID() {
        return mFromOrgID;
    }

    public void setmFromOrgID(int mFromOrgID) {
        this.mFromOrgID = mFromOrgID;
    }

    boolean mCheckToOrg = false;

    /**
     * 发出机构id.
     */
    int mFromOrgID = -1;

    /**
     * 到达机构id.
     */
    int mToOrgID = -1;

    /**
     * 数据库id.
     */
    int mMoveId = -1;

    /**
     * Instantiates a new Barcode parser.
     *
     * @param context    the context
     * @param move_id    传入的已保存的出库单id
     * @param checkToOrg 是否在扫描时判定到货地是否符合
     * @param toOrgId    要判定的到货地id
     */
    public BarcodeParser(Context context, int move_id, int fromOrgId, int toOrgId, boolean checkToOrg) {
        mContext = context;

        ((Injector)mContext).inject(this);

        mScanedBarcode = new ArrayList<GoodsInfo>();
        mCheckToOrg = checkToOrg;
        mFromOrgID = fromOrgId;
        mToOrgID = toOrgId;
        mMoveId = move_id;
        initData();
    }

    /**
     * Init data.
     * 初始化数据
     */
    private void initData() {
        if (mMoveId > 0) {
            LmisDataRow record = mInventoryOutDB.select(mMoveId);
            for (LmisDataRow line : record.getO2MRecord("line_ids").browseEach()) {
                try {
                    GoodsInfo gs = new GoodsInfo(mContext, line.getString("barcode"));
                    mScanedBarcode.add(gs);
                }
                catch(InvalidBarcodeException ex){
                    Log.e(TAG,ex.getMessage());
                }
            }
        }

    }

    /**
     * Save dB.
     * 将扫码数据保存奥数据库
     */
    private long save2DB(String barcode) {
        //需要新建数据库
        LmisValues row = new LmisValues();
        row.put("sum_goods_count",sumGoodsCount());
        row.put("sum_bills_count",sumBillsCount());
        if (mMoveId == -1) {
            row.put("from_org_id", mFromOrgID);
            row.put("to_org_id", mToOrgID);
            row.put("processed", "false");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date now = new Date();
            row.put("bill_date", sdf.format(now));
            mMoveId = (int) mInventoryOutDB.create(row);
        }else{
            //更新数据库
            mInventoryOutDB.update(row,mMoveId);
        }

        //添加inventory_line信息
        LmisValues lineValue = new LmisValues();
        lineValue.put("inventory_move_id", mMoveId);
        lineValue.put("barcode", barcode);
        lineValue.put("manual_set_all", false);
        return mInventoryLineDB.create(lineValue);
    }

    /**
     * 添加将扫描的条码.
     *
     * @param barcode the barcode
     */
    public void addBarcode(String barcode)
            throws InvalidBarcodeException, InvalidToOrgException,DBException,BarcodeDuplicateException {
        GoodsInfo gs = new GoodsInfo(mContext, barcode);

        //条码已解析事件
        mBus.post(new BarcodeParseSuccessEvent(gs));

        //判断是否重复扫描
        for (GoodsInfo f : mScanedBarcode) {
            if (f.getmBarcode().equals(barcode))
                throw new BarcodeDuplicateException("重复扫描条码!");
        }

        for (GoodsInfo f : mScanedBarcode) {
            if (f.getmBarcode().equals(barcode))
                throw new BarcodeDuplicateException("重复扫描条码!");
        }

        //判断到货地是否正确
        if (mCheckToOrg && gs.getmToOrgId() != mToOrgID) {
            throw new InvalidToOrgException("到货地不正确");

        }
        mScanedBarcode.add(gs);
        if(save2DB(barcode) > 0) {
            //publish相关事件
            mBus.post(new GoodsInfoAddSuccessEvent(gs));
            mBus.post(new ScandedBarcodeChangeEvent(sumGoodsCount(), sumBillsCount()));
        }
        else{
            throw new DBException("保存条码信息时出现错误!");
        }

    }

    /**
     * 获取已扫描的货物数量.
     *
     * @return the integer
     */
    public Integer sumGoodsCount() {
        return mScanedBarcode.size();
    }

    /**
     * TODO
     * 获取已扫描的运单数量.
     *
     * @return the integer
     */
    public Integer sumBillsCount() {
        return getBillsHash().keySet().size();

    }

    /**
     * Get bills hash.
     * 返回类似于 bill_no : count形式
     *
     * @return the hash map
     */
    public HashMap getBillsHash() {
        HashMap<String, Integer> hashBills = new HashMap<String, Integer>();
        for (GoodsInfo g : mScanedBarcode) {
            Integer count = hashBills.get(g.getmBillNo());
            if (count == null)
                count = 0;
            hashBills.put(g.getmBillNo(), count + 1);
        }

        return hashBills;
    }

    public List<GoodsInfo> getmScanedBarcode() {
        return mScanedBarcode;
    }

    public void setmScanedBarcode(List<GoodsInfo> mScanedBarcode) {
        this.mScanedBarcode = mScanedBarcode;
    }

    public Boolean getmCheckToOrg() {
        return mCheckToOrg;
    }

    public void setmCheckToOrg(Boolean mCheckToOrg) {
        this.mCheckToOrg = mCheckToOrg;
    }

    public Integer getmToOrgID() {
        return mToOrgID;
    }

    public void setmToOrgID(Integer mToOrgID) {
        this.mToOrgID = mToOrgID;
    }
}
