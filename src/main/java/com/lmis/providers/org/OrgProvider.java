package com.lmis.providers.org;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 14-6-15.
 */
public class OrgProvider extends LmisContentProvider {

    public static String CONTENTURI = "OrgProvider";
    public static String AUTHORITY = "com.lmis.providers.org";

    @Override
    public String authority() {
        return AUTHORITY;
    }

    @Override
    public String contentUri() {
        return CONTENTURI;
    }
}
