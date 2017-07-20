package com.lmis.dagger_module;

import android.content.Context;

import com.fizzbuzz.android.dagger.InjectingActivityModule;
import com.lmis.Lmis;
import com.lmis.addons.inventory.InventoryLineDB;
import com.lmis.addons.inventory.InventoryMoveDB;
import com.lmis.addons.scan_header.ScanHeaderDB;
import com.lmis.addons.scan_header.ScanLineDB;
import com.lmis.base.org.OrgDB;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import dagger.Module;
import dagger.Provides;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by chengdh on 14-7-2.
 */
@Module(complete = false, library = true)
public class DbModule {
    /**
     * Provide inventory move dB.
     * 生成移库db
     *
     * @param context the context
     * @return the inventory move dB
     */
    @Provides
    public InventoryMoveDB provideInventoryMoveDB(@InjectingActivityModule.Activity Context context) {
        return new InventoryMoveDB(context);
    }

    @Provides
    public InventoryLineDB provideInventoryLineDB(@InjectingActivityModule.Activity Context context) {
        return new InventoryLineDB(context);

    }

    @Provides
    public ScanHeaderDB provideScanHeaderDB(@InjectingActivityModule.Activity Context context) {
        return new ScanHeaderDB(context);
    }

    @Provides
    public ScanLineDB provideScanLineDB(@InjectingActivityModule.Activity Context context) {
        return new ScanLineDB(context);

    }
    @Provides
    @AccessLmis
    public Lmis provideLmis(@InjectingActivityModule.Activity Context context) {
        return new OrgDB(context).getLmisInstance();
    }
    //当前用户可访问机构
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface AccessLmis{
    }
}
