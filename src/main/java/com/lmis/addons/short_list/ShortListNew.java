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
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.barcode_scan_header.GoodsInfo;
import com.lmis.util.barcode_scan_header.ScanHeaderOpType;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
        inflater.inflate(R.menu.menu_fragment_scan_header_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan_header_upload:
                if (validateBeforeUpload()) {
                    mUploadAsync = new Uploader();
                    mUploadAsync.execute((Void) null);
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean validateBeforeUpload() {

        boolean success = true;
        boolean successToOrg = true;
        mPager.setCurrentItem(0);
        FragmentVehicleForm page = (FragmentVehicleForm) mPageAdapter.getRegisteredFragment(0);
        success = page.validateBeforeUpload();
        if (!success) {
            Toast.makeText(scope.context(), "请输入装车信息!", Toast.LENGTH_SHORT).show();
        }

        return success;
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
                DrawerListener drawer = scope.main();
                drawer.refreshDrawer("short_lists");
                //返回已处理界面
                ShortListList list = new ShortListList();
                Bundle arg = new Bundle();
                arg.putString("state", "loaded");
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
