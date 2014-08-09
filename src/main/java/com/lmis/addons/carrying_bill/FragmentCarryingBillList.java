package com.lmis.addons.carrying_bill;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.drawer.DrawerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-9.
 */
public class FragmentCarryingBillList extends BaseFragment {
    public static final String TAG = "FragmentCarryingBillList";

    int mSelectedItemPosition = -1;

    String mCurrentType = "draft";


    LmisListAdapter mListViewAdapter = null;

    @InjectView(R.id.listCarryingBills)
    ListView mListView;

    @InjectView(R.id.txvCarryingBillBlank)
    TextView mTxvBlank;

    SearchView mSearchView;
    View mView = null;

    List<Object> mInventoryObjects = new ArrayList<Object>();

    BillLoader mInventoryLoader = null;


    HashMap<Integer, Boolean> mMultiSelectedRows = new HashMap<Integer, Boolean>();

    Integer mSelectedCounter = 0;


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
        return mView;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new CarryingBillDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        drawerItems.add(new DrawerItem(TAG, "运单管理", true));
        drawerItems.add(new DrawerItem(TAG, "待处理", count(MType.DRAFT, context), R.drawable.ic_action_inbox, getFragment("draft")));
        drawerItems.add(new DrawerItem(TAG, "已处理", count(MType.PROCESSED, context), R.drawable.ic_action_archive, getFragment("processed")));
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

    private BaseFragment getFragment(String value) {
        FragmentCarryingBillList list = new FragmentCarryingBillList();
        Bundle bundle = new Bundle();
        bundle.putString("type", value);
        list.setArguments(bundle);
        return list;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_carrying_bill_list, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_carrying_bill_list_search).getActionView();
        //mSearchView.setOnQueryTextListener(getQueryListener(mListViewAdapter));
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
            mInventoryObjects.clear();
            mInventoryObjects.addAll(result);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "BillLoader#onPostExecute");
            mListViewAdapter.notifiyDataChange(mInventoryObjects);
            mInventoryLoader = null;
        }

    }

}
