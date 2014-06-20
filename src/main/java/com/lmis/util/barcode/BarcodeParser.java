package com.lmis.util.barcode;

import android.content.Context;

import com.fizzbuzz.android.dagger.Injector;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by chengdh on 14-6-18.
 * 条码处理器,前端扫描条码时,对条码进行解析
 * 同时判断条码是否正确
 */
public class BarcodeParser {

    /**
     * event bus.
     */
    @Inject
    Bus mBus;

    Context mContext;
    /**
     * 已扫描的条码列表.
     */
    List<GoodsInfo> mScanedBarcode;

    /**
     * 原来已扫描的条码列表对象.
     */
    List<GoodsInfo> mOldScanedBarcode;

    Boolean mCheckToOrg = false;

    Integer mToOrgID;

    /**
     * Instantiates a new Barcode parser.
     *
     * @param context    the context
     * @param oldScans   原来扫描的条码信息数据,有可能用户在扫描一半
     *                   的时候退出系统去做别的事情,然后一段时间后又重新进入系统扫描
     * @param checkToOrg 是否在扫描时判定到货地是否符合
     * @param toOrgId    要判定的到货地id
     */
    public BarcodeParser(Context context, List<GoodsInfo> oldScans, Boolean checkToOrg, Integer toOrgId) {
        mContext = context;
        mScanedBarcode = new ArrayList<GoodsInfo>();
        mOldScanedBarcode = new ArrayList<GoodsInfo>();
        ((Injector) context).inject(this);
        if (oldScans != null) {
            mOldScanedBarcode = oldScans;
        }
        mCheckToOrg = checkToOrg;
        mToOrgID = toOrgId;
    }

    /**
     * 添加将扫描的条码.
     *
     * @param barcode the barcode
     */
    public void addBarcode(String barcode) throws InvalidBarcodeException,InvalidToOrgException,BarcodeDuplicateException {
        GoodsInfo gs = new GoodsInfo(mContext, barcode);

        //条码已解析事件
        mBus.post(new BarcodeParseSuccessEvent(gs));

        //判断是否重复扫描
        for(GoodsInfo f : mOldScanedBarcode){
            if(f.getmBarcode().equals(barcode))
                throw new BarcodeDuplicateException("重复扫描条码!");
        }

        for(GoodsInfo f : mScanedBarcode){
            if(f.getmBarcode().equals(barcode))
                throw new BarcodeDuplicateException("重复扫描条码!");
        }

        //判断到货地是否正确
        if (mCheckToOrg && gs.getmToOrgId() != mToOrgID) {
            throw new InvalidToOrgException("到货地不正确");

        }
        mScanedBarcode.add(gs);
        //publish相关事件
        mBus.post(new GoodsInfoAddSuccessEvent(gs));
        mBus.post(new ScandedBarcodeChangeEvent(sumGoodsCount(), sumBillsCount()));
    }

    /**
     * 获取已扫描的货物数量.
     *
     * @return the integer
     */
    public Integer sumGoodsCount(){
        return mOldScanedBarcode.size() + mScanedBarcode.size();
    }

    /**
     * TODO
     * 获取已扫描的运单数量.
     *
     * @return the integer
     */
    public Integer sumBillsCount(){
        return getBillsHash().keySet().size();

    }

    /**
     * Get bills hash.
     * 返回类似于 bill_no : count形式
     *
     * @return the hash map
     */
    public HashMap getBillsHash(){
         HashMap<String,Integer> hashBills = new HashMap<String, Integer>();
        for(GoodsInfo g : mScanedBarcode){
            Integer count = hashBills.get(g.getmBillNo());
            if(count == null)
                count = 0;
            hashBills.put(g.getmBillNo(),count+1);
        }

        for(GoodsInfo g : mOldScanedBarcode){
            Integer count = hashBills.get(g.getmBillNo());
            if(count == null)
                count = 0;
            hashBills.put(g.getmBillNo(),count+1);
        }
        return hashBills;
    }

    public List<GoodsInfo> getmScanedBarcode() {
        return mScanedBarcode;
    }

    public void setmScanedBarcode(List<GoodsInfo> mScanedBarcode) {
        this.mScanedBarcode = mScanedBarcode;
    }

    public List<GoodsInfo> getmOldScanedBarcode() {
        return mOldScanedBarcode;
    }

    public void setmOldScanedBarcode(List<GoodsInfo> mOldScanedBarcode) {
        this.mOldScanedBarcode = mOldScanedBarcode;
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
