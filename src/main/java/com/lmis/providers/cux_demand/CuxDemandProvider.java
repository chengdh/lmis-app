package com.lmis.providers.cux_demand;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 14-9-16.
 */

public class CuxDemandProvider extends LmisContentProvider {
	public static String CONTENTURI = "CuxDemandProvider";
	public static String AUTHORITY = "com.lmis.providers.cux_demand";

	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String contentUri() {
		return CONTENTURI;
	}
}