package com.lmis.util.barcode;

import android.content.Context;

import com.lmis.util.SoundPlayer;

/**
 * Created by chengdh on 14-9-18.
 */
public class YardConfirmBarcodeParser extends BarcodeParser {
    final static String CONFIRMED = "confirmed";
    /**
     * Instantiates a new Barcode parser.
     *
     * @param context   the context
     * @param move_id   传入的已保存的出库单id
     * @param fromOrgID the from org iD
     * @param toOrgID   the to org iD
     */
    public YardConfirmBarcodeParser(Context context, int move_id, int fromOrgID, int toOrgID) {
        super(context, move_id, fromOrgID, toOrgID, false, false, InventoryMoveOpType.YARD_CONFIRM);
    }

    @Override
    public void addBarcode(String barcode) throws InvalidBarcodeException, InvalidToOrgException, DBException,BarcodeNotExistsException, BarcodeDuplicateException {
        GoodsInfo findedItem = null;
        for (GoodsInfo f : mScanedBarcode) {
            if (f.getmBarcode().equals(barcode)) {
                findedItem = f;
            }
        }

        //扫描的条码不存在
        if (findedItem == null) {
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            throw new BarcodeNotExistsException("扫描条码不存在!");
        }

        //该条码已确认
        if(findedItem != null && findedItem.getmState().equals(CONFIRMED)){
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            throw new BarcodeDuplicateException("该条码已确认!");
        }

        //该条码等待确认
        findedItem.setmState(CONFIRMED);
        SoundPlayer.playBarcodeScanSuccessSound(mContext);
        if (confirm2DB(findedItem)) {
            //publish相关事件
            mBus.post(new GoodsInfoConfirmSuccessEvent(findedItem));
            mBus.post(new ScandedBarcodeConfirmChangeEvent(sumGoodsCount(),sumConfirmedGoodsCount()));
        } else {
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            throw new DBException("更新条码信息时出现错误!");
        }
    }
}
