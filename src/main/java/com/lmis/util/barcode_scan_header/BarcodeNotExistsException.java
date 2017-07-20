package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 14-7-30.
 */
public class BarcodeNotExistsException extends Exception {
    public BarcodeNotExistsException(String detailMessage) {
        super(detailMessage);
    }
}
