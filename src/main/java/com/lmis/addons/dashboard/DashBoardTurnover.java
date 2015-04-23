package com.lmis.addons.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lmis.R;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;

import org.xwalk.core.XWalkView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14/12/25.
 */
public class DashBoardTurnover extends BaseFragment {
    public static final String TAG = "DashBoardTurnover";
    public static String TURNOBER_URL = "http://122.0.76.160:3000/progress_bars";

    @Inject
    Bus mBus;

    @InjectView(R.id.web_view_dashboard_turnover)
    XWalkView mWebView;

    View mView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_dashboard_turnover, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        init();

        return mView;

    }

    private void init() {
        mWebView.load(TURNOBER_URL, null);
    }

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    private BaseFragment getFragment() {
        DashBoard ds = new DashBoard();
        Bundle bundle = new Bundle();
        bundle.putInt("defaut_org_id", -1);
        ds.setArguments(bundle);
        return ds;
    }
}
