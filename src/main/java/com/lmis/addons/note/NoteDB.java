package com.lmis.addons.note;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.base.res.ResPartnerDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisFields;
import com.lmis.orm.LmisDatabase;

public class NoteDB extends LmisDatabase {
	Context mContext = null;

	public NoteDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "note.note";
	}

	@Override
	public List<LmisColumn> getModelColumns() {
		List<LmisColumn> cols = new ArrayList<LmisColumn>();
		cols.add(new LmisColumn("name", "Name", LmisFields.varchar(64)));
		cols.add(new LmisColumn("memo", "Memo", LmisFields.varchar(64)));
		cols.add(new LmisColumn("open", "Open", LmisFields.varchar(64)));
		cols.add(new LmisColumn("date_done", "Date_Done", LmisFields.varchar(64)));
		cols.add(new LmisColumn("stage_id", "NoteStages", LmisFields
				.manyToOne(new NoteStages(mContext))));
		cols.add(new LmisColumn("tag_ids", "NoteTags", LmisFields
				.manyToMany(new NoteTags(mContext))));
		cols.add(new LmisColumn("current_partner_id", "Res_Partner", LmisFields
				.manyToOne(new ResPartnerDB(mContext))));
		cols.add(new LmisColumn("note_pad_url", "URL", LmisFields.text()));
		cols.add(new LmisColumn("message_follower_ids", "Followers", LmisFields
				.manyToMany(new ResPartnerDB(mContext))));
		return cols;
	}

	public class NoteStages extends LmisDatabase {

		public NoteStages(Context context) {
			super(context);
		}

		@Override
		public String getModelName() {
			return "note.stage";
		}

		@Override
		public List<LmisColumn> getModelColumns() {
			List<LmisColumn> cols = new ArrayList<LmisColumn>();
			cols.add(new LmisColumn("name", "Name", LmisFields.text()));
			return cols;
		}

	}

	public class NoteTags extends LmisDatabase {

		public NoteTags(Context context) {
			super(context);
		}

		@Override
		public String getModelName() {
			return "note.tag";
		}

		@Override
		public List<LmisColumn> getModelColumns() {
			List<LmisColumn> cols = new ArrayList<LmisColumn>();
			cols.add(new LmisColumn("name", "Name", LmisFields.text()));
			return cols;
		}
	}

}
