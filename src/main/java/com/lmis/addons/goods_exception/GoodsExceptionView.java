package com.lmis.addons.goods_exception;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.ImageUtil;
import com.lmis.util.controls.ExceptionTypeSpinner;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-9-12.
 */
public class GoodsExceptionView extends BaseFragment {
    public static final String TAG = "GoodsExceptionView";

    @InjectView(R.id.txv_bill_date)
    TextView mTxvBillDate;

    @InjectView(R.id.txv_org_name)
    TextView mTxvOrgName;

    @InjectView(R.id.txv_exception_type)
    TextView mTxvExceptType;

    @InjectView(R.id.txv_except_num)
    TextView mTxvExceptNum;

    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

    @InjectView(R.id.txv_note)
    TextView mTxvNote;

    @InjectView(R.id.img_photo)
    ImageView mPhoto;

    View mView = null;
    int mGoodsExceptionID = -1;
    LmisDataRow mGoodsException = null;

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_goods_exception_view, container, false);
        ButterKnife.inject(this, mView);
        init();
        return mView;
    }

    private void init() {
        Log.d(TAG, "GoodsExceptionView#init");
        Bundle bundle = getArguments();
        if(bundle != null){
            mGoodsExceptionID = bundle.getInt("goods_exception_id");
            GoodsExceptionDB db = new GoodsExceptionDB(scope.context());
            mGoodsException = db.select(mGoodsExceptionID);
            String billDate = mGoodsException.getString("bill_date");
            LmisDataRow org = mGoodsException.getM2ORecord("org_id").browse();
            String orgName = org.getString("name");
            String exceptType = mGoodsException.getString("exception_type");
            String exceptTypeDes = "";
            for (Object type : ExceptionTypeSpinner.exceptionTypes()){
                String[] arr = (String[]) type;
                if(arr[0].equals(exceptType) ){
                    exceptTypeDes = arr[1];
                }

            }
            int num = mGoodsException.getInt("except_num");
            Bitmap photo = null;
            String note = mGoodsException.getString("note");
            byte[] photoByte = (byte[])mGoodsException.get("photo");
            if(photoByte != null){
                photo = ImageUtil.getImage(photoByte);
                mPhoto.setImageBitmap(photo);
            }
            String billNo = mGoodsException.getString("bill_no");
            String goodslNo = mGoodsException.getString("goods_no");
            mTxvBillDate.setText(billDate);
            mTxvOrgName.setText(orgName);
            mTxvExceptType.setText(exceptTypeDes);
            mTxvExceptNum.setText(num  + "件");
            mTxvBillNo.setText(billNo + "/" + goodslNo);
            mTxvNote.setText(note);
        }

    }
}
