package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 已扫描额条码数据发生变化
 */
public class ScandedBarcodeChangeEvent {
    Integer mSumGoodsNum;
    Integer mSumBillsCount;
    public ScandedBarcodeChangeEvent(Integer sumGoodsNum,Integer sumBillsCount){
        mSumBillsCount = sumBillsCount;
        mSumGoodsNum = sumGoodsNum;

    }

    public Integer getmSumGoodsNum() {
        return mSumGoodsNum;
    }

    public Integer getmSumBillsCount() {
        return mSumBillsCount;
    }
}
