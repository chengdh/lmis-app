package com.lmis.support.calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

import com.lmis.addons.meeting.MeetingDB;
import com.lmis.auth.LmisAccountManager;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisHelper;
import com.lmis.orm.LmisM2MIds;
import com.lmis.orm.LmisValues;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisDate;

public class LmisCalendar {

	public static final String TAG = "LmisCalendar";

	Context mContext = null;
	ContentResolver mContentResolver = null;
	Account mAccount = null;

	Long mCalendarId = 0L;

	MeetingDB mMeetingDB = null;

	/**
	 * Event Projections
	 */
	String[] mEventsProjection = new String[] { CalendarContract.Events._ID,
			CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION,
			CalendarContract.Events.CALENDAR_ID,
			CalendarContract.Events.DURATION, CalendarContract.Events.DTSTART,
			CalendarContract.Events.DTEND,
			CalendarContract.Events.EVENT_LOCATION,
			CalendarContract.Events.ALL_DAY };

	public LmisCalendar(Context context) {
		Log.d(TAG, "LmisCalendar->constructor()");
		mContext = context;
		mMeetingDB = new MeetingDB(mContext);
		mAccount = LmisAccountManager.getAccount(mContext,
                LmisUser.current(mContext).getAndroidName());
		mContentResolver = mContext.getContentResolver();

		if (mAccount != null)
			initCalendar();
	}

	private void initCalendar() {
		Log.d(TAG, "LmisCalendar->initCalendar()");
		Uri mCalendarUri = Calendars.CONTENT_URI;

		long calendar_id = 0L;

		String[] projection = new String[] { Calendars._ID,
				Calendars.ACCOUNT_NAME, Calendars.CALENDAR_DISPLAY_NAME };

		String selection = Calendars.ACCOUNT_NAME + " = ? ";
		String[] selectionArgs = new String[] { mAccount.name };

		Cursor cr = mContentResolver.query(mCalendarUri, projection, selection,
				selectionArgs, null);
		if (cr.moveToFirst()) {
			calendar_id = cr.getLong(cr.getColumnIndex(Calendars._ID));
		} else {
			calendar_id = createCalendar();
		}
		cr.close();
		mCalendarId = calendar_id;
		cr.close();
	}

