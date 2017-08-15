package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 2017/8/15.
 */

public class LoadOutGetBillFromServerSuccessEvent {
    GoodsInfo mGoodsInfo;

    public LoadOutGetBillFromServerSuccessEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }
}
