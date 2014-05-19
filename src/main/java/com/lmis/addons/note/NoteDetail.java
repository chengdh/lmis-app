package com.lmis.addons.note;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisHelper;
import com.lmis.support.fragment.FragmentListener;
import com.lmis.util.HTMLHelper;
import com.lmis.util.controls.LmisTextView;
import com.lmis.util.tags.MultiTagsTextView;
import com.lmis.R;
import com.lmis.addons.message.MessageComposeActivity;
import com.lmis.addons.note.Note.NoteToggleStatus;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.tags.TagsItem;
import com.lmis.util.tags.TagsView;

public class NoteDetail extends BaseFragment implements MultiTagsTextView.TokenListener {

	public static final String TAG = "NoteDetail";
	View mView = null;
	Bundle mArgument = null;
	LmisTextView mNoteDetailTitle;
	LmisTextView mNoteDetailMemo;
	TagsView mNoteTags = null;
	int mStageColor = 0;
	String mPadURL = "";
	String mNoteMemo = "";
	String mMessageBody = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_note_detail, container,
				false);
		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle != null) {
			mArgument = bundle;
			if (mArgument.containsKey("stage_color")) {
				View vStageColor = (View) mView
						.findViewById(R.id.viewNoteStageColor);
				vStageColor.setBackgroundColor(mArgument.getInt("stage_color"));
				mStageColor = mArgument.getInt("stage_color");
			}
		}
	}

	private void showNoteDetails(int note_id) {
		mNoteDetailTitle = (LmisTextView) mView
				.findViewById(R.id.txvNoteDetailTitle);
		mNoteDetailMemo = (LmisTextView) mView
				.findViewById(R.id.txvNoteDetailMemo);
		mNoteTags = (TagsView) mView.findViewById(R.id.edtNoteTagsView);
		mNoteTags.setTokenListener(this);
		mNoteTags.setCustomTagView(new TagsView.CustomTagViewListener() {

			@Override
			public View getViewForTags(LayoutInflater layoutInflater,
					Object object, ViewGroup tagsViewGroup) {
				View view = (View) layoutInflater.inflate(
						R.layout.custom_note_tagsview_item, tagsViewGroup,
						false);
				TagsItem item = (TagsItem) object;
				LmisTextView txvTitle = (LmisTextView) view
						.findViewById(R.id.txvCustomNoteTagsViewItem);
				txvTitle.setText(item.getSubject());
				txvTitle.setBackgroundColor(mStageColor);
				return view;
			}
		});
		for (Object tag : mNoteTags.getObjects()) {
			mNoteTags.removeObject(tag);
		}
		mNoteTags.allowDuplicates(false);
		mNoteDetailMemo.setMovementMethod(new ScrollingMovementMethod());
		LmisDataRow result = db().select(note_id);
		if (!result.getString("note_pad_url").equals("false")) {
			mPadURL = result.getString("note_pad_url");
		}
		mNoteMemo = result.getString("memo");
		List<TagsItem> noteTags = new ArrayList<TagsItem>();
		mNoteTags.showImage(false);
		for (LmisDataRow tag : result.getM2MRecord("tag_ids").browseEach()) {
			TagsItem tag_item = new TagsItem(tag.getInt("id"),
					tag.getString("name"), null);
			noteTags.add(tag_item);
			mNoteTags.addObject(tag_item);
		}
		if (noteTags.size() == 0) {
			mNoteTags.setVisibility(View.GONE);
		}
		mMessageBody = result.getString("memo");
		mNoteDetailTitle.setText(result.getString("name"));
		mNoteDetailMemo.setText(HTMLHelper.stringToHtml(result
                .getString("memo")));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.menu_fragment_note_detail, menu);
		// disabling the Compose Note option cause you are already in that menu
		if (getArguments() != null) {
			mArgument = getArguments();
			boolean open = mArgument.getBoolean("row_status");
			if (open) {
				MenuItem mark_as_open = menu
						.findItem(R.id.menu_note_mark_asopen);
				mark_as_open.setVisible(false);
			} else {
				MenuItem mark_as_done = menu
						.findItem(R.id.menu_note_mark_asdone);
				mark_as_done.setVisible(false);
			}

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		NoteToggleStatus status = null;
		Note note = new Note();
		switch (item.getItemId()) {
		case R.id.menu_note_followers:

			NoteFollowers noteFollowers = new NoteFollowers();
			Bundle args = new Bundle();
			args.putInt("note_id", mArgument.getInt("note_id"));
			args.putString("note_name", mNoteDetailTitle.getText().toString());
			noteFollowers.setArguments(args);
			FragmentListener mFragment = (FragmentListener) getActivity();
			mFragment.startDetailFragment(noteFollowers);
			return true;

		case R.id.menu_note_forward_asmail:

			Intent sendAsMail = new Intent(getActivity(),
					MessageComposeActivity.class);
			sendAsMail.putExtra("note_body", mMessageBody);
			getActivity().startActivity(sendAsMail);
			return true;

		case R.id.menu_note_mark_asdone:

			status = note.new NoteToggleStatus(mArgument.getInt("note_id"),
					true, getActivity());
			status.execute();
			return true;

		case R.id.menu_note_mark_asopen:

			status = note.new NoteToggleStatus(mArgument.getInt("note_id"),
					false, getActivity());
			status.execute();
			return true;

		case R.id.menu_note_edit:
			Intent manageNote = new Intent(getActivity(),
					NoteComposeActivity.class);
			Bundle noteArgs = new Bundle();
			noteArgs.putInt("note_id", mArgument.getInt("note_id"));
			manageNote.putExtras(noteArgs);
			startActivity(manageNote);
			return true;

		case R.id.menu_note_delete:
			confirmDeleteNote(mArgument.getInt("note_id"));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void confirmDeleteNote(final int id) {
		AlertDialog.Builder deleteDialogConfirm = new AlertDialog.Builder(
				getActivity());
		deleteDialogConfirm.setTitle("Delete");
		deleteDialogConfirm.setMessage("Are you sure want to delete ?");
		deleteDialogConfirm.setCancelable(true);

		deleteDialogConfirm.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						LmisHelper oe = db().getOEInstance();
						if (oe != null) {
							oe.delete(id);
							getActivity().getSupportFragmentManager()
									.popBackStack();
						} else {
							Toast.makeText(getActivity(),
									"Operation aborted. No connection.",
									Toast.LENGTH_LONG).show();
						}

					}
				});

		deleteDialogConfirm.setNegativeButton("Cancel", null);
		deleteDialogConfirm.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		showNoteDetails(getArguments().getInt("note_id"));
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
	public void onTokenAdded(Object token, View view) {

	}

	@Override
	public void onTokenSelected(Object token, View view) {

	}

	@Override
	public void onTokenRemoved(Object token) {
		mNoteTags.addObject(token);

	}

}
