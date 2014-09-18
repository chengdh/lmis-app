package com.lmis.util.barcode;

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
     * @param move_id   inventory_move_id
     * @param fromOrgId 发货地id
     * @param toOrgId   到货地id
     * @param opType    操作类型 branch_out/yard_confirm/yard_out/branch_confirm
     * @return the barcode parser
     */
    public static BarcodeParser getParser(Context context, int move_id, int fromOrgId, int toOrgId, String opType) {
        BarcodeParser parser = null;
        if (opType.equals(InventoryMoveOpType.BRANCH_OUT))
            parser = new BranchOutBarcodeParser(context, move_id, fromOrgId, toOrgId);
        else if (opType.equals(InventoryMoveOpType.YARD_CONFIRM)) {
            parser = new YardConfirmBarcodeParser(context, move_id, fromOrgId, toOrgId);
        } else if (opType.equals(InventoryMoveOpType.YARD_OUT)) {
            parser = new YardOutBarcodeParser(context, move_id, fromOrgId, toOrgId);
        } else if (opType.equals(InventoryMoveOpType.BRANCH_CONFIRM)) {
            parser = new BranchConfirmBarcodeParser(context, move_id, fromOrgId, toOrgId);
        }
        return parser;
    }
}
