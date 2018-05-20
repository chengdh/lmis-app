package com.lmis.util.barcode_scan_header;

import android.content.Context;

/**
 * Created by chengdh on 14-9-18.
 * barcodeParser生成器
 */
public class BarcodeParserFactory {
    /**
     * Get parser.
     *
     * @param context   the context
     * @param id        scan_header_id
     * @param fromOrgId 发货地id
     * @param toOrgId   到货地id
     * @param opType    操作类型 branch_out/yard_confirm/yard_out/branch_confirm
     * @return the barcode parser
     */
    public static BarcodeParser getParser(Context context, int id, int fromOrgId, int toOrgId, String opType) {
        BarcodeParser parser = null;
        switch (opType) {
            case ScanHeaderOpType.SORTING_IN:
                parser = new SortingInBarcodeParser(context, id, fromOrgId, toOrgId);
                break;
            case ScanHeaderOpType.LOAD_IN:

                parser = new LoadInBarcodeParser(context, id, fromOrgId, toOrgId);
                break;
            case ScanHeaderOpType.LOAD_OUT:
                parser = new LoadOutBarcodeParser(context, id, fromOrgId, toOrgId);
                break;
            case ScanHeaderOpType.LOAD_IN_TEAM:

                parser = new LoadInTeamBarcodeParser(context, id, fromOrgId, toOrgId);
                break;
            case ScanHeaderOpType.INNER_TRANSIT_LOAD_IN:
                parser = new InnerTransitLoadInBarcodeParser(context, id, fromOrgId, toOrgId);
                break;
            case ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT:
                parser = new InnerTransitLoadOutBarcodeParser(context, id, fromOrgId, toOrgId);
                break;
            case ScanHeaderOpType.LOCAL_TOWN_LOAD_IN:
                parser = new LocalTownLoadInBarcodeParser(context, id, fromOrgId, toOrgId);
                break;
            case ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT:
//                parser = new LocalTownLoadOutBarcodeParser(context, id, fromOrgId, toOrgId);


        }
        return parser;
    }
}
