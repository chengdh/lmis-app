package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 条码扫描后,当前货物信息添加成功事件
 */
public class GoodsInfoAddSuccessEvent {
    GoodsInfo mGoodsInfo;
    public GoodsInfoAddSuccessEvent(GoodsInfo gs){
        mGoodsInfo = gs;
    }

    public GoodsInfo getmGoodsInfo() {
        return mGoodsInfo;
    }
}
