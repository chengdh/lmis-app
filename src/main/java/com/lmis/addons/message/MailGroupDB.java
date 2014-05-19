package com.lmis.addons.message;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisDatabase;

public class MailGroupDB extends LmisDatabase {

	public MailGroupDB(Context context) {
		super(context);
	}

	@Override
	public String getModelName() {
		return "mail.group";
	}

	@Override
	public List<LmisColumn> getModelColumns() {
		List<LmisColumn> columns = new ArrayList<LmisColumn>();
		columns.add(new LmisColumn("name", "Name", LmisFields.varchar(64)));
		columns.add(new LmisColumn("description", "Description", LmisFields.text()));
		columns.add(new LmisColumn("image_medium", "medium Image", LmisFields
				.blob()));
		return columns;
	}

}
