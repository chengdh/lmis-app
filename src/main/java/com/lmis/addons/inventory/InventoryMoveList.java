package com.lmis.addons.inventory;

import android.annotation.SuppressLint;
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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lmis.R;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.providers.inventory_move.InventoryMoveProvider;
import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.receivers.SyncFinishReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisUser;
import com.lmis.support.fragment.FragmentListener;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode.InventoryMoveOpType;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;
import com.lmis.CurrentOrgChangeEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-6-16.
 * 出库单列表
 */
public class InventoryMoveList extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final String TAG = "InventoryOutList";


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
     * 分理处盘货单/货场确认单/货场发货单/分公司确认单
     */
    private enum MType {
        BRANCH_OUT, YARD_CONFIRM, YARD_OUT, BRANCH_CONFIRM
    }

    MType mType = MType.BRANCH_OUT;

    /**
     * 当前选定的数据索引.
     */
    Integer mSelectedItemPosition = -1;

    String mCurrentState = "draft";

    String mCurrentType = InventoryMoveOpType.BRANCH_OUT;

    View mView = null;

    SearchView mSearchView = null;

    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.listInventoryOuts)
    PullToRefreshListView mListView;

    @InjectView(R.id.txvInventoryOutBlank)
    TextView mTxvBlank;

    List<Object> mInventoryObjects = new ArrayList<Object>();

    InventoryLoader mInventoryLoader = null;


    HashMap<Integer, Boolean> mMultiSelectedRows = new HashMap<Integer, Boolean>();

    Integer mSelectedCounter = 0;


    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolder {
        //发货地
        @InjectView(R.id.txvInventoryOutFromTo)
        TextView txvFromTo;
        //描述信息
        @InjectView(R.id.txvInventoryOutDescribe)
        TextView txvDescribe;
        //创建日期
        @InjectView(R.id.txvInventoryOutBillDate)
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
        mView = inflater.inflate(R.layout.fragment_inventory_move_list, container, false);
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
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_inventory_move_listview_items, mInventoryObjects) {
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
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {
                scope.main().requestSync(InventoryMoveProvider.AUTHORITY);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {

            }
        });


        initData();

    }

    private void initData() {
        Log.d(TAG, "InventoryOutList->initData()");
        String title = "Draft";
//        if(mSelectedItemPosition > -1) {
//            return;
//        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (mInventoryLoader != null) {
                mInventoryLoader.cancel(true);
                mInventoryLoader = null;
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
            if (bundle.containsKey("type")) {
                mCurrentType = bundle.getString("type");
                if (mCurrentType.equals(InventoryMoveOpType.BRANCH_OUT)) {
                    mType = MType.BRANCH_OUT;
                } else if (mCurrentType.equals(InventoryMoveOpType.YARD_CONFIRM)) {
                    mType = MType.YARD_CONFIRM;
                } else if (mCurrentType.equals(InventoryMoveOpType.YARD_OUT)) {
                    mType = MType.YARD_OUT;
                } else if (mCurrentType.equals(InventoryMoveOpType.BRANCH_CONFIRM)) {
                    mType = MType.BRANCH_CONFIRM;
                }

            }
            scope.main().setTitle(title);
            mInventoryLoader = new InventoryLoader(mType, mState);
            mInventoryLoader.execute((Void) null);
        }
    }

    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mInventoryObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mInventoryObjects.get(position);
            String fromOrgName = row_data.getM2ORecord("from_org_id").browse().getString("name");
            String toOrgName = row_data.getM2ORecord("to_org_id").browse().getString("name");

            Integer goodsCount = row_data.getInt("sum_goods_count");
            Integer billsCount = row_data.getInt("sum_bills_count");
            String describe = String.format("共%d票%d件", billsCount, goodsCount);
            String billDate = row_data.getString("bill_date");
            String fromTo = String.format("%s 至 %s", fromOrgName, toOrgName);
            holder.txvFromTo.setText(fromTo);
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

        @SuppressLint("UseSparseArrays")
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
            case BRANCH_OUT:
                where += " AND from_org_id = ?";
                whereArgs[0] = InventoryMoveOpType.BRANCH_OUT;
                break;
            case YARD_CONFIRM:
                where += " AND to_org_id = ?";
                whereArgs[0] = InventoryMoveOpType.YARD_CONFIRM;
                break;
            case YARD_OUT:
                where += " AND from_org_id = ?";
                whereArgs[0] = InventoryMoveOpType.YARD_OUT;
                break;
            case BRANCH_CONFIRM:
                where += " AND to_org_id = ?";
                whereArgs[0] = InventoryMoveOpType.BRANCH_CONFIRM;
                break;
            default:
                whereArgs[0] = InventoryMoveOpType.BRANCH_OUT;
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
        InventoryMoveList inventory = new InventoryMoveList();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("state", state);
        inventory.setArguments(bundle);
        return inventory;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
        LmisUser user = LmisUser.current(getActivity());

        OrgDB db = new OrgDB(context);
        LmisDataRow currentOrg = db.select(user.getDefault_org_id());

        //分理处出库
        String branchOutTitle = "出库扫码";
        String branchOutDraft = "草稿";
        String branchOutProcessed = "已上传";

        drawerItems.add(new DrawerItem(TAG, branchOutTitle, true));
        drawerItems.add(new DrawerItem(TAG, branchOutDraft, count(MType.BRANCH_OUT, MState.DRAFT, context), R.drawable.ic_menu_barcode, getFragment(InventoryMoveOpType.BRANCH_OUT, "draft")));
        drawerItems.add(new DrawerItem(TAG, branchOutProcessed, count(MType.BRANCH_OUT, MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(InventoryMoveOpType.BRANCH_OUT, "processed")));

        if (currentOrg.getBoolean("is_yard")) {

            //货场收货
            String yardConfirmTitle = "货场入库";
            String yardConfirmDraft = "待处理";
            String yardConfirmProcessed = "已处理";

            drawerItems.add(new DrawerItem(TAG, yardConfirmTitle, true));
            drawerItems.add(new DrawerItem(TAG, yardConfirmDraft, count(MType.YARD_CONFIRM, MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(InventoryMoveOpType.YARD_CONFIRM, "draft")));
            drawerItems.add(new DrawerItem(TAG, yardConfirmProcessed, count(MType.YARD_CONFIRM, MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(InventoryMoveOpType.YARD_CONFIRM, "processed")));

            //货场出库
            String yardOutTitle = "货场出库";
            String yardOutDraft = "待处理";
            String yardOutProcessed = "已处理";

            drawerItems.add(new DrawerItem(TAG, yardOutTitle, true));
            drawerItems.add(new DrawerItem(TAG, yardOutDraft, count(MType.YARD_OUT, MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(InventoryMoveOpType.YARD_OUT, "draft")));
            drawerItems.add(new DrawerItem(TAG, yardOutProcessed, count(MType.YARD_OUT, MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(InventoryMoveOpType.YARD_OUT, "processed")));
        }

        //分理处/分公司入库
        String branchConfirmTitle = "入库扫码";
        String branchConfirmDraft = "待处理";
        String branchConfirmProcessed = "已处理";

        drawerItems.add(new DrawerItem(TAG, branchConfirmTitle, true));
        drawerItems.add(new DrawerItem(TAG, branchConfirmDraft, count(MType.BRANCH_CONFIRM, MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment(InventoryMoveOpType.BRANCH_CONFIRM, "draft")));
        drawerItems.add(new DrawerItem(TAG, branchConfirmProcessed, count(MType.BRANCH_CONFIRM, MState.PROCESSED, context), R.drawable.ic_action_archive, getFragment(InventoryMoveOpType.BRANCH_CONFIRM, "processed")));

        return drawerItems;
    }

    private int count(MType type, MState state, Context context) {
        int count = 0;
        InventoryMoveDB db = new InventoryMoveDB(context);
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
        return new InventoryMoveDB(context);
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
        LmisDataRow row = (LmisDataRow) mInventoryObjects.get(position);
        BaseFragment detail;
        Bundle bundle = new Bundle();
        bundle.putInt("inventory_move_id", row.getInt("id"));
        bundle.putInt("position", position);
        bundle.putString("type", mCurrentType);
        if (row.get("processed") != null && row.getBoolean("processed")) {
            detail = new InventoryMoveReadonly();
        } else {
            detail = new InventoryMove();
        }

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
        inflater.inflate(R.menu.menu_fragment_inventory_move_list, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_inventory_out_list_search).getActionView();
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
            case (R.id.menu_inventory_out_new):
                Log.d(TAG, "New Menu select");
                Bundle args = new Bundle();
                args.putString("type", mCurrentType);
                Fragment inventoryOut = new InventoryMove();
                inventoryOut.setArguments(args);
                scope.main().startDetailFragment(inventoryOut);
                return true;
            case (R.id.menu_inventory_out_list_search):
                Log.d(TAG, "Search menu select");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onCurrentOrgChangeEvent(CurrentOrgChangeEvent evt) {
        if (mInventoryLoader != null) {
            mInventoryLoader.cancel(true);

        }
        mInventoryLoader = new InventoryLoader(mType, mState);
        mInventoryLoader.execute((Void) null);
        scope.main().refreshDrawer(TAG);
    }

    public class InventoryLoader extends AsyncTask<Void, Void, Boolean> {
        MState mState = null;
        MType mType = null;

        public InventoryLoader(MType type, MState state) {
            mState = state;
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "InventoryLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mType, mState);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "bill_date DESC");
            mInventoryObjects.clear();
            mInventoryObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "InventoryLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mInventoryObjects);
            checkInventoryListStatus();
            mInventoryLoader = null;
        }

    }

    /**
     * 设置空记录指示控件是否可见.
     */
    private void checkInventoryListStatus() {
        mTxvBlank.setVisibility(mInventoryObjects.size() > 0 ? View.GONE : View.VISIBLE);
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
                    mInventoryObjects.add(0, row);
                    mListViewAdapter.notifiyDataChange(mInventoryObjects);
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
            mInventoryObjects.clear();
            mListViewAdapter.notifiyDataChange(mInventoryObjects);
            new InventoryLoader(mType, mState).execute();
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
            LmisDataRow inventoryOut = (LmisDataRow) mInventoryObjects.get(position);
            delObjs.add(inventoryOut);
            int id = inventoryOut.getInt("id");
            db().delete(id);
            InventoryLineDB ldb = new InventoryLineDB(scope.context());
            String where = "load_list_with_barcode_id = ?";
            String[] whereArgs = new String[]{id + ""};
            ldb.delete(where, whereArgs);
            ret++;
        }
        mMultiSelectedRows.clear();
        mInventoryObjects.removeAll(delObjs);
        mListViewAdapter.notifiyDataChange(mInventoryObjects);

        DrawerListener drawer = scope.main();
        drawer.refreshDrawer(TAG);

        Toast.makeText(scope.context(), "单据已删除!", Toast.LENGTH_LONG).show();
        return ret;
    }
}
