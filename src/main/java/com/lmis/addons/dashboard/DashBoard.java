package com.lmis.addons.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lmis.CurrentOrgChangeEvent;
import com.lmis.R;
import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisUser;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-22.
 */
public class DashBoard extends BaseFragment {
    public final static String TAG = "Dashbaord";
    public final static String DASHBOARD_URL = "http://122.0.76.160:3000/sample";

    @Inject
    Bus mBus;

    @InjectView(R.id.web_view_dashboard)
    WebView mWebView;

    View mView = null;

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        init();
        return mView;
    }

    private void init() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        loadDashboardUrl();

    }

    private void loadDashboardUrl() {
        LmisUser user = scope.currentUser();
        int curOrgId = user.getDefault_org_id();
        OrgDB orgDB = new OrgDB(scope.context());
        List<LmisDataRow> children = orgDB.getChildrenOrgs(curOrgId);
        String jsArray = "&children_org_ids[]=-1";
        for (LmisDataRow r : children) {
            jsArray += "&children_org_ids[]=" + r.getInt("id") + "";
        }
        String url = DASHBOARD_URL + "?from_org_id=" + curOrgId + jsArray;
        mWebView.loadUrl(url);
        Log.d("Dashboard#loadDashboardUrl : ",url);
    }

    @Subscribe
    public void onCurrentOrgChangeEvent(CurrentOrgChangeEvent evt) {
        loadDashboardUrl();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        drawerItems.add(new DrawerItem(TAG, "业务看板", 0, R.drawable.ic_menu_dashboard, getFragment()));
        return drawerItems;
    }

    private BaseFragment getFragment() {
        DashBoard ds = new DashBoard();
        Bundle bundle = new Bundle();
        bundle.putInt("defaut_org_id", -1);
        ds.setArguments(bundle);
        return ds;
    }
}
