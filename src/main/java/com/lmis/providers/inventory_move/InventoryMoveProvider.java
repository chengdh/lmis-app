package com.lmis.providers.inventory_move;

import com.lmis.support.provider.LmisContentProvider;

/**
 * Created by chengdh on 14-9-16.
 */

public class InventoryMoveProvider extends LmisContentProvider {
	public static String CONTENTURI = "InventoryMoveProvider";
	public static String AUTHORITY = "com.lmis.providers.inventory_move";

	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String contentUri() {
		return CONTENTURI;
	}
}