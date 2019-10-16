package com.lmis.addons.short_list;

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
 * 短驳单列表
 * Created by chengdh on 2017/7/12.
 */

public class ShortListList extends BaseFragment implements AdapterView.OnItemClickListener {
    public static final String TAG = "ShortListList";


    @Inject
    Bus mBus;

    /**
     * The enum state.
     * 状态
     * draft 草稿
     * loaded 已装车
     * shipped 已发车
     * reached 已到车
     */
    private enum MState {
        DRAFT, LOADED, SHIPPED, REACHED
    }

    MState mState = MState.DRAFT;


    /**
     * 当前选定的数据索引.
     */
    Integer mSelectedItemPosition = -1;

    String mCurrentState = "draft";

    View mView = null;

    SearchView mSearchView = null;

    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.listShortLists)
    ListView mListView;

    @InjectView(R.id.txvShortListBlank)
    TextView mTxvBlank;

    List<Object> mShortListObjects = new ArrayList<Object>();

    ShortListLoader mShortListLoader = null;


    List<String> mMultiSelectedRows = new ArrayList<>();

    Integer mSelectedCounter = 0;


    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolder {
        //发货地
        @InjectView(R.id.txvShortListToOrg)
        TextView txvFromTo;
        //描述信息
        @InjectView(R.id.txvShortListDescribe)
        TextView txvDescribe;
        //创建日期
        @InjectView(R.id.txvShortListBillDate)
        TextView txvBillDate;

        @InjectView(R.id.txvShortListVno)
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
        mView = inflater.inflate(R.layout.fragment_short_list_list, container, false);
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
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_short_list_listview_items, mShortListObjects) {
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

        initData();

    }

    private void initData() {
        Log.d(TAG, "ShortListList->initData()");
        String title = "草稿";

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (mShortListLoader != null) {
                mShortListLoader.cancel(true);
                mShortListLoader = null;
            }
            if (bundle.containsKey("state")) {
                mCurrentState = bundle.getString("state");
                if (mCurrentState.equals("draft")) {
                    mState = MState.DRAFT;
                } else if (mCurrentState.equals("loaded")) {
                    mState = MState.LOADED;
                    title = "已装车";
                } else if (mCurrentState.equals("shipped")) {
                    mState = MState.SHIPPED;
                    title = "已发车";

                } else if (mCurrentState.equals("reached")) {
                    mState = MState.REACHED;
                    title = "已到车";
                }

            }
            scope.main().setTitle(title);
            mShortListLoader = new ShortListLoader(mState);
            mShortListLoader.execute((Void) null);
        }
    }

    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mShortListObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mShortListObjects.get(position);
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
            String vNo = row_data.getString("vehicle_no");
            String fromTo = String.format("%s 至 %s", fromOrgName, toOrgName);
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
        String[] whereArgs = new String[1];
        String where = " 1=1 ";

        //当前机构

        LmisUser user = LmisUser.current(getActivity());
        switch (state) {
            case DRAFT:
                where += " AND (state = ? or processed IS NULL)";
                whereArgs[0] = "draft";
                break;
            case LOADED:
                where += " AND state= ? ";
                whereArgs[0] = "loaded";
                break;
            //TODO 以下状态未处理
            case SHIPPED:
                where += " AND state= ? ";
                whereArgs[0] = "shipped";
                break;

            case REACHED:
                where += " AND state = ? ";
                whereArgs[0] = "reached";
                break;


            default:
                where += " AND (state = ? or state IS NULL)";
                whereArgs[0] = "draft";
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
     * @param state the state
     * @return the fragment
     */
    private BaseFragment getFragment(String state) {
        ShortListList list = new ShortListList();
        Bundle bundle = new Bundle();
        bundle.putString("state", state);
        list.setArguments(bundle);
        return list;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
        LmisUser user = LmisUser.current(getActivity());

        OrgDB db = new OrgDB(context);

        String groupTitle = "短驳单";

        String draftTitle = "草稿";

        String loadedTitle = "已装车";

        String shippedTitle = "已发车";
        String reachedTitle = "已到货";


        drawerItems.add(new DrawerItem("short_lists", groupTitle, true));
        drawerItems.add(new DrawerItem("short_lists", draftTitle, count(MState.DRAFT, context), R.drawable.ic_action_inbox, getFragment("draft")));
//        drawerItems.add(new DrawerItem("shoft_lists", loadedTitle, count(MState.LOADED, context), R.drawable.ic_action_archive, getFragment("loaded")));
//        drawerItems.add(new DrawerItem("shoft_lists", shippedTitle, count(MState.SHIPPED, context), R.drawable.ic_action_archive, getFragment("shipped")));
        drawerItems.add(new DrawerItem("shoft_lists", reachedTitle, count(MState.REACHED, context), R.drawable.ic_action_archive, getFragment("reached")));
        return drawerItems;
    }

    private int count(MState state, Context context) {
        int count = 0;
        ShortListDB db = new ShortListDB(context);
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
        return new ShortListDB(context);
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
        LmisDataRow row = (LmisDataRow) mShortListObjects.get(position);
        BaseFragment detail;
        Bundle bundle = new Bundle();
        bundle.putInt("short_list_id", row.getInt("id"));
        bundle.putInt("position", position);
        if (!row.get("processed").equals("false")) {
            detail = new ShortListDetail();
        } else {
            detail = new ShortListNew();
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
        inflater.inflate(R.menu.menu_fragment_short_list_list, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_short_list_search).getActionView();
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
            case (R.id.menu_short_list_new):
                Log.d(TAG, "New Menu select");
                Bundle args = new Bundle();
                Fragment fragment = new ShortListNew();
                fragment.setArguments(args);
                scope.main().startDetailFragment(fragment);
                return true;
            case (R.id.menu_short_list_search):
                Log.d(TAG, "Search menu select");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onCurrentOrgChangeEvent(CurrentOrgChangeEvent evt) {
        if (mShortListLoader != null) {
            mShortListLoader.cancel(true);

        }
        mShortListLoader = new ShortListLoader(mState);
        mShortListLoader.execute((Void) null);
        scope.main().refreshDrawer(TAG);
    }

    public class ShortListLoader extends AsyncTask<Void, Void, Boolean> {
        MState mState = null;
        String mType = null;

        public ShortListLoader(MState state) {
            mState = state;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "ShortListLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mState);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "bill_date DESC");
            mShortListObjects.clear();
            mShortListObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "ShortListLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mShortListObjects);
            checkShortListListStatus();
            mShortListLoader = null;
        }

    }

    /**
     * 设置空记录指示控件是否可见.
     */
    private void checkShortListListStatus() {
        mTxvBlank.setVisibility(mShortListObjects.size() > 0 ? View.GONE : View.VISIBLE);
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

                Log.d(TAG, "ShortListList->datasetChangeReceiver@onReceive");

                String id = intent.getExtras().getString("id");
                String model = intent.getExtras().getString("model");
                if (model.equals("ShortList")) {
                    LmisDataRow row = db().select(Integer.parseInt(id));
                    mShortListObjects.add(0, row);
                    mListViewAdapter.notifiyDataChange(mShortListObjects);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };
    private SyncFinishReceiver inventoryMoveSyncFinish = new SyncFinishReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "ShortListList->SyncFinishReceiverReceiver@onReceive");
            scope.main().refreshDrawer(TAG);
            mListViewAdapter.clear();
            mShortListObjects.clear();
            mListViewAdapter.notifiyDataChange(mShortListObjects);
            new ShortListLoader(mState).execute();
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
            LmisDataRow scanHeader = (LmisDataRow) mShortListObjects.get(Integer.parseInt(position));
            delObjs.add(scanHeader);
            int id = scanHeader.getInt("id");
            db().delete(id);
            ShortListLineDB lineDB = new ShortListLineDB(scope.context());
            String where = "short_list_id = ?";
            String[] whereArgs = new String[]{id + ""};
            lineDB.delete(where, whereArgs);
            ret++;
        }
        mMultiSelectedRows.clear();
        mShortListObjects.removeAll(delObjs);
        mListViewAdapter.notifiyDataChange(mShortListObjects);

        DrawerListener drawer = scope.main();
        drawer.refreshDrawer("short_lists");

        Toast.makeText(scope.context(), "单据已删除!", Toast.LENGTH_LONG).show();
        return ret;
    }
}
