/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.lmis.auth;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.lmis.support.LmisUser;
import com.lmis.support.SyncValue;
import com.lmis.config.SyncWizardValues;

/**
 * The Class LmisAccountManager.
 */
public class LmisAccountManager {

    /**
     * The Constant PARAM_AUTHTOKEN_TYPE.
     */
    private static final String PARAM_AUTHTOKEN_TYPE = "com.lmis.auth";
    private static LmisUser current_user = null;

    /**
     * Fetch all accounts.
     *
     * @param context the context
     * @return the list
     */
    public static List<LmisUser> fetchAllAccounts(Context context) {
        List<LmisUser> userObjs = null;

        AccountManager accMgr = AccountManager.get(context);
        Account[] accounts = accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE);
        if (accounts.length > 0) {
            userObjs = new ArrayList<LmisUser>();
            for (Account account : accounts) {
                LmisUser userobj = new LmisUser();
                userobj.fillFromAccount(accMgr, account);
                userObjs.add(userobj);
            }
        }
        return userObjs;
    }

    /**
     * hasAccounts
     * <p/>
     * checks for availability of any account for Lmis
     *
     * @param context
     * @return true if there is any account related to type
     */
    public static boolean hasAccounts(Context context) {
        boolean flag = false;
        AccountManager accMgr = AccountManager.get(context);
        if (accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE).length > 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * Creates the account.
     *
     * @param context    the context
     * @param bundleData the bundle data
     * @return true, if successful
     */
    public static boolean createAccount(Context context, LmisUser bundleData) {
        AccountManager accMgr = null;
        accMgr = AccountManager.get(context);
        String accountType = PARAM_AUTHTOKEN_TYPE;
        String password = String.valueOf(bundleData.getPassword());
        String accountName = bundleData.getAndroidName();
        Account account = new Account(accountName, accountType);
        Bundle bundle = bundleData.getAsBundle();
        return accMgr.addAccountExplicitly(account, password, bundle);
    }

    /**
     * Checks if is any currentUser.
     *
     * @param context the context
     * @return true, if is any currentUser
     */
    public static boolean isAnyUser(Context context) {
        boolean flag = false;
        if (current_user != null) {
            flag = true;
        } else {
            List<LmisUser> accounts = LmisAccountManager.fetchAllAccounts(context);
            if (accounts != null) {
                for (LmisUser user : accounts) {
                    if (user.isIsactive()) {
                        flag = true;
                        current_user = user;
                        break;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * Current currentUser.
     *
     * @param context the context
     * @return the currentUser object
     */
    public static LmisUser currentUser(Context context) {
        if (current_user != null) {
            return current_user;
        } else {
            if (LmisAccountManager.isAnyUser(context)) {
                List<LmisUser> accounts = LmisAccountManager.fetchAllAccounts(context);
                for (LmisUser user : accounts) {
                    if (user.isIsactive()) {
                        return user;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the account detail.
     *
     * @param context  the context
     * @param username the username
     * @return the account detail
     */
    public static LmisUser getAccountDetail(Context context, String username) {

        List<LmisUser> allAccounts = LmisAccountManager.fetchAllAccounts(context);
        for (LmisUser user : allAccounts) {
            if (user.getAndroidName().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Gets the account.
     *
     * @param context  the context
     * @param username the username
     * @return the account
     */
    public static Account getAccount(Context context, String username) {
        AccountManager accMgr = AccountManager.get(context);
        Account[] accounts = accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE);

        Account userAc = null;
        for (Account account : accounts) {

            LmisUser userData = new LmisUser();
            userData.fillFromAccount(accMgr, account);

            if (userData != null) {
                if (userData.getAndroidName().equals(username)) {
                    userAc = account;
                }
            }
        }
        return userAc;

    }

    /**
     * Logout currentUser.
     *
     * @param context  the context
     * @param username the username
     * @return true, if successful
     */
    public static boolean logoutUser(Context context, String username) {
        boolean flag = false;
        LmisUser user = LmisAccountManager.getAccountDetail(context, username);
        Account account = LmisAccountManager.getAccount(context, user.getAndroidName());
        if (user != null) {
            if (cancelAllSync(account)) {
                AccountManager accMgr = AccountManager.get(context);
                user.setIsactive(false);

                accMgr.setUserData(account, "isactive", "0");
                flag = true;
                current_user = null;
            }
        }
        return flag;

    }

    public static boolean changeDefaultOrgId(Context context, int newOrgId) {
        boolean flag = false;
        LmisUser user = currentUser(context);
        Account account = LmisAccountManager.getAccount(context, user.getAndroidName());
        if (user != null) {
            AccountManager accMgr = AccountManager.get(context);
            user.setDefault_org_id(newOrgId);
            accMgr.setUserData(account, "default_org_id", newOrgId + "");
            return true;
        }
        return false;
    }

    private static boolean cancelAllSync(Account account) {
        SyncWizardValues syncVals = new SyncWizardValues();
        boolean flag = false;
        for (SyncValue sync : syncVals.syncValues()) {
            ContentResolver.cancelSync(account, sync.getAuthority());
            flag = true;
        }
        return flag;
    }

    /**
     * Login currentUser.
     *
     * @param context  the context
     * @param username the username
     * @return the currentUser object
     */
    public static LmisUser loginUser(Context context, String username) {
        LmisUser userData = null;

        List<LmisUser> allAccounts = LmisAccountManager.fetchAllAccounts(context);
        for (LmisUser user : allAccounts) {
            LmisAccountManager.logoutUser(context, user.getAndroidName());
        }

        userData = LmisAccountManager.getAccountDetail(context, username);
        if (userData != null) {
            AccountManager accMgr = AccountManager.get(context);

            accMgr.setUserData(LmisAccountManager.getAccount(context, userData.getAndroidName()), "isactive", "true");
        }
        current_user = userData;
        return userData;
    }

    /**
     * Removes the account.
     *
     * @param context  the context
     * @param username the username
     */
    public static void removeAccount(Context context, String username) {
        AccountManager accMgr = AccountManager.get(context);
        accMgr.removeAccount(LmisAccountManager.getAccount(context, username), null, null);

    }

    public static boolean updateAccountDetails(Context context, LmisUser userObject) {

        boolean flag = false;
        LmisUser user = LmisAccountManager.getAccountDetail(context,
                userObject.getAndroidName());
        Bundle userBundle = userObject.getAsBundle();
        if (user != null) {
            AccountManager accMgr = AccountManager.get(context);
            for (String key : userBundle.keySet()) {
                accMgr.setUserData(LmisAccountManager.getAccount(context, user.getAndroidName()), key, userBundle.getString(key));
            }

            flag = true;
        }
        return flag;
    }
}
