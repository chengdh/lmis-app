package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 条码解析正确事件
 */
public class ScannerSuccessEvent {
    private String strBarcode;

    public ScannerSuccessEvent(String barcode) {
        strBarcode = barcode;
    }

    public String getStrBarcode() {
        return strBarcode;
    }

    public void setStrBarcode(String strBarcode) {
        this.strBarcode = strBarcode;
    }
}
