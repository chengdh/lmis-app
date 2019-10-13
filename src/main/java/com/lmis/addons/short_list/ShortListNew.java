package com.lmis.addons.short_list;

import android.app.ActionBar;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisValues;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONObject;

import static android.app.ActionBar.NAVIGATION_MODE_TABS;

/**
 * Created by chengdh on 2017/7/12.
 */

public class ShortListNew extends BaseFragment {
    public static final String TAG = "ShortListNew";

    @InjectView(R.id.pager)
    ViewPager mPager;


    ShortListPagerAdapter mPageAdapter = null;

    View mView = null;
    Integer mShortListId = -1;
    LmisDataRow mShortList = null;

    Uploader mUploadAsync = null;


    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_short_list_new, container, false);

        ButterKnife.inject(this, mView);
        initData();
        initControls();
        return mView;
    }

    private void initData() {
        Log.d(TAG, "ShortListNew#initData");

        LmisUser currentUser = scope.currentUser();
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("short_list_id")) {
                mShortListId = bundle.getInt("short_list_id");
            }
        }

        if (mShortListId > 0) {
            mShortList = new ShortListDB(scope.context()).select(mShortListId);
        } else {
            mShortList = new LmisDataRow();
            mShortList.put("from_org_id", currentUser.getDefault_org_id());
            mShortList.put("user_id", currentUser.getUser_id());
        }

    }

    /**
     * 初始化控件.
     */
    private void initControls() {
        initTabs();
        initPager();
    }

    private void initPager() {
        mPageAdapter = new ShortListPagerAdapter(getFragmentManager(), mShortList);
        mPager.setAdapter(mPageAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                final ActionBar actionBar = getActivity().getActionBar();
                actionBar.setSelectedNavigationItem(position);
                scope.main().supportInvalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    /**
     * 初始化tabs.
     */
    private void initTabs() {
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

        };

        final ActionBar actionBar = getActivity().getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        actionBar.addTab(actionBar.newTab().setText("车辆信息").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("选择票据").setTabListener(tabListener));
    }


    @Override
    public Object databaseHelper(Context context) {
        return new ShortListDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_short_list_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_short_list_upload:
                if (validateBeforeUpload()) {
                    if (save2DB("shipped")) {
                        mUploadAsync = new Uploader();
                        mUploadAsync.execute((Void) null);
                        return true;
                    }
                } else {
                    Toast.makeText(scope.context(), "保存数据失败!", Toast.LENGTH_SHORT);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean validateBeforeUpload() {

        boolean success = true;
        mPager.setCurrentItem(0);
        FragmentVehicleForm page = (FragmentVehicleForm) mPageAdapter.getRegisteredFragment(0);
        success = page.validateBeforeUpload();
        if (!success) {
            Toast.makeText(scope.context(), "请输入装车信息!", Toast.LENGTH_SHORT).show();
        }

        return success;
    }

    //获取货物数量合计
    private int sumGoodsCount() {
        List bills = (List) mShortList.get("bills");
        int goodsCount = 0;
        try {

            for (Object o : bills) {
                int goodsNum = ((JSONObject) o).getInt("goods_num");
                goodsCount += goodsNum;

            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return goodsCount;
    }

    //获取票数合计
    private int sumBillsCount() {
        List bills = (List) mShortList.get("bills");
        return bills.size();
    }


    //保存至数据库
    protected boolean save2DB(String state) {
        boolean ret = true;
        //需要新建数据库
        LmisValues row = new LmisValues();
        row.put("sum_goods_count", sumGoodsCount());
        row.put("sum_bills_count", sumBillsCount());
        row.put("state", state);
        if (mShortListId == -1) {
            row.put("from_org_id", mShortList.get("from_org_id"));
            row.put("to_org_id", mShortList.get("to_org_id"));

            row.put("driver", mShortList.get("driver"));
            row.put("vehicle_no", mShortList.get("vehicle_no"));
            row.put("mobile", mShortList.get("mobile"));

            row.put("processed", false);
            row.put("user_id", mShortList.getString("user_id"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date now = new Date();
            row.put("bill_date", sdf.format(now));
            mShortListId = (int) db().create(row);
        } else {
            //更新数据库
            db().update(row, mShortListId);
        }

        //添加short_line信息

        ShortListLineDB lineDB = new ShortListLineDB(scope.context());

        List bills = (List) mShortList.get("bills");
        for (Object o : bills) {
            try {
                String carrying_bill_id = ((JSONObject) o).getString("id");
                String from_org_id = ((JSONObject) o).getString("from_org_id");
                String to_org_id = ((JSONObject) o).getString("to_org_id");
                String bill_no = ((JSONObject) o).getString("bill_no");
                String from_org_name = ((JSONObject) o).getString("from_org_name");
                String to_org_name = ((JSONObject) o).getString("to_org_name");
                String carrying_fee = ((JSONObject) o).getString("carrying_fee");
                String goods_fee = ((JSONObject) o).getString("goods_fee");
                String goods_info = ((JSONObject) o).getString("goods_info");
                String goods_num = ((JSONObject) o).getString("goods_num");

                LmisValues lineValue = new LmisValues();
                lineValue.put("short_list_id", mShortListId);
                lineValue.put("carrying_bill_id", carrying_bill_id);
                lineValue.put("from_org_id", from_org_id);
                lineValue.put("to_org_id", to_org_id);
                lineValue.put("from_org_name", from_org_name);
                lineValue.put("to_org_name", to_org_name);
                lineValue.put("bill_no", bill_no);
                lineValue.put("carrying_fee", carrying_fee);
                lineValue.put("goods_fee", goods_fee);
                lineValue.put("goods_info", goods_info);
                lineValue.put("goods_num", goods_num);

                lineValue.put("qty", goods_num);
                lineValue.put("barcode", goods_num);
                lineDB.create(lineValue);
            } catch (Exception ex) {
                ret = false;
                Log.e(TAG, ex.getMessage());
            }

        }
        mShortList = db().select(mShortListId);
        return ret;
    }

    @Override
    public LmisDatabase db() {
        return new ShortListDB(scope.context());
    }

    /**
     * 上传数据.
     */
    private class Uploader extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在上传数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ((ShortListDB) db()).save2server(mShortListId);

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mUploadAsync.cancel(true);
                Toast.makeText(scope.context(), "上传数据成功!", Toast.LENGTH_SHORT).show();
                ShortListPrintCpcl.printShortListCpcl(scope.context(),mShortList,ShortListPrintCpcl.PRINTER_NAME);
                DrawerListener drawer = scope.main();
                drawer.refreshDrawer("short_lists");
                //返回已处理界面
                ShortListList list = new ShortListList();
                Bundle arg = new Bundle();
                arg.putString("state", "shipped");
                list.setArguments(arg);
                scope.main().startMainFragment(list, true);

            } else {
                Toast.makeText(scope.context(), "上传数据失败!", Toast.LENGTH_SHORT).show();
            }

            pdialog.dismiss();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        final ActionBar actionBar = getActivity().getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.removeAllTabs();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    }


    @Override
    public void onPause() {
        super.onPause();
        final ActionBar actionBar = getActivity().getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.removeAllTabs();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.remove("android:support:fragments");
//    }

    public void onResume() {
        super.onResume();
        initTabs();
    }


}
