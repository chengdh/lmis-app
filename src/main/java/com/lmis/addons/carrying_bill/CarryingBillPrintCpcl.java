package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.util.Log;

import com.example.tscdll.TSCActivity;

import zpSDK.zpSDK.*;

import com.lmis.orm.LmisDataRow;
import com.lmis.support.LmisUser;
import com.lmis.util.BlueTooth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import us.monoid.json.JSONObject;

/**
 * 移动打印相关功能,使用cpcl语言
 * Created by chengdh on 14-10-24.
 */
public class CarryingBillPrintCpcl {
    /**
     * Print void.
     *
     * @param ctx
     * @param bill the bill
     * @param user
     */
    public static void print(Context ctx, LmisDataRow bill, LmisUser user, Boolean rePrint) {
        String devAddress = BlueTooth.getAddress("CS3_8751");
        if (devAddress == null) {
            return;
        }

//        zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(ctx);
//        zpSDK.connect(devAddress);


        printCarryingBill(ctx, bill, user, rePrint);

//        zpSDK.pageSetup(568, 568);
//        zpSDK.drawBarCode(8, 540, "12345678901234567", 128, true, 3, 60);
//        zpSDK.drawQrCode(350, 48, "111111111", 0, 3, 0);
//        zpSDK.drawText(90, 48 + 100, "400-8800-", 3, 0, 0, false, false);
//        zpSDK.drawText(100, 48 + 100 + 56, "你好", 4, 0, 0, false, false);
//        zpSDK.drawText(250, 48 + 100 + 56 + 56, "测试字符串1", 2, 0, 0, false, false);
//
//        zpSDK.drawText(100, 48 + 100 + 56 + 56 + 80, "2015110101079-01-01   测试", 3, 0, 0, false, false);
//        zpSDK.drawText(100, 48 + 100 + 56 + 56 + 80 + 80, "2015-11-01  23:00    测试", 3, 0, 0, false, false);
//
//        zpSDK.drawBarCode(124, 48 + 100 + 56 + 56 + 80 + 80 + 80, "12345678901234567", 128, false, 3, 60);
//        zpSDK.print(0, 0);
//        zpSDK.disconnect();

//        TSCActivity tscDll = new TSCActivity();
//
//        tscDll.openport(devAddress);
//
//        tscDll.clearbuffer();
//        List commands = formatPrintCommand(bill, user, rePrint);
//        try {
//            for (Object cmd : commands) {
//                if (cmd instanceof String)
//                    tscDll.sendcommand(cmd.toString());
//
//                if (cmd instanceof byte[]) {
//                    tscDll.sendcommand((byte[]) cmd);
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        Log.d("bluetooth print", tscDll.status());
//        tscDll.closeport();
    }

