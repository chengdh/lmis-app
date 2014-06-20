package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 条码重复扫描错误
 */
public class BarcodeDuplicateException extends Exception {
    public BarcodeDuplicateException(String detailMessage) {
        super(detailMessage);
    }
}
