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

package com.lmis.base.res;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

/**
 * The Class Res_PartnerDBHelper.
 */
public class ResPartnerDB extends LmisDatabase {

	Context mContext = null;

	/**
	 * Instantiates a new res_ partner db helper.
	 * 
	 * @param context
	 *            the context
	 */
	public ResPartnerDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "res.partner";
	}

	@Override
	public List<LmisColumn> getModelColumns() {
		List<LmisColumn> columns = new ArrayList<LmisColumn>();
		columns.add(new LmisColumn("is_company", "Is Company", LmisFields.text()));
		columns.add(new LmisColumn("name", "Name", LmisFields.text()));
		columns.add(new LmisColumn("image_small", "Image", LmisFields.blob()));
		columns.add(new LmisColumn("street", "Street", LmisFields.text()));
		columns.add(new LmisColumn("street2", "Street 2", LmisFields.text()));
		columns.add(new LmisColumn("city", "City", LmisFields.text()));
		columns.add(new LmisColumn("zip", "Zip", LmisFields.text()));
		columns.add(new LmisColumn("website", "website", LmisFields.text()));
		columns.add(new LmisColumn("phone", "Phone", LmisFields.text()));
		columns.add(new LmisColumn("mobile", "Mobile", LmisFields.text()));
		columns.add(new LmisColumn("email", "email", LmisFields.text()));
		columns.add(new LmisColumn("company_id", "company", LmisFields
				.manyToOne(new ResCompanyDB(mContext))));
		return columns;
	}

}