    //打印运单
    public static void printCarryingBill(Context ctx, LmisDataRow bill, LmisUser user, Boolean rePrint) {
        String devAddress = BlueTooth.getAddress("CS3_8751");
        if (devAddress == null) {
            return;
        }
        zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(ctx);
        zpSDK.connect(devAddress);


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
        String billNo = bill.getString("bill_no");
        String goodsNo = bill.getString("goods_no");

        String toOrgName = bill.getM2ORecord("to_org_id").browse().getString("name");
        zpSDK.drawText(x21 + 10, y21, toOrgName, 3, 0, 1, false, false);
        zpSDK.drawText(x21 + 10 + 150, y21, billNo, 3, 0, 1, false, false);
        zpSDK.drawText(x21 + 10 + 150 + 150, y21, goodsNo, 3, 0, 1, false, false);


        //直线
        zpSDK.drawLine(3, x21, y21 + 40, bottomRightX, y21 + 40, true);

        //收货网点/打印时间
        int x3 = 0;
        int y3 = y21 + 50;
        String fromOrgName = bill.getM2ORecord("from_org_id").browse().getString("name");
        String createdAt = bill.getString("created_at");
        zpSDK.drawText(x3 + 10, y3, fromOrgName, 0, 0, 1, false, false);
        zpSDK.drawText(x3 + 300, y3, createdAt, 0, 0, 1, false, false);

        //直线
        zpSDK.drawLine(3, x3, y3 + 40, bottomRightX, y3 + 40, true);

        //寄方
        int x4 = 0;
        int y4 = y3 + 50;

        String fromCustomerName = bill.getString("from_customer_name");
        String fromCustomerMobile = bill.getString("from_customer_mobile");
        zpSDK.drawText(x4 + 10, y4, "寄", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40, y4, "寄件人:", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40 + 80, y4, fromCustomerName, 0, 0, 1, false, false);

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
        String toCustomerName = bill.getString("to_customer_name");
        String toCustomerMobile = bill.getString("to_customer_mobile");
        zpSDK.drawText(x5 + 10, y5, "收", 0, 0, 1, false, false);
        zpSDK.drawText(x5 + 40, y5, "收件人:", 0, 0, 1, false, false);
        zpSDK.drawText(x5 + 40 + 80, y5, toCustomerName, 0, 0, 1, false, false);

        //竖线
        zpSDK.drawLine(3, x5 + 40 + 80 + 110, y4 + 60 + 40, x5 + 40 + 80 + 110, y5 + 40, true);


        zpSDK.drawText(x5 + 40 + 80 + 120, y5, "电话:", 0, 0, 1, false, false);
        zpSDK.drawText(x5 + 40 + 80 + 120 + 80, y5, toCustomerMobile, 0, 0, 1, false, false);


        zpSDK.drawLine(3, x5 + 40, y5 + 40, bottomRightX, y5 + 40, true);

        zpSDK.drawText(x5 + 10, y5 + 50, "方", 0, 0, 1, false, false);
        zpSDK.drawText(x5 + 40, y5 + 50, "地址:", 0, 0, 1, false, false);


        zpSDK.print(0, 0);

        zpSDK.disconnect();

        printCarryingBillSecondPage(ctx, bill, user, rePrint);

    }