	private long createCalendar() {
		Log.d(TAG, "LmisCalendar->createCalendar()");
		long calendar_id = 0;

		ContentValues values = new ContentValues();
		values.put(Calendars.ACCOUNT_NAME, mAccount.name);
		values.put(CalendarContract.Calendars.ACCOUNT_TYPE, mAccount.type);
		values.put(Calendars.NAME, mAccount.name);
		values.put(Calendars.CALENDAR_DISPLAY_NAME, mAccount.name);
		values.put(Calendars.CALENDAR_COLOR, Color.parseColor("#cc0000"));
		values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
		values.put(Calendars.OWNER_ACCOUNT, mAccount.name);
		values.put(Calendars.SYNC_EVENTS, 1);
		values.put(Calendars.VISIBLE, 1);
		values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone
				.getDefault().getID());
		Uri newUri = mContentResolver.insert(getSyncAdapter(), values);
		calendar_id = Long.parseLong(newUri.getLastPathSegment());
		Log.d(TAG, "CreateCalendar()->calendar_id = " + calendar_id);
		return calendar_id;
	}

	private Uri getSyncAdapter() {
		Uri uri = Calendars.CONTENT_URI;
		return uri
				.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
						"true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, mAccount.name)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, mAccount.type)
				.build();
	}

	public void syncCalendar() {
		Log.d(TAG, "LmisCalendar->syncCalendar()");
		Log.i(TAG, "Calendar ID : " + mCalendarId);
		if (addNewMeetings()) {
			List<LmisValues> calendarEvents = calendarEvents();
			List<Integer> eventIds = new ArrayList<Integer>();
			for (LmisValues event : calendarEvents)
				eventIds.add(event.getInt(CalendarContract.Events._ID));
			for (LmisDataRow meeting : mMeetingDB.select()) {
				int event_id = meeting.getInt("calendar_event_id");
				if (eventIds.contains(event_id)) {
					eventIds.remove(eventIds.indexOf(event_id));
				}
			}
			if (pushOnServer(eventIds)) {
				updateAllEvents();
			}
		}

	}

	private void updateAllEvents() {
		for (LmisDataRow meeting : mMeetingDB.select()) {
			updateEvent(meeting);
		}
	}

	private boolean pushOnServer(List<Integer> eventIds) {
		if (eventIds.size() > 0) {
			LmisHelper oe = mMeetingDB.getOEInstance();
			if (oe != null) {
				for (int event_id : eventIds) {
					LmisValues values = new LmisValues();
					LmisValues event = selectEvent(event_id);
					values.put("name",
							event.getString(CalendarContract.Events.TITLE));

					long startDate = event
							.getLong(CalendarContract.Events.DTSTART);
					long endDate = event.getLong(CalendarContract.Events.DTEND);
					values.put("date", LmisDate.getDateFromMilis(startDate));
					values.put("date_deadline",
							LmisDate.getDateFromMilis(endDate));
					int allday = event.getInt(CalendarContract.Events.ALL_DAY);
					values.put("duration", (allday == 1) ? allday
							: getDuration(startDate, endDate));
					values.put("allday", (allday == 1) ? true : false);
					values.put("location", event
							.getString(CalendarContract.Events.EVENT_LOCATION));
					values.put("description", event
							.getString(CalendarContract.Events.DESCRIPTION));

					List<Integer> partners = new ArrayList<Integer>();
					partners.add(LmisUser.current(mContext).getPartner_id());
					LmisM2MIds partner_ids = new LmisM2MIds(LmisM2MIds.Operation.ADD, partners);
					values.put("partner_ids", partner_ids);
					int newId = oe.create(values);
					values = new LmisValues();
					values.put("calendar_event_id", event_id);
					values.put("calendar_id", mCalendarId);
					mMeetingDB.update(values, newId);
				}
			}
		}
		return true;
	}

	private float getDuration(long startDate, long endDate) {
		long offset = endDate - startDate;
		long datehours = offset / (60 * 60 * 1000);
		long dateminutes = offset / (60 * 1000);
		long duration = dateminutes - (datehours * 60);
		String hour = String.valueOf(datehours);
		String minute = String.valueOf(duration);
		StringBuffer durationString = new StringBuffer();
		durationString.append(hour);
		durationString.append(".");
		durationString.append(minute);
		return Float.parseFloat(durationString.toString());
	}

	private boolean addNewMeetings() {
		Log.d(TAG, "LmisCalendar->addNewMeetings()");
		List<LmisDataRow> new_meetings = mMeetingDB.select(
				"calendar_id IS NULL AND calendar_event_id IS NULL",
				new String[] {});
		for (LmisDataRow meeting : new_meetings) {
			long new_calendar_event_id = createEvent(meeting);
			LmisValues values = new LmisValues();
			values.put("calendar_event_id", new_calendar_event_id);
			values.put("calendar_id", mCalendarId);
			int count = mMeetingDB.update(values, meeting.getInt("id"));
			Log.i(TAG, count + " meeting updated");
		}
		return true;
	}

	public boolean removeEvents(List<LmisDataRow> meetings) {
		Log.d(TAG, "LmisCalendar->removeMeetings()");
		for (LmisDataRow row : meetings)
			removeEvent(row.getInt("calendar_event_id"));
		return true;
	}

	private LmisValues selectEvent(int event_id) {
		LmisValues event = new LmisValues();
		Cursor cr = mContentResolver.query(Events.CONTENT_URI,
				mEventsProjection, CalendarContract.Events._ID + " = ?",
				new String[] { event_id + "" }, null);
		if (cr.moveToFirst()) {
			for (String key : mEventsProjection) {
				int index = cr.getColumnIndex(key);
				Object value = "";
				if (cr.getString(index) != null)
					value = cr.getString(index);
				event.put(key, value);
			}
		}
		cr.close();
		return event;
	}

	private List<LmisValues> calendarEvents() {
		Log.d(TAG, "LmisCalendar->calendarEvents()");
		List<LmisValues> events = new ArrayList<LmisValues>();

		Cursor cr = mContentResolver.query(Events.CONTENT_URI,
				mEventsProjection, null, null, null);
		if (cr.moveToFirst()) {
			do {
				LmisValues event = new LmisValues();
				for (String key : mEventsProjection) {
					int index = cr.getColumnIndex(key);
					if (cr.getType(index) == Cursor.FIELD_TYPE_INTEGER)
						event.put(key, cr.getInt(index));
					else
						event.put(key, cr.getString(index));
				}
				events.add(event);
			} while (cr.moveToNext());
		}
		cr.close();
		return events;
	}

	private boolean removeEvent(int event_id) {
		Log.d(TAG, "LmisCalendar->removeEvent()");
		boolean flag = false;
		int count = mContentResolver.delete(Events.CONTENT_URI,
				CalendarContract.Events._ID + " = ? AND "
						+ CalendarContract.Events.CALENDAR_ID + " = ?",
				new String[] { event_id + "", mCalendarId + "" });
		if (count > 0) {
			flag = true;
			Log.i(TAG, "removedEvent : " + event_id);
		}
		return flag;
	}

	private long createEvent(LmisDataRow meeting) {
		Log.d(TAG, "LmisCalendar->createEvent()");
		long calendar_event_id = 0L;
		calendar_event_id = Long.parseLong(mContentResolver.insert(
				Events.CONTENT_URI, getValues(meeting)).getLastPathSegment());
		Log.i(TAG, "LmisCalendar->createEvent() : " + calendar_event_id);
		return calendar_event_id;
	}

	private void updateEvent(LmisDataRow meeting) {
		Log.d(TAG, "LmisCalendar->updateEvent()");
		mContentResolver.update(Events.CONTENT_URI, getValues(meeting),
				Events._ID + " = ?",
				new String[] { meeting.getString("calendar_event_id") });
		Log.i(TAG,
				"LmisCalendar->updateEvent() : "
						+ meeting.getString("calendar_event_id"));
	}

	private ContentValues getValues(LmisDataRow meeting) {
		ContentValues values = new ContentValues();
		values.put(CalendarContract.Events.TITLE, meeting.getString("name"));
		String description = meeting.getString("description");
		values.put(CalendarContract.Events.DESCRIPTION,
				(description.equals("false")) ? "" : description);
		values.put(CalendarContract.Events.CALENDAR_ID, mCalendarId);
		values.put(CalendarContract.Events.DTSTART,
				LmisDate.getDateTimeInMilis(meeting.getString("date")));
		values.put(CalendarContract.Events.ALL_DAY,
				meeting.getBoolean("allday"));

		values.put(CalendarContract.Events.DTEND,
				LmisDate.getDateTimeInMilis(meeting.getString("date_deadline")));
		String location = meeting.getString("location");
		values.put(CalendarContract.Events.EVENT_LOCATION,
				(location.equals("false")) ? "" : location);
		values.put(CalendarContract.Events.EVENT_TIMEZONE,
				LmisUser.current(mContext).getTimezone());
		return values;
	}
}
