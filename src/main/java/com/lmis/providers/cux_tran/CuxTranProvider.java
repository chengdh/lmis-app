package com.lmis.providers.cux_tran;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 14-9-16.
 */

public class CuxTranProvider extends LmisContentProvider {
	public static String CONTENTURI = "CuxTranProvider";
	public static String AUTHORITY = "com.lmis.providers.cux_tran";

	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String contentUri() {
		return CONTENTURI;
	}
}