    //打印运单第二联
    public static void printCarryingBillSecondPage(Context ctx, LmisDataRow bill, LmisUser user, Boolean rePrint) {
        String devAddress = BlueTooth.getAddress("CS3_8751");
        if (devAddress == null) {
            return;
        }
        zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(ctx);
        zpSDK.connect(devAddress);


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
        String billNo = bill.getString("bill_no");
        String goodsNo = bill.getString("goods_no");

        String toOrgName = bill.getM2ORecord("to_org_id").browse().getString("name");
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


        String goodsName = bill.getString("goods_info");
        String goodsNum = bill.getString("goods_num");
        String goodsVolume = bill.getString("goods_volume");
        zpSDK.drawText(x3 + 40, y3, "名称:", 0, 0, 1, false, false);

        zpSDK.drawText(x3 + 40 + 60, y3, goodsName, 0, 0, 1, false, false);


        //竖线
        zpSDK.drawLine(3, x3 + 40 + 80 + 110, y21 + 40, x3 + 40 + 80 + 110, y3 + 30, true);

        zpSDK.drawText(x3 + 40 + 80 + 120, y3, "件数:", 0, 0, 1, false, false);

        zpSDK.drawText(x3 + 40 + 80 + 120 + 60, y3, goodsNum, 0, 0, 1, false, false);

        //横线
        zpSDK.drawLine(3, x3 + 30, y3 + 30, bottomRightX, y3 + 30, true);

        zpSDK.drawText(x3 + 40, y3 + 40, "重量:", 0, 0, 1, false, false);

        zpSDK.drawText(x3 + 40 + 60, y3 + 40, "0", 0, 0, 1, false, false);

        //竖线
        zpSDK.drawLine(3, x3 + 40 + 80 + 110, y3 + 30, x3 + 40 + 80 + 110, y3 + 40+ 30, true);

        zpSDK.drawText(x3 + 40 + 80 + 120, y3 + 40, "体积:", 0, 0, 1, false, false);

        zpSDK.drawText(x3 + 40 + 80 + 120 + 60, y3 + 40, goodsVolume, 0, 0, 1, false, false);

        //横线
        zpSDK.drawLine(3, x3 + 30, y3 + 40 + 30, bottomRightX, y3 + 40 + 30, true);


        zpSDK.drawText(x3 + 40, y3 + 40 + 40, "包装:", 0, 0, 1, false, false);
        zpSDK.drawText(x3 + 40 + 60 , y3 + 40 + 40, "无", 0, 0, 1, false, false);

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


        int carryingFee = bill.getInt("carrying_fee");
        int fromShortCarryingFee = bill.getInt("from_short_carrying_fee");
        int toShortCarryingFee = bill.getInt("to_short_carrying_fee");
        int goodsFee = bill.getInt("goods_fee");
        String payType = PayType.payTypes().get(bill.getString("pay_type"));

        zpSDK.drawText(x4 + 40, y4, "运费:", 0, 0, 1, false, false);

        zpSDK.drawText(x4 + 40 + 60, y4, carryingFee + "", 0, 0, 1, false, false);

         //竖线
        zpSDK.drawLine(3, x4 + 40 + 60 + 70, y3 + 30 + 30 + 30 + 30, x4 + 40 + 60 + 70, y4 + 30, true);


        zpSDK.drawText(x4 + 40 + 60 + 80 + 10, y4, "送货费:", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40 + 60 + 80 + 10+ 60, y4, toShortCarryingFee + "", 0, 0, 1, false, false);

        //竖线
        zpSDK.drawLine(3, x4 + 40 + 60 + 80 + 10+ 60 + 60, y3 + 30 + 30 + 30 + 30, x4 + 40 + 60 + 80 + 10+ 60 + 60, y4 + 30, true);


        zpSDK.drawText(x4 + 40 + 60 + 80 + 10+ 60 + 80 , y4, "接货费:", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40 + 60 + 80 + 10+ 60 + 80 + 80, y4, fromShortCarryingFee + "", 0, 0, 1, false, false);

        //横线
        zpSDK.drawLine(3, x4 + 30, y4 + 30, bottomRightX, y4 + 30, true);


        zpSDK.drawText(x4 + 40, y4 + 40, "外转费:", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40 + 80, y4 + 40, "0", 0, 0, 1, false, false);

        //竖线
        zpSDK.drawLine(3, x4 + 40 + 60 + 70 , y4 + 30, x4 + 40 + 60 + 70, y4 + 40+ 30,  true);


        zpSDK.drawText(x4 + 40 + 60 + 80, y4 + 40, "保价费:", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40 + 60 + 80 + 80, y4 + 40, "0", 0, 0, 1, false, false);

        //横线
        zpSDK.drawLine(3, x4 + 30, y4 + 40 + 30, bottomRightX, y4 + 40 + 30, true);


        zpSDK.drawText(x4 + 40, y4 + 40 + 40, "代收款:", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40 + 80, y4 + 40 + 40, goodsFee + "", 0, 0, 1, false, false);


        //竖线
        zpSDK.drawLine(3, x4 + 40 + 60 + 70 , y4 + 40+ 30, x4 + 40 + 60 + 70, y4 + 30 +30 + 30 + 30,  true);

        zpSDK.drawText(x4 + 40 + 60 + 80, y4 + 40 + 40, "付款方式:", 0, 0, 1, false, false);
        zpSDK.drawText(x4 + 40 + 60 + 80 + 120, y4 + 40 + 40, payType, 0, 0, 1, false, false);

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

        zpSDK.disconnect();


    }

