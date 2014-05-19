package com.lmis.addons.meeting;

import java.util.ArrayList;
import java.util.List;

import com.lmis.Lmis;
import com.lmis.base.res.ResPartnerDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisHelper;

import android.content.Context;

public class MeetingDB extends LmisDatabase {

	Context mContext = null;

	public MeetingDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		String name = "crm.meeting";
		LmisHelper oe = getOEInstance();
		if (oe != null) {
			Lmis.LmisVersion version = oe.getOEVersion();
			if ((version.getVersion_number() == 7
					&& version.getVersion_type().equals("saas") && version
					.getVersion_type_number() == 3)
					|| (version.getVersion_number() >= 8)) {
				name = "calendar.event";
			}
		}
		return name;
	}

	@Override
	public List<LmisColumn> getModelColumns() {
		List<LmisColumn> columns = new ArrayList<LmisColumn>();
		columns.add(new LmisColumn("name", "Name", LmisFields.varchar(64)));
		columns.add(new LmisColumn("date", "Date", LmisFields.text()));
		columns.add(new LmisColumn("duration", "Duration", LmisFields.text()));
		columns.add(new LmisColumn("allday", "All day", LmisFields.varchar(6)));
		columns.add(new LmisColumn("description", "Description", LmisFields.text()));
		columns.add(new LmisColumn("location", "Location", LmisFields.text()));
		columns.add(new LmisColumn("date_deadline", "Dead_line", LmisFields.text()));
		columns.add(new LmisColumn("partner_ids", "Partner_ids", LmisFields
				.manyToMany(new ResPartnerDB(mContext))));

		// Event id of Lmis Mobile Calendar for meeting
		columns.add(new LmisColumn("calendar_event_id", "Calendar_event_id",
				LmisFields.integer(), false));

		// Lmis Calendar Id under which meetings synced as events
		columns.add(new LmisColumn("calendar_id", "Calendar_id", LmisFields
				.integer(), false));
		return columns;
	}
}
