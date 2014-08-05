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

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.receivers.DataSetChangeReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.support.fragment.FragmentListener;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-6-16.
 * 出库单列表
 */
public class InventoryOutList extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final String TAG = "InventoryOutList";


    /**
     * The enum M type.
     * 数据分为草稿及已处理
     */
    private enum MType {
        DRAFT, PROCESSED
    }

    ;

    /**
     * 当前选定的数据索引.
     */
    Integer mSelectedItemPosition = -1;

    String mCurrentType = "draft";

    View mView = null;

    SearchView mSearchView = null;

    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.listInventoryOuts)
    ListView mListView;

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
        mView = inflater.inflate(R.layout.fragment_inventory_out_list, container, false);
        ButterKnife.inject(this, mView);
        return mView;
    }

    /**
     * Init void.
     * 初始化界面及数据
     */
    private void init() {
        handleRowView();
        initData();

    }

    private void initData() {
        Log.d(TAG, "InventoryOutList->initData()");
        String title = "Draft";
        MType type = MType.DRAFT;

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (mInventoryLoader != null) {
                mInventoryLoader.cancel(true);
                mInventoryLoader = null;
            }
            if (bundle.containsKey("type")) {
                mCurrentType = bundle.getString("type");
                if (mCurrentType.equals("draft")) {
                    type = MType.DRAFT;
                } else if (mCurrentType.equals("processed")) {
                    type = MType.PROCESSED;
                    title = "Processed";
                }
            }
            scope.main().setTitle(title);
            mInventoryLoader = new InventoryLoader(type);
            mInventoryLoader.execute((Void) null);
        }
    }


    /**
     * Sets list view.
     * 设置单条信息显示界面
     */
    private void handleRowView() {
        mListView.setOnItemClickListener(this);
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_inventory_out_listview_items, mInventoryObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolder holder;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_inventory_out_listview_items, parent, false);
                    holder = new ViewHolder(mView);
                    mView.setTag(holder);
                } else {
                    holder = (ViewHolder) mView.getTag();
                }
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
                return mView;
            }

        };
        mListView.setAdapter(mListViewAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemLongClickListener(this);
        mListView.setMultiChoiceModeListener(mMultiChoiceListener);
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
                case R.id.menu_inventory_out_list_choice_delete:
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
            inflater.inflate(R.menu.menu_fragment_inventory_out_list_context, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mSelectedCounter = 0;
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
     * @param type the type
     * @return the where
     */
    public HashMap<String, Object> getWhere(MType type) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String where = null;
        String[] whereArgs = null;
        switch (type) {
            case DRAFT:
                where = "processed = ? ";
                whereArgs = new String[]{"false"};
                break;
            default:
                where = "processed = ? ";
                whereArgs = new String[]{"true"};
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
        InventoryOutList inventory = new InventoryOutList();
        Bundle bundle = new Bundle();
        bundle.putString("type", value);
        inventory.setArguments(bundle);
        return inventory;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        String inventory_out_title = context.getResources().getString(R.string.inventory_out_group_title);
        String inventory_out_draft = context.getResources().getString(R.string.inventory_out_draw_item_draft);
        String inventory_out_processed = context.getResources().getString(R.string.inventory_out_draw_item_processed);

        drawerItems.add(new DrawerItem(TAG, inventory_out_title, true));
        drawerItems.add(new DrawerItem(TAG, inventory_out_draft, count(MType.DRAFT, context), R.drawable.ic_action_inbox, getFragment("draft")));
        drawerItems.add(new DrawerItem(TAG, inventory_out_processed, count(MType.PROCESSED, context), R.drawable.ic_action_archive, getFragment("processed")));
        return drawerItems;
    }

    private int count(MType type, Context context) {
        int count = 0;
        InventoryMoveDB db = new InventoryMoveDB(context);
        String where = null;
        String whereArgs[] = null;
        HashMap<String, Object> obj = getWhere(type);
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
        mSelectedItemPosition = position;
        LmisDataRow row = (LmisDataRow) mInventoryObjects.get(position);
        BaseFragment detail;
        if (row.getBoolean("processed")) {
            detail = new InventoryOutReadonly();
        } else {
            detail = new InventoryOut();
        }
        Bundle bundle = new Bundle();
        bundle.putInt("inventory_out_id", row.getInt("id"));
        bundle.putInt("position", position);
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
        inflater.inflate(R.menu.menu_fragment_inventory_out_list, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_inventory_out_list_search).getActionView();
        init();
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
                Fragment inventoryOut = new InventoryOut();
                scope.main().startDetailFragment(inventoryOut);
                return true;
            case (R.id.menu_inventory_out_list_search):
                Log.d(TAG, "Search menu select");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class InventoryLoader extends AsyncTask<Void, Void, Boolean> {
        MType mType = null;

        public InventoryLoader(MType type) {
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            HashMap<String, Object> map = getWhere(mType);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "bill_date DESC");
            mInventoryObjects.clear();
            if (result.size() > 0) {
                for (LmisDataRow row : result) {
                    mInventoryObjects.add(row);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mListViewAdapter.notifiyDataChange(mInventoryObjects);
            mSearchView.setOnQueryTextListener(getQueryListener(mListViewAdapter));
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
    }

    @Override
    public void onPause() {
        super.onPause();
        scope.context().unregisterReceiver(datasetChangeReceiver);
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
            }

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
            LmisDataRow inventoryOut = (LmisDataRow)mInventoryObjects.get(position);
            delObjs.add(inventoryOut);
            int id = inventoryOut.getInt("id");
            db().delete(id);
            InventoryLineDB ldb = new InventoryLineDB(scope.context());
            String where = "load_list_with_barcode_id = ?";
            String[] whereArgs = new String[] {id + ""};
            ldb.delete(where, whereArgs);
            ret++;
        }
        mInventoryObjects.removeAll(delObjs);
        mListViewAdapter.notifiyDataChange(mInventoryObjects);

        DrawerListener drawer = scope.main();
        drawer.refreshDrawer(TAG);

        Toast.makeText(scope.context(), "单据已删除!", Toast.LENGTH_LONG).show();
        return ret;
    }
}
