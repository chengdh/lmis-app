package com.lmis.addons.wf_notification;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 2017/2/16.
 */

public class WfNotificationDB extends LmisDatabase {

    public WfNotificationDB(Context context) {
        super(context);
    }

    @Override
    public String getModelName() {
        return "WfNotification";
    }

    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<>();

        cols.add(new LmisColumn("id", "id", LmisFields.integer(16), true));
        cols.add(new LmisColumn("message_type", "message type", LmisFields.varchar(60), true));
        cols.add(new LmisColumn("status", "status", LmisFields.varchar(20), true));
        cols.add(new LmisColumn("from_user", "from user", LmisFields.varchar(20), true));
        cols.add(new LmisColumn("to_user", "to user", LmisFields.varchar(20), true));
        cols.add(new LmisColumn("subject", "subject", LmisFields.text(), true));
        cols.add(new LmisColumn("begin_date", "begin date", LmisFields.varchar(40), true));
        cols.add(new LmisColumn("item_key", "item_key", LmisFields.varchar(60), true));
        cols.add(new LmisColumn("fuser_id", "fuser_id", LmisFields.integer(16), true));
        //是否已查看
        cols.add(new LmisColumn("processed", "processed", LmisFields.varchar(10), false));
        //查看时间
        cols.add(new LmisColumn("process_datetime", "process time", LmisFields.varchar(20), false));

        return cols;
    }
}
