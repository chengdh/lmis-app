package com.lmis.addons.inventory;

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
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-6-17.
 * 调拨出库单新增或修改界面
 */
public class InventoryOut extends BaseFragment implements TabHost.OnTabChangeListener {

    public static final String TAG = "InventoryOut";

    @InjectView(R.id.tabHost)
    TabHost mTabHost;


    @InjectView(R.id.pager)
    ViewPager mPager;


    InventoryOutPagerAdapter mPageAdapter = null;

    View mView = null;
    Integer mInventoryOutId = null;
    LmisDataRow mInventoryOut = null;

    BarcodeParser mBarcodeParser = null;


    MenuItem mSearchViewBillIcon;
    MenuItem mSearchViewBarcodeIcon;

    SearchView mSearchViewBillList;

    SearchView mSearchViewBarcodeList;

    Upload mUploadAsync = null;


    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_inventory_out, container, false);

        ButterKnife.inject(this, mView);
        initData();
        initControls();
        return mView;
    }

    private void initData() {
        Log.d(TAG, "inventory_out#initData");
        LmisUser currentUser = scope.currentUser();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mInventoryOutId = bundle.getInt("inventory_out_id");
            mInventoryOut = new InventoryMoveDB(scope.context()).select(mInventoryOutId);
            LmisDataRow fromOrg = mInventoryOut.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = mInventoryOut.getM2ORecord("to_org_id").browse();
            mBarcodeParser = new BarcodeParser(scope.context(), mInventoryOutId, fromOrg.getInt("id"), toOrg.getInt("id"), false);
        } else
            mBarcodeParser = new BarcodeParser(scope.context(), -1, currentUser.getDefault_org_id(), -1, false);
    }

    /**
     * /**
     * 初始化控件.
     */
    private void initControls() {
        initTabs();
        initPager();
    }

    private void initPager() {
        mPageAdapter = new InventoryOutPagerAdapter(getFragmentManager(), mBarcodeParser, mInventoryOut);
        mPager.setAdapter(mPageAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 初始化tabs.
     */
    private void initTabs() {
        mTabHost.setup();
        //TODO 此处加上tab图标
        mTabHost.addTab(mTabHost.newTabSpec("TAB_SCAN_BARCODE").setIndicator("扫描条码").setContent(R.id.tab_blank));
        mTabHost.addTab(mTabHost.newTabSpec("TAB_BILLS_LIST").setIndicator("票据明细").setContent(R.id.tab_blank));
        mTabHost.addTab(mTabHost.newTabSpec("TAB_BARCODES_LIST").setIndicator("条码明细").setContent(R.id.tab_blank));
        mTabHost.setOnTabChangedListener(this);
    }


    /**
     * On tab changed.
     *
     * @param s the s
     */
    @Override
    public void onTabChanged(String s) {
        //mSearchViewBillIcon.collapseActionView();
        //mSearchViewBarcodeIcon.collapseActionView();

        if (s.equals("TAB_SCAN_BARCODE")) {
            //mSearchViewBillIcon.setVisible(false);
            //mSearchViewBarcodeIcon.setVisible(false);
            mPager.setCurrentItem(0);
        } else if (s.equals("TAB_BILLS_LIST")) {
            //mSearchViewBillIcon.setVisible(true);
           // mSearchViewBarcodeIcon.setVisible(false);
            mPager.setCurrentItem(1);
        } else if (s.equals("TAB_BARCODES_LIST")) {
            //mSearchViewBillIcon.setVisible(false);
            //mSearchViewBarcodeIcon.setVisible(true);
            mPager.setCurrentItem(2);
        }
    }

    @Override
    public Object databaseHelper(Context context) {
        return new InventoryMoveDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_inventory_out, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_inventory_out_upload:
                mUploadAsync = new Upload();
                mUploadAsync.execute((Void) null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 上传数据.
     */
    private class Upload extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在上传数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ((InventoryMoveDB) db()).save2server(mBarcodeParser.getmMoveId());
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
                drawer.refreshDrawer(InventoryOutList.TAG);
                //返回已处理界面
                InventoryOutList list = new InventoryOutList();
                Bundle arg = new Bundle();
                arg.putString("type", "draft");
                list.setArguments(arg);
                scope.main().startMainFragment(list, true);

            } else {
                Toast.makeText(scope.context(), "上传数据失败!", Toast.LENGTH_SHORT).show();
            }

            pdialog.dismiss();
        }
    }
}
