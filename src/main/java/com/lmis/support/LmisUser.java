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
package com.lmis.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import com.lmis.auth.LmisAccountManager;

// TODO: Auto-generated Javadoc

/**
 * The Class UserObject.
 */
public class LmisUser {

    /**
     * The username.
     */
    private String username;

    /**
     * The user_id.
     */
    private int user_id;

    /**
     * The isactive.
     */
    private boolean is_active;

    /**
     * The host.
     */
    private String host;

    /**
     * The android_name.
     */
    private String android_name;

    /**
     * The password.
     */
    private String password;

    /**
     * isactive
     */
    private Boolean isactive;


    /**
     * The default_org_id.
     */
    private int default_org_id;

    /**
     * The real name
     */
    private String real_name;

    /**
     * The auth_token
     */
    private String authentication_token;

    /**
     * Gets the data as bundle.
     *
     * @return the as bundle
     */
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("username", this.getUsername());
        bundle.putString("user_id", this.getUser_id() + "");
        bundle.putString("isactive", String.valueOf(this.isIsactive()));
        bundle.putString("host", this.getHost());
        bundle.putString("android_name", this.getAndroidName());
        bundle.putString("password", this.getPassword());
        bundle.putString("default_org_id", this.getDefault_org_id() + "");
        bundle.putString("real_name", this.getReal_name());
        bundle.putString("authentication_token", this.getAuthentication_token());
        return bundle;
    }


    /**
     * Sets the data from bundle.
     *
     * @param data the new from bundle
     */
    public void setFromBundle(Bundle data) {
        this.setUsername(data.getString("username"));
        this.setUser_id(Integer.parseInt(data.getString("user_id")));
        this.setIsactive(data.getBoolean("isactive"));
        this.setHost(data.getString("host"));
        this.setAndroidName(data.getString("android_name"));
        this.setPassword(data.getString("password"));
        this.setReal_name(data.getString("real_name"));
        this.setDefault_org_id(Integer.parseInt(data.getString("default_org_id")));
        this.setAuthentication_token(data.getString("authentication_token"));
    }

    /**
     * Fill from account.
     *
     * @param accMgr  the acc mgr
     * @param account the account
     */
    public void fillFromAccount(AccountManager accMgr, Account account) {
        this.setUsername(accMgr.getUserData(account, "username"));
        this.setReal_name(accMgr.getUserData(account, "real_name"));
        this.setUser_id(Integer.parseInt(accMgr.getUserData(account, "user_id")));
        this.setDefault_org_id(Integer.parseInt(accMgr.getUserData(account, "default_org_id")));
        this.setIsactive(Boolean.parseBoolean(accMgr.getUserData(account, "isactive")));
        this.setHost(accMgr.getUserData(account, "host"));
        this.setAndroidName(accMgr.getUserData(account, "android_name"));
        this.setPassword(accMgr.getUserData(account, "password"));
        this.setAuthentication_token(accMgr.getUserData(account, "authentication_token"));
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the android name.
     *
     * @return the android name
     */
    public String getAndroidName() {
        return this.android_name;
    }

    /**
     * Sets the android name.
     *
     * @param android_name the new android name
     */
    public void setAndroidName(String android_name) {
        this.android_name = android_name;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public int getDefault_org_id() {
        return default_org_id;
    }

    public String getReal_name() {
        return real_name;
    }

    public String getAuthentication_token() {
        return authentication_token;
    }


    public void setDefault_org_id(int default_org_id) {
        this.default_org_id = default_org_id;
    }

    public void setReal_name(String real_name) {
        this.real_name = real_name;
    }

    public void setAuthentication_token(String authentication_token) {
        this.authentication_token = authentication_token;
    }

    /**
     * Gets the user_id.
     *
     * @return the user_id
     */
    public int getUser_id() {
        return user_id;
    }

    /**
     * Sets the user_id.
     *
     * @param user_id the new user_id
     */
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }


    /**
     * Checks if is isactive.
     *
     * @return true, if is isactive
     */
    public boolean isIsactive() {
        return isactive;
    }

    /**
     * Sets the isactive.
     *
     * @param isactive the new isactive
     */
    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }


    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param host the new host
     */
    public void setHost(String host) {
        this.host = host;
    }

    public static LmisUser current(Context context) {
        return LmisAccountManager.currentUser(context);
    }
}
