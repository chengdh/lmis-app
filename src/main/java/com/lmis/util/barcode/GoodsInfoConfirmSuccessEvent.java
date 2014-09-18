package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-9-18.
 */
public class GoodsInfoConfirmSuccessEvent {
    GoodsInfo mGoodsInfo;

    public GoodsInfoConfirmSuccessEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }
}
