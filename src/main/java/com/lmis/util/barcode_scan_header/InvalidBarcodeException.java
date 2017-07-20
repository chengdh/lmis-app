package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 14-6-19.
 */
public class InvalidBarcodeException extends Exception {
    public InvalidBarcodeException(String detailMessage) {
        super(detailMessage);
    }
}
