package com.lmis.util.barcode_scan_header;

import android.content.Context;
import android.widget.Toast;

import com.lmis.orm.LmisDataRow;
import com.lmis.util.SoundPlayer;
import com.squareup.otto.Subscribe;

/**
 * 分拣组入库扫码操作
 * Created by chengdh on 2017/7/16.
 */

public class SortingInBarcodeParser extends BarcodeParser {
    final static String CONFIRMED = "confirmed";

    /**
     * Instantiates a new Barcode parser.
     *
     * @param context   the context
     * @param id        传入的已保存的出库单id
     * @param fromOrgID the from org iD
     * @param toOrgID   the to org iD
     */
    public SortingInBarcodeParser(Context context, int id, int fromOrgID, int toOrgID) {
        super(context, id, fromOrgID, toOrgID, false, false, ScanHeaderOpType.SORTING_IN);
    }

    @Override
    public void addBarcode(String barcode) throws InvalidBarcodeException {
        GoodsInfo gs = new GoodsInfo(mContext, barcode);

        //条码已解析事件
        mBus.post(new BarcodeParseSuccessEventForScanHeader(gs));

    }
    //判断当前分拣组是否有权限扫描该票据
    private boolean checkOrgPower(int from_org_id) {
        for (LmisDataRow o : getmAccessLoadOrgs()) {
            if (o.getInt("id") == from_org_id) {
                return true;
            }
        }
        return false;

    }

    @Subscribe
    public void onGetBillFromServerSuccessEvent(GetBillFromServerSuccessEvent evt) throws InvalidToOrgException, DBException, BarcodeNotExistsException, BarcodeDuplicateException {
        GoodsInfo gs = evt.getmGoodsInfo();

        //判断单据状态
        if(!gs.getmState().equals("billed")){
            SoundPlayer.playBarcodeScanErrorSound(mContext);
            Toast.makeText(mContext, "单据状态不是已开票状态!", Toast.LENGTH_SHORT).show();
            return;
        }
        //判断当前分拣组是否有权限扫描该货物
        if (!checkOrgPower(gs.getmFromOrgId())) {
            Toast.makeText(mContext, "您无权扫描该票据!", Toast.LENGTH_SHORT).show();

            SoundPlayer.playBarcodeScanErrorSound(mContext);
            return;
        }
        //判断是否重复扫描
        for (GoodsInfo f : mScanedBarcode) {
            if (f.getmBarcode().equals(gs.getmBarcode())) {
                SoundPlayer.playBarcodeScanErrorSound(mContext);
                Toast.makeText(mContext, "重复扫描条码!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mScanedBarcode.add(gs);
        SoundPlayer.playBarcodeScanSuccessSound(mContext);
        if (save2DB(gs) > 0) {
            //publish相关事件
            mBus.post(new ScanHeaderGoodsInfoAddSuccessEvent(gs));
            mBus.post(new ScandedBarcodeChangeEventForScanHeader(sumGoodsCount(), sumBillsCount()));
        } else {
            SoundPlayer.playBarcodeScanErrorSound(mContext);

            Toast.makeText(mContext, "保存条码信息时出现错误!", Toast.LENGTH_SHORT).show();
//            throw new DBException("保存条码信息时出现错误!");
        }
    }
}
