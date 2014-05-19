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

import com.lmis.addons.expense.Expense;
import com.lmis.addons.message.Message;
import com.lmis.addons.voucher.Voucher;
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
		add(new Module("module_expense", "Expense", new Expense(), 0), true);
		add(new Module("module_voucher", "Voucher", new Voucher(), 0), true);
		add(new Module("module_message", "Message", new Message()), true);
		//add(new Module("module_mail_groups", "Mail Groups", new MailGroup()));
		//add(new Module("module_note", "Note", new Note()));
		//add(new Module("module_meeting", "Meeting/Calendar Event",new Meeting()));
	}
}
