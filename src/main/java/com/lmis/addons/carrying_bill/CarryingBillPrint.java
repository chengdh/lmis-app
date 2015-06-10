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
        try {
            for (Object cmd : formatPrintCommand(bill, user, rePrint)) {
                if (cmd instanceof String)
                    tscDll.sendcommand(cmd.toString());

                if (cmd instanceof byte[]) {
                    tscDll.sendcommand((byte[]) cmd);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tscDll.closeport();
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
                    "SIZE 70 mm,110 mm\n",
                    "GAP 0 mm,0\n",
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

                    "BAR 15,140,500,3\n",
                    "TEXT 15,150,\"Font001\",0,2,2,\"",
                    String.format("运单编号:%s", bill.getString("bill_no")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,180,\"Font001\",0,2,2,\"",
                    String.format("发 货 地:%s", bill.getM2ORecord("from_org_id").browse().getString("name")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,210,\"Font001\",0,2,2,\"",
                    String.format("到 货 地:%s", bill.getM2ORecord("to_org_id").browse().getString("name")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,240,\"Font001\",0,2,2,\"",
                    String.format("货    号:%s", bill.getString("goods_no").substring(6)).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,270,\"Font001\",0,2,2,\"",
                    String.format("支付方式:%s", PayType.payTypes().get(bill.getString("pay_type"))).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,300,\"Font001\",0,2,2,\"",
                    String.format("客户卡号:%s", bill.getString("from_customer_code") == "false" ? "[无]" : bill.getString("from_customer_code")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,330,\"Font001\",0,2,2,\"",
                    String.format("发 货 人:%s  电话:%s", bill.getString("from_customer_name"), bill.getString("from_customer_mobile")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,360,\"Font001\",0,2,2,\"",
                    String.format("收 货 人:%s 电话:%s", bill.getString("to_customer_name"), bill.getString("to_customer_mobile")).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,390,\"Font001\",0,2,2,\"",
                    String.format("运费总计:%s元 保险费:%s元", bill.getInt("carrying_fee"), bill.getInt("insured_fee")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,420,\"Font001\",0,2,2,\"",
                    String.format("代收货款:%s元", bill.getInt("goods_fee")).getBytes("GB2312"),
                    "\"\n",


                    "BAR 15,450,500,3\n",

                    "TEXT 15,460,\"Font001\",0,2,2,\"",
                    "货物名称                   数量".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,490,\"Font001\",0,2,2,\"",
                    String.format("%s                        %s", bill.getString("goods_info"), bill.getInt("goods_num")).getBytes("GB2312"),
                    "\"\n",

                    "BAR 15,520,500,3\n",

                    "TEXT 15,530,\"Font001\",0,2,2,\"",
                    String.format("备    注:%s", bill.getString("note")).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,570,\"Font001\",0,2,2,\"",
                    String.format("开 票 人:%S : ", user.getAndroidName()).getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,600,\"Font001\",0,2,2,\"",
                    String.format("日    期: %s", curDateTimeStr).getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,630,\"Font001\",0,2,2,\"",
                    "全国统一客服热线:400-619-4448".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,660,\"Font001\",0,2,2,\"",
                    "本票据以我公司同期票据运输协议条款为依据".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,690,\"Font001\",0,2,2,\"",
                    "未盖我公司收货章为无效票;".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,720,\"Font001\",0,2,2,\"",
                    "本人确认以上票据信息,并同意发货!".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,750,\"Font001\",0,2,2,\"",
                    "温馨提示:此票为热敏票据,请妥善保存!".getBytes("GB2312"),
                    "\"\n",

                    "TEXT 15,780,\"Font001\",0,2,2,\"",
                    "   ".getBytes("GB2312"),
                    "\"\n",


                    "TEXT 15,820,\"Font001\",0,2,2,\"",
                    "发货人签字:________________".getBytes("GB2312"),
                    "\"\n"

            ));
            if (rePrint) {
                String cmd1 = "TEXT 15,850,\"Font001\",0,2,2,\"";
                byte[] cmd2 = "[重打]".getBytes("GB2312");
                String cmd3 = "\"\n";
                commands.add(cmd1);
                commands.add(cmd2);
                commands.add(cmd3);
            }
            commands.add("PRINT 2\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return commands;
    }
}
