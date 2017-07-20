package com.lmis.addons.scan_header;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lmis.CurrentOrgChangeEvent;
import com.lmis.R;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisUser;
import com.lmis.support.fragment.FragmentListener;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode_scan_header.ScanHeaderOpType;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 2017/7/12.
 */

public class ScanHeaderList extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static final String TAG = "ScanHeaderList";


    @Inject
    Bus mBus;

    /**
     * The enum state.
     * 数据分为草稿及已处理
     */
    private enum MState {
        DRAFT, PROCESSED
    }

    MState mState = MState.DRAFT;

    /**
     * 单据类型.
     * 分拣组入库/装卸组入库/装卸组出库
     */
    public enum MType {
        SORTING_IN, LOAD_IN, LOAD_OUT
    }

    public MType getmType() {
        return mType;
    }

    public void setmType(MType mType) {
        this.mType = mType;
    }

    MType mType = MType.SORTING_IN;


    /**
     * 当前选定的数据索引.
     */
    Integer mSelectedItemPosition = -1;

    String mCurrentState = "draft";

    String mCurrentType = ScanHeaderOpType.SORTING_IN;

    View mView = null;

    SearchView mSearchView = null;

    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.listScanHeaders)
    PullToRefreshListView mListView;

    @InjectView(R.id.txvScanHeaderBlank)
    TextView mTxvBlank;

    List<Object> mScanHeaderObjects = new ArrayList<Object>();

    ScanHeaderLoader mScanHeaderLoader = null;


    HashMap<Integer, Boolean> mMultiSelectedRows = new HashMap<Integer, Boolean>();

    Integer mSelectedCounter = 0;


    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolder {
        //发货地
        @InjectView(R.id.txvScanHeaderOrg)
        TextView txvFromTo;
        //描述信息
        @InjectView(R.id.txvScanHeaderDescribe)
        TextView txvDescribe;
        //创建日期
        @InjectView(R.id.txvScanHeaderBillDate)
        TextView txvBillDate;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectedItemPosition = savedInstanceState.getInt("mSelectedItemPosition", -1);
        }
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_scan_header_list, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        init();
        return mView;
    }

    /**
     * Init void.
     * 初始化界面及数据
     */
    private void init() {
        mTxvBlank.setVisibility(View.GONE);
        ListView listView = mListView.getRefreshableView();
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemLongClickListener(this);
        listView.setMultiChoiceModeListener(mMultiChoiceListener);
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_scan_header_listview_items, mScanHeaderObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolder viewHolder = null;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(getResource(), parent, false);
                    mView.setTag(new ViewHolder(mView));
                }
                mView = handleRowView(mView, position);
                return mView;
            }

        };
        listView.setAdapter(mListViewAdapter);
//        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
//            @Override
//            public void onPullDownToRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {
//                scope.main().requestSync(InventoryMoveProvider.AUTHORITY);
//            }
//
//            @Override
//            public void onPullUpToRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {
//
//            }
//        });


        initData();

    }

    private void initData() {
        Log.d(TAG, "ScanHeaderList->initData()");
        String title = "Draft";

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (mScanHeaderLoader != null) {
                mScanHeaderLoader.cancel(true);
                mScanHeaderLoader = null;
            }
            if (bundle.containsKey("state")) {
                mCurrentState = bundle.getString("state");
                if (mCurrentState.equals("draft")) {
                    mState = MState.DRAFT;
                } else if (mCurrentState.equals("processed")) {
                    mState = MState.PROCESSED;
                    title = "Processed";
                }
            }

            scope.main().setTitle(title);
            mScanHeaderLoader = new ScanHeaderLoader(mType, mState);
            mScanHeaderLoader.execute((Void) null);
        }
    }

    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mScanHeaderObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mScanHeaderObjects.get(position);
//            String fromOrgName = row_data.getM2ORecord("from_org_id").browse().getString("name");
//            String toOrgName = row_data.getM2ORecord("to_org_id").browse().getString("name");

            Integer goodsCount = row_data.getInt("sum_goods_count");
            Integer billsCount = row_data.getInt("sum_bills_count");
            String describe = String.format("共%d票%d件", billsCount, goodsCount);
            String billDate = row_data.getString("bill_date");
