package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 2017/8/15.
 */

public class LocalTownLoadInGetBillFromServerSuccessEvent {
    GoodsInfo mGoodsInfo;

    public LocalTownLoadInGetBillFromServerSuccessEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }
}
