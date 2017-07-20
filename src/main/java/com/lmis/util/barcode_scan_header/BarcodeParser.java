package com.lmis.util.barcode_scan_header;

import android.content.Context;
import android.util.Log;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.addons.scan_header.ScanHeaderDB;
import com.lmis.addons.scan_header.ScanLineDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisValues;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by chengdh on 14-6-18.
 * 条码处理器,前端扫描条码时,对条码进行解析
 * 同时判断条码是否正确
 */
public abstract class BarcodeParser {

    public final static String TAG = "BarcodeParser";

    /**
     * event bus.
     */
    @Inject
    protected Bus mBus;

    /**
     * The M context.
     */
    Context mContext;

    @Inject
    protected ScanHeaderDB mScanHeaderDB;
    @Inject
    protected ScanLineDB mScanLineDB;
    /**
     * 已扫描的条码列表.
     */
    protected List<GoodsInfo> mScanedBarcode;


    protected boolean mCheckFromOrg = false;
    protected boolean mCheckToOrg = false;

    /**
     * 发出机构id.
     */
    protected int mFromOrgID = -1;

    /**
     * 到达机构id.
     */
    protected int mToOrgID = -1;


    /**
     * 数据库id.
     */
    protected int mId = -1;

    protected String mOpType = null;

    /**
     * Instantiates a new Barcode parser.
     *
     * @param context      the context
     * @param id           传入的已保存的出库单id
     * @param fromOrgId    the from org id
     * @param toOrgId      要判定的到货地id
     * @param checkFromOrg
     * @param checkToOrg   是否在扫描时判定到货地是否符合
     * @param opType       the op type
     */
    public BarcodeParser(Context context, int id, int fromOrgId, int toOrgId, Boolean checkFromOrg, boolean checkToOrg, String opType) {
        mContext = context;

        ((Injector) mContext).inject(this);
        mBus.register(this);

        mScanedBarcode = new ArrayList<GoodsInfo>();
        mCheckFromOrg = checkFromOrg;
        mCheckToOrg = checkToOrg;
        mFromOrgID = fromOrgId;
        mToOrgID = toOrgId;
        mId = id;
        mOpType = opType;
        initData();
    }