//            String fromTo = String.format("%s 至 %s", fromOrgName, toOrgName);
//            holder.txvFromTo.setText(fromTo);
            holder.txvBillDate.setText(billDate);
            holder.txvDescribe.setText(describe);
        }

        return mView;
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

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_inventory_move_list_choice_delete:
                    deleteSelected();
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


    /**
     * Gets where.
     * 根据类型构造where条件
     *
     * @param type the type
     * @return the where
     */
    public HashMap<String, Object> getWhere(MType type, MState state) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String[] whereArgs = new String[3];
        String where = "op_type = ?";
        switch (type) {
            case SORTING_IN:
                where += " AND to_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.SORTING_IN;
                break;
            case LOAD_IN:
                where += " AND to_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.LOAD_IN;
                break;
            case LOAD_OUT:
                where += " AND from_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.LOAD_OUT;
                break;
            default:
                whereArgs[0] = ScanHeaderOpType.LOAD_IN;
                break;
        }

        //当前机构

        LmisUser user = LmisUser.current(getActivity());
        whereArgs[1] = user.getDefault_org_id() + "";

        switch (state) {
            case DRAFT:
                where += " AND (processed = ? or processed IS NULL)";
                whereArgs[2] = "false";
                break;
            default:
                where += "AND processed = ? ";
                whereArgs[2] = "true";
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
     * @param type  the type
     * @param state the state
     * @return the fragment
     */
    private BaseFragment getFragment(String type, String state) {
        ScanHeaderList list = new ScanHeaderList();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("state", state);
        list.setArguments(bundle);
        return list;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
        LmisUser user = LmisUser.current(getActivity());

        OrgDB db = new OrgDB(context);
        LmisDataRow currentOrg = db.select(user.getDefault_org_id());
        String groupTitle = "";
        String draftTitle = "待处理";
        String processedTitle = "已处理";

        //分拣组入库
        if (mType == MType.SORTING_IN) {
            groupTitle = "分拣组入库";

            drawerItems.add(new DrawerItem(TAG, groupTitle, true));
            drawerItems.add(new DrawerItem(TAG, draftTitle, count(MType.SORTING_IN, MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.SORTING_IN, "draft")));
            drawerItems.add(new DrawerItem(TAG, processedTitle, count(MType.SORTING_IN, MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.SORTING_IN, "processed")));
        }

        //装卸组入库
        if (mType == MType.LOAD_IN) {
            groupTitle = "装卸组入库";

            drawerItems.add(new DrawerItem(TAG, groupTitle, true));
            drawerItems.add(new DrawerItem(TAG, draftTitle, count(MType.LOAD_IN, MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.LOAD_IN, "draft")));
            drawerItems.add(new DrawerItem(TAG, processedTitle, count(MType.LOAD_IN, MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOAD_IN, "processed")));
        }
        //装卸组出库
        if (mType == MType.LOAD_IN) {
            groupTitle = "装卸组出库";

            drawerItems.add(new DrawerItem(TAG, groupTitle, true));
            drawerItems.add(new DrawerItem(TAG, draftTitle, count(MType.LOAD_OUT, MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.LOAD_OUT, "draft")));
            drawerItems.add(new DrawerItem(TAG, processedTitle, count(MType.LOAD_OUT, MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOAD_OUT, "processed")));
        }
        return drawerItems;
    }

    private int count(MType type, MState state, Context context) {
        int count = 0;
        ScanHeaderDB db = new ScanHeaderDB(context);
        String where = null;
        String whereArgs[] = null;
        HashMap<String, Object> obj = getWhere(type, state);
        where = (String) obj.get("where");
        whereArgs = (String[]) obj.get("whereArgs");
        count = db.count(where, whereArgs);
        return count;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new ScanHeaderDB(context);
    }

    /**
     * list item click event.
     *
     * @param adapterView the adapter view
     * @param view        the view
     * @param position    the position
     * @param id          the l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 0)
            return;

        position -= mListView.getRefreshableView().getHeaderViewsCount();

        mSelectedItemPosition = position;
        LmisDataRow row = (LmisDataRow) mScanHeaderObjects.get(position);
        BaseFragment detail;
        Bundle bundle = new Bundle();
        bundle.putInt("scan_header_id", row.getInt("id"));
        bundle.putInt("position", position);
        bundle.putString("type", mCurrentType);
        if (row.get("processed") != null && row.getBoolean("processed")) {
//            detail = new ScanHeaderDetail();
        } else {
            detail = new ScanHeaderNew();
        }

        detail = new ScanHeaderNew();

        detail.setArguments(bundle);

        FragmentListener listener = (FragmentListener) getActivity();
        listener.startDetailFragment(detail);
    }

    /**
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_scan_header_list, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_scan_header_list_search).getActionView();
        mSearchView.setOnQueryTextListener(getQueryListener(mListViewAdapter));
    }

    /**
     * 菜单项选择处理
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_scan_header_new):
                Log.d(TAG, "New Menu select");
                Bundle args = new Bundle();
                args.putString("type", mCurrentType);
                Fragment fragment = new ScanHeaderNew();
                fragment.setArguments(args);
                scope.main().startDetailFragment(fragment);
                return true;
            case (R.id.menu_scan_header_list_search):
                Log.d(TAG, "Search menu select");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onCurrentOrgChangeEvent(CurrentOrgChangeEvent evt) {
        if (mScanHeaderLoader != null) {
            mScanHeaderLoader.cancel(true);

        }
        mScanHeaderLoader = new ScanHeaderLoader(mType, mState);
        mScanHeaderLoader.execute((Void) null);
        scope.main().refreshDrawer(TAG);
    }

    public class ScanHeaderLoader extends AsyncTask<Void, Void, Boolean> {
        MState mState = null;
        MType mType = null;

        public ScanHeaderLoader(MType type, MState state) {
            mState = state;
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "ScanHeaderLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mType, mState);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "bill_date DESC");
            mScanHeaderObjects.clear();
            mScanHeaderObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "ScanHeaderLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mScanHeaderObjects);
            checkScanHeaderListStatus();
            mScanHeaderLoader = null;
        }

    }

    /**
     * 设置空记录指示控件是否可见.
     */
    private void checkScanHeaderListStatus() {
        mTxvBlank.setVisibility(mScanHeaderObjects.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        scope.context().registerReceiver(datasetChangeReceiver, new IntentFilter(DataSetChangeReceiver.DATA_CHANGED));
        scope.context().registerReceiver(inventoryMoveSyncFinish, new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
    }

    @Override
    public void onPause() {
        super.onPause();
        scope.context().unregisterReceiver(datasetChangeReceiver);
        scope.context().unregisterReceiver(inventoryMoveSyncFinish);
        Bundle outState = new Bundle();
        outState.putInt("mSelectedItemPosition", mSelectedItemPosition);
        onSaveInstanceState(outState);

    }

    private DataSetChangeReceiver datasetChangeReceiver = new DataSetChangeReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                Log.d(TAG, "InventoryOutList->datasetChangeReceiver@onReceive");

                String id = intent.getExtras().getString("id");
                String model = intent.getExtras().getString("model");
                if (model.equals("LoadListWithBarcode")) {
                    LmisDataRow row = db().select(Integer.parseInt(id));
                    mScanHeaderObjects.add(0, row);
                    mListViewAdapter.notifiyDataChange(mScanHeaderObjects);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };
    private SyncFinishReceiver inventoryMoveSyncFinish = new SyncFinishReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "InventoryOutList->SyncFinishReceiverReceiver@onReceive");
            scope.main().refreshDrawer(TAG);
            mListView.onRefreshComplete();
            mListViewAdapter.clear();
            mScanHeaderObjects.clear();
            mListViewAdapter.notifiyDataChange(mScanHeaderObjects);
            new ScanHeaderLoader(mType, mState).execute();
        }
    };


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    /**
     * 删除选中单据.
     *
     * @return the int
     */
    private int deleteSelected() {
        int ret = 0;
        List<Object> delObjs = new ArrayList<Object>();
        for (int position : mMultiSelectedRows.keySet()) {
            LmisDataRow scanHeader = (LmisDataRow) mScanHeaderObjects.get(position);
            delObjs.add(scanHeader);
            int id = scanHeader.getInt("id");
            db().delete(id);
            ScanLineDB lineDB = new ScanLineDB(scope.context());
            String where = "scan_header_id = ?";
            String[] whereArgs = new String[]{id + ""};
            lineDB.delete(where, whereArgs);
            ret++;
        }
        mMultiSelectedRows.clear();
        mScanHeaderObjects.removeAll(delObjs);
        mListViewAdapter.notifiyDataChange(mScanHeaderObjects);

        DrawerListener drawer = scope.main();
        drawer.refreshDrawer(TAG);

        Toast.makeText(scope.context(), "单据已删除!", Toast.LENGTH_LONG).show();
        return ret;
    }
}
