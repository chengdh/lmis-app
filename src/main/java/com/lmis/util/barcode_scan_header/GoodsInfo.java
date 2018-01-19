package com.lmis.util.barcode_scan_header;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.Lmis;
import com.lmis.dagger_module.DbModule;
import com.lmis.dagger_module.OrgModule;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.LmisDialog;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-6-18.
 * 根据barcode解析出得货物信息
 */
public class GoodsInfo {
    public final static String TAG = "GoodsInfo";

    @Inject
    protected Bus mBus;

    @Inject
    @OrgModule.AllOrgs
    List<LmisDataRow> mOrgs;

    @Inject
    @DbModule.AccessLmis
    Lmis mLmis;

    //操作方式
    String mOptype;

    Context mContext = null;
    String mBarcode = null;
    private GetBillTask mGetBillTask = null;
    /**
     * 到货地名称.
     */
    String mToOrgName = null;
    /**
     * 到货地id.
     */
    Integer mToOrgId = -1;
    String mFromOrgName = null;
    Integer mFromOrgId = -1;
    /**
     * 运单编号.
     */
    String mBillNo = null;
    /**
     * 货物总件数.
     */
    Integer mGoodsNum = 0;
    /**
     * 货物序号.
     */
    Integer mSeq = 0;

    String mState = "draft";

    String mStateDes = "";

    /**
     * 货号.
     */
    String mGoodsNo = null;

    /**
     * 票据日期.
     */
    String mBillDate = null;
    /**
     * 运费.
     */
    double mCarryingFee = 0;
    /**
     * 货款.
     */
    double mGoodsFee = 0;
    /**
     * 货物信息.
     */
    String mGoodsInfo = "";
    /**
     * 操作数量.
     */
    int mScanedQty = 0;

    String mPayTypeDes = "";

    //货物异常类别及描述
    int mGoodsStatusType = 0;
    String mGoodsStatusNote = "";

    /**
     * 如果是更新数据时,对应的明细id.
     */
    Integer mID = -1;

    Integer goodsStatusTypeLoadIn = 1;
    String goodsStatusNoteLoadIn = "";
    Integer goodsStatusTypeLoadOut = 1;
    String goodsStatusNoteLoadOut = "";


    //FIXME 数据量较大时 为防止扫描过慢 不再注入任何信息
    public GoodsInfo(String barcode) {
        mBarcode = barcode;
    }

    public GoodsInfo(Context context) {
        mContext = context;
        ((Injector) context).inject(this);
    }

    public GoodsInfo(Context context, String barcode, String opType) throws InvalidBarcodeException {
        mContext = context;
        mBarcode = barcode;
        mOptype = opType;
        ((Injector) context).inject(this);
        parseBarcodeFromServer();
    }

    public GoodsInfo(Context context, String barcode) throws InvalidBarcodeException {
        mContext = context;
        mBarcode = barcode;
        ((Injector) context).inject(this);
    }

    public Integer getmID() {
        return mID;
    }

    public void setmID(Integer mID) {
        this.mID = mID;
    }

    public String getmBarcode() {
        return mBarcode;
    }

    public String getmToOrgName() {
        return mToOrgName;
    }

    public Integer getmToOrgId() {
        return mToOrgId;
    }

    public String getmBillNo() {
        return mBillNo;
    }

    public Integer getmGoodsNum() {
        return mGoodsNum;
    }

    public Integer getmSeq() {
        return mSeq;
    }

    public String getmState() {
        return mState;
    }

    public void setmState(String mState) {
        this.mState = mState;
    }

    public void setmBillNo(String mBillNo) {
        this.mBillNo = mBillNo;
    }

    public void setmGoodsNum(Integer mGoodsNum) {
        this.mGoodsNum = mGoodsNum;
    }

    public void setmSeq(Integer mSeq) {
        this.mSeq = mSeq;
    }

    public String getmGoodsNo() {
        return mGoodsNo;
    }

    public void setmGoodsNo(String mGoodsNo) {
        this.mGoodsNo = mGoodsNo;
    }

    public String getmBillDate() {
        return mBillDate;
    }

    public void setmBillDate(String mBillDate) {
        this.mBillDate = mBillDate;
    }

    public double getmCarryingFee() {
        return mCarryingFee;
    }

