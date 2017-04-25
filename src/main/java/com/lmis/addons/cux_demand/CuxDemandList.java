package com.lmis.addons.cux_demand;

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
import com.lmis.orm.LmisDataRow;
import com.lmis.providers.cux_demand.CuxDemandProvider;
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
public class CuxDemandList extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final String TAG = "CuxDemandList";


    int mSelectedItemPosition = -1;

    String mCurrentType = "draft";

    View mView = null;

    SearchView mSearchView = null;

    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.list_cux_demand)
    PullToRefreshListView mListView;

    @InjectView(R.id.txv_cux_demand_blank)
    TextView mTxvBlank;

    List<Object> mCuxDemandObjects = new ArrayList<Object>();

    CuxDemandLoader mCuxDemandLoader = null;


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
        @InjectView(R.id.txv_apply_department)
        TextView txvApplyDept;
        @InjectView(R.id.txv_applier_user)
        TextView txvUser;

        @InjectView(R.id.txv_apply_date)
        TextView txvApplyDate;

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
        mView = inflater.inflate(R.layout.fragment_cux_demand_list, container, false);
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
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_cux_demand_list_item, mCuxDemandObjects) {
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
                scope.main().requestSync(CuxDemandProvider.AUTHORITY);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {

            }
        });

        initData();

    }

    private void initData() {
        Log.d(TAG, "CuxDemandList->initData()");
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
            mCuxDemandLoader = new CuxDemandLoader(mType);
            mCuxDemandLoader.execute((Void) null);
        }
    }

    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mCuxDemandObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mCuxDemandObjects.get(position);

            String projectName = row_data.getString("project_name");
            String applyDept = row_data.getString("apply_deparment");
            String applyUser = row_data.getString("applier_user");
            String applyDate = row_data.getString("apply_date");
            String headerBugdet = row_data.getString("header_bugdet");
            holder.txvProjectName.setText(projectName);
            holder.txvApplyDept.setText(applyDept);
            holder.txvUser.setText("[" + applyUser + "]");
            holder.txvApplyDate.setText(applyDate.substring(0, 10));
            holder.txvHeaderBugdet.setText("RMB:" + headerBugdet);
        }

        return mView;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new CuxDemandPlatformHeaderDB(context);
    }


    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        drawerItems.add(new DrawerItem(TAG, "需求计划", true));
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
        CuxDemandList list = new CuxDemandList();
        Bundle bundle = new Bundle();
        bundle.putString("type", value);
        list.setArguments(bundle);
        return list;
    }

    private int count(MType type, Context context) {
        int count = 0;
        CuxDemandPlatformHeaderDB db = (CuxDemandPlatformHeaderDB) databaseHelper(context);
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
        LmisDataRow row = (LmisDataRow) mCuxDemandObjects.get(position);
        BaseFragment detail;
        detail = new CuxDemandDetailWebView();
        Bundle bundle = new Bundle();
        bundle.putInt("cux_demand_id", row.getInt("id"));
        bundle.putInt("position", position);
        detail.setArguments(bundle);

        FragmentListener listener = (FragmentListener) getActivity();
        listener.startDetailFragment(detail);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    public class CuxDemandLoader extends AsyncTask<Void, Void, Boolean> {
        MType mType = null;

        public CuxDemandLoader(MType type) {
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "CuxTranLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mType);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "");
            mCuxDemandObjects.clear();
            mCuxDemandObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "CuxTranLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mCuxDemandObjects);
            mTxvBlank.setVisibility(mCuxDemandObjects.size() > 0 ? View.GONE : View.VISIBLE);
            mCuxDemandLoader = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        scope.context().registerReceiver(datasetChangeReceiver, new IntentFilter(DataSetChangeReceiver.DATA_CHANGED));
        scope.context().registerReceiver(cuxDemandSyncFinish, new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
    }

    @Override
    public void onPause() {
        super.onPause();
        scope.context().unregisterReceiver(datasetChangeReceiver);
        scope.context().unregisterReceiver(cuxDemandSyncFinish);
        Bundle outState = new Bundle();
        outState.putInt("mSelectedItemPosition", mSelectedItemPosition);
        onSaveInstanceState(outState);

    }

    private SyncFinishReceiver cuxDemandSyncFinish = new SyncFinishReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "CuxDemandList->SyncFinishReceiverReceiver@onReceive");
            scope.main().refreshDrawer(TAG);
            mListView.onRefreshComplete();
            mListViewAdapter.clear();
            mCuxDemandObjects.clear();
            mListViewAdapter.notifiyDataChange(mCuxDemandObjects);
            new CuxDemandLoader(mType).execute();
        }
    };


    private DataSetChangeReceiver datasetChangeReceiver = new DataSetChangeReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                Log.d(TAG, "CuxDemandList->datasetChangeReceiver@onReceive");

                String id = intent.getExtras().getString("id");
                String model = intent.getExtras().getString("model");
                if (model.equals("CuxDemand")) {
                    LmisDataRow row = db().select(Integer.parseInt(id));
                    mCuxDemandObjects.add(0, row);
                    mListViewAdapter.notifiyDataChange(mCuxDemandObjects);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
}
