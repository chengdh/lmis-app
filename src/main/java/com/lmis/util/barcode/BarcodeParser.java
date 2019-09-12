package com.lmis.util.barcode;

import android.content.Context;
import android.util.Log;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.addons.inventory.InventoryLineDB;
import com.lmis.addons.inventory.InventoryMoveDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisValues;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
    protected InventoryMoveDB mInventoryOutDB;
    @Inject
    protected InventoryLineDB mInventoryLineDB;
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
    protected int mMoveId = -1;

    protected String mOpType = null;

    public String getmOpType() {
        return mOpType;
    }

    public void setmOpType(String mOpType) {
        this.mOpType = mOpType;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getmVehicleNo() {
        return mVehicleNo;
    }

    public void setmVehicleNo(String mVehicleNo) {
        this.mVehicleNo = mVehicleNo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    protected  String driver = null;
    protected  String mVehicleNo = null;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    protected  String mobile= null;
    protected  String note= null;

    /**
     * Instantiates a new Barcode parser.
     *
     * @param context      the context
     * @param move_id      传入的已保存的出库单id
     * @param fromOrgId    the from org id
     * @param toOrgId      要判定的到货地id
     * @param checkFromOrg
     * @param checkToOrg   是否在扫描时判定到货地是否符合
     * @param opType       the op type
     */
    public BarcodeParser(Context context, int move_id, int fromOrgId, int toOrgId, Boolean checkFromOrg, boolean checkToOrg, String opType) {
        mContext = context;

        ((Injector) mContext).inject(this);

        mScanedBarcode = new ArrayList<GoodsInfo>();
        mCheckFromOrg = checkFromOrg;
        mCheckToOrg = checkToOrg;
        mFromOrgID = fromOrgId;
        mToOrgID = toOrgId;
        mMoveId = move_id;
        mOpType = opType;
        initData();
    }

    /**
     * Init data.
     * 初始化数据
     */
    private void initData() {
        if (mMoveId > 0) {
            LmisDataRow record = mInventoryOutDB.select(mMoveId);
            for (LmisDataRow line : record.getO2MRecord("load_list_with_barcode_lines").browseEach()) {
                try {
                    GoodsInfo gs = new GoodsInfo(mContext, line.getString("barcode"));
                    gs.setmState(line.getString("state"));
                    gs.setmID(line.getInt("id"));
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
     * 将扫码数据保存奥数据库
     */
    protected long save2DB(String barcode) {
        //需要新建数据库
        LmisValues row = new LmisValues();
        row.put("sum_goods_count", sumGoodsCount());
        row.put("sum_bills_count", sumBillsCount());
        if (mMoveId == -1) {
            row.put("from_org_id", mFromOrgID);
            row.put("to_org_id", mToOrgID);
            row.put("driver", driver);
            row.put("vehicle_no", mVehicleNo);
            row.put("mobile", mobile);
            row.put("note", note);

            row.put("processed", false);
            row.put("op_type", mOpType);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date now = new Date();
            row.put("bill_date", sdf.format(now));
            row.put("state","draft");
            mMoveId = (int) mInventoryOutDB.create(row);
        } else {
            //更新数据库
            mInventoryOutDB.update(row, mMoveId);
        }

        //添加inventory_line信息
        LmisValues lineValue = new LmisValues();
        lineValue.put("load_list_with_barcode_id", mMoveId);
        lineValue.put("barcode", barcode);
        lineValue.put("manual_set_all", false);
        return mInventoryLineDB.create(lineValue);
    }


    /**
     * 将条码确认信息保存到数据库.
     *
     * @param gs the gs
     * @return the boolean
     */
    protected boolean confirm2DB(GoodsInfo gs) {
        LmisValues vals = new LmisValues();
        vals.put("state", "confirmed");
        int ret = mInventoryLineDB.update(vals, gs.getmID());
        return ret > 0;
    }

    /**
     * 添加将扫描的条码.
     *
     * @param barcode the barcode
     */
    public abstract void addBarcode(String barcode)
            throws InvalidBarcodeException, InvalidToOrgException, DBException, BarcodeNotExistsException, BarcodeDuplicateException;

    /**
     * 设置该条码关联的运单货物全部为已扫描.
     *
     * @param barcode the barcode
     * @return the boolean
     */
    public int setAllGoodsScaned(String barcode) throws InvalidBarcodeException, BarcodeNotExistsException {
        if (!containsBarcode(barcode))
            throw new BarcodeNotExistsException("条码不存在!");

        String where = "load_list_with_barcode_id = ? AND barcode = ?";
        String[] whereArgs = new String[]{mMoveId + "", barcode};

        LmisValues update_vals = new LmisValues();
        update_vals.put("manual_set_all",1);

        return mInventoryLineDB.update(update_vals,where,whereArgs);
    }

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
        String where = "load_list_with_barcode_id = ? AND barcode = ?";
        String[] whereArgs = new String[]{mMoveId + "", barcode};
        mInventoryLineDB.delete(where, whereArgs);

        //更新合计数量
        LmisValues row = new LmisValues();
        row.put("sum_goods_count", sumGoodsCount());
        row.put("sum_bills_count", sumBillsCount());
        mInventoryOutDB.update(row, mMoveId);


        mBus.post(new BarcodeRemoveEvent(gs));
        mBus.post(new ScandedBarcodeChangeEvent(sumGoodsCount(), sumBillsCount()));
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
        String where = "load_list_with_barcode_id = ? AND barcode in (?)";
        String[] whereArgs = new String[]{mMoveId + "", barcodeStr};
        mInventoryLineDB.delete(where, whereArgs);

        //更新合计数量
        LmisValues row = new LmisValues();
        row.put("sum_goods_count", sumGoodsCount());
        row.put("sum_bills_count", sumBillsCount());
        mInventoryOutDB.update(row, mMoveId);

        mBus.post(new ScandedBarcodeChangeEvent(sumGoodsCount(), sumBillsCount()));
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
        return mScanedBarcode.size();
    }

    public Integer sumConfirmedGoodsCount() {
        int ret = 0;
        for (GoodsInfo gs : mScanedBarcode) {
            if (gs.getmState().equals("confirmed")) {
                ret++;
            }
        }
        return ret;
    }

    /**
     * 获取已扫描的运单数量.
     *
     * @return the integer
     */
    public Integer sumBillsCount() {
        return getBillsHash().keySet().size();

    }

    /**
     * Get bills hash.
     * 返回类似于 bill_no : count形式
     *
     * @return the hash map
     */
    public HashMap getBillsHash() {
        HashMap<String, Integer> hashBills = new HashMap<String, Integer>();
        for (GoodsInfo g : mScanedBarcode) {
            Integer count = hashBills.get(g.getmBillNo());
            if (count == null)
                count = 0;
            hashBills.put(g.getmBillNo(), count + 1);
        }

        return hashBills;
    }

    /**
     * 获取票据列表,形式是 票号 -- 扫描数量.
     *
     * @return the list
     */
    public List<Object> getBillsList() {
        List<Object> billsList = new ArrayList<Object>(getBillsHash().entrySet());
        Collections.sort(billsList, new ComparatorBillNo());
        return billsList;
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

    public int getmMoveId() {
        return mMoveId;
    }
}
