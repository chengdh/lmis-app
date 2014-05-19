package com.lmis.addons.idea;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisM2MIds;
import com.lmis.orm.LmisValues;
import com.lmis.util.logger.LmisLog;

public class IdeaDemoRecords {
	IdeaDBHelper ideaDb = null;
	Context mContext = null;

	public IdeaDemoRecords(Context context) {
		mContext = context;
		ideaDb = new IdeaDBHelper(mContext);
	}

	public void createDemoRecords() {
		createIdeaUserTypes();
		createIdeaUsers();
		createIdeaCategory();
		createIdea();
		int count = updateRecords();
		LmisLog.log("Row updated : " + count);
	}

	private void createIdeaUserTypes() {
		IdeaDBHelper.IdeaUserType userType = ideaDb.new IdeaUserType(mContext);
		userType.truncateTable();
		for (int i = 1; i <= 3; i++) {
			LmisValues values = new LmisValues();
			values.put("id", i);
			values.put("type", "Type " + i);
			long newId = userType.create(values);
			Log.d("IdeaDemoRecords", newId
					+ " Record created for idea.user.type");
		}
	}

	private void createIdeaUsers() {
		IdeaDBHelper.IdeaUsers ideaUsers = ideaDb.new IdeaUsers(mContext);
		ideaUsers.truncateTable();
		for (int i = 1; i <= 5; i++) {
			LmisValues values = new LmisValues();
			values.put("id", i);
			values.put("name", "User " + i);
			values.put("city", "City " + i);
			values.put("user_type", i); // many to one field
			long newId = ideaUsers.create(values);
			Log.d("IdeaDemoRecords", newId + " Record created for idea.users");
		}
	}

	private void createIdeaCategory() {
		IdeaDBHelper.IdeaCategory ideaCategory = ideaDb.new IdeaCategory(
				mContext);
		ideaCategory.truncateTable();
		for (int i = 1; i <= 3; i++) {
			LmisValues values = new LmisValues();
			values.put("id", i);
			values.put("name", "Category " + i);
			long newId = ideaCategory.create(values);
			Log.d("IdeaDemoRecords", newId
					+ " Record created for idea.category");
		}
	}

	private void createIdea() {
		ideaDb.truncateTable();
		for (int i = 1; i <= 3; i++) {
			LmisValues values = new LmisValues();
			values.put("id", i);
			values.put("name", "Idea " + i);
			values.put("description", "Description " + i);
			values.put("category_id", i);
			Integer[] ids = new Integer[] { 1, 2 };
			List<Integer> user_ids = Arrays.asList(ids);
			values.put("user_ids", user_ids);
			long newId = ideaDb.create(values);
			Log.d("IdeaDemoRecords", newId + " Record created for idea.idea");
		}
	}

	public void selectAll() {
		for (LmisDataRow row : ideaDb.select()) {
			LmisLog.log("RECORD :::::::::::::::::::::::: " + row.getString("id"));
			LmisLog.log("name : " + row.getString("name"));
			LmisLog.log("category : "
                    + row.getM2ORecord("category_id").browse()
                    .getString("name"));
			LmisLog.log("user_ids : "
                    + row.getM2MRecord("user_ids").browseEach().get(0)
                    .getM2ORecord("user_type").browse()
                    .getString("type"));
		}
	}

	public int updateRecords() {
		int count = 0;
		LmisValues values = new LmisValues();
		values.put("description", "Updated Description");
		values.put("category_id", 3);
		Integer[] ids = new Integer[] { 3, 4 };
		List<Integer> user_ids = Arrays.asList(ids);
		values.put("user_ids", new LmisM2MIds(LmisM2MIds.Operation.APPEND, user_ids));
		count = ideaDb.update(values, "id = ?", new String[] { "2" });
		return count;
	}
}