/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */

package com.lmis.base.ir;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.base.res.ResCompanyDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

/**
 * The Class Ir_AttachmentDBHelper.
 */
public class Ir_AttachmentDBHelper extends LmisDatabase {
	Context mContext = null;

	public Ir_AttachmentDBHelper(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "ir.attachment";
	}

	@Override
	public List<LmisColumn> getModelColumns() {
		List<LmisColumn> columns = new ArrayList<LmisColumn>();
		columns.add(new LmisColumn("name", "Name", LmisFields.text()));
		columns.add(new LmisColumn("datas_fname", "Data File Name", LmisFields
				.text()));
		columns.add(new LmisColumn("type", "Type", LmisFields.text()));
		columns.add(new LmisColumn("file_size", "File Size", LmisFields.integer()));
		columns.add(new LmisColumn("res_model", "Model", LmisFields.varchar(100)));
		columns.add(new LmisColumn("company_id", "company id", LmisFields
				.manyToOne(new ResCompanyDB(mContext))));
		columns.add(new LmisColumn("res_id", "resource id", LmisFields.integer()));
		return columns;
	}
}
