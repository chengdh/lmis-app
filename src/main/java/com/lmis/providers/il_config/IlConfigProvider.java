package com.lmis.providers.il_config;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 14/11/26.
 */
public class IlConfigProvider extends LmisContentProvider {
    public static final String TAG = "LmisContentProvider";
    public static String CONTENTURI = "IlConfigProvider";
    public static String AUTHORITY = "com.lmis.providers.il_config";


    @Override
    public String authority() {
        return AUTHORITY;
    }

    @Override
    public String contentUri() {
        return CONTENTURI;
    }
}
