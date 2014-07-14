package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 */
public class InvalidBarcodeException extends Exception {
    public InvalidBarcodeException(String detailMessage) {
        super(detailMessage);
    }
}
