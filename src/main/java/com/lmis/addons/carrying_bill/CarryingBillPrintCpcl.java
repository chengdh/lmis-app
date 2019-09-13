package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.util.Log;

import zpSDK.zpSDK.*;

import com.lmis.orm.LmisDataRow;
import com.lmis.support.LmisUser;
import com.lmis.util.BlueTooth;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.monoid.json.JSONObject;

/**
 * 移动打印相关功能,使用cpcl语言
 * Created by chengdh on 14-10-24.
 */
public class CarryingBillPrintCpcl {

    public final static String TAG = "CarryingBillPrintCpcl";
    public final static String PRINTER_NAME = "CS3_8751";

    /**
     * Print void.
     *
     * @param ctx
     * @param bill        the bill
     * @param user
     * @param printerName
     */
    public static void print(Context ctx, LmisDataRow bill, LmisUser user, Boolean rePrint, String printerName) {
        zpBluetoothPrinter zpSDK = getZpBluetoothPrinter(ctx, printerName);
        if (zpSDK == null) return;

        printCarryingBill(bill, user, rePrint, zpSDK);
        printCarryingBillSecondPage(bill, user, rePrint, zpSDK);

        zpSDK.disconnect();


    }

    private static zpBluetoothPrinter getZpBluetoothPrinter(Context ctx, String printerName) {
        String devAddress = BlueTooth.getAddress(printerName);
        if (devAddress == null) {
            return null;
        }

        zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(ctx);
        zpSDK.connect(devAddress);
        return zpSDK;
    }

    public static void testPrint(zpBluetoothPrinter zpSDK) {
        zpSDK.pageSetup(568, 568);
        zpSDK.drawBarCode(8, 540, "12345678901234567", 128, true, 3, 60);
        zpSDK.drawQrCode(350, 48, "111111111", 0, 3, 0);
        zpSDK.drawText(90, 48 + 100, "400-8800-", 3, 0, 0, false, false);
        zpSDK.drawText(100, 48 + 100 + 56, "你好", 4, 0, 0, false, false);
        zpSDK.drawText(250, 48 + 100 + 56 + 56, "测试字符串1", 2, 0, 0, false, false);

        zpSDK.drawText(100, 48 + 100 + 56 + 56 + 80, "2015110101079-01-01   测试", 3, 0, 0, false, false);
        zpSDK.drawText(100, 48 + 100 + 56 + 56 + 80 + 80, "2015-11-01  23:00    测试", 3, 0, 0, false, false);

        zpSDK.drawBarCode(124, 48 + 100 + 56 + 56 + 80 + 80 + 80, "12345678901234567", 128, false, 3, 60);
        zpSDK.print(0, 0);
        zpSDK.disconnect();


    }

