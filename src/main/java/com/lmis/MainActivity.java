/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.lmis;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fizzbuzz.android.dagger.InjectingActivityModule.Activity;
import com.fizzbuzz.android.dagger.InjectingFragmentActivity;
import com.github.snowdream.android.app.UpdateFormat;
import com.github.snowdream.android.app.UpdateManager;
import com.github.snowdream.android.app.UpdateOptions;
import com.github.snowdream.android.app.UpdatePeriod;
import com.lmis.auth.LmisAccountManager;
import com.lmis.base.about.AboutFragment;
import com.lmis.base.account.AccountFragment;
import com.lmis.base.account.AccountsDetail;
import com.lmis.base.account.UserProfile;
import com.lmis.dagger_module.ActivityModule;
import com.lmis.dagger_module.OrgModule;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisUser;
import com.lmis.support.fragment.FragmentListener;
import com.lmis.util.OnBackButtonPressedListener;
import com.lmis.util.PreferenceManager;
import com.lmis.util.drawer.DrawerAdatper;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;
import com.lmis.widgets.WidgetHelper;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The Class MainActivity.
 */
public class MainActivity extends InjectingFragmentActivity implements
        DrawerItem.DrawerItemClickListener, FragmentListener, DrawerListener, ActionBar.OnNavigationListener {

    /**
     * The constant TAG.
     */
    public static final String TAG = "MainActivity";
    /**
     * The constant RESULT_SETTINGS.
     */
    public static final int RESULT_SETTINGS = 1;
    /**
     * The constant set_setting_menu.
     */
    public static boolean set_setting_menu = false;

    /**
     * The M context.
     */
    @Inject
    @Activity
    Context mContext;

    @Inject
    Bus mBus;

    /**
     * The M fragment.
     */
    @Inject
    @Activity
    FragmentManager mFragment;

    /**
     * The M drawer list items.
     */
    @Inject
    @Activity
    List<DrawerItem> mDrawerListItems;

    /**
     * The M orgs.
     */
    @Inject
    @OrgModule.AccessOrgs
    List<LmisDataRow> mOrgs;

    /**
     * The M drawer layout.
     */
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    /**
     * The M drawer list view.
     */
    @InjectView(R.id.left_drawer)
    ListView mDrawerListView;

    /**
     * The M drawer toggle.
     */
    ActionBarDrawerToggle mDrawerToggle = null;
    /**
     * The M drawer adatper.
     */
    DrawerAdatper mDrawerAdatper = null;

    ArrayAdapter<LmisDataRow> mOrgSpinnerAdapter = null;
    /**
     * The M app title.
     */
    String mAppTitle = "";
    /**
     * The M drawer title.
     */
    String mDrawerTitle = "";
    /**
     * The M drawer subtitle.
     */
    String mDrawerSubtitle = "";
    /**
     * The M drawer item selected position.
     */
    int mDrawerItemSelectedPosition = -1;


    /**
     * The enum Setting keys.
     */
    public enum SettingKeys {
        /**
         * The GLOBAL_SETTING.
         */
        GLOBAL_SETTING, /**
         * The PROFILE.
         */
        PROFILE, /**
         * The ACCOUNTS.
         */
        ACCOUNTS, /**
         * The ABOUT_US.
         */
        ABOUT_US
    }

    private CharSequence mTitle;
    private OnBackButtonPressedListener backPressed = null;
    private boolean mLandscape = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            mDrawerItemSelectedPosition = savedInstanceState.getInt("current_drawer_item");
        }
        if (findViewById(R.id.fragment_container) != null) {
            mLandscape = false;
        } else {
            mLandscape = true;
        }
        init();
    }

    @Override
    protected List<Object> getModules() {
        List<Object> ret = super.getModules();
        ret.add(new ActivityModule(this, this));
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_check_upgrade:
                checkUpgrade();
                break;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
        return true;
    }

    private void checkUpgrade() {
        UpdateManager manager = new UpdateManager(this);

        UpdateOptions options = new UpdateOptions.Builder(this)
                .checkUrl("https://raw.githubusercontent.com/chengdh/openerp-mobile/master/update.xml")
                .updateFormat(UpdateFormat.XML)
                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
                .checkPackageName(true)
                .build();

        manager.check(this, options);
    }

    private void init() {
        Log.d(TAG, "MainActivity->init()");
        initDrawerControls();
        boolean reqForNewAccount = getIntent().getBooleanExtra("create_new_account", false);
        /**
         * checks for available account related to Lmis
         */
        if (!LmisAccountManager.hasAccounts(this) || reqForNewAccount) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setHomeButtonEnabled(false);
            lockDrawer(true);
            AccountFragment account = new AccountFragment();
            startMainFragment(account, false);
        } else {
            lockDrawer(false);
            /**
             * currentUser found but not logged in. Requesting for login with available
             * accounts.
             */
            if (!LmisAccountManager.isAnyUser(mContext)) {
                accountSelectionDialog(LmisAccountManager.fetchAllAccounts(mContext)).show();
            } else {
                initDrawer();
                initSpinner();
            }
        }
    }

    private void initDrawerControls() {
        Log.d(TAG, "MainActivity->initDrawerControls()");

        mDrawerAdatper = new DrawerAdatper(this, R.layout.drawer_item_layout, R.layout.drawer_item_group_layout, mDrawerListItems);

        mDrawerListView.setAdapter(mDrawerAdatper);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActionBar().setIcon(R.drawable.ic_launcher);
                setTitle(mAppTitle, null);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setTitle(mDrawerTitle, mDrawerSubtitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setDrawerItems() {
        Log.d(TAG, "MainActivity->setDrawerItems()");
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (mDrawerItemSelectedPosition >= 0) {
            mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);
        }
        if (LmisUser.current(mContext) != null) {
            mDrawerTitle = LmisUser.current(mContext).getUsername();
            mDrawerSubtitle = LmisUser.current(mContext).getHost();
        }
        mDrawerAdatper.notifiyDataChange(mDrawerListItems);
        Log.d(TAG, "MainActivity->setDrawerItems() finish");
    }

    private void initDrawer() {
        setDrawerItems();
        Log.d(TAG, "MainActivity->initDrawer()");
        mDrawerListView.setOnItemClickListener(this);
        int position = -1;
        if (mDrawerListItems.size() > 0) {
            if (!mDrawerListItems.get(0).isGroupTitle()) {
                mDrawerListView.setItemChecked(0, true);
                position = 0;
            } else {
                mDrawerListView.setItemChecked(1, true);
                position = 1;
            }
        }
        if (mDrawerItemSelectedPosition >= 0) {
            position = mDrawerItemSelectedPosition;
        }
        mAppTitle = mDrawerListItems.get(position).getTitle();
        setTitle(mAppTitle);
        if (getIntent().getAction() != null
                && !getIntent().getAction().toString()
                .equalsIgnoreCase("android.intent.action.MAIN")) {
            if (getIntent().getAction().toString().equalsIgnoreCase("MESSAGE")) {
                int size = mDrawerListItems.size();
                for (int i = 0; i < size; i++) {
                    if (mDrawerAdatper.getItem(i).getTitle()
                            .equalsIgnoreCase("Messages")) {
                        loadFragment(mDrawerAdatper.getItem(i + 1));
                    }
                }
            }
            if (getIntent().getAction().toString().equalsIgnoreCase("NOTES")) {
                int size = mDrawerListItems.size();
                for (int i = 0; i < size; i++) {
                    if (mDrawerAdatper.getItem(i).getTitle()
                            .equalsIgnoreCase("Notes")) {
                        loadFragment(mDrawerAdatper.getItem(i + 1));
                        break;
                    }
                }
            }

            /**
             * Handling widget fragment requests.
             */
            if (getIntent().getAction().equals(WidgetHelper.ACTION_WIDGET_CALL)) {
                Log.d(TAG, "MainActivity->ACTION_WIDGET_CALL");
                String key = getIntent().getExtras().getString(
                        WidgetHelper.EXTRA_WIDGET_ITEM_KEY);
            }
        } else {
            if (position >= 0) {
                if (position != mDrawerItemSelectedPosition) {
                    loadFragment(mDrawerListItems.get(position));
                }
            }
        }
        Log.d(TAG, "MainActivity->initDrawer() finish");
    }

    /**
     * Init actionbar spinner.
     */
    private void initSpinner() {
        mOrgSpinnerAdapter = new ArrayAdapter<LmisDataRow>(mContext, android.R.layout.simple_spinner_dropdown_item, mOrgs);

        ActionBar ab = getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        ab.setListNavigationCallbacks(mOrgSpinnerAdapter, this);

        LmisUser curUser = LmisAccountManager.currentUser(this);
        int orgId = curUser.getDefault_org_id();
        int position = 0;
        for (int i = 0; i < mOrgs.size(); i++) {
            if (mOrgs.get(i).getInt("id").equals(orgId))
                position = i;
        }

        ab.setSelectedNavigationItem(position);

    }

    @Override
    public boolean onNavigationItemSelected(int position, long l) {
        //修改当前的org_id
        LmisDataRow curOrg = mOrgs.get(position);
        LmisAccountManager.changeDefaultOrgId(this, curOrg.getInt("id"));
        mBus.post(new CurrentOrgChangeEvent(curOrg));
        return true;
    }

    /**
     * Account list.
     *
     * @param accounts the accounts
     * @return the string [ ]
     */
    private String[] accountList(List<LmisUser> accounts) {
        String[] account_list = new String[accounts.size()];
        int i = 0;
        for (LmisUser user : accounts) {
            account_list[i] = user.getAndroidName();
            i++;
        }
        return account_list;
    }

    /**
     * The M account.
     */
    LmisUser mAccount = null;

    /**
     * Account selection dialog.
     *
     * @param accounts the accounts
     * @return the dialog
     */
    public Dialog accountSelectionDialog(final List<LmisUser> accounts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Account")
                .setSingleChoiceItems(accountList(accounts), 1,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAccount = accounts.get(which);
                            }
                        }
                )
                .setNeutralButton("New", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActionBar().setDisplayHomeAsUpEnabled(false);
                        getActionBar().setHomeButtonEnabled(false);
                        AccountFragment fragment = new AccountFragment();
                        startMainFragment(fragment, false);
                    }
                })
                        // Set the action buttons
                .setPositiveButton("Login",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (mAccount != null) {
                                    LmisAccountManager.loginUser(mContext,
                                            mAccount.getAndroidName());
                                } else {
                                    Toast.makeText(mContext,
                                            "Please select account",
                                            Toast.LENGTH_LONG).show();
                                }
                                init();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        }
                );

        return builder.create();
    }

    @Override
    public void refreshDrawer(String tag_key) {
        Log.d(TAG, "MainActivity->DrawerListener->refreshDrawer()");
        int start_index = -1;
        List<DrawerItem> updated_menus = new ArrayList<DrawerItem>();
        for (int i = 0; i < mDrawerListItems.size(); i++) {
            DrawerItem item = mDrawerListItems.get(i);
            if (item.getKey().equals(tag_key) && !item.isGroupTitle()) {
                if (start_index < 0) {
                    start_index = i - 1;
                    BaseFragment instance = (BaseFragment) item.getFragmentInstace();
                    updated_menus.addAll(instance.drawerMenus(mContext));
                    break;
                }
            }
        }
        for (DrawerItem item : updated_menus) {
            mDrawerAdatper.updateDrawerItem(start_index, item);
            start_index++;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Sets title.
     *
     * @param title    the title
     * @param subtitle the subtitle
     */
    public void setTitle(CharSequence title, CharSequence subtitle) {
        mTitle = title;
        this.setTitle(mTitle);
        getActionBar().setSubtitle(subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On setting item selected.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean onSettingItemSelected(SettingKeys key) {
        switch (key) {
            case GLOBAL_SETTING:
                set_setting_menu = false;
                Intent i = new Intent(this, AppSettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
            case ABOUT_US:
                set_setting_menu = true;
                getActionBar().setDisplayHomeAsUpEnabled(false);
                getActionBar().setHomeButtonEnabled(false);
                AboutFragment about = new AboutFragment();
                startMainFragment(about, true);
                return true;
            case ACCOUNTS:
                set_setting_menu = true;
                AccountsDetail acFragment = new AccountsDetail();
                startMainFragment(acFragment, true);
                return true;
            case PROFILE:
                set_setting_menu = true;
                UserProfile profileFragment = new UserProfile();
                startMainFragment(profileFragment, true);
                return true;
            default:
                return true;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                updateSyncSettings();
                break;
        }

    }

    private void updateSyncSettings() {
        Log.d(TAG, "MainActivity->updateSyncSettings()");

        PreferenceManager mPref = new PreferenceManager(mContext);
        int sync_interval = mPref.getInt("sync_interval", 1440);

        List<String> default_authorities = new ArrayList<String>();
        default_authorities.add("com.android.calendar");
        default_authorities.add("com.android.contacts");

        SyncAdapterType[] list = ContentResolver.getSyncAdapterTypes();

        Account mAccount = LmisAccountManager.getAccount(mContext, LmisUser
                .current(mContext).getAndroidName());

        for (SyncAdapterType lst : list) {
            if (lst.authority.contains("com.lmis.providers")) {
                default_authorities.add(lst.authority);
            }
        }
        for (String authority : default_authorities) {
            boolean isSyncActive = ContentResolver.getSyncAutomatically(
                    mAccount, authority);
            if (isSyncActive) {
                setSyncPeriodic(authority, sync_interval, 1, 1);
            }
        }
        Toast.makeText(this, "Setting saved.", Toast.LENGTH_LONG).show();
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /**
     * Sets the auto sync.
     *
     * @param authority the authority
     * @param isON      the is on
     */
    public void setAutoSync(String authority, boolean isON) {
        try {
            Account account = LmisAccountManager.getAccount(this, LmisUser
                    .current(mContext).getAndroidName());
            if (!ContentResolver.isSyncActive(account, authority)) {
                ContentResolver.setSyncAutomatically(account, authority, isON);
            }
        } catch (NullPointerException eNull) {

        }
    }

    /**
     * Request sync.
     *
     * @param authority the authority
     * @param bundle    the extra data
     */
    public void requestSync(String authority, Bundle bundle) {
        Account account = LmisAccountManager.getAccount(getApplicationContext(), LmisUser.current(getApplicationContext()).getAndroidName());
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        if (bundle != null) {
            settingsBundle.putAll(bundle);
        }
        ContentResolver.requestSync(account, authority, settingsBundle);
    }

    /**
     * Request sync.
     *
     * @param authority the authority
     */
    public void requestSync(String authority) {
        requestSync(authority, null);
    }

    /**
     * Sets the sync periodic.
     *
     * @param authority               the authority
     * @param interval_in_minute      the interval_in_minute
     * @param seconds_per_minute      the seconds_per_minute
     * @param milliseconds_per_second the milliseconds_per_second
     */
    public void setSyncPeriodic(String authority, long interval_in_minute,
                                long seconds_per_minute, long milliseconds_per_second) {
        Account account = LmisAccountManager.getAccount(this, LmisUser
                .current(mContext).getAndroidName());
        Bundle extras = new Bundle();
        this.setAutoSync(authority, true);
        ContentResolver.setIsSyncable(account, authority, 1);
        final long sync_interval = interval_in_minute * seconds_per_minute * milliseconds_per_second;
        ContentResolver.addPeriodicSync(account, authority, extras, sync_interval);

    }

    /**
     * Cancel sync.
     *
     * @param authority the authority
     */
    public void cancelSync(String authority) {
        Account account = LmisAccountManager.getAccount(this, LmisUser.current(mContext).getAndroidName());
        ContentResolver.cancelSync(account, authority);
    }

    @Override
    public void onBackPressed() {
        if (backPressed != null) {
            if (backPressed.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Sets on back pressed.
     *
     * @param callback the callback
     */
    public void setOnBackPressed(OnBackButtonPressedListener callback) {
        backPressed = callback;
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        DrawerItem item = mDrawerListItems.get(position);
        if (!item.isGroupTitle()) {
            if (!item.getKey().equals("com.lmis.settings")) {
                mDrawerItemSelectedPosition = position;
            }
            mAppTitle = item.getTitle();
            loadFragment(item);
            mDrawerLayout.closeDrawers();
        }
        mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);

    }

    private void loadFragment(DrawerItem item) {

        Fragment fragment = (Fragment) item.getFragmentInstace();
        if (item.getTagColor() != null && !fragment.getArguments().containsKey("tag_color")) {
            Bundle tagcolor = fragment.getArguments();
            tagcolor.putInt("tag_color", Color.parseColor(item.getTagColor()));
            fragment.setArguments(tagcolor);
        }
        loadFragment(fragment);
    }

    private void loadFragment(Object instance) {
        if (instance instanceof Intent) {
            startActivity((Intent) instance);
        } else {
            Fragment fragment = (Fragment) instance;
            if (fragment.getArguments() != null && fragment.getArguments().containsKey("settings")) {
                onSettingItemSelected(SettingKeys.valueOf(fragment.getArguments().get("settings").toString()));
            }
            if (fragment != null && !fragment.getArguments().containsKey("settings")) {
                startMainFragment(fragment, false);
            }
        }
    }

    private void lockDrawer(boolean flag) {
        if (!flag) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.STATE_IDLE);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("current_drawer_item", mDrawerItemSelectedPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void startMainFragment(Fragment fragment, boolean addToBackState) {
        Log.d(TAG, "MainActivity->FragmentListener->startMainFragment()");
        FragmentTransaction tran = mFragment.beginTransaction().replace(R.id.fragment_container, fragment);
        if (addToBackState) {
            tran.addToBackStack(null);
        }
        tran.commit();
    }

    @Override
    public void startDetailFragment(Fragment fragment) {
        Log.d(TAG, "MainActivity->FragmentListener->startDetailFragment()");
        FragmentTransaction tran = mFragment.beginTransaction().replace(
                R.id.fragment_container, fragment);
        if (!mLandscape) {
            tran.addToBackStack(null);
        }
        tran.commit();
    }

    @Override
    public void restart() {
        Log.d(TAG, "MainActivity->FragmentListener->restart()");
        getIntent().putExtra("create_new_account", false);
        init();
    }
}
