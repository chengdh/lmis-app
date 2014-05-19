package com.lmis.addons.message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisValues;
import com.lmis.base.ir.Ir_AttachmentDBHelper;
import com.lmis.base.res.ResPartnerDB;
import com.lmis.orm.LmisFieldsHelper.ValueWatcher;

public class MessageDB extends LmisDatabase {

	Context mContext = null;

	public MessageDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "mail.message";
	}

	@Override
	public List<LmisColumn> getModelColumns() {
		List<LmisColumn> columns = new ArrayList<LmisColumn>();
		columns.add(new LmisColumn("partner_ids", "Partners", LmisFields
				.manyToMany(new ResPartnerDB(mContext))));
		columns.add(new LmisColumn("subject", "Subject", LmisFields.text()));
		columns.add(new LmisColumn("type", "Type", LmisFields.varchar(30)));
		columns.add(new LmisColumn("body", "Body", LmisFields.text()));
		columns.add(new LmisColumn("email_from", "Email From", LmisFields.text(),
				false));

		columns.add(new LmisColumn("parent_id", "Parent", LmisFields.integer()));
		columns.add(new LmisColumn("record_name", "Record Title", LmisFields.text()));
		columns.add(new LmisColumn("to_read", "To Read", LmisFields.varchar(5)));

		ValueWatcher mValueWatcher = new ValueWatcher() {

			@Override
			public LmisValues getValue(LmisColumn col, Object value) {
				LmisValues values = new LmisValues();
				try {
					if (value instanceof JSONArray) {
						JSONArray array = (JSONArray) value;
						if (array.getInt(0) == 0) {
							values.put(col.getName(), false);
							values.put("email_from", array.getString(1));
						} else {
							values.put("email_from", false);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return values;
			}

		};
		columns.add(new LmisColumn("author_id", "author", LmisFields
				.manyToOne(new ResPartnerDB(mContext)), mValueWatcher));
		columns.add(new LmisColumn("model", "Model", LmisFields.varchar(50)));
		columns.add(new LmisColumn("res_id", "Resouce Reference", LmisFields.text()));
		columns.add(new LmisColumn("date", "Date", LmisFields.varchar(20)));
		columns.add(new LmisColumn("has_voted", "Has Voted", LmisFields.varchar(5)));
		columns.add(new LmisColumn("vote_nb", "vote numbers", LmisFields.integer()));
		columns.add(new LmisColumn("starred", "Starred", LmisFields.varchar(5)));
		columns.add(new LmisColumn("attachment_ids", "Attachments", LmisFields
				.manyToMany(new Ir_AttachmentDBHelper(mContext))));
		return columns;
	}
}
