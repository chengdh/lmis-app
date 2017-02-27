package com.lmis.addons.cux_tran;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lmis.R;
import com.lmis.addons.cux_demand.CuxDemandDetail;
import com.lmis.orm.LmisDataRow;
import com.lmis.providers.cux_tran.CuxTranProvider;
import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.support.fragment.FragmentListener;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.drawer.DrawerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-27.
 */
public class CuxTranList extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final String TAG = "CuxTranList";


    int mSelectedItemPosition = -1;

    String mCurrentType = "draft";

    View mView = null;

    SearchView mSearchView = null;

    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.list_cux_tran)
    PullToRefreshListView mListView;

    @InjectView(R.id.txv_cux_tran_blank)
    TextView mTxvBlank;

    List<Object> mCuxTranObjects = new ArrayList<Object>();

    CuxTranLoader mCuxDemandLoader = null;


    HashMap<Integer, Boolean> mMultiSelectedRows = new HashMap<Integer, Boolean>();

    Integer mSelectedCounter = 0;

    /**
     * The enum M type.
     * 数据分为草稿及已处理
     */
    private enum MType {
        DRAFT, PROCESSED
    }

    ;
    MType mType = MType.DRAFT;

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolder {
        @InjectView(R.id.txv_project_name)
        TextView txvProjectName;
        @InjectView(R.id.txv_require_department)
        TextView txvRequireDept;
        @InjectView(R.id.txv_require_person)
        TextView txvUser;

        @InjectView(R.id.txv_require_date)
        TextView txvRequireDate;

        @InjectView(R.id.txv_header_bugdet)
        TextView txvHeaderBugdet;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    AbsListView.MultiChoiceModeListener mMultiChoiceListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mMultiSelectedRows.put(position, checked);
            if (checked) {
                mSelectedCounter++;
            } else {
                mSelectedCounter--;
            }
            if (mSelectedCounter != 0) {
                mode.setTitle(mSelectedCounter + "");
            }
        }

        @SuppressLint("UseSparseArrays")
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_inventory_move_list_choice_delete:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_fragment_inventory_move_list_context, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mSelectedCounter = 0;
            mListView.getRefreshableView().clearChoices();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectedItemPosition = savedInstanceState.getInt("mSelectedItemPosition", -1);
        }
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_cux_tran_list, container, false);
        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    /**
     * Init void.
     * 初始化界面及数据
     */
    private void init() {
        mTxvBlank.setVisibility(View.GONE);
        mListView.getRefreshableView().setOnItemClickListener(this);
        mListView.getRefreshableView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.getRefreshableView().setOnItemLongClickListener(this);
        mListView.getRefreshableView().setMultiChoiceModeListener(mMultiChoiceListener);
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_cux_tran_list_item, mCuxTranObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getActivity().getLayoutInflater().inflate(getResource(), parent, false);
                    view.setTag(new ViewHolder(view));
                }
                view = handleRowView(view, position);
                return view;
            }

        };
        mListView.getRefreshableView().setAdapter(mListViewAdapter);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {
                scope.main().requestSync(CuxTranProvider.AUTHORITY);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {

            }
        });

        initData();

    }

    private void initData() {
        Log.d(TAG, "CuxTranList->initData()");
        String title = "Draft";

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (mCuxDemandLoader != null) {
                mCuxDemandLoader.cancel(true);
                mCuxDemandLoader = null;
            }
            if (bundle.containsKey("type")) {
                mCurrentType = bundle.getString("type");
                if (mCurrentType.equals("draft")) {
                    mType = MType.DRAFT;
                } else if (mCurrentType.equals("processed")) {
                    mType = MType.PROCESSED;
                    title = "Processed";
                }
            }
            scope.main().setTitle(title);
            mCuxDemandLoader = new CuxTranLoader(mType);
            mCuxDemandLoader.execute((Void) null);
        }
    }

    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mCuxTranObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mCuxTranObjects.get(position);

            String projectName = row_data.getString("name");
            String requireDept = row_data.getString("require_deparment");
            String requirePerson = row_data.getString("require_person");
            String requireDate = row_data.getString("require_date");
            String headerBugdet = row_data.getString("header_bugdet");
            holder.txvProjectName.setText(projectName);
            holder.txvRequireDept.setText(requireDept);
            holder.txvUser.setText("[" + requirePerson + "]");
            holder.txvRequireDate.setText(requireDate);
            holder.txvHeaderBugdet.setText("RMB:" + headerBugdet);
        }

        return mView;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new CuxTranHeaderDB(context);
    }


    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        drawerItems.add(new DrawerItem(TAG, "领料单", true));
        drawerItems.add(new DrawerItem(TAG, "待处理", count(MType.DRAFT, context), R.drawable.ic_menu_message_unread, getFragment("draft")));
        drawerItems.add(new DrawerItem(TAG, "已处理", count(MType.PROCESSED, context), R.drawable.ic_menu_message_read, getFragment("processed")));
        return drawerItems;
    }

    /**
     * Gets where.
     * 根据类型构造where条件
     *
     * @param type the type
     * @return the where
     */
    public HashMap<String, Object> getWhere(MType type) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String where = null;
        String[] whereArgs = null;
        switch (type) {
            case DRAFT:
                where = "processed = ? or processed is null ";
                whereArgs = new String[]{"false"};
                break;
            case PROCESSED:
                where = "processed = ? ";
                whereArgs = new String[]{"true"};
                break;
            default:
                where = "processed = ? ";
                whereArgs = new String[]{"false"};
                break;
        }
        map.put("where", where);
        map.put("whereArgs", whereArgs);
        return map;
    }

    /**
     * Gets fragment.
     * 根据查看数据类型构造显示界面
     *
     * @param value the value
     * @return the fragment
     */
    private BaseFragment getFragment(String value) {
        CuxTranList list = new CuxTranList();
        Bundle bundle = new Bundle();
        bundle.putString("type", value);
        list.setArguments(bundle);
        return list;
    }

    private int count(MType type, Context context) {
        int count = 0;
        CuxTranHeaderDB db = (CuxTranHeaderDB) databaseHelper(context);
        String where = null;
        String whereArgs[] = null;
        HashMap<String, Object> obj = getWhere(type);
        where = (String) obj.get("where");
        whereArgs = (String[]) obj.get("whereArgs");
        count = db.count(where, whereArgs);
        return count;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 0)
            return;

        position -= mListView.getRefreshableView().getHeaderViewsCount();

        mSelectedItemPosition = position;
        LmisDataRow row = (LmisDataRow) mCuxTranObjects.get(position);
        BaseFragment detail;
        detail = new CuxTranDetail();
        Bundle bundle = new Bundle();
        bundle.putInt("cux_tran_id", row.getInt("id"));
        bundle.putInt("position", position);
        detail.setArguments(bundle);

        FragmentListener listener = (FragmentListener) getActivity();
        listener.startDetailFragment(detail);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    public class CuxTranLoader extends AsyncTask<Void, Void, Boolean> {
        MType mType = null;

        public CuxTranLoader(MType type) {
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "CuxTranLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mType);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "");
            mCuxTranObjects.clear();
            mCuxTranObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "CuxTranLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mCuxTranObjects);
            mTxvBlank.setVisibility(mCuxTranObjects.size() > 0 ? View.GONE : View.VISIBLE);
            mCuxDemandLoader = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        scope.context().registerReceiver(datasetChangeReceiver, new IntentFilter(DataSetChangeReceiver.DATA_CHANGED));
        scope.context().registerReceiver(cuxTranSyncFinish, new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
    }

    @Override
    public void onPause() {
        super.onPause();
        scope.context().unregisterReceiver(datasetChangeReceiver);
        scope.context().unregisterReceiver(cuxTranSyncFinish);
        Bundle outState = new Bundle();
        outState.putInt("mSelectedItemPosition", mSelectedItemPosition);
        onSaveInstanceState(outState);

    }

    private SyncFinishReceiver cuxTranSyncFinish = new SyncFinishReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "CuxTranList->SyncFinishReceiverReceiver@onReceive");
            scope.main().refreshDrawer(TAG);
            mListView.onRefreshComplete();
            mListViewAdapter.clear();
            mCuxTranObjects.clear();
            mListViewAdapter.notifiyDataChange(mCuxTranObjects);
            new CuxTranLoader(mType).execute();
        }
    };


    private DataSetChangeReceiver datasetChangeReceiver = new DataSetChangeReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                Log.d(TAG, "CuxTranList->datasetChangeReceiver@onReceive");

                String id = intent.getExtras().getString("id");
                String model = intent.getExtras().getString("model");
                if (model.equals("CuxTran")) {
                    LmisDataRow row = db().select(Integer.parseInt(id));
                    mCuxTranObjects.add(0, row);
                    mListViewAdapter.notifiyDataChange(mCuxTranObjects);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
}
