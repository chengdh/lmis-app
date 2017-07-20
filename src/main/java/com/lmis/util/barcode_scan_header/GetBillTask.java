package com.lmis.util.barcode_scan_header;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.lmis.Lmis;
import com.lmis.support.LmisDialog;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * 自服务端查询运单信息
 * Created by chengdh on 2017/7/18.
 */

public class GetBillTask extends AsyncTask<Void, Void, Boolean> {

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

    Context mContext;

    GoodsInfo mGoodsInfo;
    Lmis mLmis;

    /**
     * Instantiates a new Search customer task.
     *
     * @param barcode 扫描的条码
     */
    public GetBillTask(Context ctx, Lmis lmis, GoodsInfo gs, String barcode) {
        mContext = ctx;
        mLmis = lmis;
        mGoodsInfo = gs;
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
            try {
                if (result.get("result").toString() == "null") {
                    Toast.makeText(mContext, "未查到运单信息!", Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject bill = result.getJSONObject("result");
                    mGoodsInfo.setmBillDate(bill.getString("bill_date"));
                    mGoodsInfo.setmFromOrgId(bill.getInt("from_org_id"));
                    mGoodsInfo.setmFromOrgName(bill.getString("from_org_name"));
                    mGoodsInfo.setmToOrgId(bill.getInt("to_org_id"));
                    mGoodsInfo.setmToOrgName(bill.getString("to_org_name"));
                    mGoodsInfo.setmGoodsNo(bill.getString("goods_no"));
                    mGoodsInfo.setmBillNo(bill.getString("bill_no"));
                    mGoodsInfo.setmID(bill.getInt("id"));
                    mGoodsInfo.setmGoodsInfo(bill.getString("goods_info"));
                    mGoodsInfo.setmGoodsNum(bill.getInt("goods_num"));
                    mGoodsInfo.setmCarryingFee(bill.getDouble("carrying_fee"));
                    mGoodsInfo.setmGoodsFee(bill.getDouble("goods_fee"));
                    mGoodsInfo.setmState(bill.getString("state"));
                    Toast.makeText(mContext, "已查到运单信息!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "查找运单信息失败!", Toast.LENGTH_SHORT).show();
        }
        pdialog.dismiss();
    }
}
