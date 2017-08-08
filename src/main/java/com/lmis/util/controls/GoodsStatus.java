package com.lmis.util.controls;

import com.j256.ormlite.stmt.query.In;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 货物状态
 * Created by chengdh on 2017/8/8.
 */

public class GoodsStatus {
    public static final int GOODS_STATUS_NORMAL = 0;             //正常
    public static final int GOODS_STATUS_DAMAGE = 90;           //破损
    public static final int GOODS_STATUS_LOST_LT = 91;         //缺货
    public static final int GOODS_STATUS_LOST_GT = 92;         //多货
    public static final int GOODS_STATUS_ERROR = 93;           //错货
    public static final int GOODS_STATUS_INPUT = 94;           //错货

    public static Map<Integer, String> statusList() {
        Map<Integer, String> ret = new LinkedHashMap<Integer, String>();

        ret.put(GOODS_STATUS_NORMAL, "正常");
        ret.put(GOODS_STATUS_DAMAGE, "破损");
        ret.put(GOODS_STATUS_LOST_LT, "缺货");
        ret.put(GOODS_STATUS_LOST_GT, "多货");
        ret.put(GOODS_STATUS_INPUT, "手工录入");
//        ret.put(GOODS_STATUS_ERROR, "错货");
        return ret;
    }

    public static int getGoodsStatusIndex(int goodsStatus) {
        int ret = -1;
        int i = 0;
        for (Map.Entry item : statusList().entrySet()) {
            if (item.getKey().equals(goodsStatus)) {
                ret = i;
            }
            i++;

        }
        return ret;
    }
}

