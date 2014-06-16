package com.lmis.dagger_module;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.fizzbuzz.android.dagger.InjectingActivityModule;
import com.fizzbuzz.android.dagger.InjectingFragmentModule;
import com.fizzbuzz.android.dagger.Injector;
import com.lmis.addons.idea.Idea;
import com.lmis.base.about.AboutFragment;
import com.lmis.base.account.AccountFragment;
import com.lmis.base.account.AccountsDetail;
import com.lmis.base.account.UserProfile;
import com.lmis.base.login.Login;
import com.lmis.base.login.SyncWizard;
import com.lmis.support.AppScope;
import com.lmis.support.BaseFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by chengdh on 14-6-14.
 * 为fragment提供组件
 */
@Module(complete = false, library = true,
        injects = {
                BaseFragment.class, AccountFragment.class,
                Login.class, SyncWizard.class,Idea.class,
                AccountsDetail.class, AboutFragment.class, UserProfile.class
        })
public class FragmentModule {

    private Fragment mFragment;
    private Injector mInjector;

    public FragmentModule(Fragment fragment, Injector injector) {

        mFragment = fragment;
        mInjector = injector;

    }

    @Provides
    @InjectingFragmentModule.Fragment
    @Singleton
    public AppScope provideAppScope(@InjectingActivityModule.Activity Context activityContext) {
        return new AppScope(activityContext);
    }
}
