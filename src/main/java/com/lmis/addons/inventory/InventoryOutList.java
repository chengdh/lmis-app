package com.lmis.addons.inventory;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.controls.LmisTextView;
import com.lmis.util.drawer.DrawerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-6-16.
 * 出库单列表
 */
public class InventoryOutList extends BaseFragment implements AdapterView.OnItemClickListener {

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

    List<Object> mInventoryObjects = new ArrayList<Object>();

    InventoryLoader mInventoryLoader = null;

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolder {
        //发货地
        @InjectView(R.id.txvInventoryOutFromOrgName)
        TextView txvFromOrgName;
        //到货地
        @InjectView(R.id.txvInventoryOutToOrgName)
        TextView txvToOrgName;
        //描述信息
        @InjectView(R.id.txvInventoryOutDescribe)
        TextView txvDescribe;
        //创建日期
        @InjectView(R.id.txvInventoryOutBillDate)
        LmisTextView txvBillDate;

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
        init();
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
        if (mSelectedItemPosition > -1) {
            return;
        }

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
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_inventory_out_listview_items, mInventoryObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolder holder;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(getResource(), parent, false);
                    holder = new ViewHolder(mView);
                    mView.setTag(holder);
                } else {
                    holder = (ViewHolder) mView.getTag();
                }
                LmisDataRow row_data = (LmisDataRow) mInventoryObjects.get(position);
                String fromOrgName = row_data.getM2ORecord("from_org_id").browse().getString("name");
                String toOrgName = row_data.getM2ORecord("to_org_id").browse().getString("name");

                Integer goodsCount = row_data.getInt("goods_count");
                Integer billsCount = row_data.getInt("bills_count");
                String describe = String.format("共%1件,%2票", goodsCount, billsCount);
                String billDate = row_data.getString("bill_date");
                holder.txvFromOrgName.setText(fromOrgName);
                holder.txvToOrgName.setText(toOrgName);
                holder.txvBillDate.setText(billDate);
                holder.txvDescribe.setText(describe);
                return mView;
            }

        };
        mListView.setAdapter(mListViewAdapter);
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
        drawerItems.add(new DrawerItem(TAG, inventory_out_draft, count(MType.DRAFT, context),
                R.drawable.ic_action_inbox,
                getFragment("draft")));
        drawerItems.add(new DrawerItem(TAG, inventory_out_processed, 0, R.drawable.ic_action_archive, getFragment("processed")));
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

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
        //mSearchView = (SearchView) menu.findItem(R.id.menu_fragment_inventory_out_list_search).getActionView();
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
            case (R.id.menu_fragment_inventory_out_list_new):
                Log.d(TAG, "New Menu select");
                return true;
            case (R.id.menu_fragment_inventory_out_list_search):
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
            //mSearchView.setOnQueryTextListener(getQueryListener(mListViewAdapter));
            mListViewAdapter.notifiyDataChange(mInventoryObjects);
            mInventoryLoader = null;
        }
    }
}
