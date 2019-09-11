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
import zpSDK.zpSDK.PrinterInterface;
import zpSDK.zpSDK.zpBluetoothPrinter;

/**
 * 移动打印标签相关功能
 * Created by chengdh on 14-10-24.
 */
public class LabelPrint {
    /**
     * Print void.
     *
     * @param ctx
     * @param bill the bill
     */
    public static void print(Context ctx, LmisDataRow bill) {
        String devAddress = BlueTooth.getAddress("CS3_3535");
        if (devAddress == null) {
            return;
        }

        TSCActivity tscDll = new TSCActivity();

        tscDll.openport(devAddress);

        tscDll.clearbuffer();
        List commands = formatPrintCommand(bill, 15);
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

    //使用cpcl语言打印标签
    public static void printLabelCpcl(Context ctx, LmisDataRow bill, int maxLabelCount) {
        String devAddress = BlueTooth.getAddress("CS3_3535");
        if (devAddress == null) {
            return;
        }
        zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(ctx);
        zpSDK.connect(devAddress);
        int printCount = bill.getInt("goods_num");
        if (printCount > maxLabelCount) {
            printCount = maxLabelCount;
        }

        List<String> labels = CarryingBillDB.getLabels(bill);
        for (int i = 0; i < labels.size(); i++) {

            String label = labels.get(i);

            //203 dpi 8点/毫米
            zpSDK.pageSetup(600, 415);

            //画外框
            zpSDK.drawBox(3, 0, 0, 568, 370);
            zpSDK.drawText(8, 8, "宇玖速运", 4, 0, 1, false, false);
            zpSDK.drawText(360, 8, bill.getString("bill_no"), 4, 0, 1, false, false);


            zpSDK.drawLine(3, 0, 80, 568, 80, true);

            zpSDK.drawLine(3, 64, 80, 64, 370, true);

            zpSDK.drawText(8 + 8, 88, "目", 0, 0, 1, false, false);
            zpSDK.drawText(8 + 8, 112, "的", 0, 0, 1, false, false);
            zpSDK.drawText(8 + 8, 136, "地", 0, 0, 1, false, false);


            String toOrgName = bill.getM2ORecord("to_org_id").browse().getString("name");
            zpSDK.drawText(64 + 16, 88, toOrgName, 4, 0, 1, false, false);


            zpSDK.drawLine(3, 0, 160, 568, 160, true);


            zpSDK.drawText(8 + 8, 160 + 8 + 24, "货", 0, 0, 1, false, false);
            zpSDK.drawText(8 + 8, 184 + 8+ 24, "物", 0, 0, 1, false, false);
            zpSDK.drawText(8 + 8, 208 + 8+ 24, "信", 0, 0, 1, false, false);
            zpSDK.drawText(8 + 8, 232 + 8+ 24, "息", 0, 0, 1, false, false);


            String seq = String.format("%s/%s",i+1,labels.size());
            zpSDK.drawText(64 + 16, 168 + 16 + 24, seq, 4,0,0, false, false);
            zpSDK.drawBarCode(64 + 16 + 80, 168 + 16, label, 128, false, 3, 80);

            zpSDK.drawText(64 + 16 + 80 + 48, 168 + 16 + 88, label, 3,0,0, false, false);

            zpSDK.drawLine(3, 0, 280 + 32, 568, 280 + 32, true);


            zpSDK.drawText(8 + 8, 280 + 8 + 24, "发", 0, 0, 1, false, false);
            zpSDK.drawText(8 + 8, 280 + 40 + 24, "货", 0, 0, 1, false, false);


            String fromOrgName = bill.getM2ORecord("from_org_id").browse().getString("name");
            String createdAt = bill.getString("bill_date");
            zpSDK.drawText(64 + 16, 280 + 8 + 40  , fromOrgName, 0, 0, 1, false, false);
            zpSDK.drawText(64 + 16 + 240, 280 + 8 + 40  , createdAt, 0, 0, 1, false, false);

            zpSDK.print(0, 0);

        }


        zpSDK.disconnect();


    }

    public static void testPrintBarcode() {
        try {
            String devAddress = BlueTooth.getAddress("BT-LABEL");
            TSCActivity tsc = new TSCActivity();

            tsc.openport(devAddress);
//            tsc.barcode(10, 10, "128", 100, 1, 0, 3, 3, "123456789");

            tsc.clearbuffer();
            tsc.sendcommand("CODEPAGE UTF-8\n");
            tsc.sendcommand("SIZE 70 mm,40 mm\n");
            tsc.sendcommand("GAP 0,0\n");
            tsc.sendcommand("SET PRINTKEY OFF\n");
            tsc.sendcommand("DIRECTION 0\n");
            tsc.sendcommand("CLS\n");
            tsc.sendcommand("BARCODE 150,20,\"EAN128\",100,2,0,2,2,2,\"123456abcd123456\"\n");
            tsc.sendcommand("PRINT 1\n");
            tsc.closeport();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
     * @param bill          the bill
     * @param maxLabelCount 最多打印条数
     * @return the list
     */
    public static List<Object> formatPrintCommand(LmisDataRow bill, int maxLabelCount) {
        int printCount = bill.getInt("goods_num");
        if (printCount > maxLabelCount) {
            printCount = maxLabelCount;
        }

        List<String> labels = CarryingBillDB.getLabels(bill);
        List<Object> commands = new ArrayList<Object>();
        for (int i = 1; i <= printCount; i++) {
            try {
                commands.addAll(Arrays.asList(
                        "CODEPAGE UTF-8\n",
                        "SIZE 70 mm,50 mm\n",
                        "GAP 0,0\n",
                        "SET PRINTKEY OFF\n",
                        "DIRECTION 0\n",
                        "CLS\n",


                        "TEXT 15,20,\"Font001\",0,2,2,\"",
                        String.format("起止:%s 至 %s", bill.getM2ORecord("from_org_id").browse().getString("name"), bill.getM2ORecord("to_org_id").browse().getString("name")).getBytes("GB2312"),
                        "\"\n",

                        //货号
                        "TEXT 15,60,\"Font001\",0,2,2,\"",
                        String.format("货号:%s", bill.getString("goods_no")).getBytes("GB2312"),
                        "\"\n",

                        //日期
                        "TEXT 15,100,\"Font001\",0,2,2,\"",
                        String.format("日期:%s   %s(收)", bill.getString("bill_date").substring(2), bill.getString("to_customer_name")).getBytes("GB2312"),
                        "\"\n",

                        //条形码
                        "BARCODE 100,150,\"EAN128\",80,0,0,2,2,2,\"",
                        String.format("%s", labels.get(i - 1)).getBytes("GB2312"),
                        "\"\n",
                        "TEXT 15,230,\"Font001\",0,2,2,\"",
                        labels.get(i - 1),
                        "\"\n"


                ));
                commands.add("PRINT 1\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return commands;
    }

    /**
     * Print for json.
     *
     * @param bill the bill
     */
    public static void printForJson(JSONObject bill) {
        String devAddress = BlueTooth.getAddress("BT-LABEL");
        if (devAddress == null) {
            return;
        }

        TSCActivity tscDll = new TSCActivity();

        tscDll.openport(devAddress);

        tscDll.clearbuffer();
        List commands = formatPrintCommandForJson(bill, 15);
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
     * @param bill          the bill
     * @param maxLabelCount the max label count
     * @return the list
     */
    public static List<Object> formatPrintCommandForJson(JSONObject bill, int maxLabelCount) {
        int printCount = 0;
        try {
            printCount = bill.getInt("goods_num");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (printCount > maxLabelCount) {
            printCount = maxLabelCount;
        }
        List<String> labels = CarryingBillDB.getLabels(bill);
        List<Object> commands = new ArrayList<Object>();
        for (int i = 1; i <= printCount; i++) {
            try {
                commands.addAll(Arrays.asList(
                        "CODEPAGE UTF-8\n",
                        "SIZE 70 mm,50 mm\n",
                        "GAP 0,0\n",
                        "SET PRINTKEY OFF\n",
                        "DIRECTION 0\n",
                        "CLS\n",


                        "TEXT 15,20,\"Font001\",0,2,2,\"",
                        String.format("起止:%s 至 %s", bill.getString("from_org_name"), bill.getString("to_org_name")).getBytes("GB2312"),
                        "\"\n",

                        //货号
                        "TEXT 15,60,\"Font001\",0,2,2,\"",
                        String.format("货号:").getBytes("GB2312"),
                        "\"\n",
                        "TEXT 135,60,\"Font001\",0,3,3,\"",
                        String.format("%s", bill.getString("goods_no")).getBytes("GB2312"),
                        "\"\n",
                        //日期
                        "TEXT 15,100,\"Font001\",0,2,2,\"",
                        String.format("日期 :").getBytes("GB2312"),
                        "\"\n",
                        "TEXT 135,100,\"Font001\",0,3,3,\"",
                        String.format("%s", bill.getString("bill_date").substring(2)).getBytes("GB2312"),
                        "\"\n",

                        //条形码
                        "BARCODE 15,140,\"EAN128\",80,0,0,2,2,2,\"",
                        String.format("%s", labels.get(i - 1)).getBytes("GB2312"),
                        "\"\n"

                ));
                commands.add("PRINT 1\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return commands;

    }
}