    public void setmCarryingFee(double mCarryingFee) {
        this.mCarryingFee = mCarryingFee;
    }

    public double getmGoodsFee() {
        return mGoodsFee;
    }

    public void setmGoodsFee(double mGoodsFee) {
        this.mGoodsFee = mGoodsFee;
    }

    public String getmGoodsInfo() {
        return mGoodsInfo;
    }

    public void setmGoodsInfo(String mGoodsInfo) {
        this.mGoodsInfo = mGoodsInfo;
    }

    public void setmToOrgName(String mToOrgName) {
        this.mToOrgName = mToOrgName;
    }

    public void setmToOrgId(Integer mToOrgId) {
        this.mToOrgId = mToOrgId;
    }

    public String getmFromOrgName() {
        return mFromOrgName;
    }

    public void setmFromOrgName(String mFromOrgName) {
        this.mFromOrgName = mFromOrgName;
    }

    public Integer getmFromOrgId() {
        return mFromOrgId;
    }

    public void setmFromOrgId(Integer mFromOrgId) {
        this.mFromOrgId = mFromOrgId;
    }

    public String getmPayTypeDes() {
        return mPayTypeDes;
    }

    public void setmPayTypeDes(String mPayTypeDes) {
        this.mPayTypeDes = mPayTypeDes;
    }

    public void setmBarcode(String mBarcode) {
        this.mBarcode = mBarcode;
    }

    public static String getTAG() {
        return TAG;
    }

    public int getmGoodsStatusType() {
        return mGoodsStatusType;
    }

    public void setmGoodsStatusType(int mGoodsStatusType) {
        this.mGoodsStatusType = mGoodsStatusType;
    }

    public String getmGoodsStatusNote() {
        return mGoodsStatusNote;
    }

    public void setmGoodsStatusNote(String mGoodsStatusNote) {
        this.mGoodsStatusNote = mGoodsStatusNote;
    }

    public String getmOptype() {
        return mOptype;
    }

    public void setmOptype(String mOptype) {
        this.mOptype = mOptype;
    }

    public Integer getGoodsStatusTypeLoadIn() {
        return goodsStatusTypeLoadIn;
    }

    public void setGoodsStatusTypeLoadIn(Integer goodsStatusTypeLoadIn) {
        this.goodsStatusTypeLoadIn = goodsStatusTypeLoadIn;
    }

    public String getGoodsStatusNoteLoadIn() {
        return goodsStatusNoteLoadIn;
    }

    public void setGoodsStatusNoteLoadIn(String goodsStatusNoteLoadIn) {
        this.goodsStatusNoteLoadIn = goodsStatusNoteLoadIn;
    }

    public Integer getGoodsStatusTypeLoadOut() {
        return goodsStatusTypeLoadOut;
    }

    public void setGoodsStatusTypeLoadOut(Integer goodsStatusTypeLoadOut) {
        this.goodsStatusTypeLoadOut = goodsStatusTypeLoadOut;
    }

    public String getGoodsStatusNoteLoadOut() {
        return goodsStatusNoteLoadOut;
    }

    public void setGoodsStatusNoteLoadOut(String goodsStatusNoteLoadOut) {
        this.goodsStatusNoteLoadOut = goodsStatusNoteLoadOut;
    }

    /**
     * 验证barcode
     */
    public void validate() throws InvalidBarcodeException {
        if ((mBarcode == null) || mBarcode.isEmpty())
            throw new InvalidBarcodeException("条码格式不正确!");
    }

    /**
     * 解析条形码.
     */
    public void parseBarcode() throws InvalidBarcodeException {
        validate();

        mToOrgId = Integer.parseInt(mBarcode.substring(0, 3));
        mBillNo = mBarcode.substring(3, 10);
        mGoodsNum = Integer.parseInt(mBarcode.substring(10, 13));
        mSeq = Integer.parseInt(mBarcode.substring(13));
        getOrgName();
    }

    /**
     * Parse barcode from server.
     */
    public void parseBarcodeFromServer() throws InvalidBarcodeException {
        validate();

        if (mGetBillTask != null) {
            mGetBillTask.cancel(true);
        }
        mGetBillTask = new GetBillTask(mBarcode);
        mGetBillTask.execute((Void) null);

    }

