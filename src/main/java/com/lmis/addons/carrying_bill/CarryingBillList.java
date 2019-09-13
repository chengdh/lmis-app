package com.lmis.addons.carrying_bill;

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
import com.lmis.addons.search_bill.SearchBill;
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
 * Created by chengdh on 14-8-9.
 */
public class CarryingBillList extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static final String TAG = "CarryingBillList";

    int mSelectedItemPosition = -1;

    String mCurrentType = "draft";


    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.listCarryingBills)
    ListView mListView;

    @InjectView(R.id.txvCarryingBillBlank)
    TextView mTxvBlank;

    SearchView mSearchView;
    View mView = null;

    List<Object> mCarryingBillObjects = new ArrayList<Object>();

    BillLoader mCarryingBillLoader = null;


    HashMap<Integer, Boolean> mMultiSelectedRows = new HashMap<Integer, Boolean>();

    Integer mSelectedCounter = 0;

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
                case R.id.menu_carrying_bill_choice_delete:
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
            inflater.inflate(R.menu.menu_fragment_carrying_bill_list_context, menu);
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

    static class ViewHolder {

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        @InjectView(R.id.txv_bill_no)
        TextView txvBillNo;

        @InjectView(R.id.txv_bill_date)
        TextView txvBillDate;

        @InjectView(R.id.txv_carrying_fee)
        TextView txvCarryingFee;

        @InjectView(R.id.txv_goods_fee)
        TextView txvGoodsFee;

        @InjectView(R.id.txv_goods_num)
        TextView txvGoodsNum;

        @InjectView(R.id.txv_pay_type)
        TextView txvPayType;

    }

    /**
     * The enum M type.
     * 数据分为草稿及已处理
     */
    private enum MType {
        DRAFT, PROCESSED
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectedItemPosition = savedInstanceState.getInt("mSelectedItemPosition", -1);
        }
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_bill_list, container, false);
        ButterKnife.inject(this, mView);
        init();
        return mView;
    }


    private void init() {
        mTxvBlank.setVisibility(View.GONE);
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemLongClickListener(this);
        mListView.setMultiChoiceModeListener(mMultiChoiceListener);
        mListViewAdapter = new LmisListAdapter(getActivity(), R.layout.fragment_carrying_bill_listview_item, mCarryingBillObjects) {
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
        Log.d(TAG, "CarryingBillList->initData()");
        String title = "Draft";
        MType type = MType.DRAFT;
//        if(mSelectedItemPosition > -1) {
//            return;
//        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (mCarryingBillLoader != null) {
                mCarryingBillLoader.cancel(true);
                mCarryingBillLoader = null;
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
            mCarryingBillLoader = new BillLoader(type);
            mCarryingBillLoader.execute((Void) null);
        }
    }


    private View handleRowView(View mView, final int position) {
        ViewHolder holder = (ViewHolder) mView.getTag();
        if (mCarryingBillObjects.size() > 0) {
            LmisDataRow row_data = (LmisDataRow) mCarryingBillObjects.get(position);
            String billNo = row_data.getString("bill_no");
            String goodsNo = row_data.getString("goods_no");
            String billDate = row_data.getString("bill_date");

            int carryingFee = row_data.getInt("carrying_fee");
            int goodsFee = row_data.getInt("goods_fee");
            int goodsNum = row_data.getInt("goods_num");
            holder.txvBillNo.setText(billNo + String.format("[%s]",goodsNo));
            holder.txvBillDate.setText(billDate);
            holder.txvCarryingFee.setText(carryingFee + "");
            holder.txvGoodsNum.setText(goodsNum + "");
            holder.txvGoodsFee.setText(goodsFee + "");
        }

        return mView;
    }


    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        drawerItems.add(new DrawerItem(TAG, "运单", true));
        drawerItems.add(new DrawerItem(TAG, "运单录入", count(MType.PROCESSED, context), R.drawable.ic_menu_carrying_bill, getFragment("processed")));
        Bundle args = new Bundle();
        args.putInt("no_use", 1);
        SearchBill fragment = new SearchBill();
        fragment.setArguments(args);
        drawerItems.add(new DrawerItem(TAG, "运单查询", 0, R.drawable.ic_menu_carrying_bill_search, fragment));
        return drawerItems;
    }

    private int count(MType type, Context context) {
        int count = 0;
        CarryingBillDB db = (CarryingBillDB) databaseHelper(context);
        String where = null;
        String whereArgs[] = null;
        HashMap<String, Object> obj = getWhere(type);
        where = (String) obj.get("where");
        whereArgs = (String[]) obj.get("whereArgs");
        count = db.count(where, whereArgs);
        return count;
    }

    public HashMap<String, Object> getWhere(MType type) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String where = null;
        String[] whereArgs = null;
        switch (type) {
            case DRAFT:
                where = "processed is null or processed = ? ";
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

    private BaseFragment getFragment(String value) {
        CarryingBillList list = new CarryingBillList();
        Bundle bundle = new Bundle();
        bundle.putString("type", value);
        list.setArguments(bundle);
        return list;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_carrying_bill_list, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_carrying_bill_list_search).getActionView();
        mSearchView.setOnQueryTextListener(getQueryListener(mListViewAdapter));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_carrying_bill_new):
                Log.d(TAG, "New Menu select");
                Fragment fragmentNew = new CarryingBillNew();
                scope.main().startDetailFragment(fragmentNew);
                return true;
            case (R.id.menu_carrying_bill_list_search):
                Log.d(TAG, "Search menu select");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 设置空记录指示控件是否可见.
     */
    private void checkListStatus() {
        mTxvBlank.setVisibility(mCarryingBillObjects.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public class BillLoader extends AsyncTask<Void, Void, Boolean> {
        MType mType = null;

        public BillLoader(MType type) {
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "BillLoader#doInBackground");
            HashMap<String, Object> map = getWhere(mType);
            String where = (String) map.get("where");
            String whereArgs[] = (String[]) map.get("whereArgs");
            List<LmisDataRow> result = db().select(where, whereArgs, null, null, "bill_date DESC");
            mCarryingBillObjects.clear();
            mCarryingBillObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "BillLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mCarryingBillObjects);
            checkListStatus();
            mCarryingBillLoader = null;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSelectedItemPosition = position;
        LmisDataRow row = (LmisDataRow) mCarryingBillObjects.get(position);
        BaseFragment detail;
        detail = new CarryingBillView();
        Bundle bundle = new Bundle();
        bundle.putInt("carrying_bill_id", row.getInt("id"));
        bundle.putInt("position", position);
        detail.setArguments(bundle);

        FragmentListener listener = (FragmentListener) getActivity();
        listener.startDetailFragment(detail);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    /**
     * 删除选定的运单信息.
     *
     * @return the int
     * FIXME 已删除时界面未更新
     */
    private int deleteSelected() {
        int ret = 0;
        List<Object> delObjs = new ArrayList<Object>();
        for (int position : mMultiSelectedRows.keySet()) {
            LmisDataRow bill = (LmisDataRow) mCarryingBillObjects.get(position);
            delObjs.add(mCarryingBillObjects);
            int id = bill.getInt("id");
            db().delete(id);
        }

        mMultiSelectedRows.clear();
        mCarryingBillObjects.removeAll(delObjs);
        mListViewAdapter.notifiyDataChange(mCarryingBillObjects);


        DrawerListener drawer = scope.main();
        drawer.refreshDrawer(TAG);

        Toast.makeText(scope.context(), "单据已删除!", Toast.LENGTH_LONG).show();
        return ret;
    }
}
