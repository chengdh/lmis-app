package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 14-6-19.
 * 条码扫描后,当前货物信息添加成功事件
 */
public class ScanHeaderGoodsInfoAddSuccessEvent {
    GoodsInfo mGoodsInfo;

    public ScanHeaderGoodsInfoAddSuccessEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }
    public ScanHeaderGoodsInfoAddSuccessEvent(String gs) {
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }
}
