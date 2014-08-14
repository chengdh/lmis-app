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

import com.lmis.addons.carrying_bill.CarryingBillList;
import com.lmis.addons.inventory.InventoryOutList;
import com.lmis.support.Module;
import com.lmis.support.ModulesConfigHelper;

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
        add(new Module("module_inventory_out", "Inventory Out", new InventoryOutList(), 0), true);
        add(new Module("module_carrying_bill", "Carrying Bill", new CarryingBillList(), 0), true);
    }
}
