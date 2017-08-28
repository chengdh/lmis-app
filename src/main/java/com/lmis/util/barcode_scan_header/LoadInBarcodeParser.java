package com.lmis.util.barcode_scan_header;

import android.content.Context;
import android.widget.Toast;

import com.lmis.orm.LmisDataRow;
import com.lmis.util.SoundPlayer;
import com.squareup.otto.Subscribe;

/**
 * Created by chengdh on 2017/7/23.
 */

public class LoadInBarcodeParser extends BarcodeParser {

    /**
     * Instantiates a new Barcode parser.
     *
     * @param context   the context
     * @param id        传入的已保存的出库单id
     * @param fromOrgID the from org iD
     * @param toOrgID   the to org iD
     */
    public LoadInBarcodeParser(Context context, int id, int fromOrgID, int toOrgID) {
        super(context, id, fromOrgID, toOrgID, false, false, ScanHeaderOpType.LOAD_IN);
    }

    @Override
    public void addBarcode(String barcode) throws InvalidBarcodeException {
        GoodsInfo gs = new GoodsInfo(mContext, barcode, mOpType);

        //条码已解析事件
        mBus.post(new BarcodeParseSuccessEventForScanHeader(gs));

    }

    @Override
    public void unRegisterEventBus() {
        mBus.unregister(this);
    }


    //判断当前装卸组是否有权限扫描该票据
    private boolean checkOrgPower(int to_org_id) {
        for (LmisDataRow o : getmAccessLoadOrgs()) {
            if (o.getInt("id") == to_org_id) {
                return true;
            }
        }
        return false;

    }

    @Subscribe
    public void onLoadInGetBillFromServerSuccessEvent(LoadInGetBillFromServerSuccessEvent evt) throws InvalidToOrgException, DBException, BarcodeNotExistsException, BarcodeDuplicateException {
        GoodsInfo gs = evt.getmGoodsInfo();

        //判断单据状态是否分拣入库状态
        if (!gs.getmState().equals("billed")) {
            Toast.makeText(mContext, "单据状态不是草稿状态!", Toast.LENGTH_SHORT).show();

            SoundPlayer.playBarcodeScanErrorSound(mContext);
            return;
        }
        //判断当前分拣组是否有权限扫描该货物
        if (!checkOrgPower(gs.getmToOrgId())) {
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
//                throw new BarcodeDuplicateException("重复扫描条码!");
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
