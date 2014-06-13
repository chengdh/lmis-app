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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDBHelper;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

public class IdeaDBHelper extends LmisDatabase {
    Context mContext = null;

    public IdeaDBHelper(Context context) {
        super(context);
        mContext = context;
    }

    class IdeaCategory extends LmisDatabase implements LmisDBHelper {
        Context mContext = null;

        public IdeaCategory(Context context) {
            super(context);
            mContext = context;

        }

        @Override
        public String getModelName() {
            return "idea.category";
        }

        @Override
        public List<LmisColumn> getModelColumns() {
            List<LmisColumn> cols = new ArrayList<LmisColumn>();
            cols.add(new LmisColumn("name", "Name", LmisFields.varchar(50)));
            return cols;
        }

    }

    class IdeaUsers extends LmisDatabase implements LmisDBHelper {
        Context mContext = null;

        public IdeaUsers(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public String getModelName() {
            return "idea.users";
        }

        @Override
        public List<LmisColumn> getModelColumns() {
            List<LmisColumn> cols = new ArrayList<LmisColumn>();
            cols.add(new LmisColumn("name", "Name", LmisFields.varchar(50)));
            cols.add(new LmisColumn("city", "city", LmisFields.varchar(50)));
            cols.add(new LmisColumn("user_type", "Type", LmisFields
                    .manyToOne(new IdeaUserType(mContext))));
            return cols;
        }

    }

    class IdeaUserType extends LmisDatabase implements LmisDBHelper {
        Context mContext = null;

        public IdeaUserType(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public String getModelName() {
            return "idea.user.type";
        }

        @Override
        public List<LmisColumn> getModelColumns() {
            List<LmisColumn> cols = new ArrayList<LmisColumn>();
            cols.add(new LmisColumn("type", "Name", LmisFields.varchar(50)));
            return cols;
        }

    }

    @Override
    public String getModelName() {
        return "idea.idea";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> columns = new ArrayList<LmisColumn>();

        columns.add(new LmisColumn("name", "Name", LmisFields.varchar(64)));
        columns.add(new LmisColumn("description", "Description", LmisFields.text()));
        columns.add(new LmisColumn("category_id", "Idea Category", LmisFields
                .manyToOne(new IdeaCategory(mContext))));
        columns.add(new LmisColumn("user_ids", "Idea Users", LmisFields
                .manyToMany(new IdeaUsers(mContext))));
        return columns;
    }

}
