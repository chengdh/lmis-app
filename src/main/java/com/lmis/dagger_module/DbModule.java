package com.lmis.dagger_module;

import android.content.Context;

import com.fizzbuzz.android.dagger.InjectingActivityModule;
import com.lmis.addons.inventory.InventoryLineDB;
import com.lmis.addons.inventory.InventoryMoveDB;

import dagger.Module;
import dagger.Provides;

/**
 * Created by chengdh on 14-7-2.
 */
@Module(complete = false,library = true)
public class DbModule {
    /**
     * Provide inventory move dB.
     * 生成移库db
     *
     * @param context the context
     * @return the inventory move dB
     */
    @Provides
    public InventoryMoveDB provideInventoryMoveDB(@InjectingActivityModule.Activity Context context){
        return new InventoryMoveDB(context);
    }
    @Provides
    public InventoryLineDB provideInventoryLineDB(@InjectingActivityModule.Activity Context context){
        return new InventoryLineDB(context);

    }
}
