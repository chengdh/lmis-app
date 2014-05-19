package com.lmis.util.drawer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.config.ModulesConfig;
import com.lmis.support.Module;
import com.lmis.support.fragment.FragmentHelper;

public class DrawerHelper {
	public static List<DrawerItem> drawerItems(Context context) {
		List<DrawerItem> items = new ArrayList<DrawerItem>();
		for (Module module : new ModulesConfig().modules()) {
			FragmentHelper model = (FragmentHelper) module.getModuleInstance();
			List<DrawerItem> drawerItems = model.drawerMenus(context);
			if (drawerItems != null)
				items.addAll(drawerItems);
		}
		return items;
	}
}
