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

package com.lmis.addons.idea;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisHelper;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * The Class Idea.
 */
public class Idea extends BaseFragment {
    public static String TAG = "lmis.addons.ieda";
    View mView = null;
    LmisHelper mLmisHelper = null;
    Context mContext = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_idea, container,
                false);
        mView = rootView;
        setHasOptionsMenu(true);
        DrawerListener drawer = (DrawerListener) getActivity();
        drawer.refreshDrawer("idea");
        mContext = this.getActivity();
        IdeaDBHelper db = (IdeaDBHelper)databaseHelper(mContext);
        mLmisHelper = db.getLmisInstance();
        return rootView;
    }

    @Override
    public Object databaseHelper(Context context) {
        return new IdeaDBHelper(context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_idea, menu);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        List<DrawerItem> menu = new ArrayList<DrawerItem>();
        menu.add(new DrawerItem("idea_home", "Idea", true));
        Idea idea = new Idea();
        Bundle args = new Bundle();
        args.putString("key", "idea");
        idea.setArguments(args);
        menu.add(new DrawerItem("idea_home", "Idea", 5, 0, idea));
        return menu;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // handle item selection
        switch (item.getItemId()) {
            case R.id.menu_test_search_read:
                Log.d(TAG, "Idea#onOptionsItemSelected#test_search_read");
                test_search_read();
                return true;
            case R.id.menu_test_call_kw:
                Log.d(TAG, "Idea#onOptionsItemSelected#test_call_kw");
                testCallMethod();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //测试search_read
    private JSONArray test_search_read(){

        try {

            String model = "computer_bill";

            JSONArray fields = new JSONArray();
            fields.put("id");
            fields.put("bill_no");
            fields.put("goods_no");
            fields.put("from_customer_name");
            JSONObject fields_arg = new JSONObject();
            fields_arg.put("fields", fields);

            JSONObject domain = new JSONObject();
            domain.put("completed_eq",0);
            int limit = 5;
            int offset = 0;
            String sortField = "created_at";
            String sortType = "DESC";


            JSONArray ret = mLmisHelper.search_read(model,fields_arg,domain,offset,limit,sortField,sortType);
            TextView tv_result = (TextView) mView.findViewById(R.id.tv_http_test);
            tv_result.setText(ret.toString());
            return ret;

        }
        catch (Exception ex){

        }
        return null;
    }
        //测试search_read
    private JSONArray testCallMethod(){

        try {

            String model = "computer_bill";
            String method = "search";

            JSONArray args = new JSONArray();

            JSONObject arg_1 = new JSONObject();
            arg_1.put("completed_eq",0);
            args.put(arg_1);

            JSONObject ret = mLmisHelper.callMethod(model,method,args,null);
            TextView tv_result = (TextView) mView.findViewById(R.id.tv_http_test);
            tv_result.setText(ret.getJSONArray("result").toString());
            return ret.getJSONArray("result");
        }
        catch (Exception ex){

        }
        return null;
    }
}
