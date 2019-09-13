package com.lmis.addons.short_list;

import android.content.Context;
import android.util.Log;

import com.lmis.addons.carrying_bill.CarryingBillDB;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.util.BlueTooth;

import java.util.List;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import zpSDK.zpSDK.zpBluetoothPrinter;

/**
 * 短驳单打印
 * Created by chengdh on 14-10-24.
 */
public class ShortListPrintCpcl {
    public final static String TAG = "ShortListPrintCpcl";
    public final static String PRINTER_NAME = "CS3_8751";

    private static zpBluetoothPrinter getZpBluetoothPrinter(Context ctx, String printerName) {
        String devAddress = BlueTooth.getAddress(printerName);
        if (devAddress == null) {
            return null;
        }

        zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(ctx);
        zpSDK.connect(devAddress);
        return zpSDK;
    }


    public static void printShortListCpcl(Context ctx, LmisDataRow bill, String printerName) {
        try {
            JSONObject jsonBill = bill.exportAsJSON(false);
            printShortListCpclWithJson(ctx, jsonBill, printerName);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
    }

    //使用cpcl语言打印标签
    public static void printShortListCpclWithJson(Context ctx, JSONObject jsonBill, String printerName) {
        zpBluetoothPrinter zpSDK = getZpBluetoothPrinter(ctx, printerName);
        if (zpSDK == null) return;
        zpSDK.pageSetup(600, 415);

        //打印时打印机会有部分边距
        int bottomRightX = 568;
        int bottomRightY = 370;


        try {
            int fromOrgId = jsonBill.getInt("from_org_id");
            int toOrgId = jsonBill.getInt("to_org_id");
            OrgDB orgDB = new OrgDB(ctx);
            LmisDataRow fromOrg = orgDB.select(fromOrgId);
            LmisDataRow toOrg = orgDB.select(toOrgId);
            String fromOrgName = fromOrg.getString("name");
            String toOrgName = toOrg.getString("name");
            String driver = jsonBill.getString("driver");
            String vehicleNo = jsonBill.getString("vehicle_no");
            String mobile = jsonBill.getString("mobile");
            String billDate = jsonBill.getString("bill_date");

            //第一行
            int x1 = 0;
            int y1 = 0;

            zpSDK.drawText(x1 + 250, y1, "短驳单", 4, 0, 1, false, false);
            String fromTo = String.format("%s 至 %s  %s", fromOrgName, toOrgName, billDate);
            zpSDK.drawText(x1 + 30, y1 + 50 + 10, fromTo, 2, 0, 1, false, false);
            String note = String.format("%s  %s   %s", vehicleNo, driver, mobile);
            zpSDK.drawText(x1 + 30, y1 + 50 + 10 + 40, note, 2, 0, 1, false, false);

            zpSDK.drawLine(3, x1, y1 + 50 + 10 + 40 + 20, bottomRightX, y1 + 50 + 10 + 40 + 20, true);

            JSONArray lines = jsonBill.getJSONArray("carrying_bills_attributes");

            int x2 = 0;
            int y2 = y1 + 50 + 10 + 40 + 20 + 20;

            int firstLinesSize = 6;
            if (lines.length() <= 6) {
                firstLinesSize = lines.length();
            }
            JSONArray firstPageLines = new JSONArray();
            for (int j = 0; j < firstLinesSize; j++) {
                firstPageLines.put(lines.getJSONObject(j));
            }
            printFirstPageLinesWithJson(ctx, firstPageLines, y2, zpSDK);
            zpSDK.print(0, 0);
            zpSDK.disconnect();

            //打印剩余的行数

            JSONArray leftLines = new JSONArray();
            for (int k = firstLinesSize; k < lines.length(); k++) {
                leftLines.put(lines.getJSONObject(k));
            }
            printLeftLinesWithJson(ctx, leftLines, printerName);

        } catch (
                Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

    }

    public static void printFirstPageLinesWithJson(Context ctx, JSONArray jsonLines, int startY, zpBluetoothPrinter zpSDK) {
        //打印时打印机会有部分边距
        int bottomRightX = 568;
        int bottomRightY = 370;

        int x2 = 0;
        int y2 = startY;
        try {
            for (int i = 0; i < jsonLines.length(); i++) {
                JSONObject line = jsonLines.getJSONObject(i);

                int stepY = 40 * i;


                String billNo = line.getString("bill_no");
                String goodsInfo = line.getString("goods_info");
                String goodsNum = line.getString("goods_num");
                String fOrgName = line.getString("from_org_name");
                String tOrgName = line.getString("to_org_name");

                String ft = String.format("%s至%s", fOrgName, tOrgName);

                String lineDes = String.format("%s %s %s %s件", billNo, ft, goodsInfo, goodsNum);

                zpSDK.drawText(x2 + 10, y2 + stepY, lineDes, 2, 0, 1, false, false);

                zpSDK.drawLine(3, x2, y2 + stepY + 30, bottomRightX, y2 + stepY + 30, true);

            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void printLeftLinesWithJson(Context ctx, JSONArray jsonLines, String printerName) {
        zpBluetoothPrinter zpSDK = getZpBluetoothPrinter(ctx, printerName);
        if (zpSDK == null) return;
        zpSDK.pageSetup(600, 415);

        //打印时打印机会有部分边距
        int bottomRightX = 568;
        int bottomRightY = 370;

        int x2 = 0;
        int y2 = 0;
        try {
            for (int i = 0; i < jsonLines.length(); i++) {
                JSONObject line = jsonLines.getJSONObject(i);

                int stepY = 40 * i;

                //判断是否超过打印高度
                if (i > 0 && i % 10 == 0) {
                    JSONArray leftLines = new JSONArray();
                    for (int j = i; j < jsonLines.length(); j++) {
                        leftLines.put(jsonLines.getJSONObject(j));
                    }
                    printLeftLinesWithJson(ctx, leftLines, printerName);

                } else {

                    String billNo = line.getString("bill_no");
                    String goodsInfo = line.getString("goods_info");
                    String goodsNum = line.getString("goods_num");
                    String fOrgName = line.getString("from_org_name");
                    String tOrgName = line.getString("to_org_name");

                    String ft = String.format("%s至%s", fOrgName, tOrgName);

                    String lineDes = String.format("%s %s %s %s件", billNo, ft, goodsInfo, goodsNum);

                    zpSDK.drawText(x2 + 10, y2 + stepY, lineDes, 2, 0, 1, false, false);

                    zpSDK.drawLine(3, x2, y2 + stepY + 30, bottomRightX, y2 + stepY + 30, true);
                }

            }
            zpSDK.print(0, 0);
            zpSDK.disconnect();
        } catch (
                Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
    }


}
