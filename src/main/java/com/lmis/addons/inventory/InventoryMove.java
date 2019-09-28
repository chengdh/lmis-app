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
import android.widget.TabHost;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.BarcodeParserFactory;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-6-17.
 * 调拨出库单新增或修改界面
 */
public class InventoryMove extends BaseFragment implements TabHost.OnTabChangeListener {

    public static final String TAG = "InventoryOut";

    @InjectView(R.id.tabHost)
    TabHost mTabHost;


    @InjectView(R.id.pager)
    ViewPager mPager;


    /**
     * 出入库类型,具体参照 InventoryOutList#MType.
     */
    String mOpType = null;

    InventoryMovePagerAdapter mPageAdapter = null;

    View mView = null;
    Integer mInventoryMoveId = -1;
    LmisDataRow mInventoryMove = null;

    BarcodeParser mBarcodeParser = null;

    Upload mUploadAsync = null;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG,"onSaveInstanceState:");
        outState.putString("state", "test");
    }

    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            String state = savedInstanceState.getString("curChoice", "new test");
            Log.d(TAG,"onCreateView:" + state);
        }
        mView = inflater.inflate(R.layout.fragment_inventory_move, container, false);


        ButterKnife.inject(this, mView);
        initData();
        initControls();
        return mView;
    }

    private void initData() {
        Log.d(TAG, "inventory_move#initData");

        LmisUser currentUser = scope.currentUser();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOpType = bundle.getString("type");
            if (bundle.containsKey("inventory_move_id")) {
                mInventoryMoveId = bundle.getInt("inventory_move_id");
            }
        }

        int fromOrgID = -1;
        int toOrgID = -1;
        if (mInventoryMoveId > 0) {
            mInventoryMove = new InventoryMoveDB(scope.context()).select(mInventoryMoveId);
            LmisDataRow fromOrg = mInventoryMove.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = mInventoryMove.getM2ORecord("to_org_id").browse();

            fromOrgID = fromOrg.getInt("id");
            toOrgID = toOrg.getInt("id");
        } else {
            fromOrgID = currentUser.getDefault_org_id();
        }
        mBarcodeParser = BarcodeParserFactory.getParser(scope.context(), mInventoryMoveId, fromOrgID, toOrgID, mOpType);
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
        mPageAdapter = new InventoryMovePagerAdapter(getFragmentManager(), mBarcodeParser, mInventoryMove);
        mPager.setAdapter(mPageAdapter);
        mPageAdapter.notifyDataSetChanged();
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        mTabHost.addTab(mTabHost.newTabSpec("TAB_VEHICLE_FORM").setIndicator("车辆信息").setContent(R.id.tab_blank));
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
        if (s.equals("TAB_VEHICLE_FORM")) {
            mPager.setCurrentItem(0);
        } else if (s.equals("TAB_SCAN_BARCODE")) {
            mPager.setCurrentItem(1);
        } else if (s.equals("TAB_BILLS_LIST")) {
            mPager.setCurrentItem(2);
        } else if (s.equals("TAB_BARCODES_LIST")) {
            mPager.setCurrentItem(3);
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
        inflater.inflate(R.menu.menu_fragment_inventory_move, menu);
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
            if (mBarcodeParser.getmMoveId() == -1) {
                return false;
            }
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
                drawer.refreshDrawer(InventoryMoveList.TAG);
                //返回已处理界面
                InventoryMoveList list = new InventoryMoveList();
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
