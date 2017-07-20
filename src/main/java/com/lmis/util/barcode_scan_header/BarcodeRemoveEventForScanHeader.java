package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 14-7-30.
 */
public class BarcodeRemoveEventForScanHeader {
    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }

    GoodsInfo mGoodsInfo;

    /**
     * Instantiates a new Barcode parse success event.
     *
     * @param gs the gs
     */
    public BarcodeRemoveEventForScanHeader(GoodsInfo gs) {
        mGoodsInfo = gs;
    }
}
