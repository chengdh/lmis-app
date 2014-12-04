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
package com.lmis.base.account;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.auth.LmisAccountManager;
import com.lmis.orm.LmisHelper;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisUser;
import com.lmis.util.Base64Helper;
import com.lmis.util.controls.LmisTextView;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UserProfile extends BaseFragment {

    public static final String TAG = "UserProfile";

    View rootView = null;
    @InjectView(R.id.txvUserLoginName)
    LmisTextView txvUserLoginName;

    @InjectView(R.id.txvUserName)
    LmisTextView txvUsername;

    @InjectView(R.id.txvServerUrl)
    LmisTextView txvServerUrl;

    @InjectView(R.id.imgUserProfilePic)
    ImageView imgUserPic;

    EditText password;
    AlertDialog.Builder builder = null;
    Dialog dialog = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_account_user_profile,
                container, false);
        scope.main().setTitle("Lmis currentUser Profile");
        ButterKnife.inject(this, rootView);

        setupView();
        return rootView;
    }

    private void setupView() {

        imgUserPic = null;
        String avatar = "false";
        if (!avatar.equals("false")) {
            Log.d(TAG, "currentUser avata : " + avatar);
            imgUserPic.setImageBitmap(Base64Helper.getBitmapImage(scope.context(), avatar));
        }
        txvUserLoginName.setText(scope.currentUser().getAndroidName());

        txvUsername.setText(scope.currentUser().getUsername());

        txvServerUrl.setText(scope.currentUser().getHost());

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_account_user_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_account_user_profile_sync:
                dialog = inputPasswordDialog();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private Dialog inputPasswordDialog() {
        builder = new Builder(scope.context());
        password = new EditText(scope.context());
        password.setTransformationMethod(PasswordTransformationMethod
                .getInstance());
        builder.setTitle("Enter Password").setMessage("Provide your password")
                .setView(password);
        builder.setPositiveButton("Update Info", new OnClickListener() {
            public void onClick(DialogInterface di, int i) {
                LmisUser userData = null;
                try {
                    LmisHelper openerp = new LmisHelper(scope.context(), scope
                            .currentUser().getHost());

                    userData = openerp.login(scope.currentUser().getUsername(),
                            password.getText().toString(), scope.currentUser().getHost());
                } catch (Exception e) {
                }
                if (userData != null) {
                    if (LmisAccountManager.updateAccountDetails(
                            scope.context(), userData)) {
                        Toast.makeText(getActivity(), "Infomation Updated.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Invalid Password !",
                            Toast.LENGTH_LONG).show();
                }
                setupView();
                dialog.cancel();
                dialog = null;
            }
        });
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface di, int i) {
                dialog.cancel();
                dialog = null;
            }
        });
        return builder.create();

    }

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }
}
