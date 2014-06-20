package com.lmis.util.barcode;

/**
 * Created by chengdh on 14-6-19.
 * 到货地异常
 */
public class InvalidToOrgException extends Exception {
    public InvalidToOrgException(String detailMessage) {
        super(detailMessage);
    }
}
