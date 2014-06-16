package com.lmis.providers.user_org;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 14-6-15.
 */
public class UserOrgProvider extends LmisContentProvider {

    public static String CONTENTURI = "UserOrgProvider";
    public static String AUTHORITY = "com.lmis.providers.user_org";

    @Override
    public String authority() {
        return AUTHORITY;
    }

    @Override
    public String contentUri() {
        return CONTENTURI;
    }
}