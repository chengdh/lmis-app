package com.lmis.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.lmis.LmisArguments;
import com.lmis.base.res.ResPartnerDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisHelper;
import com.lmis.orm.LmisM2MIds;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.Base64Helper;
import com.lmis.util.contactview.LmisContactView;
import com.lmis.util.controls.LmisTextView;
import com.lmis.util.tags.MultiTagsTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.tags.TagsItem;
import com.lmis.util.tags.TagsView;

public class NoteFollowers extends BaseFragment implements MultiTagsTextView.TokenListener,
		OnClickListener {
	public static final String TAG = "NoteFollowers";
	View mView = null;
	TagsView mFollowersTag = null;
	GridView mNoteFollowerGrid = null;
	LmisListAdapter mNoteFollowerAdapter = null;
	Bundle mArguments = null;
	List<Object> mFollowerList = new ArrayList<Object>();
	List<Object> mPartnersList = new ArrayList<Object>();
	PartnerLoader mPartnersLoader = null;

	UnSubscribeOperation mUnSubscriber = null;
	SubscribeOperation mSubscriber = null;
	HashMap<String, TagsItem> mSelectedPartners = new HashMap<String, TagsItem>();
	LmisListAdapter mTagsAdapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_note_followers, container,
				false);
		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
		mArguments = getArguments();
		getActivity().setTitle(mArguments.getString("note_name"));
		LmisDataRow note = db().select(mArguments.getInt("note_id"));
		mFollowerList.addAll(note.getM2MRecord("message_follower_ids")
				.browseEach());
		mPartnersLoader = new PartnerLoader(getActivity());
		mPartnersLoader.execute();
		setupTagsView();
		setupGridView();
	}

	private void setupTagsView() {
		mFollowersTag = (TagsView) mView.findViewById(R.id.edtNoteFollowers);
		mFollowersTag.setTokenListener(this);
		mTagsAdapter = new LmisListAdapter(getActivity(),
				R.layout.tags_view_partner_item_layout, mPartnersList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				TagsItem item = (TagsItem) mPartnersList.get(position);
				TextView txvSubSubject, txvSubject;
				ImageView imgPic = (ImageView) mView
						.findViewById(R.id.imgReceipientPic);
				txvSubject = (TextView) mView.findViewById(R.id.txvSubject);
				txvSubSubject = (TextView) mView
						.findViewById(R.id.txvSubSubject);
				txvSubject.setText(item.getSubject());
				if (!item.getSub_subject().equals("false")) {
					txvSubSubject.setText(item.getSub_subject());
				} else {
					txvSubSubject.setText("No email");
				}
				if (item.getImage() != null && !item.getImage().equals("false")) {
					imgPic.setImageBitmap(Base64Helper.getBitmapImage(
                            getActivity(), item.getImage()));
				}
				return mView;
			}
		};
		mFollowersTag.setAdapter(mTagsAdapter);
		mView.findViewById(R.id.imgBtnAddFollower).setOnClickListener(this);
	}

	private void setupGridView() {
		mNoteFollowerGrid = (GridView) mView
				.findViewById(R.id.noteFollowersGridView);
		mNoteFollowerAdapter = new LmisListAdapter(getActivity(),
				R.layout.fragment_note_followers_grid_item_view, mFollowerList) {
			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				LmisDataRow row = (LmisDataRow) mFollowerList.get(position);
				LmisContactView imgContact = (LmisContactView) mView
						.findViewById(R.id.imgFollowerPic);

				LmisTextView txvName = (LmisTextView) mView
						.findViewById(R.id.txvFollowerName);
				LmisTextView txvEmail = (LmisTextView) mView
						.findViewById(R.id.txvFollowerEmail);
				txvName.setText(row.getString("name"));
				txvEmail.setText(row.getString("email"));

				imgContact.assignPartnerId(row.getInt("id"));
				imgContact.setImageBitmap(Base64Helper.getBitmapImage(
						getActivity(), row.getString("image_small")));
				mView.findViewById(R.id.imgFollowerRemove).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								confirmRemoveFollower(position);
							}
						});

				return mView;
			}
		};
		mNoteFollowerGrid.setAdapter(mNoteFollowerAdapter);
	}

	private void confirmRemoveFollower(final int position) {
		AlertDialog.Builder deleteDialogConfirm = new AlertDialog.Builder(
				getActivity());
		deleteDialogConfirm.setTitle("Unsubscribe");
		deleteDialogConfirm
				.setMessage("Are you sure want to remove follower ?");
		deleteDialogConfirm.setCancelable(true);

		deleteDialogConfirm.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mUnSubscriber = new UnSubscribeOperation(position,
								mArguments.getInt("note_id"));
						mUnSubscriber.execute();
					}
				});

		deleteDialogConfirm.setNegativeButton("No", null);
		deleteDialogConfirm.show();
	}

	class UnSubscribeOperation extends AsyncTask<Void, Void, Void> {

		int mPosition = 0;
		int mNoteID = 0;
		int mPartnerId = 0;
		String mToast = "";

		public UnSubscribeOperation(int position, int note_id) {
			mPosition = position;
			mNoteID = note_id;
			LmisDataRow row = (LmisDataRow) mFollowerList.get(position);
			mPartnerId = row.getInt("id");
		}

		@Override
		protected Void doInBackground(Void... params) {
			LmisHelper oe = db().getOEInstance();
			mToast = "No Connection.";
			if (oe != null) {
				LmisArguments arguments = new LmisArguments();
				arguments.add(new JSONArray().put(mNoteID));
				arguments.add(new JSONArray().put(mPartnerId));
				Boolean result = (Boolean) oe.call_kw("message_unsubscribe",
						arguments);
				if (result) {
					db().updateManyToManyRecords("message_follower_ids",
							LmisM2MIds.Operation.REMOVE, mNoteID, mPartnerId);
				}
				mToast = "Follower removed";
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mFollowerList.remove(mPosition);
			mNoteFollowerAdapter.notifiyDataChange(mFollowerList);
			Toast.makeText(getActivity(), mToast, Toast.LENGTH_LONG).show();

		}
	}

	class SubscribeOperation extends AsyncTask<Void, Void, Void> {
		List<Integer> mPartnerIds = null;
		int mNoteID = 0;
		String mToast = "";

		public SubscribeOperation(List<Integer> ids, int note_id) {
			mPartnerIds = ids;
			mNoteID = note_id;
		}

		@Override
		protected Void doInBackground(Void... params) {
			LmisHelper oe = db().getOEInstance();
			mToast = "No Connection.";
			if (oe != null) {
				try {
					JSONObject args = new JSONObject();
					args.put("res_model", "note.note");
					args.put("res_id", mNoteID);
					args.put("message", "You have been invited to follow "
							+ getArguments().getString("note_name"));
					JSONArray partner_ids = new JSONArray();
					partner_ids.put(6);
					partner_ids.put(false);
					JSONArray partnerIds = new JSONArray();
					for (int id : mPartnerIds) {
						partnerIds.put(id);
					}
					partner_ids.put(partnerIds);
					args.put("partner_ids",
							new JSONArray("[" + partner_ids.toString() + "]"));
					JSONObject result = oe
							.createNew("mail.wizard.invite", args);
					int id = result.getInt("result");
					LmisArguments arguments = new LmisArguments();
					arguments.add(new JSONArray("[" + id + "]"));
					JSONObject context = new JSONObject();
					context.put("default_res_model", "note.note");
					context.put("default_res_id", mNoteID);
					oe.debugMode(true);
					JSONObject res = (JSONObject) oe.call_kw(
							"mail.wizard.invite", "add_followers", arguments,
							context);
					if (res != null) {
						db().updateManyToManyRecords("message_follower_ids",
								LmisM2MIds.Operation.APPEND, mNoteID, mPartnerIds);
						mToast = "Follower added";
						for (Object obj : mFollowersTag.getObjects()) {
							mFollowersTag.removeObject(obj);
						}
					}
				} catch (Exception e) {
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ResPartnerDB partner = new ResPartnerDB(getActivity());
			for (int id : mPartnerIds) {
				mFollowerList.add(partner.select(id));
			}
			mNoteFollowerAdapter.notifiyDataChange(mFollowerList);
			Toast.makeText(getActivity(), mToast, Toast.LENGTH_LONG).show();
		}

	}

	class PartnerLoader extends AsyncTask<Void, Void, Void> {

		ResPartnerDB mPartner = null;
		FragmentActivity mActivity = null;
		LmisHelper mOpenERP = null;

		public PartnerLoader(FragmentActivity activity) {
			mPartner = new ResPartnerDB(activity);
			mActivity = activity;
			mOpenERP = mPartner.getOEInstance();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mPartnersList.clear();
			if (mOpenERP != null) {
				for (LmisDataRow row : mOpenERP.search_read()) {
					mPartnersList.add(new TagsItem(row.getInt("id"), row
							.getString("name"), row.getString("email"), row
							.getString("image_small")));
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mTagsAdapter.notifiyDataChange(mPartnersList);
		}

	}

	@Override
	public Object databaseHelper(Context context) {
		return new NoteDB(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	@Override
	public void onPause() {
		super.onPause();
		mPartnersLoader.cancel(true);
		mPartnersLoader = null;
	}

	@Override
	public void onTokenAdded(Object token, View view) {
		TagsItem tag = (TagsItem) token;
		mSelectedPartners.put("_" + tag.getId(), tag);
	}

	@Override
	public void onTokenSelected(Object token, View view) {

	}

	@Override
	public void onTokenRemoved(Object token) {
		TagsItem tag = (TagsItem) token;
		mSelectedPartners.remove("_" + tag.getId());
	}

	/**
	 * Add Follower click listener
	 */
	@Override
	public void onClick(View v) {
		List<Integer> ids = new ArrayList<Integer>();
		List<Object> mIds = new ArrayList<Object>();
		ResPartnerDB partner = new ResPartnerDB(getActivity());
		for (String key : mSelectedPartners.keySet()) {
			TagsItem item = mSelectedPartners.get(key);
			ids.add(item.getId());
			mIds.add(item.getId());
		}
		LmisHelper oe = partner.getOEInstance();
		if (oe != null) {
			oe.syncWithServer(false, null, mIds);
		}
		mSubscriber = new SubscribeOperation(ids, mArguments.getInt("note_id"));
		mSubscriber.execute();
	}
}
