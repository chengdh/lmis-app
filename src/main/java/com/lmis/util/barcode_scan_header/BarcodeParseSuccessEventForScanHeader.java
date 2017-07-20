package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 14-6-19.
 * 条码解析正确事件
 */
public class BarcodeParseSuccessEventForScanHeader {
    public GoodsInfo getmGoodsInfo() {

        return mGoodsInfo;
    }

    GoodsInfo mGoodsInfo;

    /**
     * Instantiates a new Barcode parse success event.
     *
     * @param gs the gs
     */
    public BarcodeParseSuccessEventForScanHeader(GoodsInfo gs) {
        mGoodsInfo = gs;
    }
}
