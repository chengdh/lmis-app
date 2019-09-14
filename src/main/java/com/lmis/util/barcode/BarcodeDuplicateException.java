package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 条码重复扫描错误
 */
public class BarcodeDuplicateException extends Exception {
    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }

    public void setmGoodsInfo(GoodsInfo mGoodsInfo) {
        this.mGoodsInfo = mGoodsInfo;
    }

    private GoodsInfo mGoodsInfo;


    public BarcodeDuplicateException(String detailMessage) {
        super(detailMessage);
    }
    public BarcodeDuplicateException(String detailMessage,GoodsInfo gs) {
        super(detailMessage);
        this.mGoodsInfo = gs;
    }
}
