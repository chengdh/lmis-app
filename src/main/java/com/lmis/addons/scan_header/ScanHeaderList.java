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
import com.lmis.orm.LmisM2ORecord;
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

public class ScanHeaderList extends BaseFragment implements AdapterView.OnItemClickListener {
    public static final String TAG = "ScanHeaderList";


    @Inject
    Bus mBus;

    /**
     * The enum state.
     * 数据分为草稿  装卸入库  已装车  已发货
     */
    private enum MState {
        DRAFT, PROCESSED, SHIPPED
    }

    MState mState = MState.DRAFT;


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
    ListView mListView;

    @InjectView(R.id.txvScanHeaderBlank)
    TextView mTxvBlank;

    List<Object> mScanHeaderObjects = new ArrayList<Object>();

    ScanHeaderLoader mScanHeaderLoader = null;


    List<String> mMultiSelectedRows = new ArrayList<>();

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

        @InjectView(R.id.txvScanHeaderVNo)
        TextView txvVno;


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
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(mMultiChoiceListener);
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
        mListView.setAdapter(mListViewAdapter);
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
        String title = "草稿";

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
                    title = "已上传";
                } else if (mCurrentState.equals("shipped")) {
                    mState = MState.SHIPPED;
                    title = "已发车";
                }

            }
            if (bundle.containsKey("type")) {
                mCurrentType = bundle.getString("type");
            }

            scope.main().setTitle(title);
            mScanHeaderLoader = new ScanHeaderLoader(mCurrentType, mState);
            mScanHeaderLoader.execute((Void) null);
        }
    }

    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mScanHeaderObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mScanHeaderObjects.get(position);
            String fromOrgName = "";
            String toOrgName = "";
            LmisDataRow fromOrg = row_data.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = row_data.getM2ORecord("to_org_id").browse();
            if (fromOrg != null) {
                fromOrgName = fromOrg.getString("name");
            }

            if (toOrg != null) {
                toOrgName = toOrg.getString("name");
            }

            Integer goodsCount = row_data.getInt("sum_goods_count");
            Integer billsCount = row_data.getInt("sum_bills_count");
            String describe = String.format("共%d票%d件", billsCount, goodsCount);
            String billDate = row_data.getString("bill_date");
            String vNo = row_data.getString("v_no");
            String fromTo = String.format("%s  %s", fromOrgName, toOrgName);
            holder.txvFromTo.setText(fromTo);
            holder.txvBillDate.setText(billDate);
            holder.txvDescribe.setText(describe);
            holder.txvVno.setText(vNo);
        }

        return mView;
    }

    AbsListView.MultiChoiceModeListener mMultiChoiceListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            if (checked) {
                mMultiSelectedRows.add(Integer.toString(position));

                mSelectedCounter++;
            } else {
                mMultiSelectedRows.remove(Integer.toString(position));
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
            mMultiSelectedRows.clear();
            mListView.clearChoices();
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
     * @return the where
     */
    public HashMap<String, Object> getWhere(MState state) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String[] whereArgs = new String[3];
        String where = "op_type = ?";
        switch (mCurrentType) {
            case ScanHeaderOpType.SUB_BRANCH:
                where += " AND from_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.SUB_BRANCH;
                break;

            case ScanHeaderOpType.SORTING_IN:
                where += " AND to_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.SORTING_IN;
                break;
            case ScanHeaderOpType.LOAD_IN:
                where += " AND to_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.LOAD_IN;
                break;
            case ScanHeaderOpType.LOAD_OUT:
                where += " AND from_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.LOAD_OUT;
                break;
            case ScanHeaderOpType.INNER_TRANSIT_LOAD_IN:
                where += " AND to_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.INNER_TRANSIT_LOAD_IN;
                break;
            case ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT:
                where += " AND from_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT;
                break;

            case ScanHeaderOpType.LOCAL_TOWN_LOAD_IN:
                where += " AND to_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.LOCAL_TOWN_LOAD_IN;
                break;
            case ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT:
                where += " AND from_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT;
                break;
            case ScanHeaderOpType.LOAD_IN_TEAM:
                where += " AND to_org_id = ?";
                whereArgs[0] = ScanHeaderOpType.LOAD_IN_TEAM;
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
            case PROCESSED:
                where += "AND processed = ? ";
                whereArgs[2] = "true";
                break;
            case SHIPPED:
                where += "AND processed = ? ";
                whereArgs[2] = "shipped";
                break;

            default:
                where += " AND (processed = ? or processed IS NULL)";
                whereArgs[2] = "false";
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
        switch (mCurrentType) {
            case (ScanHeaderOpType.SUB_BRANCH):

                groupTitle = "分理处装车";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, draftTitle, count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.SUB_BRANCH, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, processedTitle, count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.SUB_BRANCH, "processed")));
