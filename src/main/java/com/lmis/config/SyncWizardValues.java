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

import com.lmis.providers.il_config.IlConfigProvider;
import com.lmis.providers.inventory_move.InventoryMoveProvider;
import com.lmis.providers.message.MessageProvider;
import com.lmis.providers.org.OrgProvider;
import com.lmis.providers.user_org.UserOrgProvider;
import com.lmis.support.SyncValue;
import com.lmis.support.SyncWizardHelper;

import java.util.ArrayList;
import java.util.List;

public class SyncWizardValues implements SyncWizardHelper {

    @Override
    public List<SyncValue> syncValues() {
        List<SyncValue> list = new ArrayList<SyncValue>();

        //单据信息
        list.add(new SyncValue("basic"));
        list.add(new SyncValue("il_configs", IlConfigProvider.AUTHORITY, SyncValue.Type.CHECKBOX));
        list.add(new SyncValue("orgs", OrgProvider.AUTHORITY, SyncValue.Type.CHECKBOX));
        list.add(new SyncValue("user_orgs", UserOrgProvider.AUTHORITY, SyncValue.Type.CHECKBOX));
        list.add(new SyncValue("inventory_moves", InventoryMoveProvider.AUTHORITY, SyncValue.Type.CHECKBOX));
//        list.add(new SyncValue("messages", MessageProvider.AUTHORITY, SyncValue.Type.CHECKBOX));
        return list;
    }
}
