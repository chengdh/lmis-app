package com.lmis.base.mail;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.base.res.ResPartnerDB;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisFields;

public class MailFollowers extends LmisDatabase {
	Context mContext = null;

	public MailFollowers(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "mail.followers";
	}

	@Override
	public List<LmisColumn> getModelColumns() {
		List<LmisColumn> cols = new ArrayList<LmisColumn>();
		cols.add(new LmisColumn("res_model", "Model", LmisFields.text()));
		cols.add(new LmisColumn("res_id", "Note ID", LmisFields.integer()));
		cols.add(new LmisColumn("partner_id", "Partner ID", LmisFields
				.manyToOne(new ResPartnerDB(mContext))));
		return cols;
	}
}