    public static void testPrint(TSCActivity tsc) {
        try {
            byte[] qq2 = new byte[1024];
            tsc.sendcommand("CODEPAGE UTF-8\n");
            //tsc.setup(70, 50, 4, 4, 0, 0, 0);
            tsc.sendcommand("CLS\n");
            String name = "河南开瑞物流有限公司";
            qq2 = name.getBytes("GB2312");
            String string1 = new String(qq2, "GB2312");
            tsc.sendcommand("TEXT 5,90,\"Font001\",0,5,5,\"");
            tsc.sendcommand(qq2);
            tsc.sendcommand("\"\n");
            tsc.sendcommand("PRINT 1\n");
            tsc.closeport();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 小票打印指令
     * The constant PRINT_CONTENT.
     *
     * @param bill    the bill
     * @param user
     * @param rePrint the re print  @return the list
     */
    public static List<Object> formatPrintCommand(LmisDataRow bill, LmisUser user, Boolean rePrint) {
        List<Object> commands = new ArrayList<Object>();
        try {
            String curDateTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            commands.addAll(Arrays.asList(
                    "CODEPAGE UTF-8\n",
                    "SIZE 70 mm,170 mm\n",
                    "GAP 0,0\n",
                    "SET PRINTKEY OFF\n",
                    "DIRECTION 0\n",
                    "CLS\n",
                    "PUTBMP 165,5,\"logo.BMP\"\n",
                    "BAR 15,50,500,3\n",

                    "TEXT 65,60,\"Font001\",0,3,3,\"",
                    "河南凯瑞物流有限公司".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 185,110,\"Font001\",0,2,2,\"",
                    "运输协议单".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 150,140,\"5.EFT\",0,1,1,\"",
                    String.format("%s", bill.getString("bill_no")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,200,500,3\n",

                    "TEXT 15,210,\"Font001\",0,2,2,\"",
                    String.format("日    期: %s", curDateTimeStr).getBytes("GB2312"),
                    "\"\n",

                    //货号
                    "TEXT 15,250,\"Font001\",0,3,3,\"",
                    String.format("货    号:%s", bill.getString("goods_no").substring(2)).getBytes("GB2312"),
                    "\"\n",

                    //时间
//                    "TEXT 15,210,\"Font001\",0,2,2,\"",
//                    String.format("时    间:%s", bill.getString("bill_date").substring(2)).getBytes("GB2312"),
//                    "\"\n",

                    "BAR 15,290,500,1\n",

                    "TEXT 15,300,\"Font001\",0,2,2,\"",
                    String.format("发 货 地:%s", bill.getM2ORecord("from_org_id").browse().getString("name")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,340,\"Font001\",0,2,2,\"",
                    String.format("到 货 地:").getBytes("GB2312"),
                    "\"\n",
                    "TEXT 135,340,\"Font001\",0,3,3,\"",
                    String.format("%s", bill.getM2ORecord("to_org_id").browse().getString("name")).getBytes("GB2312"),
                    "\"\n",


                    "BAR 15,380,500,1\n",


                    "TEXT 15,400,\"Font001\",0,2,2,\"",
                    String.format("客户卡号:%s", bill.getString("from_customer_code") == "false" ? "[无]" : bill.getString("from_customer_code")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,440,\"Font001\",0,2,2,\"",
                    String.format("发 货 人:%s", bill.getString("from_customer_name")).getBytes("GB2312"),
                    "\"\n",
                    "TEXT 280,440,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("from_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,480,\"Font001\",0,2,2,\"",
                    String.format("收 货 人:%s", bill.getString("to_customer_name")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 280,480,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("to_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,520,500,1\n",

                    "TEXT 15,530,\"Font001\",0,2,2,\"",
                    String.format("支付方式:%s", PayType.payTypes().get(bill.getString("pay_type"))).getBytes("GB2312"),
                    "\"\n",

//                    "TEXT 15,450,\"Font001\",0,2,2,\"",
//                    String.format("发货短途:%s元 ", bill.getInt("from_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",
//
//                    "TEXT 15,480,\"Font001\",0,2,2,\"",
//                    String.format("到货短途:%s元 ", bill.getInt("to_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",


                    "TEXT 15,570,\"Font001\",0,2,2,\"",
                    "运费总计:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,570,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getInt("carrying_fee") + bill.getInt("from_short_carrying_fee") + bill.getInt("to_short_carrying_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 280,570,\"Font001\",0,2,2,\"",
                    "保 险 费:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 395,570,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getInt("insured_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,610,\"Font001\",0,2,2,\"",
                    "代收货款:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,610,\"Font001\",0,3,3,\"",
                    String.format("%s元", bill.getInt("goods_fee")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,650,500,2\n",

                    "TEXT 15,660,\"Font001\",0,2,2,\"",
                    "货物名称                   数量".getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,700,500,1\n",

                    "TEXT 15,710,\"Font001\",0,2,2,\"",
                    String.format("%s                        %s", bill.getString("goods_info"), bill.getInt("goods_num")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,750,500,1\n",

                    "TEXT 15,760,\"Font001\",0,2,2,\"",
                    String.format("备    注:%s", bill.getString("note")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,800,\"Font001\",0,2,2,\"",
                    String.format("开 票 人:%S : ", user.getAndroidName()).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,850,\"Font001\",0,2,2,\"",
                    "发货人签字:________________".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,920,\"Font001\",0,2,2,\"",
                    "1.本票据以我公司同期票据运输协议条款为依据".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,960,\"Font001\",0,2,2,\"",
                    "2.本协议等同运输合同，有效期为三十天".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1000,\"Font001\",0,2,2,\"",
                    "3.严禁托运国家规定的危险品，违禁管制物品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1040,\"Font001\",0,2,2,\"",
                    "  及假冒伪劣产品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1080,\"Font001\",0,2,2,\"",
                    "4.发货人请核对发货票据信息,如有问题请及时".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1120,\"Font001\",0,2,2,\"",
                    "  更正票据信息".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,1160,\"Font001\",0,2,2,\"",
                    "5.未盖我公司收货章为无效票;".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1200,\"Font001\",0,2,2,\"",
                    "6.温馨提示:此票为热敏票据,请妥善保存!".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1240,\"Font001\",0,2,2,\"",
                    "全国统一客服热线:400-619-4448".getBytes("GB2312"),
                    "\"\n"


            ));
            if (rePrint) {
                String cmd1 = "TEXT 15,1280,\"Font001\",0,2,2,\"";
                byte[] cmd2 = "[重打]".getBytes("GB2312");
                String cmd3 = "\"\n";
                commands.add(cmd1);
                commands.add(cmd2);
                commands.add(cmd3);
            }
            commands.add("PRINT 1\n");
            commands.add("DELAY 5000\n");
            commands.add("PRINT 1\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return commands;
    }

    /**
     * Print for json.
     *
     * @param bill    the bill
     * @param user    the user
     * @param rePrint the re print
     */
    public static void printForJson(JSONObject bill, LmisUser user, Boolean rePrint) {
        String devAddress = BlueTooth.getAddress("BT-SPP");
        if (devAddress == null) {
            return;
        }

        TSCActivity tscDll = new TSCActivity();

        tscDll.openport(devAddress);

        tscDll.clearbuffer();
        List commands = formatPrintCommandForJson(bill, user, rePrint);
        try {
            for (Object cmd : commands) {
                if (cmd instanceof String)
                    tscDll.sendcommand(cmd.toString());

                if (cmd instanceof byte[]) {
                    tscDll.sendcommand((byte[]) cmd);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Log.d("bluetooth print", tscDll.status());
        tscDll.closeport();
    }


    /**
     * Format print command for json list.
     *
     * @param bill    the bill
     * @param user    the user
     * @param rePrint the re print
     * @return the list
     */
    public static List<Object> formatPrintCommandForJson(JSONObject bill, LmisUser user, Boolean rePrint) {
        List<Object> commands = new ArrayList<Object>();
        try {
            String curDateTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            commands.addAll(Arrays.asList(
                    "CODEPAGE UTF-8\n",
                    "SIZE 70 mm,170 mm\n",
                    "GAP 0,0\n",
                    "SET PRINTKEY OFF\n",
                    "DIRECTION 0\n",
                    "CLS\n",
                    "PUTBMP 165,5,\"logo.BMP\"\n",
                    "BAR 15,50,500,3\n",

                    "TEXT 65,60,\"Font001\",0,3,3,\"",
                    "河南凯瑞物流有限公司".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 185,110,\"Font001\",0,2,2,\"",
                    "运输协议单".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 150,140,\"5.EFT\",0,1,1,\"",
                    String.format("%s", bill.getString("bill_no")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,200,500,3\n",

                    "TEXT 15,210,\"Font001\",0,2,2,\"",
                    String.format("日    期: %s", curDateTimeStr).getBytes("GB2312"),
                    "\"\n",

                    //货号
                    "TEXT 15,250,\"Font001\",0,2,2,\"",
                    String.format("货    号:%s", bill.getString("goods_no").substring(2)).getBytes("GB2312"),
                    "\"\n",

                    //时间
//                    "TEXT 15,210,\"Font001\",0,2,2,\"",
//                    String.format("时    间:%s", bill.getString("bill_date").substring(2)).getBytes("GB2312"),
//                    "\"\n",

                    "BAR 15,290,500,1\n",

                    "TEXT 15,300,\"Font001\",0,2,2,\"",
                    String.format("发 货 地:%s", bill.getString("from_org_name")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,340,\"Font001\",0,2,2,\"",
                    String.format("到 货 地:").getBytes("GB2312"),
                    "\"\n",
                    "TEXT 135,340,\"Font001\",0,3,3,\"",
                    String.format("%s", bill.getString("to_org_name")).getBytes("GB2312"),
                    "\"\n",


                    "BAR 15,380,500,1\n",


                    "TEXT 15,400,\"Font001\",0,2,2,\"",
                    String.format("客户卡号:%s", bill.getString("from_customer_code") == "false" ? "[无]" : bill.getString("from_customer_code")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,440,\"Font001\",0,2,2,\"",
                    String.format("发 货 人:%s", bill.getString("from_customer_name")).getBytes("GB2312"),
                    "\"\n",
                    "TEXT 280,440,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("from_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,480,\"Font001\",0,2,2,\"",
                    String.format("收 货 人:%s", bill.getString("to_customer_name")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 280,480,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("to_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,520,500,1\n",

                    "TEXT 15,530,\"Font001\",0,2,2,\"",
                    String.format("支付方式:%s", bill.getString("pay_type_des")).getBytes("GB2312"),
                    "\"\n",

//                    "TEXT 15,450,\"Font001\",0,2,2,\"",
//                    String.format("发货短途:%s元 ", bill.getInt("from_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",
//
//                    "TEXT 15,480,\"Font001\",0,2,2,\"",
//                    String.format("到货短途:%s元 ", bill.getInt("to_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",


                    "TEXT 15,570,\"Font001\",0,2,2,\"",
                    "运费总计:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,570,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getDouble("carrying_fee") + bill.getDouble("from_short_carrying_fee") + bill.getDouble("to_short_carrying_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 280,570,\"Font001\",0,2,2,\"",
                    "保 险 费:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 395,570,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getDouble("insured_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,610,\"Font001\",0,2,2,\"",
                    "代收货款:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,610,\"Font001\",0,3,3,\"",
                    String.format("%s元", bill.getDouble("goods_fee")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,650,500,2\n",

                    "TEXT 15,660,\"Font001\",0,2,2,\"",
                    "货物名称                   数量".getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,700,500,1\n",

                    "TEXT 15,710,\"Font001\",0,2,2,\"",
                    String.format("%s                        %s", bill.getString("goods_info"), bill.getInt("goods_num")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,750,500,1\n",

                    "TEXT 15,760,\"Font001\",0,2,2,\"",
                    String.format("备    注:%s", bill.getString("note")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,800,\"Font001\",0,2,2,\"",
                    String.format("开 票 人:%S : ", user.getAndroidName()).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,850,\"Font001\",0,2,2,\"",
                    "发货人签字:________________".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,920,\"Font001\",0,2,2,\"",
                    "1.本票据以我公司同期票据运输协议条款为依据".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,960,\"Font001\",0,2,2,\"",
                    "2.本协议等同运输合同，有效期为三十天".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1000,\"Font001\",0,2,2,\"",
                    "3.严禁托运国家规定的危险品，违禁管制物品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1040,\"Font001\",0,2,2,\"",
                    "  及假冒伪劣产品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1080,\"Font001\",0,2,2,\"",
                    "4.发货人请核对发货票据信息,如有问题请及时".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1120,\"Font001\",0,2,2,\"",
                    "  及时更正票据信息".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,1160,\"Font001\",0,2,2,\"",
                    "5.未盖我公司收货章为无效票;".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1200,\"Font001\",0,2,2,\"",
                    "6.温馨提示:此票为热敏票据,请妥善保存!".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1240,\"Font001\",0,2,2,\"",
                    "全国统一客服热线:400-619-4448".getBytes("GB2312"),
                    "\"\n"


            ));

            if (rePrint) {
                String cmd1 = "TEXT 15,1240,\"Font001\",0,2,2,\"";
                byte[] cmd2 = "[重打]".getBytes("GB2312");
                String cmd3 = "\"\n";
                commands.add(cmd1);
                commands.add(cmd2);
                commands.add(cmd3);
            }

            commands.add("PRINT 1\n");
            commands.add("DELAY 5000\n");
            commands.add("PRINT 1\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return commands;
    }
}