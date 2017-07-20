package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 2017/7/19.
 */

public class GetBillFromServerSuccessEvent {
    GoodsInfo mGoodsInfo;

    public GetBillFromServerSuccessEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }
}
