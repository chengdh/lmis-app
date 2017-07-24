package com.lmis.util.barcode_scan_header;

/**
 * 装卸组出库时检查事件
 * Created by chengdh on 2017/7/23.
 */

public class RequireLoadOutCheckEvent {

    GoodsInfo mGoodsInfo;

    public RequireLoadOutCheckEvent(GoodsInfo gs) {
        mGoodsInfo = gs;
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }


}
