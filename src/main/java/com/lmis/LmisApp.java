package com.lmis;

import android.content.Context;

import com.fizzbuzz.android.dagger.InjectingApplication;
import com.lmis.dagger_module.AndroidModule;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by chengdh on 14-6-13.
 * 主程序
 */
public class LmisApp extends InjectingApplication {
    @Override
    protected List<Object> getModules() {
        List<Object> ret =  super.getModules();
        ret.add(new AndroidModule());
        return ret;
    }
}
