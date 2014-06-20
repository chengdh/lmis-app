package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 条码解析正确时间
 */
public class BarcodeParseSuccessEvent {
    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }

    GoodsInfo mGoodsInfo;
    public BarcodeParseSuccessEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }
}