    /**
     * Init data.
     * 初始化数据
     */
    private void initData() {
        if (mId > 0) {
            LmisDataRow record = mScanHeaderDB.select(mId);
            for (LmisDataRow line : record.getO2MRecord("scan_lines").browseEach()) {
                try {
                    GoodsInfo gs = new GoodsInfo(mContext, line.getString("barcode"));
                    mScanedBarcode.add(gs);
                } catch (InvalidBarcodeException ex) {
                    Log.e(TAG, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Save dB.
     * 将扫码数据保存到数据库
     */
    protected long save2DB(String barcode) {
        //需要新建数据库
        LmisValues row = new LmisValues();
        row.put("sum_goods_count", sumGoodsCount());
        row.put("sum_bills_count", sumBillsCount());
        if (mId == -1) {
            row.put("from_org_id", mFromOrgID);
            row.put("to_org_id", mToOrgID);
            row.put("processed", false);
            row.put("op_type", mOpType);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date now = new Date();
            row.put("bill_date", sdf.format(now));
            row.put("state", "draft");
            mId = (int) mScanHeaderDB.create(row);
        } else {
            //更新数据库
            mScanHeaderDB.update(row, mId);
        }

        //添加scan_line信息
        LmisValues lineValue = new LmisValues();
        lineValue.put("scan_header_id", mId);
        lineValue.put("barcode", barcode);
        lineValue.put("manual_set_all", false);
        return mScanLineDB.create(lineValue);
    }


    /**
     * 添加将扫描的条码.
     * 在子类中override
     *
     * @param barcode the barcode
     */
    public abstract void addBarcode(String barcode)
            throws InvalidBarcodeException, InvalidToOrgException, DBException, BarcodeNotExistsException, BarcodeDuplicateException;


    /**
     * 从已扫描的记录中删除给定的条码数据.
     *
     * @param barcode the barcode
     */
    public void removeBarcode(String barcode) throws InvalidBarcodeException, BarcodeNotExistsException {
        GoodsInfo gs = new GoodsInfo(mContext, barcode);
        if (!containsBarcode(barcode))
            throw new BarcodeNotExistsException("条码不存在!");

        int idx = barcodeIndex(barcode);

        mScanedBarcode.remove(idx);
        String where = "scan_header_id = ? AND barcode = ?";
        String[] whereArgs = new String[]{mId + "", barcode};
        mScanLineDB.delete(where, whereArgs);

        //更新合计数量
        LmisValues row = new LmisValues();
        row.put("sum_goods_count", sumGoodsCount());
        row.put("sum_bills_count", sumBillsCount());
        mScanHeaderDB.update(row, mId);


        mBus.post(new BarcodeRemoveEventForScanHeader(gs));
        mBus.post(new ScandedBarcodeChangeEventForScanHeader(sumGoodsCount(), sumBillsCount()));
    }

    /**
     * 批量删除条码.
     *
     * @param gsList the gs list
     * @return the int
     */
    public int removeBarcodeAll(List<GoodsInfo> gsList) {
        mScanedBarcode.removeAll(gsList);
        String barcodeStr = "-1";
        for (GoodsInfo gs : gsList) {
            barcodeStr = "," + gs.getmBarcode();
        }
        String where = "scan_header_id= ? AND barcode in (?)";
        String[] whereArgs = new String[]{mId + "", barcodeStr};
        mScanLineDB.delete(where, whereArgs);

        //更新合计数量
        LmisValues row = new LmisValues();
        row.put("sum_goods_count", sumGoodsCount());
        row.put("sum_bills_count", sumBillsCount());
        mScanHeaderDB.update(row, mId);

        mBus.post(new ScandedBarcodeChangeEventForScanHeader(sumGoodsCount(), sumBillsCount()));
        return gsList.size();
    }

    /**
     * 删除给定的运单(该运单关联的所有条码记录将被删除).
     *
     * @param billNo the bill no
     * @return the int
     */
    public int removeBill(String billNo) throws BarcodeNotExistsException, InvalidBarcodeException {
        List<GoodsInfo> delList = new ArrayList<GoodsInfo>();
        for (GoodsInfo gs : mScanedBarcode) {
            if (gs.getmBillNo().equals(billNo)) {
                delList.add(gs);
            }
        }
        return removeBarcodeAll(delList);
    }


    /**
     * 判断已扫描的条码中是否包含给定的条码.
     *
     * @param barcode the barcode
     * @return the boolean
     */
    private Boolean containsBarcode(String barcode) {
        for (GoodsInfo gs : mScanedBarcode) {
            if (gs.getmBarcode().equals(barcode))
                return true;
        }
        return false;
    }

    /**
     * 获取给定的条码index.
     *
     * @param barcode the barcode
     * @return the int
     */
    private int barcodeIndex(String barcode) {
        for (int i = 0; i < mScanedBarcode.size(); i++) {
            if (mScanedBarcode.get(i).getmBarcode().equals(barcode))
                return i;
        }
        return -1;
    }

    /**
     * 获取已扫描的货物数量.
     *
     * @return the integer
     */
    public Integer sumGoodsCount() {
        int sumGoodsCount = 0;
        for (GoodsInfo gs : mScanedBarcode) {
            sumGoodsCount += gs.getmScanedQty();

        }
        return sumGoodsCount;
    }


    /**
     * 获取已扫描的运单数量.
     *
     * @return the integer
     */
    public Integer sumBillsCount() {
        return mScanedBarcode.size();

    }

    public List<GoodsInfo> getmScanedBarcode() {
        Collections.sort(mScanedBarcode, new ComparatorGoodsInfo());
        return mScanedBarcode;
    }

    public void setmScanedBarcode(List<GoodsInfo> mScanedBarcode) {
        this.mScanedBarcode = mScanedBarcode;
    }

    public Boolean getmCheckToOrg() {
        return mCheckToOrg;
    }

    public void setmCheckToOrg(Boolean mCheckToOrg) {
        this.mCheckToOrg = mCheckToOrg;
    }

    public Integer getmToOrgID() {
        return mToOrgID;
    }

    public void setmToOrgID(Integer mToOrgID) {

        this.mToOrgID = mToOrgID;
    }

    /**
     * 运单号比较器，用于比较运单号
     * 传入的对象是k,v样式,k为条码,v为扫描数量
     */
    public class ComparatorBillNo implements Comparator<Object> {
        @Override
        public int compare(Object barcode_1, Object barcode_2) {
            return ((Map.Entry) barcode_1).getKey().toString().compareTo(((Map.Entry) barcode_2).getKey().toString());
        }
    }

    public class ComparatorGoodsInfo implements Comparator<GoodsInfo> {
        @Override
        public int compare(GoodsInfo gs_1, GoodsInfo gs_2) {
            return gs_1.getmBarcode().compareTo(gs_2.getmBarcode());
        }
    }

    public int getmFromOrgID() {
        return mFromOrgID;
    }

    public void setmFromOrgID(int mFromOrgID) {
        this.mFromOrgID = mFromOrgID;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getmOpType() {
        return mOpType;
    }

    public void setmOpType(String mOpType) {
        this.mOpType = mOpType;
    }
}
