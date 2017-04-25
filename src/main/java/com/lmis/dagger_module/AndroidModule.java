package com.lmis.dagger_module;

/**
 * Created by chengdh on 14-6-13.
 */

import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;

import com.fizzbuzz.android.dagger.InjectingApplication.InjectingApplicationModule.Application;
import com.lmis.LmisApp;
import com.lmis.config.ModulesConfig;
import com.lmis.support.LmisUser;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Module for all Android related provisions
 */
@Module(complete = false, library = true, injects = LmisApp.class)

public class AndroidModule {
    @Provides
    SharedPreferences provideDefaultSharedPreferences(@Application final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    PackageInfo providePackageInfo(@Application Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    TelephonyManager provideTelephonyManager(@Application Context context) {
        return getSystemService(context, Context.TELEPHONY_SERVICE);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSystemService(Context context, String serviceConstant) {
        return (T) context.getSystemService(serviceConstant);
    }

    @Provides
    InputMethodManager provideInputMethodManager(@Application final Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Provides
    ApplicationInfo provideApplicationInfo(@Application final Context context) {
        return context.getApplicationInfo();
    }

    @Provides
    AccountManager provideAccountManager(@Application final Context context) {
        return AccountManager.get(context);
    }

    @Provides
    ClassLoader provideClassLoader(@Application final Context context) {
        return context.getClassLoader();
    }
    @Provides
    ConnectivityManager provideConnectivityManager(@Application Context context) {
        return getSystemService(context, Context.CONNECTIVITY_SERVICE);
    }

    @Provides
    NotificationManager provideNotificationManager(@Application final Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    @Provides
    ModulesConfig provideModuleConfig(){
        return new ModulesConfig();

    }
    @Provides
    @Application
    public LmisUser proviceCurrentUser(@Application final Context context){
        return LmisUser.current(context);
    }

    /**
     * Provide 事件bus对象.
     *
     * @return the bus
     */
    @Provides
    @Singleton
    public Bus provideBus(){
        return new Bus();
    }

}
