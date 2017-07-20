package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 14-6-19.
 * 已扫描额条码数据发生变化
 */
public class ScandedBarcodeChangeEventForScanHeader {
    Integer mSumGoodsNum;
    Integer mSumBillsCount;
    public ScandedBarcodeChangeEventForScanHeader(Integer sumGoodsNum, Integer sumBillsCount){
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
