package com.lmis.util.barcode_scan_header;

/**
 * Created by chengdh on 2017/7/12.
 */

public class ScanHeaderOpType {


    //分理处装车
    public static final String SUB_BRANCH = "ScanHeaderSubBranch";
    //分拣组入库
    public static final String SORTING_IN = "ScanHeaderSortingIn";

    //装卸组入库
    public static final String LOAD_IN = "ScanHeaderLoadIn";

    //装卸组装车
    public static final String LOAD_OUT = "ScanHeaderLoadOut";

    //装卸组卸货
    public static final String LOAD_IN_TEAM = "ScanHeaderTeam";

    //内部中转装卸组入库

    public static final String INNER_TRANSIT_LOAD_IN = "ScanHeaderInnerTransitLoadIn";

    //内部中转装卸组出库

    public static final String INNER_TRANSIT_LOAD_OUT = "ScanHeaderInnerTransitLoadOut";

    //同城装卸组入库
    public static final String LOCAL_TOWN_LOAD_IN = "ScanHeaderLocalTownLoadIn";

    //同城装卸组出库
    public static final String LOCAL_TOWN_LOAD_OUT = "ScanHeaderLocalTownLoadOut";
}
