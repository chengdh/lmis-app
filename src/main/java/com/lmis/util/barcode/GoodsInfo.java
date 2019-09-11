package com.lmis.util.barcode;

import android.content.Context;

import com.fizzbuzz.android.dagger.Injector;
import com.lmis.dagger_module.OrgModule;
import com.lmis.orm.LmisDataRow;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by chengdh on 14-6-18.
 * 根据barcode解析出得货物信息
 */
public class GoodsInfo {
    @Inject
    @OrgModule.AllOrgs
    List<LmisDataRow> mOrgs;

    Context mContext = null;
    String mBarcode = null;
    /**
     * 到货地名称.
     */
    String mToOrgName = null;
    /**
     * 到货地id.
     */
    Integer mToOrgId = -1;
    /**
     * 运单编号.
     */
    String mBillNo = null;

    /**
     * 货号.
     */
    String mGoodsNo = null;
    String mBillDate = null;
    double mCarryingFee = 0;
    double mGoodsFee = 0;
    String mGoodsInfo = "";

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
     * 如果是更新数据时,对应的明细id.
     */
    Integer mID = -1;

    public GoodsInfo(Context context, String barcode) throws InvalidBarcodeException {
        mContext = context;
        mBarcode = barcode;
        ((Injector) context).inject(this);
        parseBarcode();
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

    /**
     * 验证barcode
     */
    public void validate() throws InvalidBarcodeException {
        if ((mBarcode == null) || mBarcode.isEmpty() || mBarcode.length() != 21)
            throw new InvalidBarcodeException("条码格式不正确!");
    }

    /**
     * 解析条形码.
     */
    public void parseBarcode() throws InvalidBarcodeException {
        validate();

        mToOrgId = Integer.parseInt(mBarcode.substring(0, 3));
        mBillNo = mBarcode.substring(3, 15);
        mGoodsNum = Integer.parseInt(mBarcode.substring(15, 18));
        mSeq = Integer.parseInt(mBarcode.substring(18));
        getOrgName();
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

    @Override
    public String toString() {
        return getmBarcode();
    }
}
