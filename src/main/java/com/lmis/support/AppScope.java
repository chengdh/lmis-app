/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */

package com.lmis.support;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.lmis.MainActivity;

/**
 * The Class AppScope.
 */
public class AppScope {

	/** The user. */
	private LmisUser mUser = new LmisUser();

	/** The context. */
	private Context mContext = null;

	public AppScope(Fragment fragment) {
		mContext = (Context) fragment.getActivity();
		mUser = LmisUser.current(mContext);
	}

	/**
	 * Instantiates a new app scope.
	 * 
	 * @param user
	 *            the user
	 * @param context
	 *            the context
	 */
	public AppScope(Context context) {
		mContext = context;
		mUser = LmisUser.current(mContext);
	}

	/**
	 * User.
	 * 
	 * @return the user object
	 */
	public LmisUser User() {
		return mUser;
	}

	/**
	 * Context.
	 * 
	 * @return the main activity
	 */
	public Context context() {
		return mContext;
	}

	public MainActivity main() {
		return (MainActivity) mContext;
	}
}
