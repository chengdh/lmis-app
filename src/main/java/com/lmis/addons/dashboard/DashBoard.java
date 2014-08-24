package com.lmis.addons.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-22.
 */
public class DashBoard extends BaseFragment {
    public final static String TAG = "Dashbaord";
    public final static String SERVER_URL = "http://192.168.1.99:3030";
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
        init();
        return mView;
    }

    private void init() {
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        int curOrgId = scope.currentUser().getDefault_org_id();
        mWebView.loadUrl(SERVER_URL + "?from_org_id=" + curOrgId + "");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();

        drawerItems.add(new DrawerItem(TAG, "业务看板", 0, R.drawable.ic_action_archive, getFragment()));
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
