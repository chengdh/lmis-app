package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 条码解析正确事件
 */
public class BarcodeParseSuccessEvent {
    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }

    GoodsInfo mGoodsInfo;

    /**
     * Instantiates a new Barcode parse success event.
     *
     * @param gs the gs
     */
    public BarcodeParseSuccessEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }
}
