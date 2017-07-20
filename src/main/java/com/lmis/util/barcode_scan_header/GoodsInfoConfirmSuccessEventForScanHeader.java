package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 14-9-18.
 */
public class GoodsInfoConfirmSuccessEventForScanHeader {
    GoodsInfo mGoodsInfo;

    public GoodsInfoConfirmSuccessEventForScanHeader(GoodsInfo gs) {
        mGoodsInfo = gs;
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }
}