    /**
     * 获取到货地名称.
     *
     * @return the string
     */
    private String getOrgName() {
        for (LmisDataRow org : mOrgs) {
            Integer id = org.getInt("id");
            if (id.equals(mToOrgId))
                mToOrgName = org.getString("name");
        }
        return mToOrgName;
    }

    public int getmScanedQty() {
        return mScanedQty;
    }

    public void setmScanedQty(int mScanedQty) {
        this.mScanedQty = mScanedQty;
    }

    public String getmStateDes() {
        return mStateDes;
    }

    public void setmStateDes(String mStateDes) {
        this.mStateDes = mStateDes;
    }

    @Override
    public String toString() {
        return getmBarcode();
    }

    /**
     * 自服务端查询运单信息
     */
    private class GetBillTask extends AsyncTask<Void, Void, Boolean> {

        /**
         * The Pdialog.
         */
        LmisDialog pdialog;
        /**
         * The M vip code.
         */
        String mBarcode = "";
        /**
         * The Result.
         */
        JSONObject result = null;

        /**
         * Instantiates a new Search customer task.
         *
         * @param barcode 扫描的条码
         */
        private GetBillTask(String barcode) {
            mBarcode = barcode;
        }

        /**
         * On pre execute.
         */
        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(mContext, false, "查询中...");
            pdialog.show();
        }

        /**
         * Do in background.
         *
         * @param voids the voids
         * @return the boolean
         */
        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean ret = true;
            JSONArray args = new JSONArray();
            args.put(mBarcode);
            try {
                result = mLmis.callMethod("CarryingBill", "find_by_bill_no", args, null);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ret = false;
            }
            return ret;
        }

        /**
         * On post execute.
         *
         * @param success the success
         */
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mGetBillTask.cancel(true);
                try {
                    if (result.get("result").toString() == "null") {
                        Toast.makeText(mContext, "未查到运单信息!", Toast.LENGTH_SHORT).show();
                    } else {
                        JSONObject bill = result.getJSONObject("result");
                        setmBillDate(bill.getString("bill_date"));
                        setmFromOrgId(bill.getInt("from_org_id"));
                        setmFromOrgName(bill.getString("from_org_name"));
                        setmPayTypeDes(bill.getString("pay_type_des"));
                        setmToOrgId(bill.getInt("to_org_id"));
                        setmToOrgName(bill.getString("to_org_name"));
                        setmGoodsNo(bill.getString("goods_no"));
                        setmBillNo(bill.getString("bill_no"));
                        setmID(bill.getInt("id"));
                        setmGoodsInfo(bill.getString("goods_info"));
                        setmGoodsNum(bill.getInt("goods_num"));
                        setmCarryingFee(bill.getDouble("carrying_fee"));
                        setmGoodsFee(bill.getDouble("goods_fee"));
                        setmState(bill.getString("state"));
                        setmStateDes(bill.getString("human_state_name"));
                        setGoodsStatusTypeLoadIn(bill.getInt("goods_status_type_load_in"));
                        setGoodsStatusNoteLoadIn(bill.getString("goods_status_note_load_in"));

                        setGoodsStatusTypeLoadOut(bill.getInt("goods_status_type_load_out"));
                        setGoodsStatusNoteLoadOut(bill.getString("goods_status_note_load_out"));

                        //FIXME 默认扫描全部条码
                        setmScanedQty(bill.getInt("goods_num"));

                        switch (mOptype) {
                            case ScanHeaderOpType.SORTING_IN:
                                mBus.post(new SortingInGetBillFromServerSuccessEvent(GoodsInfo.this));
                                break;

                            case ScanHeaderOpType.LOAD_IN:

                                mBus.post(new LoadInGetBillFromServerSuccessEvent(GoodsInfo.this));
                                break;
                            case ScanHeaderOpType.LOAD_IN_TEAM:

                                mBus.post(new LoadInTeamGetBillFromServerSuccessEvent(GoodsInfo.this));
                                break;
                            case ScanHeaderOpType.LOAD_OUT:

                                mBus.post(new LoadOutGetBillFromServerSuccessEvent(GoodsInfo.this));
                                break;
                            default:
                                break;
                        }


//                        Toast.makeText(mContext, "已查到运单信息!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(mContext, "查找运单信息失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mGetBillTask = null;
        }
    }
}
