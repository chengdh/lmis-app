package com.lmis.providers.wf_notification;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 2017/2/17.
 */

public class WfNotificationProvider extends LmisContentProvider {

    public static String CONTENTURI = "WfNotificationProvider";
    public static String AUTHORITY = "com.lmis.providers.wf_notification";

    @Override
    public String authority() {
        return AUTHORITY;
    }

    @Override
    public String contentUri() {
        return CONTENTURI;
    }
}
