package com.lmis.addons.goods_exception;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.Lmis;
import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisValues;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.util.LmisDate;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Created by chengdh on 14-9-4.
 */
public class GoodsExceptionNew extends BaseFragment {
    public static final String TAG = "GoodsExceptionNew";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    View mView = null;

    @InjectView(R.id.search_view_bill)
    SearchView mSearch;

    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

    @InjectView(R.id.spinner_op_org)
    Spinner mSpinnerOpOrg;

    @InjectView(R.id.spinner_exception_type)
    Spinner mSpinnerExceptionType;

    @InjectView(R.id.edt_except_num)
    EditText mEdtExcepNum;

    @InjectView(R.id.edt_note)
    EditText mEdtNote;

    @InjectView(R.id.btn_camera)
    ImageButton mBtnCamera;

    @InjectView(R.id.img)
    ImageView mImgView;

    BillSearcher mSearcher = null;
    SaveTask mSaveTask = null;
    UploadTask mUploadTask = null;
    int mGoodsExceptonID = -1;
    int mCarryingBillID = -1;
    String mBillNo = null;
    String mGoodsNo = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_goods_exception_new, container, false);
        ButterKnife.inject(this, mView);
        initControls();
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_goods_exception_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_goods_exception_save:
                if (validate()) {
                    mSaveTask = new SaveTask();
                    mSaveTask.execute((Void) null);
                    return true;
                } else {
                    return false;
                }
            case R.id.menu_goods_exception_upload:
                if (validate()) {
                    mUploadTask = new UploadTask();
                    mUploadTask.execute((Void) null);
                    return true;
                } else
                    return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initControls() {
        mSearch.onActionViewExpanded();
        mSearch.setQueryHint("输入运单号查询");
        mEdtExcepNum.setText(1 + "");
        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //注意运单是7位
                if (s != null && s.length() >= 7) {
                    if (mSearcher != null) {
                        mSearcher.cancel(true);
                        mSearcher = null;
                    }
                    mSearcher = new BillSearcher(s);
                    mSearcher.execute((Void) null);

                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    /**
     * 验证数据是否有效.
     *
     * @return the boolean
     */
    private Boolean validate() {
        mEdtExcepNum.setError(null);
        mTxvBillNo.setText(null);
        if (mCarryingBillID == -1) {
            mTxvBillNo.setError("请先查询运单信息!");

        }
        if (Integer.parseInt(mEdtExcepNum.getText().toString()) <= 0) {
            mEdtExcepNum.setError("异常数量应大于0!");
        }
        return true;
    }

    private Boolean save2DB() {
        LmisValues vals = new LmisValues();
        LmisUser currentUser = scope.currentUser();
        vals.put("org_id", currentUser.getDefault_org_id());
        LmisDataRow toOrg = (LmisDataRow) mSpinnerOpOrg.getSelectedItem();
        vals.put("op_org_id", toOrg.getInt("id"));
        vals.put("bill_no",mBillNo);
        vals.put("goods_no",mGoodsNo);
        vals.put("bill_date", LmisDate.getDate());
        String[] exceptType = (String[]) mSpinnerExceptionType.getSelectedItem();
        vals.put("exception_type", exceptType[0]);
        vals.put("exception_num", Integer.parseInt(mEdtExcepNum.getText().toString()));
        vals.put("note", mEdtNote.getText());
        GoodsExceptionDB db = (GoodsExceptionDB) databaseHelper(scope.context());
        if (mGoodsExceptonID == -1) {
            mGoodsExceptonID = (int) db.create(vals);
        } else {
            db.update(vals, mGoodsExceptonID);
        }
        return true;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                if (data.hasExtra("data")) {
                    Bitmap thumbnail = data.getParcelableExtra("data");
                    mImgView.setImageBitmap(thumbnail);

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }

    }

    @Override
    public Object databaseHelper(Context context) {
        return new GoodsExceptionDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {

        return null;
    }

    private class SaveTask extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在保存数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return save2DB();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mSaveTask.cancel(true);
                Toast.makeText(scope.context(), "保存异常信息成功!", Toast.LENGTH_SHORT).show();
                DrawerListener drawer = scope.main();
                drawer.refreshDrawer(GoodsExceptionList.TAG);

            } else {
                Toast.makeText(scope.context(), "保存异常信息失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mSaveTask = null;
        }
    }

    private class BillSearcher extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;
        String mQueryText = "";
        JSONObject ret = null;

        public BillSearcher(String queryText) {
            mQueryText = queryText;
        }

        @Override
        protected void onPreExecute() {
            mCarryingBillID = -1;
            mBillNo = null;
            mGoodsNo = null;
            mTxvBillNo.setText("运单号");
            pdialog = new LmisDialog(getActivity(), false, "正在查找...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            JSONArray args = new JSONArray();
            args.put(mQueryText);
            Lmis instance = ((GoodsExceptionDB) databaseHelper(scope.context())).getLmisInstance();
            try {
                ret = instance.callMethod("carrying_bill", "find_by_bill_no", args, null);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mSearcher.cancel(true);
                try {
                    if (ret.get("result").toString() == "null") {
                        Toast.makeText(scope.context(), "未查到符合条件的运单!", Toast.LENGTH_SHORT).show();
                    } else {
                        JSONObject jsonBill = ret.getJSONObject("result");
                        mCarryingBillID = jsonBill.getInt("id");
                        mGoodsNo = jsonBill.getString("goods_no");
                        mBillNo = jsonBill.getString("bill_no");
                        mTxvBillNo.setText(jsonBill.getString("bill_no") + "/" + jsonBill.getString("goods_no"));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            pdialog.dismiss();
        }
    }

    /**
     * 上传运单数据到服务器.
     */
    private class UploadTask extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在上传数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean ret = true;
            ret = save2DB();
            try {
                ((GoodsExceptionDB) db()).save2server(mGoodsExceptonID);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ret = false;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mUploadTask.cancel(true);
                Toast.makeText(scope.context(), "上传异常数据成功!", Toast.LENGTH_SHORT).show();
                DrawerListener drawer = scope.main();
                drawer.refreshDrawer(GoodsExceptionList.TAG);
                //返回已处理界面
                GoodsExceptionList list = new GoodsExceptionList();
                Bundle arg = new Bundle();
                arg.putString("type", "processed");
                list.setArguments(arg);
                scope.main().startMainFragment(list, true);

            } else {
                Toast.makeText(scope.context(), "上传异常数据失败!", Toast.LENGTH_SHORT).show();
            }
            pdialog.dismiss();
            mUploadTask = null;
        }
    }
}
