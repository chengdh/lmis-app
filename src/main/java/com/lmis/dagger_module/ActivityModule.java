package com.lmis.dagger_module;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.fizzbuzz.android.dagger.InjectingActivityModule.Activity;
import com.fizzbuzz.android.dagger.Injector;
import com.lmis.MainActivity;
import com.lmis.R;
import com.lmis.support.LmisUser;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.GoodsInfo;
import com.lmis.util.controls.AccessOrgSpinner;
import com.lmis.util.controls.AllOrgSpinner;
import com.lmis.util.controls.ExcludeAccessOrgSpinner;
import com.lmis.util.controls.PayTypeSpinner;
import com.lmis.util.controls.YardsOrgSpinner;
import com.lmis.util.drawer.DrawerHelper;
import com.lmis.util.drawer.DrawerItem;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

/**
 * Created by chengdh on 14-6-13.
 */
@Module(complete = false, library = true,
        includes = {OrgModule.class, DbModule.class},
        injects = {MainActivity.class,
                AccessOrgSpinner.class,
                ExcludeAccessOrgSpinner.class,
                PayTypeSpinner.class,
                AllOrgSpinner.class,
                YardsOrgSpinner.class,
                BarcodeParser.class,
                GoodsInfo.class,
        })
public class ActivityModule {
    private MainActivity mActivity;
    private Injector mInjector;

    public ActivityModule(MainActivity activity, Injector injector) {
        mActivity = activity;
        mInjector = injector;
    }

    @Provides
    @Activity
    public FragmentManager provideFragmentManager() {
        return mActivity.getSupportFragmentManager();
    }

    //提供MainActivity中要使用的List<DrawerItem>
    @Provides
    @Activity
    public List<DrawerItem> provideDrawItems() {
        List<DrawerItem> ret = new ArrayList<DrawerItem>();
        //未登录时,不显示菜单
        if (LmisUser.current(mActivity) != null) {
            ret.addAll(DrawerHelper.drawerItems(mActivity));
            ret.addAll(getSysMenuItems());
        }
        return ret;

    }


    private List<DrawerItem> getSysMenuItems() {
        List<DrawerItem> sys = new ArrayList<DrawerItem>();
        String key = "com.lmis.settings";

        String settings_group_title = mActivity.getResources().getString(R.string.settings_group_title);
        String locale_profile = mActivity.getResources().getString(R.string.settings_drawer_item_profile);
        String locale_general_setting = mActivity.getResources().getString(R.string.settings_drawer_item_general_setting);
        String locale_account = mActivity.getResources().getString(R.string.settings_drawer_item_account);
        String locale_about_us = mActivity.getResources().getString(R.string.settings_drawer_item_about_us);


        sys.add(new DrawerItem(key, settings_group_title, true));
        sys.add(new DrawerItem(key, locale_profile, 0, R.drawable.ic_action_user,
                getFragBundle(new Fragment(), "settings", MainActivity.SettingKeys.PROFILE)));

        sys.add(new DrawerItem(key, locale_general_setting, 0,
                R.drawable.ic_action_settings, getFragBundle(new Fragment(),
                "settings", MainActivity.SettingKeys.GLOBAL_SETTING)
        ));

        sys.add(new DrawerItem(key, locale_account, 0,
                R.drawable.ic_action_accounts, getFragBundle(new Fragment(),
                "settings", MainActivity.SettingKeys.ACCOUNTS)
        ));
        sys.add(new DrawerItem(key, locale_about_us, 0, R.drawable.ic_action_about,
                getFragBundle(new Fragment(), "settings", MainActivity.SettingKeys.ABOUT_US)));
        return sys;
    }

    private Fragment getFragBundle(Fragment fragment, String key, MainActivity.SettingKeys val) {
        Bundle bundle = new Bundle();
        bundle.putString(key, val.toString());
        fragment.setArguments(bundle);
        return fragment;
    }
}
