package com.lmis.providers.message;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 14-8-26.
 */
public class MessageProvider extends LmisContentProvider {

    /**
     * Created by chengdh on 14-6-15.
     */

    public static String CONTENTURI = "MessageProvider";
    public static String AUTHORITY = "com.lmis.providers.message";

    @Override
    public String authority() {
        return AUTHORITY;
    }

    @Override
    public String contentUri() {
        return CONTENTURI;
    }
}
