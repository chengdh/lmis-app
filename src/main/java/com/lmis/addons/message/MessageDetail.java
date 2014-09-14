package com.lmis.addons.message;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.lmis.LmisArguments;
import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisHelper;
import com.lmis.orm.LmisValues;
import com.lmis.support.BaseFragment;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-8-27.
 */
public class MessageDetail extends BaseFragment {

    public static final String TAG = "MessageDetail";

    @InjectView(R.id.txv_title)
    TextView mTxvTitle;

    @InjectView(R.id.txv_org_name)
    TextView mTxvOrgName;

    @InjectView(R.id.txv_publish_date)
    TextView mTxvPublishDate;

    @InjectView(R.id.wv_body)
    WebView mWvBody;

    int mMessageId = -1;

    View mView = null;

    MessageViewCountUpdater mUpdater = null;

    @Override
    public Object databaseHelper(Context context) {
        return new MessageDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_message_detail, container, false);

        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    private void init() {
        Log.d(TAG, "MessageDetail#init");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMessageId = bundle.getInt("message_id");
            MessageDB db = (MessageDB) databaseHelper(scope.context());
            LmisDataRow row = db.select(mMessageId);
            mTxvTitle.setText(row.getString("title"));
            mTxvOrgName.setText(row.getString("org_name"));
            mTxvPublishDate.setText(row.getString("publish_date").substring(0, 10));
            String body = row.getString("body");
            //FIXME 此处参考了
            //http://wj495175289.blog.163.com/blog/static/1620826662012364512840/
            //对webview处理乱码的方法
            mWvBody.loadData(body, "text/html;charset=UTF-8", null);

            //更新查看次数
            updateViewCount();

        }
    }

    /**
     * 更新查看次数.
     */
    private void updateViewCount() {
        if (mUpdater != null) {
            mUpdater.cancel(true);
            mUpdater = null;
        }
        mUpdater = new MessageViewCountUpdater();
        mUpdater.execute((Void) null);
    }

    private class MessageViewCountUpdater extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "MessageViewCountUpdater#doInBackground");
            MessageDB db = (MessageDB) databaseHelper(scope.context());
            LmisHelper helper = db.getLmisInstance();
            LmisArguments arguments = new LmisArguments();
            int currentUserId = scope.currentUser().getUser_id();
            arguments.add(mMessageId);
            arguments.add(currentUserId);

            helper.call_kw("update_view_count", arguments);
            LmisValues updateValues = new LmisValues();
            updateValues.put("processed", true);
            updateValues.put("process_datetime", new Date());
            db.update(updateValues, mMessageId);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(TAG, "MessageViewCountUpdater#onPostExecute");
            DrawerListener drawer = scope.main();
            drawer.refreshDrawer(MessageList.TAG);
            mUpdater = null;
        }

    }
}
