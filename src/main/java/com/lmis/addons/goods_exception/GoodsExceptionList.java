package com.lmis.addons.goods_exception;

import android.annotation.SuppressLint;
import android.content.Context;
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
 * Created by chengdh on 14-9-4.
 */
public class GoodsExceptionList extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final String TAG = "GoodsExceptionList";

    int mSelectedItemPosition = -1;

    String mCurrentType = "draft";

    View mView = null;

    SearchView mSearchView = null;


    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.txv_goods_exception_blank)
    TextView mTxvGoodsExceptionBlank;

    @InjectView(R.id.list_goods_exception)
    ListView mListView;

    List<Object> mGoodsExceptionObjects = new ArrayList<Object>();

    GoodsExceptionLoader mGoodsExceptionLoader = null;


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
        @InjectView(R.id.txv_bill_no)
        TextView txvBillNo;
        @InjectView(R.id.txv_org_name)
        TextView txvOrgName;
        @InjectView(R.id.txv_date)
        TextView txvBillDate;
        @InjectView(R.id.txv_note)
        TextView txvNote;

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
                case R.id.menu_goods_exception_list_choice_delete:
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
            inflater.inflate(R.menu.menu_fragment_goods_exception_list_context, menu);
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

    private int deleteSelected() {
        int ret = 0;
        List<Object> delObjs = new ArrayList<Object>();
        for (int position : mMultiSelectedRows.keySet()) {
            LmisDataRow goodsException = (LmisDataRow) mGoodsExceptionObjects.get(position);
            delObjs.add(goodsException);
            int id = goodsException.getInt("id");
            db().delete(id);
            ret++;
        }
        mMultiSelectedRows.clear();
        mGoodsExceptionObjects.removeAll(delObjs);
        mListViewAdapter.notifiyDataChange(mGoodsExceptionObjects);

        DrawerListener drawer = scope.main();
        drawer.refreshDrawer(TAG);

        Toast.makeText(scope.context(), "单据已删除!", Toast.LENGTH_LONG).show();
        return ret;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectedItemPosition = savedInstanceState.getInt("mSelectedItemPosition", -1);
        }
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_goods_exception_list, container, false);
        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_goods_exception_list, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_goods_exception_list_search).getActionView();
        mSearchView.setOnQueryTextListener(getQueryListener(mListViewAdapter));
    }

    /**
     * Init void.
     * 初始化界面及数据
     */
    private void init() {
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemLongClickListener(this);
        mListView.setMultiChoiceModeListener(mMultiChoiceListener);
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_goods_exception_list_item, mGoodsExceptionObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
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
        Log.d(TAG, "GoodsExceptionList->initData()");
        String title = "Draft";
        if (mSelectedItemPosition > -1) {
            return;
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (mGoodsExceptionLoader != null) {
                mGoodsExceptionLoader.cancel(true);
                mGoodsExceptionLoader = null;
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
            mGoodsExceptionLoader = new GoodsExceptionLoader(mType);
            mGoodsExceptionLoader.execute((Void) null);
        }
    }

    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mGoodsExceptionObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mGoodsExceptionObjects.get(position);

            String billNo = row_data.getString("bill_no");
            String goodsNo = row_data.getString("goods_no");
            String note = row_data.getString("note");
            String billDate = row_data.getString("bill_date");
            LmisDataRow org = row_data.getM2ORecord("org_id").browse();
            String orgName = org.getString("name");
            String exceptType = row_data.getString("exception_type");
            String exceptDes = GoodsExceptionDB.getExceptionTypeDes(exceptType);
            int exceptNum = row_data.getInt("except_num");
            String formatNote = String.format("%s %d件 %s", exceptDes, exceptNum, note);

            holder.txvBillNo.setText(billNo + "/" + goodsNo);
            holder.txvBillDate.setText(billDate);
            holder.txvOrgName.setText(orgName);
            holder.txvNote.setText(formatNote);
        }

        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_goods_exception_new):
                Log.d(TAG, "New Menu select");
                Fragment fragmentNew = new GoodsExceptionNew();
                scope.main().startDetailFragment(fragmentNew);
                return true;
            case (R.id.menu_goods_exception_list_search):
                Log.d(TAG, "Search menu select");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private int count(MType type, Context context) {
        int count = 0;
        GoodsExceptionDB db = (GoodsExceptionDB) databaseHelper(context);
        String where = null;
        String whereArgs[] = null;
        HashMap<String, Object> obj = getWhere(type);
        where = (String) obj.get("where");
        whereArgs = (String[]) obj.get("whereArgs");
        count = db.count(where, whereArgs);
        return count;
    }

    private BaseFragment getFragment(String value) {
        GoodsExceptionList list = new GoodsExceptionList();
        Bundle bundle = new Bundle();
        bundle.putString("type", value);
        list.setArguments(bundle);
        return list;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new GoodsExceptionDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        String title = "异常处理";

        drawerItems.add(new DrawerItem(TAG, title, true));
        drawerItems.add(new DrawerItem(TAG, "草稿", count(MType.DRAFT, context), R.drawable.ic_action_inbox, getFragment("draft")));
        drawerItems.add(new DrawerItem(TAG, "已上传", count(MType.PROCESSED, context), R.drawable.ic_action_archive, getFragment("processed")));
        return drawerItems;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSelectedItemPosition = position;
        LmisDataRow row = (LmisDataRow) mGoodsExceptionObjects.get(position);
        BaseFragment detail;
        if (row.getBoolean("processed")) {
            detail = new GoodsExceptionView();
        } else {
            detail = new GoodsExceptionNew();
        }
        Bundle bundle = new Bundle();
        bundle.putInt("goods_exception_id", row.getInt("id"));
        bundle.putInt("position", position);
        detail.setArguments(bundle);

        FragmentListener listener = (FragmentListener) getActivity();
        listener.startDetailFragment(detail);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    public class GoodsExceptionLoader extends AsyncTask<Void, Void, Boolean> {
        MType mType = null;

        public GoodsExceptionLoader(MType type) {
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "GoodsExceptionLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mType);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "bill_date DESC");
            mGoodsExceptionObjects.clear();
            mGoodsExceptionObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "MessageLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mGoodsExceptionObjects);
            mTxvGoodsExceptionBlank.setVisibility(mGoodsExceptionObjects.size() > 0 ? View.GONE : View.VISIBLE);
            mGoodsExceptionLoader = null;
        }

    }

}