//                drawerItems.add(new DrawerItem(mCurrentType, "已发车", count(MState.SHIPPED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.SUB_BRANCH, "shipped")));
                break;

            case (ScanHeaderOpType.SORTING_IN):

                groupTitle = "分拣组入库";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, draftTitle, count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.SORTING_IN, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, processedTitle, count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.SORTING_IN, "processed")));
                break;

            //装卸组入库
            case (ScanHeaderOpType.LOAD_IN):
                groupTitle = "装卸组入库";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, draftTitle, count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.LOAD_IN, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, processedTitle, count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOAD_IN, "processed")));
                break;

            //装卸组出库
            case (ScanHeaderOpType.LOAD_OUT):
                groupTitle = "装卸组出库";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, "草稿", count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.LOAD_OUT, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, "已上传", count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOAD_OUT, "processed")));
                drawerItems.add(new DrawerItem(mCurrentType, "已发车", count(MState.SHIPPED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOAD_OUT, "shipped")));

                break;

            //内部中转-装卸组入库
            case (ScanHeaderOpType.INNER_TRANSIT_LOAD_IN):
                groupTitle = "内部中转-装卸组入库";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, draftTitle, count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.INNER_TRANSIT_LOAD_IN, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, processedTitle, count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.INNER_TRANSIT_LOAD_IN, "processed")));
                break;

            //内部中转-装卸组出库
            case (ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT):
                groupTitle = "内部中转-装卸组出库";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, "草稿", count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, "已上传", count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT, "processed")));
                drawerItems.add(new DrawerItem(mCurrentType, "已发车", count(MState.SHIPPED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT, "shipped")));
                break;

            //同城快运-装卸组入库
            case (ScanHeaderOpType.LOCAL_TOWN_LOAD_IN):
                groupTitle = "同城快运-装卸组入库";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, draftTitle, count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.LOCAL_TOWN_LOAD_IN, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, processedTitle, count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOCAL_TOWN_LOAD_IN, "processed")));
                break;

            //同城快运-装卸组出库
            case (ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT):
                groupTitle = "同城快运-装卸组出库";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, "草稿", count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, "已上传", count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOCAL_TOWN_LOAD_OUT, "processed")));
                drawerItems.add(new DrawerItem(mCurrentType, "已发车", count(MState.SHIPPED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.INNER_TRANSIT_LOAD_OUT, "shipped")));
                break;


            //装卸组工作量统计
            case (ScanHeaderOpType.LOAD_IN_TEAM):
                groupTitle = "卸货操作";

                drawerItems.add(new DrawerItem(mCurrentType, groupTitle, true));
                drawerItems.add(new DrawerItem(mCurrentType, "草稿", count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(ScanHeaderOpType.LOAD_IN_TEAM, "draft")));
                drawerItems.add(new DrawerItem(mCurrentType, "已上传", count(MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(ScanHeaderOpType.LOAD_IN_TEAM, "processed")));

                break;
            default:
                break;
        }
        return drawerItems;
    }

    private int count(MState state, Context context) {
        int count = 0;
        ScanHeaderDB db = new ScanHeaderDB(context);
        String where = null;
        String whereArgs[] = null;
        HashMap<String, Object> obj = getWhere(state);
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

        mSelectedItemPosition = position;
        LmisDataRow row = (LmisDataRow) mScanHeaderObjects.get(position);
        BaseFragment detail;
        Bundle bundle = new Bundle();
        bundle.putInt("scan_header_id", row.getInt("id"));
        bundle.putInt("position", position);
        bundle.putString("type", mCurrentType);
        if (!row.get("processed").equals("false")) {
            detail = new ScanHeaderDetail();
        } else {
            detail = new ScanHeaderNew();
        }

        detail.setArguments(bundle);

        FragmentListener listener = (FragmentListener) getActivity();
        listener.startDetailFragment(detail);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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
        mScanHeaderLoader = new ScanHeaderLoader(mCurrentType, mState);
        mScanHeaderLoader.execute((Void) null);
        scope.main().refreshDrawer(TAG);
    }

    public class ScanHeaderLoader extends AsyncTask<Void, Void, Boolean> {
        MState mState = null;
        String mType = null;

        public ScanHeaderLoader(String type, MState state) {
            mState = state;
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "ScanHeaderLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mState);
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

                Log.d(TAG, "ScanHeaderList->datasetChangeReceiver@onReceive");

                String id = intent.getExtras().getString("id");
                String model = intent.getExtras().getString("model");
                if (model.equals("ScanHeader")) {
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

            Log.d(TAG, "ScanHeaderList->SyncFinishReceiverReceiver@onReceive");
            scope.main().refreshDrawer(TAG);
            mListViewAdapter.clear();
            mScanHeaderObjects.clear();
            mListViewAdapter.notifiyDataChange(mScanHeaderObjects);
            new ScanHeaderLoader(mCurrentType, mState).execute();
        }
    };


    /**
     * 删除选中单据.
     *
     * @return the int
     */
    private int deleteSelected() {
        int ret = 0;
        List<Object> delObjs = new ArrayList<Object>();
        for (String position : mMultiSelectedRows) {
            LmisDataRow scanHeader = (LmisDataRow) mScanHeaderObjects.get(Integer.parseInt(position));
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
        drawer.refreshDrawer(mCurrentType);

        Toast.makeText(scope.context(), "单据已删除!", Toast.LENGTH_LONG).show();
        return ret;
    }

    public String getmCurrentType() {
        return mCurrentType;
    }

    public void setmCurrentType(String mCurrentType) {
        this.mCurrentType = mCurrentType;
    }
}
