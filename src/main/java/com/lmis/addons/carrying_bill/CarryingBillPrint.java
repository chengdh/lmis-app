package com.lmis.addons.carrying_bill;

import com.example.tscdll.TSCActivity;
import com.lmis.orm.LmisDataRow;
import com.lmis.util.BlueTooth;

import java.util.ArrayList;
import java.util.Arrays;
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
     */
    public static void print(LmisDataRow bill, Boolean rePrint) {
        String devAddress = BlueTooth.getAddress("BT-SPP");
        if (devAddress == null) {
            return;
        }

        TSCActivity tscDll = new TSCActivity();

        tscDll.openport(devAddress);
        try {
            for (String cmd : formatPrintCommand(bill, rePrint)) {
                tscDll.sendcommand(cmd + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tscDll.printlabel(1, 2);
        tscDll.closeport();
    }

    /**
     * 小票打印指令
     * The constant PRINT_CONTENT.
     */
    public static List<String> formatPrintCommand(LmisDataRow bill, Boolean rePrint) {
        List<String> commands = new ArrayList<String>();
        commands.addAll(Arrays.asList(
                "CODEPAGE UTF-8",
                "SIZE 55 mm,110 mm",
                "GAP 0 mm,0",
                "DIRECTION 0",
                "CLS",
                //"PUTBMP 5,5,\"logo.BMP\"",
                "TEXT 5,60,\"DroidSan.TTF\",0,12,12,1,\"河南凯瑞物流有限公司\"",
                "TEXT 5,90,\"DroidSan.TTF\",0,10,10,1,\"运输协议单\"",
                "BAR 5,120,300,3",
                String.format("TEXT 5,150,\"DroidSan.TTF\",0,7,7,\"运单编号:%s\"", bill.getString("bill_no")),
                String.format("TEXT 5,180,\"DroidSan.TTF\",0,7,7,\"到货地:%s\"", bill.getM2ORecord("to_org_id").browse().getString("name")),
                String.format("TEXT 5,210,\"DroidSan.TTF\",0,7,7,\"货号:%s\"", bill.getString("goods_no")),
                String.format("TEXT 5,240,\"DroidSan.TTF\",0,7,7,\"运费支付方式:%s\"", PayType.payTypes().get(bill.getString("pay_type"))),
                String.format("TEXT 5,270,\"DroidSan.TTF\",0,7,7,\"客户卡号:%s\"", ""),
                String.format("TEXT 5,300,\"DroidSan.TTF\",0,7,7,\"发货人:%s  电话:%s\"", bill.getString("from_customer_name"), bill.getString("from_customer_mobile")),
                String.format("TEXT 5,330,\"DroidSan.TTF\",0,7,7,\"收货人:%s 电话:%s\"", bill.getString("to_customer_name"), bill.getString("to_customer_mobile")),
                String.format("TEXT 5,360,\"DroidSan.TTF\",0,7,7,\"货物名称:%s 数量:%s\"", bill.getString("goods_info"), bill.getInt("goods_num")),
                String.format("TEXT 5,390,\"DroidSan.TTF\",0,7,7,\"运费总计:%s元 保险费:%s元\"", bill.getInt("carrying_fee"), bill.getInt("insured_fee")),
                String.format("TEXT 5,420,\"DroidSan.TTF\",0,9,9,\"代收货款:%s元\"", bill.getInt("goods_fee")),
                String.format("TEXT 5,450,\"DroidSan.TTF\",0,7,7,\"备注:%s\"", bill.getString("note")),
                "BAR 5,480,300,3",
                String.format("TEXT 5,510,\"DroidSan.TTF\",0,7,7,\"开票人:%S 日期: %s\"", "凯瑞物流", bill.getString("bill_date")),
                "TEXT 5,540,\"DroidSan.TTF\",0,7,7,\"发货人签字:\"",
                "BAR 5,570,300,3",
                "TEXT 5,600,\"DroidSan.TTF\",0,7,7,\"全国统一客服热线:\"",
                "TEXT 5,630,\"DroidSan.TTF\",0,7,7,\"400-619-4448\"",
                "BAR 5,660,300,3",
                "TEXT 5,690,\"DroidSan.TTF\",0,7,7,\"发货人存根\"",
                "TEXT 5,720,\"DroidSan.TTF\",0,7,7,\"本人确认以上票据信息,并同意发货!\"",
                "TEXT 5,750,\"DroidSan.TTF\",0,7,7,\"温馨提示:此票为热敏票据,不宜长期保存,\"",
                "TEXT 5,780,\"DroidSan.TTF\",0,7,7,\"            请及时复印\""
                //"PRINT 1"
        ));
        if (rePrint) {
            String cmd = "TEXT 5,810,\"DroidSan.TTF\",0,7,7,\"[重打]\"";
            commands.add(cmd);
        }
        return commands;
    }
}
