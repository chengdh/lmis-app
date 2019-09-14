package com.lmis.util.barcode;

import android.content.Context;

import com.lmis.util.SoundPlayer;

/**
 * Created by chengdh on 14-9-18.
 */
public class BranchConfirmBarcodeParser extends BarcodeParser {

    final static String CONFIRMED = "confirmed";

    /**
     * Instantiates a new Barcode parser.
     *
     * @param context the context
     * @param move_id 传入的已保存的出库单id
     * @param fromOrgId the from org id
     * @param toOrgId 要判定的到货地id
     */
    public BranchConfirmBarcodeParser(Context context, int move_id, int fromOrgId, int toOrgId) {
        super(context, move_id, fromOrgId, toOrgId, false, true, InventoryMoveOpType.BRANCH_CONFIRM);
    }

    @Override
    public void addBarcode(String barcode) throws InvalidBarcodeException, InvalidToOrgException, DBException, BarcodeNotExistsException, BarcodeDuplicateException {
        GoodsInfo gs = new GoodsInfo(mContext, barcode);

        //条码已解析事件
        mBus.post(new BarcodeParseSuccessEvent(gs));

        GoodsInfo findedItem = null;
        for (GoodsInfo f : mScanedBarcode) {
            if (f.getmBarcode().equals(barcode)) {
                findedItem = f;
                SoundPlayer.playBarcodeScanErrorSound(mContext);
                throw new BarcodeDuplicateException("重复扫描条码!",f);
            }
        }

        //扫描的条码不存在
        if (findedItem == null) {
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            throw new BarcodeNotExistsException("扫描条码不存在!");
        }

        //该条码已确认
        if (findedItem != null && findedItem.getmState().equals(CONFIRMED)) {
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            throw new BarcodeDuplicateException("该条码已确认!");
        }
        //到货地不匹配
        if(findedItem.getmToOrgId() != mToOrgID){
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            throw new InvalidBarcodeException("到货地不正确!");
        }

        //该条码等待确认

        if (confirm2DB(gs)) {
            gs.setmState(CONFIRMED);
            SoundPlayer.playBarcodeScanSuccessSound(mContext);
            //publish相关事件
            mBus.post(new GoodsInfoConfirmSuccessEvent(gs));
            mBus.post(new ScandedBarcodeConfirmChangeEvent(sumGoodsCount(), sumConfirmedGoodsCount()));
        } else {
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            throw new DBException("更新条码信息时出现错误!");
        }
    }
}