    private static String getServiceNote(JSONObject jsonBill) {
        //service note
        String serviceNote1 = "";
        String serviceNote2 = "";
        List serviceNotes = new ArrayList();
        try {
            if (jsonBill.getString("is_urgent").equals("true")) {
                serviceNote1 = "急";
                serviceNotes.add(serviceNote1);

            }
            if (jsonBill.getString("is_receipt").equals("true")) {
                serviceNote2 = "回单";
                serviceNotes.add(serviceNote2);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        String serviceNote = StringUtils.join(serviceNotes, "|");
        return serviceNote;
    }

    //打印运单第一联
    //传入的是json对象


    public static void printCarryingBillWithJson(JSONObject jsonBill, LmisUser user, Boolean rePrint, zpBluetoothPrinter zpSDK) {
        try {
            //203 dpi 8点/毫米
            zpSDK.pageSetup(600, 415);

            //打印时打印机会有部分边距
            int bottomRightX = 568;
            int bottomRightY = 370;
            //第一行
            int x1 = 0;
            int y1 = 0;

            zpSDK.drawText(x1, y1, "宇玖速运", 4, 0, 1, false, false);

            zpSDK.drawBox(5, x1 + 400, 0, bottomRightX - 20, y1 + 50);
            zpSDK.drawText(x1 + 420, y1 + 10, "客户联", 3, 0, 1, false, false);

            //外框
            int x2 = 0;
            int y2 = y1 + 60;
            zpSDK.drawBox(3, x2, y2, bottomRightX, bottomRightY);

            //运单号 目的地  货号
            int x21 = 0;
            int y21 = y2 + 10;
            String billNo = jsonBill.getString("bill_no");
            String goodsNo = jsonBill.getString("goods_no");

            String toOrgName = jsonBill.getString("to_org_name");
            zpSDK.drawText(x21 + 10, y21, toOrgName, 3, 0, 1, false, false);
            zpSDK.drawText(x21 + 10 + 120, y21, billNo, 3, 0, 1, false, false);
            zpSDK.drawText(x21 + 10 + 120 + 200, y21, goodsNo, 3, 0, 1, false, false);

            //service note
            String serviceNote = getServiceNote(jsonBill);
            if (serviceNote.length() > 0) {
                zpSDK.drawBox(5, x21 + 10 + 120 + 200 + 120, y21 - 5, bottomRightX - 10, y21 + 30);
                zpSDK.drawText(x21 + 10 + 120 + 200 + 120 + 20, y21 + 5, serviceNote, 2, 0, 1, false, false);

            }


            //直线
            zpSDK.drawLine(3, x21, y21 + 40, bottomRightX, y21 + 40, true);

            //收货网点/打印时间
            int x3 = 0;
            int y3 = y21 + 50;
            String fromOrgName = jsonBill.getString("from_org_name");
            String createdAt = jsonBill.getString("created_at_str");
            zpSDK.drawText(x3 + 10, y3, fromOrgName, 0, 0, 1, false, false);
            zpSDK.drawText(x3 + 300, y3, createdAt, 0, 0, 1, false, false);

            //直线
            zpSDK.drawLine(3, x3, y3 + 40, bottomRightX, y3 + 40, true);

            //寄方
            int x4 = 0;
            int y4 = y3 + 50;

            String fromCustomerName = jsonBill.getString("from_customer_name");
            String fromCustomerMobile = jsonBill.getString("from_customer_mobile");
            zpSDK.drawText(x4 + 10, y4, "寄", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40, y4, "寄件人:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 100, y4, fromCustomerName, 0, 0, 1, false, false);

            //竖线
            zpSDK.drawLine(3, x4 + 40 + 80 + 110, y3 + 40, x4 + 40 + 80 + 110, y4 + 40, true);

            zpSDK.drawText(x4 + 40 + 80 + 120, y4, "电话:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 80 + 120 + 80, y4, fromCustomerMobile, 0, 0, 1, false, false);


            zpSDK.drawLine(3, x4 + 40, y4 + 40, bottomRightX, y4 + 40, true);

            zpSDK.drawText(x4 + 10, y4 + 50, "方", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40, y4 + 60, "银行卡号:", 0, 0, 1, false, false);


            //横线
            zpSDK.drawLine(3, x4, y4 + 60 + 40, bottomRightX, y4 + 60 + 40, true);


            //竖线

            zpSDK.drawLine(3, x4 + 35, y4 - 10, x4 + 35, bottomRightY, true);

            //收方
            int x5 = 0;
            int y5 = y4 + 70 + 50;
            String toCustomerName = jsonBill.getString("to_customer_name");
            String toCustomerMobile = jsonBill.getString("to_customer_mobile");
            zpSDK.drawText(x5 + 10, y5, "收", 0, 0, 1, false, false);
            zpSDK.drawText(x5 + 40, y5, "收件人:", 0, 0, 1, false, false);
            zpSDK.drawText(x5 + 40 + 100, y5, toCustomerName, 0, 0, 1, false, false);

            //竖线
            zpSDK.drawLine(3, x5 + 40 + 80 + 110, y4 + 60 + 40, x5 + 40 + 80 + 110, y5 + 40, true);


            zpSDK.drawText(x5 + 40 + 80 + 120, y5, "电话:", 0, 0, 1, false, false);
            zpSDK.drawText(x5 + 40 + 80 + 120 + 80, y5, toCustomerMobile, 0, 0, 1, false, false);


            zpSDK.drawLine(3, x5 + 40, y5 + 40, bottomRightX, y5 + 40, true);

            zpSDK.drawText(x5 + 10, y5 + 50, "方", 0, 0, 1, false, false);
            zpSDK.drawText(x5 + 40, y5 + 50, "地址:", 0, 0, 1, false, false);


            zpSDK.print(0, 0);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }


    }

    //打印运单第二联
    public static void printCarryingBillSecondPageWithJson(JSONObject jsonBill, LmisUser user, Boolean rePrint, zpBluetoothPrinter zpSDK) {
        try {
            //203 dpi 8点/毫米
            zpSDK.pageSetup(600, 415);

            //打印时打印机会有部分边距
            int bottomRightX = 568;
            int bottomRightY = 370;
            //第一行
            int x1 = 0;
            int y1 = 0;

//        zpSDK.drawText(x1, y1, "宇玖速运", 4, 0, 1, false, false);
//
//        zpSDK.drawBox(5, x1 + 400, 0, bottomRightX - 20, y1 + 50);
//        zpSDK.drawText(x1 + 420, y1 + 10, "客户联", 3, 0, 1, false, false);

            //外框
            int x2 = 0;
            int y2 = 0;
            zpSDK.drawBox(3, x2, y2, bottomRightX, bottomRightY);

            //运单号 目的地  货号
            int x21 = 0;
            int y21 = y2 + 10;
            String billNo = jsonBill.getString("bill_no");
            String goodsNo = jsonBill.getString("goods_no");

            String toOrgName = jsonBill.getString("to_org_name");
            zpSDK.drawText(x21 + 10, y21, toOrgName, 3, 0, 1, false, false);
            zpSDK.drawText(x21 + 10 + 150, y21, billNo, 3, 0, 1, false, false);


            zpSDK.drawBox(5, x2 + 400, y21, bottomRightX - 20, y21 + 40);
            zpSDK.drawText(x2 + 420, y21 + 5, "客户联", 0, 0, 1, false, false);


            //横线
            zpSDK.drawLine(3, x21, y21 + 40, bottomRightX, y21 + 40, true);

            //货物信息
            int x3 = x2 + 10;
            int y3 = y2 + 60;
            zpSDK.drawText(x3, y3, "货", 0, 0, 1, false, false);
            zpSDK.drawText(x3, y3 + 30, "物", 0, 0, 1, false, false);
            zpSDK.drawText(x3, y3 + 30 + 30, "信", 0, 0, 1, false, false);
            zpSDK.drawText(x3, y3 + 30 + 30 + 30, "息", 0, 0, 1, false, false);
            //横线
            zpSDK.drawLine(3, x2, y3 + 30 + 30 + 30 + 30, bottomRightX, y3 + 30 + 30 + 30 + 30, true);
            //竖线
            zpSDK.drawLine(3, x3 + 30, y21 + 40, x3 + 30, y3 + 30 + 30 + 30 + 30, true);


            String goodsName = jsonBill.getString("goods_info");
            String goodsNum = jsonBill.getString("goods_num");
            String goodsVolume = jsonBill.getString("goods_volume");
            String goodsWeight = jsonBill.getString("goods_weight");
            String goodsPackage = jsonBill.getString("package");

            zpSDK.drawText(x3 + 40, y3, "名称:", 0, 0, 1, false, false);

            zpSDK.drawText(x3 + 40 + 60, y3, goodsName, 0, 0, 1, false, false);


            //竖线
            zpSDK.drawLine(3, x3 + 40 + 80 + 110, y21 + 40, x3 + 40 + 80 + 110, y3 + 30, true);

            zpSDK.drawText(x3 + 40 + 80 + 120, y3, "件数:", 0, 0, 1, false, false);

            zpSDK.drawText(x3 + 40 + 80 + 120 + 60, y3, goodsNum, 0, 0, 1, false, false);

            //横线
            zpSDK.drawLine(3, x3 + 30, y3 + 30, bottomRightX, y3 + 30, true);

            zpSDK.drawText(x3 + 40, y3 + 40, "重量:", 0, 0, 1, false, false);

            zpSDK.drawText(x3 + 40 + 60, y3 + 40, goodsWeight, 0, 0, 1, false, false);

            //竖线
            zpSDK.drawLine(3, x3 + 40 + 80 + 110, y3 + 30, x3 + 40 + 80 + 110, y3 + 40 + 30, true);

            zpSDK.drawText(x3 + 40 + 80 + 120, y3 + 40, "体积:", 0, 0, 1, false, false);

            zpSDK.drawText(x3 + 40 + 80 + 120 + 60, y3 + 40, goodsVolume, 0, 0, 1, false, false);

            //横线
            zpSDK.drawLine(3, x3 + 30, y3 + 40 + 30, bottomRightX, y3 + 40 + 30, true);


            zpSDK.drawText(x3 + 40, y3 + 40 + 40, "包装:", 0, 0, 1, false, false);
            zpSDK.drawText(x3 + 40 + 60, y3 + 40 + 40, goodsPackage, 0, 0, 1, false, false);

            //费用信息
            int x4 = 10;
            int y4 = y3 + 120;
            zpSDK.drawText(x4, y4, "费", 0, 0, 1, false, false);
            zpSDK.drawText(x4, y4 + 30, "用", 0, 0, 1, false, false);
            zpSDK.drawText(x4, y4 + 30 + 30, "信", 0, 0, 1, false, false);
            zpSDK.drawText(x4, y4 + 30 + 30 + 30, "息", 0, 0, 1, false, false);
            //横线
            zpSDK.drawLine(3, 0, y4 + 30 + 30 + 30 + 30, bottomRightX, y4 + 30 + 30 + 30 + 30, true);
            //竖线
            zpSDK.drawLine(3, x4 + 30, y3 + 30 + 30 + 30 + 30, x4 + 30, y4 + 30 + 30 + 30 + 30, true);


            String carryingFee = jsonBill.getString("carrying_fee");
            String fromShortCarryingFee = jsonBill.getString("from_short_carrying_fee");
            String toShortCarryingFee = jsonBill.getString("to_short_carrying_fee");
            String goodsFee = jsonBill.getString("goods_fee");
            String payType = PayType.payTypes().get(jsonBill.getString("pay_type"));
            String insuredFee = jsonBill.getString("insured_fee");

            zpSDK.drawText(x4 + 40, y4, "运费:", 0, 0, 1, false, false);

            zpSDK.drawText(x4 + 40 + 60, y4, carryingFee + "", 0, 0, 1, false, false);

            //竖线
            zpSDK.drawLine(3, x4 + 40 + 60 + 100, y3 + 30 + 30 + 30 + 30, x4 + 40 + 60 + 100, y4 + 30, true);


            zpSDK.drawText(x4 + 40 + 60 + 100 + 10, y4, "送货费:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 60 + 100 + 10 + 90, y4, toShortCarryingFee + "", 0, 0, 1, false, false);

            //竖线
            zpSDK.drawLine(3, x4 + 40 + 60 + 100 + 10 + 90 + 90, y3 + 30 + 30 + 30 + 30, x4 + 40 + 60 + 100 + 10 + 90 + 90, y4 + 30, true);


            zpSDK.drawText(x4 + 40 + 60 + 100 + 10 + 90 + 90 + 10, y4, "接货费:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 60 + 100 + 10 + 90 + 90 + 10+ 90, y4, fromShortCarryingFee + "", 0, 0, 1, false, false);

            //横线
            zpSDK.drawLine(3, x4 + 30, y4 + 30, bottomRightX, y4 + 30, true);


            zpSDK.drawText(x4 + 40, y4 + 40, "外转费:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 100, y4 + 40,"0" , 0, 0, 1, false, false);

            //竖线
            zpSDK.drawLine(3, x4 + 40 + 60 + 100, y4 + 30, x4 + 40 + 60 + 100, y4 + 40 + 30, true);


            zpSDK.drawText(x4 + 40 + 60 + 100 + 10, y4 + 40, "保价费:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 60 + 100 + 10 + 90, y4 + 40, insuredFee, 0, 0, 1, false, false);

            //横线
            zpSDK.drawLine(3, x4 + 30, y4 + 40 + 30, bottomRightX, y4 + 40 + 30, true);


            zpSDK.drawText(x4 + 40, y4 + 40 + 40, "代收款:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 100, y4 + 40 + 40, goodsFee + "", 0, 0, 1, false, false);


            //竖线
            zpSDK.drawLine(3, x4 + 40 + 60 + 100, y4 + 40 + 30, x4 + 40 + 60 + 100, y4 + 30 + 30 + 30 + 30, true);

            zpSDK.drawText(x4 + 40 + 60 + 100 + 10, y4 + 40 + 40, "付款方式:", 0, 0, 1, false, false);
            zpSDK.drawText(x4 + 40 + 60 + 100 + 10 + 120, y4 + 40 + 40, payType, 0, 0, 1, false, false);

            //寄件人签名
            int x5 = 10;
            int y5 = y4 + 120;

            zpSDK.drawText(x5, y5, "寄件人签名:", 0, 0, 1, false, false);

            zpSDK.drawLine(3, 0, y5 + 30, bottomRightX, y5 + 30, true);

            //客服电话
            int x6 = 10;
            int y6 = y5 + 40;

            zpSDK.drawText(x6, y6, "客服:400-618-5656", 0, 0, 1, false, false);


            zpSDK.print(0, 0);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }


    }

    //打印运单
    public static void printCarryingBill(LmisDataRow bill, LmisUser user, Boolean rePrint, zpBluetoothPrinter zpSDK) {
        try {
            JSONObject jsonBill = bill.exportAsJSON(false);
            printCarryingBillWithJson(jsonBill, user, rePrint, zpSDK);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }


    }

    //打印运单第二联
    public static void printCarryingBillSecondPage(LmisDataRow bill, LmisUser user, Boolean rePrint, zpBluetoothPrinter zpSDK) {
        try {
            JSONObject jsonBill = bill.exportAsJSON(false);
            printCarryingBillSecondPageWithJson(jsonBill, user, rePrint, zpSDK);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

    }

    /**
     * Print for json.
     *
     * @param ctx
     * @param bill        the bill
     * @param user        the user
     * @param rePrint     the re print
     * @param printerName
     */
    public static void printForJson(Context ctx, JSONObject bill, LmisUser user, Boolean rePrint, String printerName) {
        zpBluetoothPrinter zpSDK = getZpBluetoothPrinter(ctx, printerName);
        if (zpSDK == null) return;

        printCarryingBillWithJson(bill, user, rePrint, zpSDK);
        printCarryingBillSecondPageWithJson(bill, user, rePrint, zpSDK);
        zpSDK.disconnect();
    }


}
