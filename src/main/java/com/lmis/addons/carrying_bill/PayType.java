package com.lmis.addons.carrying_bill;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付方式
 * Created by chengdh on 14-8-9.
 */
public class PayType {
    public static final String PAY_TYPE_CASH = "CA";        //现金付
    public static final String PAY_TYPE_TH = "TH";         //提货付
    public static final String PAY_TYPE_RETURN = "RE";     //回执付
    public static final String PAY_TYPE_K_GOODSFEE = "KG";  //自货款扣除

    public static Map<String, String> payTypes() {
        Map<String, String> ret = new LinkedHashMap<String, String>();

        ret.put(PAY_TYPE_TH, "提货付");
        ret.put(PAY_TYPE_CASH, "现金付");
        ret.put(PAY_TYPE_RETURN, "回执付");
        ret.put(PAY_TYPE_K_GOODSFEE, "货款扣");
        return ret;
    }
}
