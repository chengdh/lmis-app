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

    /**
     * 如果是更新数据时,对应的明细id.
     */
    Integer mID = -1;

    public GoodsInfo(Context context, String barcode) throws InvalidBarcodeException {
        mContext = context;
        mBarcode = barcode;
        ((Injector) context).inject(this);
        parseBarcodeFromServer();
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

    /**
     * 验证barcode
     */
    public void validate() throws InvalidBarcodeException {
        if ((mBarcode == null) || mBarcode.isEmpty() || mBarcode.length() != 7)
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

                        //FIXME 默认扫描全部条码
                        setmScanedQty(bill.getInt("goods_num"));

                        mBus.post(new GetBillFromServerSuccessEvent(GoodsInfo.this));
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
