package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-7-30.
 */
public class BarcodeRemoveEvent {
    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }

    GoodsInfo mGoodsInfo;

    /**
     * Instantiates a new Barcode parse success event.
     *
     * @param gs the gs
     */
    public BarcodeRemoveEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }
}
