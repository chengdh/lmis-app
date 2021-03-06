package com.lmis.addons.carrying_bill;

import com.example.tscdll.TSCActivity;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.LmisUser;
import com.lmis.util.BlueTooth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import us.monoid.json.JSONObject;

import android.util.Log;

/**
 * 移动打印相关功能
 * Created by chengdh on 14-10-24.
 */
public class CarryingBillPrint {
    /**
     * Print void.
     *
     * @param bill the bill
     * @param user
     */
    public static void print(LmisDataRow bill, LmisUser user, Boolean rePrint) {
        String devAddress = BlueTooth.getAddress("BT-SPP");
        if (devAddress == null) {
            return;
        }

        TSCActivity tscDll = new TSCActivity();

        tscDll.openport(devAddress);

        tscDll.clearbuffer();
        List commands = formatPrintCommand(bill, user, rePrint);
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

    public static void testPrintBarcode() {
        try {
            String devAddress = BlueTooth.getAddress("BT-SPP");
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
                    "SIZE 70 mm,180 mm\n",
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

                    //条形码
                    "BARCODE 235,150,\"EAN128\",80,0,0,2,2,2,\"",
                    String.format("%s", bill.getString("bill_no")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 130,240,\"5.EFT\",0,1,1,\"",
                    String.format("%s", bill.getString("bill_no")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,300,500,3\n",

                    "TEXT 15,320,\"Font001\",0,2,2,\"",
                    String.format("日    期: %s", curDateTimeStr).getBytes("GB2312"),
                    "\"\n",

                    //货号
                    "TEXT 15,360,\"Font001\",0,2,2,\"",
                    String.format("货    号:").getBytes("GB2312"),
                    "\"\n",
                    "TEXT 135,360,\"Font001\",0,3,3,\"",
                    String.format("%s", bill.getString("goods_no").substring(2)).getBytes("GB2312"),
                    "\"\n",

                    //时间
//                    "TEXT 15,210,\"Font001\",0,2,2,\"",
//                    String.format("时    间:%s", bill.getString("bill_date").substring(2)).getBytes("GB2312"),
//                    "\"\n",

                    "BAR 15,400,500,1\n",

                    "TEXT 15,420,\"Font001\",0,2,2,\"",
                    String.format("发 货 地:%s", bill.getM2ORecord("from_org_id").browse().getString("name")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,460,\"Font001\",0,2,2,\"",
                    String.format("到 货 地:").getBytes("GB2312"),
                    "\"\n",
                    "TEXT 135,460,\"Font001\",0,3,3,\"",
                    String.format("%s", bill.getM2ORecord("to_org_id").browse().getString("name")).getBytes("GB2312"),
                    "\"\n",


                    "BAR 15,500,500,1\n",


                    "TEXT 15,520,\"Font001\",0,2,2,\"",
                    String.format("客户卡号:%s", bill.getString("from_customer_code") == "false" ? "[无]" : bill.getString("from_customer_code")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,560,\"Font001\",0,2,2,\"",
                    String.format("发 货 人:%s", bill.getString("from_customer_name")).getBytes("GB2312"),
                    "\"\n",
                    "TEXT 280,560,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("from_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,600,\"Font001\",0,2,2,\"",
                    String.format("收 货 人:%s", bill.getString("to_customer_name")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 280,600,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("to_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,640,500,1\n",

                    "TEXT 15,660,\"Font001\",0,2,2,\"",
                    String.format("支付方式:%s", PayType.payTypes().get(bill.getString("pay_type"))).getBytes("GB2312"),
                    "\"\n",

//                    "TEXT 15,450,\"Font001\",0,2,2,\"",
//                    String.format("发货短途:%s元 ", bill.getInt("from_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",
//
//                    "TEXT 15,480,\"Font001\",0,2,2,\"",
//                    String.format("到货短途:%s元 ", bill.getInt("to_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",


                    "TEXT 15,700,\"Font001\",0,2,2,\"",
                    "运费总计:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,700,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getInt("carrying_fee") + bill.getInt("from_short_carrying_fee") + bill.getInt("to_short_carrying_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 280,700,\"Font001\",0,2,2,\"",
                    "保 险 费:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 395,700,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getInt("insured_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,740,\"Font001\",0,2,2,\"",
                    "代收货款:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,740,\"Font001\",0,3,3,\"",
                    String.format("%s元", bill.getInt("goods_fee")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,780,500,2\n",

                    "TEXT 15,800,\"Font001\",0,2,2,\"",
                    "货物名称                   数量".getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,840,500,1\n",

                    "TEXT 15,860,\"Font001\",0,2,2,\"",
                    String.format("%s                        %s", bill.getString("goods_info"), bill.getInt("goods_num")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,900,500,1\n",

                    "TEXT 15,920,\"Font001\",0,2,2,\"",
                    String.format("备    注:%s", bill.getString("note")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,960,\"Font001\",0,2,2,\"",
                    String.format("开 票 人:%S : ", user.getAndroidName()).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1000,\"Font001\",0,2,2,\"",
                    "发货人签字:________________".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,1040,\"Font001\",0,2,2,\"",
                    "1.本票据以我公司同期票据运输协议条款为依据".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1080,\"Font001\",0,2,2,\"",
                    "2.本协议等同运输合同，有效期为三十天".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1120,\"Font001\",0,2,2,\"",
                    "3.严禁托运国家规定的危险品，违禁管制物品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1160,\"Font001\",0,2,2,\"",
                    "  及假冒伪劣产品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1200,\"Font001\",0,2,2,\"",
                    "4.发货人请核对发货票据信息,如有问题请及时".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1240,\"Font001\",0,2,2,\"",
                    "  及时更正票据信息".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,1280,\"Font001\",0,2,2,\"",
                    "5.未盖我公司收货章为无效票;".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1320,\"Font001\",0,2,2,\"",
                    "6.温馨提示:此票为热敏票据,请妥善保存!".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1360,\"Font001\",0,2,2,\"",
                    "全国统一客服热线:400-862-9666".getBytes("GB2312"),
                    "\"\n"


            ));
            if (rePrint) {
                String cmd1 = "TEXT 15,1400,\"Font001\",0,2,2,\"";
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
                    "SIZE 70 mm,180 mm\n",
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

                    //条形码
                    "BARCODE 235,150,\"EAN128\",80,0,0,2,2,2,\"",
                    String.format("%s", bill.getString("bill_no")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 130,240,\"5.EFT\",0,1,1,\"",
                    String.format("%s", bill.getString("bill_no")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,300,500,3\n",

                    "TEXT 15,320,\"Font001\",0,2,2,\"",
                    String.format("日    期: %s", curDateTimeStr).getBytes("GB2312"),
                    "\"\n",

                    //货号
                    "TEXT 15,360,\"Font001\",0,2,2,\"",
                    String.format("货    号:").getBytes("GB2312"),
                    "\"\n",
                    "TEXT 135,360,\"Font001\",0,3,3,\"",
                    String.format("%s", bill.getString("goods_no").substring(2)).getBytes("GB2312"),
                    "\"\n",

                    //时间
//                    "TEXT 15,210,\"Font001\",0,2,2,\"",
//                    String.format("时    间:%s", bill.getString("bill_date").substring(2)).getBytes("GB2312"),
//                    "\"\n",

                    "BAR 15,400,500,1\n",

                    "TEXT 15,420,\"Font001\",0,2,2,\"",
                    String.format("发 货 地:%s", bill.getString("from_org_name")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,460,\"Font001\",0,2,2,\"",
                    String.format("到 货 地:").getBytes("GB2312"),
                    "\"\n",
                    "TEXT 135,460,\"Font001\",0,3,3,\"",
                    String.format("%s", bill.getString("to_org_name")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,500,500,1\n",

                    "TEXT 15,520,\"Font001\",0,2,2,\"",
                    String.format("客户卡号:%s", bill.getString("from_customer_code") == "false" ? "[无]" : bill.getString("from_customer_code")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,560,\"Font001\",0,2,2,\"",
                    String.format("发 货 人:%s", bill.getString("from_customer_name")).getBytes("GB2312"),
                    "\"\n",
                    "TEXT 280,560,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("from_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,600,\"Font001\",0,2,2,\"",
                    String.format("收 货 人:%s", bill.getString("to_customer_name")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 280,600,\"Font001\",0,2,2,\"",
                    String.format("电   话:%s", bill.getString("to_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,640,500,1\n",

                    "TEXT 15,660,\"Font001\",0,2,2,\"",
                    String.format("支付方式:%s", PayType.payTypes().get(bill.getString("pay_type"))).getBytes("GB2312"),
                    "\"\n",

//                    "TEXT 15,450,\"Font001\",0,2,2,\"",
//                    String.format("发货短途:%s元 ", bill.getInt("from_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",
//
//                    "TEXT 15,480,\"Font001\",0,2,2,\"",
//                    String.format("到货短途:%s元 ", bill.getInt("to_short_carrying_fee")).getBytes("GB2312"),
//                    "\"\n",


                    "TEXT 15,700,\"Font001\",0,2,2,\"",
                    "运费总计:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,700,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getDouble("carrying_fee") + bill.getDouble("from_short_carrying_fee") + bill.getDouble("to_short_carrying_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 280,700,\"Font001\",0,2,2,\"",
                    "保 险 费:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 395,700,\"Font001\",0,2,2,\"",
                    String.format("%s元", bill.getString("insured_fee")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,740,\"Font001\",0,2,2,\"",
                    "代收货款:".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 135,740,\"Font001\",0,3,3,\"",
                    String.format("%s元", bill.getString("goods_fee")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,780,500,2\n",

                    "TEXT 15,800,\"Font001\",0,2,2,\"",
                    "货物名称                   数量".getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,840,500,1\n",

                    "TEXT 15,860,\"Font001\",0,2,2,\"",
                    String.format("%s                        %s", bill.getString("goods_info"), bill.getInt("goods_num")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,900,500,1\n",

                    "TEXT 15,920,\"Font001\",0,2,2,\"",
                    String.format("备    注:%s", bill.getString("note")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,960,\"Font001\",0,2,2,\"",
                    String.format("开 票 人:%S : ", user.getAndroidName()).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1000,\"Font001\",0,2,2,\"",
                    "发货人签字:________________".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,1040,\"Font001\",0,2,2,\"",
                    "1.本票据以我公司同期票据运输协议条款为依据".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1080,\"Font001\",0,2,2,\"",
                    "2.本协议等同运输合同，有效期为三十天".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1120,\"Font001\",0,2,2,\"",
                    "3.严禁托运国家规定的危险品，违禁管制物品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1160,\"Font001\",0,2,2,\"",
                    "  及假冒伪劣产品".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1200,\"Font001\",0,2,2,\"",
                    "4.发货人请核对发货票据信息,如有问题请及时".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1240,\"Font001\",0,2,2,\"",
                    "  及时更正票据信息".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,1280,\"Font001\",0,2,2,\"",
                    "5.未盖我公司收货章为无效票;".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1320,\"Font001\",0,2,2,\"",
                    "6.温馨提示:此票为热敏票据,请妥善保存!".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,1360,\"Font001\",0,2,2,\"",
                    "全国统一客服热线:400-862-9666".getBytes("GB2312"),
                    "\"\n"

            ));
            if (rePrint) {
                String cmd1 = "TEXT 15,1400,\"Font001\",0,2,2,\"";
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
