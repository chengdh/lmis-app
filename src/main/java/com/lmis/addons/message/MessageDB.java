package com.lmis.addons.message;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 14-8-26.
 */
public class MessageDB extends LmisDatabase {

    public MessageDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "message";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        cols.add(new LmisColumn("id", "id", LmisFields.integer(16),true));
        cols.add(new LmisColumn("title", "Title", LmisFields.varchar(60),true));
        cols.add(new LmisColumn("body", "Body", LmisFields.text(),true));
        cols.add(new LmisColumn("type", "Type", LmisFields.varchar(20),true));
        cols.add(new LmisColumn("is_secure", "Is Secure", LmisFields.varchar(20),true));
        cols.add(new LmisColumn("org_id", "Org ID", LmisFields.integer(16),true));
        cols.add(new LmisColumn("org_name", "Org Name", LmisFields.varchar(60),true));
        cols.add(new LmisColumn("is_active", "Is Active", LmisFields.varchar(20),true));
        cols.add(new LmisColumn("user_id", "User ID", LmisFields.integer(16),true));
        cols.add(new LmisColumn("publish_date", "Publish Date", LmisFields.varchar(20),true));
        cols.add(new LmisColumn("publisher_id", "Publisher ID", LmisFields.integer(16),true));
        cols.add(new LmisColumn("publisher_name", "Publisher Name", LmisFields.varchar(60),true));
        cols.add(new LmisColumn("doc_no", "Document No", LmisFields.varchar(60),true));
        cols.add(new LmisColumn("attach_file_name", "Attach File Name", LmisFields.varchar(60),true));
        cols.add(new LmisColumn("attach_url", "Attach URL", LmisFields.varchar(60),true));

        //是否已查看
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //查看时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));


        return cols;
    }
}
