package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.util.Log;

import com.example.tscdll.TSCActivity;
import com.lmis.orm.LmisDataRow;
import com.lmis.util.BlueTooth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.monoid.json.JSONObject;
import zpSDK.zpSDK.zpBluetoothPrinter;

/**
 * 移动打印标签相关功能
 * Created by chengdh on 14-10-24.
 */
public class LabelPrintCpcl {
    public final static String TAG = "LabelPrintCpcl";
    public final static String PRINTER_NAME = "CS3_8751";
    public final static int MAX_PRINT_LABEL_COUNT= 30;

    private static zpBluetoothPrinter getZpBluetoothPrinter(Context ctx, String printerName) {
        String devAddress = BlueTooth.getAddress(printerName);
        if (devAddress == null) {
            return null;
        }

        zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(ctx);
        zpSDK.connect(devAddress);
        return zpSDK;
    }



    public static void printLabelCpcl(Context ctx, LmisDataRow bill, int maxLabelCount, String printerName) {
        try {
            JSONObject jsonBill = bill.exportAsJSON(false);
            printLabelCpclWithJson(ctx, jsonBill, maxLabelCount, printerName);
        }
        catch(Exception ex){
            Log.e(TAG,ex.getMessage());
            ex.printStackTrace();
        }
    }
    //使用cpcl语言打印标签
    public static void printLabelCpclWithJson(Context ctx, JSONObject jsonBill, int maxLabelCount, String printerName) {
        zpBluetoothPrinter zpSDK = getZpBluetoothPrinter(ctx, printerName);
        if (zpSDK == null) return;
        try {

            int printCount = jsonBill.getInt("goods_num");
            if (printCount > maxLabelCount) {
                printCount = maxLabelCount;
            }

            List<String> labels = CarryingBillDB.getLabels(jsonBill);
            for (int i = 0; i <= printCount; i++) {

//            for (int i = 0; i < 1; i++) {
                String label = labels.get(i);

                //203 dpi 8点/毫米
//                zpSDK.pageSetup(600, 415);
                zpSDK.pageSetup(560, 400);

                //画外框
                zpSDK.drawBox(3, 0, 0, 568, 370);
                zpSDK.drawText(8, 8, "宇玖速运", 4, 0, 1, false, false);
                zpSDK.drawText(230, 8, jsonBill.getString("bill_no"), 4, 0, 1, false, false);


                zpSDK.drawLine(3, 0, 80, 568, 80, true);

                zpSDK.drawLine(3, 64, 80, 64, 370, true);

                zpSDK.drawText(8 + 8, 88, "目", 0, 0, 1, false, false);
                zpSDK.drawText(8 + 8, 112, "的", 0, 0, 1, false, false);
                zpSDK.drawText(8 + 8, 136, "地", 0, 0, 1, false, false);


                String toOrgName = jsonBill.getString("to_org_name");
                zpSDK.drawText(64 + 16, 88, toOrgName, 4, 0, 1, false, false);


                zpSDK.drawLine(3, 0, 160, 568, 160, true);


                zpSDK.drawText(8 + 8, 160 + 8 + 24, "货", 0, 0, 1, false, false);
                zpSDK.drawText(8 + 8, 184 + 8 + 24, "物", 0, 0, 1, false, false);
                zpSDK.drawText(8 + 8, 208 + 8 + 24, "信", 0, 0, 1, false, false);
                zpSDK.drawText(8 + 8, 232 + 8 + 24, "息", 0, 0, 1, false, false);


                String seq = String.format("%s/%s", i + 1, labels.size());
                zpSDK.drawText(64 + 20 , 168 + 16 + 24, seq, 3, 0, 0, false, false);
                zpSDK.drawBarCode(64 + 20+ 100, 168 + 16, label, 128, false, 2, 80);

                zpSDK.drawText(64 + 20 + 100 , 168 + 16 + 88, label, 3, 0, 0, false, false);

                zpSDK.drawLine(3, 0, 280 + 32, 568, 280 + 32, true);


                zpSDK.drawText(8 + 8, 280 + 8 + 24, "始", 0, 0, 1, false, false);
                zpSDK.drawText(8 + 8, 280 + 8 + 24 + 30, "发", 0, 0, 1, false, false);
                zpSDK.drawText(8 + 8 +20, 280 + 8 + 24 + 15, "地", 0, 0, 1, false, false);


                String fromOrgName = jsonBill.getString("from_org_name");
                String createdAt = jsonBill.getString("created_at_str");
                zpSDK.drawText(64 + 16, 280 + 8 + 40, fromOrgName, 0, 0, 1, false, false);
                zpSDK.drawText(64 + 16 + 240, 280 + 8 + 40, createdAt, 0, 0, 1, false, false);

                zpSDK.print(0, 1);

            }


            zpSDK.disconnect();
        }
        catch(Exception ex){
            Log.e(TAG,ex.getMessage());
            ex.printStackTrace();
        }



    }

}
