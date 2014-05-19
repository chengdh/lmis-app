package com.lmis.widgets.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.lmis.addons.message.MessageDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.LmisUser;
import com.lmis.util.HTMLHelper;
import com.lmis.util.LmisDate;
import com.lmis.widgets.WidgetHelper;

public class MessageRemoteViewFactory implements RemoteViewsFactory {

	public static final String TAG = "MessageRemoteViewFactory";

	Context mContext = null;
	int mAppWidgetId = -1;
	int[] starred_drawables = new int[] { com.lmis.R.drawable.ic_action_starred,
			com.lmis.R.drawable.ic_action_unstarred };

	List<Object> mMessageListItems = new ArrayList<Object>();
	String mFilter = "inbox";

	public MessageRemoteViewFactory(Context context, Intent intent) {
		Log.d(TAG, "MessageRemoteViewFactory->constructor()");
		mContext = context;
		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		mFilter = intent.getExtras().getString(
				AppWidgetManager.EXTRA_APPWIDGET_OPTIONS);
	}

	public int getCount() {
		return mMessageListItems.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public RemoteViews getLoadingView() {
		RemoteViews mView = new RemoteViews(mContext.getPackageName(),
				com.lmis.R.layout.listview_data_loading_progress);
		return mView;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		Log.d(TAG, "getViewAt()");
		RemoteViews mView = new RemoteViews(mContext.getPackageName(),
				com.lmis.R.layout.fragment_message_listview_items);
		LmisDataRow row = (LmisDataRow) mMessageListItems.get(position);

		boolean to_read = row.getBoolean("to_read");

		if (!to_read) {
			mView.setTextColor(com.lmis.R.id.txvMessageSubject, Color.BLACK);
			mView.setTextColor(com.lmis.R.id.txvMessageFrom, Color.BLACK);
		} else {
			mView.setTextColor(com.lmis.R.id.txvMessageSubject,
					Color.parseColor("#414141"));
			mView.setTextColor(com.lmis.R.id.txvMessageFrom, Color.parseColor("#414141"));
		}

		final boolean starred = row.getBoolean("starred");
		mView.setImageViewResource(com.lmis.R.id.imgMessageStarred,
				(starred) ? starred_drawables[0] : starred_drawables[1]);

		String subject = row.getString("subject");
		if (subject.equals("false")) {
			subject = row.getString("type");
		}
		if (!row.getString("record_name").equals("false"))
			subject = row.getString("record_name");
		mView.setTextViewText(com.lmis.R.id.txvMessageSubject, subject);

		if (row.getInt("childs") > 0) {
			mView.setViewVisibility(com.lmis.R.id.txvChilds, View.VISIBLE);
			mView.setTextViewText(com.lmis.R.id.txvChilds, row.getString("childs")
					+ " reply");
		} else
			mView.setViewVisibility(com.lmis.R.id.txvChilds, View.GONE);

		mView.setTextViewText(com.lmis.R.id.txvMessageBody,
				HTMLHelper.htmlToString(row.getString("body")));
		String date = row.getString("date");
		mView.setTextViewText(com.lmis.R.id.txvMessageDate,
				LmisDate.getDate(date, TimeZone.getDefault().getID()));
		mView.setTextColor(com.lmis.R.id.txvMessageDate, Color.parseColor("#414141"));

		String from = row.getString("email_from");
		if (from.equals("false")) {
			LmisDataRow author_id = row.getM2ORecord("author_id").browse();
			if (author_id != null)
				from = row.getM2ORecord("author_id").browse().getString("name");
		}

		mView.setTextViewText(com.lmis.R.id.txvMessageFrom, from);
		mView.setViewVisibility(com.lmis.R.id.txvMessageTag, View.GONE);

		final Intent fillInIntent = new Intent();
		fillInIntent.setAction(WidgetHelper.ACTION_WIDGET_CALL);
		final Bundle bundle = new Bundle();
		bundle.putInt(WidgetHelper.EXTRA_WIDGET_DATA_VALUE, row.getInt("id"));
		fillInIntent.putExtras(bundle);
		mView.setOnClickFillInIntent(com.lmis.R.id.messageListViewItem, fillInIntent);
		return mView;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onCreate() {
		if (LmisUser.current(mContext) == null)
			return;
		mMessageListItems.clear();
		HashMap<String, LmisDataRow> parents = new HashMap<String, LmisDataRow>();
		MessageDB message = new MessageDB(mContext);
		HashMap<String, Object> whereMap = getWhere(mFilter);
		String where = (String) whereMap.get("where");
		String[] whereArgs = (String[]) whereMap.get("whereArgs");
		List<LmisDataRow> messages = message.select(where, whereArgs, null, null,
				"date DESC");
		for (LmisDataRow row : messages) {
			boolean isParent = true;
			String key = row.getString("parent_id");
			if (key.equals("false")) {
				key = row.getString("id");
			} else {
				isParent = false;
			}
			if (!parents.containsKey(key)) {
				// Fetching row parent message
				LmisDataRow newRow = null;

				if (isParent) {
					newRow = row;
				} else {
					newRow = message.select(Integer.parseInt(key));
				}

				int childs = message.count("parent_id = ? ",
						new String[] { key });
				newRow.put("childs", childs);
				parents.put(key, null);
				mMessageListItems.add(newRow);
			}
		}
	}

	public HashMap<String, Object> getWhere(String type) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		String where = null;
		String[] whereArgs = null;
		if (type != null) {
			if (type.equals("inbox")) {
				where = "to_read = ? AND starred = ?";
				whereArgs = new String[] { "true", "false" };
			}
			if (type.equals("to-do")) {
				where = "to_read = ? AND starred = ?";
				whereArgs = new String[] { "true", "true" };
			}
			if (type.equals("to:me")) {
				where = "res_id = ? AND to_read = ?";
				whereArgs = new String[] { "0", "true" };
			}
		}
		map.put("where", where);
		map.put("whereArgs", whereArgs);
		return map;
	}

	@Override
	public void onDataSetChanged() {
		onCreate();
	}

	@Override
	public void onDestroy() {
	}
}
