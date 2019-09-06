/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 */
package com.lmis.config;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.lmis.addons.carrying_bill.CarryingBillList;
import com.lmis.addons.dashboard.DashBoard;
import com.lmis.addons.goods_exception.GoodsExceptionList;
import com.lmis.addons.inventory.InventoryMoveList;
import com.lmis.addons.message.MessageList;
import com.lmis.addons.scan_header.ScanHeaderList;
import com.lmis.addons.short_list.ShortListList;
import com.lmis.support.Module;
import com.lmis.support.ModulesConfigHelper;
import com.lmis.util.barcode_scan_header.ScanHeaderOpType;

/**
 * The Class ModulesConfig.
 */
public class ModulesConfig extends ModulesConfigHelper {

    /**
     * Instantiates a new modules config.
     */
    public ModulesConfig() {
        /* application modules */
        //add(new Module("module_idea", "Idea", new Idea(), 0), true);
        add(new Module("module_carrying_bill", "Carrying Bill", new CarryingBillList(), 0), true);
//        add(new Module("module_message", "Message", new MessageList(), 0), true);
//        add(new Module("module_inventory_out", "Inventory Out", new InventoryMoveList(), 0), true);
        add(new Module("module_goods_exception", "Goods Exception", new GoodsExceptionList(), 0), true);
        add(new Module("module_short_list", "short list", new ShortListList(), 0), true);
//        add(new Module("module_dashboard", "Dashboard", new DashBoard(), 0), true);
//        ShortListList scanHeaderSortingIn = new ShortListList();
//        scanHeaderSortingIn.setmCurrentType(ScanHeaderOpType.SORTING_IN);
//        add(new Module("module_scan_header_sorting_in", "scan_header_sorting_in", scanHeaderSortingIn, 0), true);
//        ShortListList scanHeaderSubBranch = new ShortListList();
//        scanHeaderSubBranch.setmCurrentType(ScanHeaderOpType.SUB_BRANCH);
//        add(new Module("module_scan_header_sub_branch", "scan_header_sub_branch", scanHeaderSubBranch, 0), true);


        /*
        ShortListList scanHeaderLoadIn = new ShortListList();
        scanHeaderLoadIn.setmCurrentType(ScanHeaderOpType.LOAD_IN);
        add(new Module("module_scan_header_load_in", "scan_header_load_in", scanHeaderLoadIn, 0), true);

        ShortListList scanHeaderLoadOut = new ShortListList();
        scanHeaderLoadOut.setmCurrentType(ScanHeaderOpType.LOAD_OUT);
        add(new Module("module_scan_header_load_out", "scan_header_load_out", scanHeaderLoadOut, 0), true);

        ShortListList scanHeaderInnerTransitLoadIn = new ShortListList();
        scanHeaderInnerTransitLoadIn.setmCurrentType(ScanHeaderOpType.INNER_TRANSIT_LOAD_IN);
        add(new Module("module_scan_header_inner_transit_load_in", "scan_header_inner_transit_load_in", scanHeaderInnerTransitLoadIn, 0), true);

        ShortListList scanHeaderInnerTransitLoadOut = new ShortListList();
        scanHeaderInnerTransitLoadOut.setmCurrentType(ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT);
        add(new Module("module_scan_header_inner_transit_load_out", "scan_header_inner_transit_load_out", scanHeaderInnerTransitLoadOut, 0), true);

        ShortListList scanHeaderLocalTownLoadIn = new ShortListList();
        scanHeaderLocalTownLoadIn.setmCurrentType(ScanHeaderOpType.LOCAL_TOWN_LOAD_IN);
        add(new Module("module_scan_header_local_town_load_in", "scan_header_local_town_load_in", scanHeaderLocalTownLoadIn, 0), true);

        ShortListList scanHeaderLocalTownLoadOut = new ShortListList();
        scanHeaderLocalTownLoadOut.setmCurrentType(ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT);
        add(new Module("module_scan_header_local_town_load_out", "scan_header_local_town_load_out", scanHeaderLocalTownLoadOut, 0), true);

        ShortListList scanHeaderLoadInTeam = new ShortListList();
        scanHeaderLoadInTeam.setmCurrentType(ScanHeaderOpType.LOAD_IN_TEAM);
        add(new Module("module_scan_header_load_in_team", "scan_header_load_in_team", scanHeaderLoadInTeam, 0), true);
        */

    }
}
