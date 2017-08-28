package com.lmis.addons.scan_header;

import android.app.ActionBar;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.barcode_scan_header.BarcodeParser;
import com.lmis.util.barcode_scan_header.BarcodeParserFactory;
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

public class ScanHeaderNew extends BaseFragment {
    public static final String TAG = "ScanHeaderNew";

    @InjectView(R.id.pager)
    ViewPager mPager;


    /**
     * 出入库类型,具体参照 ScanHeaderOpType#MType.
     */
    String mOpType = null;

    ScanHeaderPagerAdapter mPageAdapter = null;

    View mView = null;
    Integer mScanHeaderId = -1;
    LmisDataRow mScanHeader = null;

    BarcodeParser mBarcodeParser = null;

    Uploader mUploadAsync = null;


    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_scan_header_new, container, false);

        ButterKnife.inject(this, mView);
        initData();
        initControls();
        return mView;
    }

    private void initData() {
        Log.d(TAG, "ScanHeaderNew#initData");

        LmisUser currentUser = scope.currentUser();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOpType = bundle.getString("type");
            if (bundle.containsKey("scan_header_id")) {
                mScanHeaderId = bundle.getInt("scan_header_id");
            }
        }

        int fromOrgID = -1;
        int toOrgID = -1;
        if (mScanHeaderId > 0) {
            mScanHeader = new ScanHeaderDB(scope.context()).select(mScanHeaderId);
            LmisDataRow fromOrg = mScanHeader.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = mScanHeader.getM2ORecord("to_org_id").browse();

            if (fromOrg != null) {
                fromOrgID = fromOrg.getInt("id");
            }

            if (toOrg != null) {
                toOrgID = toOrg.getInt("id");
            }
        } else {
            //根据opttype 判断fromOrg与toOrg的值
            //分拣组入库，from_org_id=-1 to_org_id=当前用户登录机构
            if (mOpType.equals(ScanHeaderOpType.SORTING_IN) || mOpType.equals(ScanHeaderOpType.LOAD_IN)) {
                fromOrgID = -1;
                toOrgID = currentUser.getDefault_org_id();
            }

            if (mOpType.equals(ScanHeaderOpType.LOAD_OUT)) {
                fromOrgID = currentUser.getDefault_org_id();
            }
        }
        mBarcodeParser = BarcodeParserFactory.getParser(scope.context(), mScanHeaderId, fromOrgID, toOrgID, mOpType);
    }

    /**
     * 初始化控件.
     */
    private void initControls() {
        initTabs();
        initPager();
    }

    private void initPager() {
        mPageAdapter = new ScanHeaderPagerAdapter(getFragmentManager(), mBarcodeParser, mScanHeader, mOpType);
        mPager.setAdapter(mPageAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                final android.app.ActionBar actionBar = getActivity().getActionBar();
                actionBar.setSelectedNavigationItem(position);

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

        final android.app.ActionBar actionBar = getActivity().getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        if (mOpType.equals(ScanHeaderOpType.LOAD_OUT)) {
            actionBar.addTab(actionBar.newTab().setText("车辆信息").setTabListener(tabListener));
        }
        actionBar.addTab(actionBar.newTab().setText("扫描票据").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("票据列表").setTabListener(tabListener));

    }


    @Override
    public Object databaseHelper(Context context) {
        return new ScanHeaderDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_scan_header_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan_header_upload:
                if (validateBeforeUpload()) {
                    mUploadAsync = new ScanHeaderNew.Uploader();
                    mUploadAsync.execute((Void) null);
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean validateBeforeUpload() {

        boolean success = true;
        if (mOpType.equals(ScanHeaderOpType.LOAD_OUT)) {
            mPager.setCurrentItem(0);
            FragmentVehicleForm page = (FragmentVehicleForm) mPageAdapter.getRegisteredFragment(0);
            success = page.validateBeforeUpload();
            if (!success) {
                Toast.makeText(scope.context(), "请输入装车信息!", Toast.LENGTH_SHORT).show();
            }
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
            if (mBarcodeParser.getmId() == -1) {
                return false;
            }
            try {
                ((ScanHeaderDB) db()).save2server(mBarcodeParser.getmId());

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
                drawer.refreshDrawer(mOpType);
                //返回已处理界面
                ScanHeaderList list = new ScanHeaderList();
                Bundle arg = new Bundle();
                arg.putString("type", mOpType);
                arg.putString("state", "processed");
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
        final android.app.ActionBar actionBar = getActivity().getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.removeAllTabs();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mBarcodeParser.unRegisterEventBus();
    }

    @Override
    public void onPause() {
        super.onPause();
        final android.app.ActionBar actionBar = getActivity().getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.removeAllTabs();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.remove("android:support:fragments");
    }

    public void onResume() {
        super.onResume();
        initTabs();
    }

}
