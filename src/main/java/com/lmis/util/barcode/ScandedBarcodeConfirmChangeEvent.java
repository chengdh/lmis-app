package com.lmis.util.barcode;

/**
 * 确认条码事件
 * Created by chengdh on 14-9-18.
 */
public class ScandedBarcodeConfirmChangeEvent {
    int mGoodsCount = 0;
    int mConfirmGoodsCount = 0;

    public ScandedBarcodeConfirmChangeEvent(int mGoodsCount,int mConfirmGoodsCount) {
        this.mGoodsCount = mGoodsCount;
        this.mConfirmGoodsCount = mConfirmGoodsCount;
    }

    public int getmConfirmGoodsCount() {
        return mConfirmGoodsCount;
    }

    public int getmGoodsCount() {
        return mGoodsCount;
    }
}
