package com.lmis.addons.inventory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisValues;
import com.lmis.receivers.ScanBarcodeBroadcastReceiver;
import com.lmis.support.BaseFragment;
import com.lmis.util.ImageUtil;
import com.lmis.util.barcode.BarcodeAlreadyConfirmedException;
import com.lmis.util.barcode.BarcodeDuplicateException;
import com.lmis.util.barcode.BarcodeNotExistsException;
import com.lmis.util.barcode.BarcodeParseSuccessEvent;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.BarcodeRemoveEvent;
import com.lmis.util.barcode.DBException;
import com.lmis.util.barcode.GoodsInfo;
import com.lmis.util.barcode.GoodsInfoAddSuccessEvent;
import com.lmis.util.barcode.GoodsInfoConfirmSuccessEvent;
import com.lmis.util.barcode.InvalidBarcodeException;
import com.lmis.util.barcode.InvalidToOrgException;
import com.lmis.util.barcode.InventoryMoveOpType;
import com.lmis.util.barcode.ScandedBarcodeChangeEvent;
import com.lmis.util.barcode.ScandedBarcodeConfirmChangeEvent;
import com.lmis.util.barcode.ScannerSuccessEvent;
import com.lmis.util.controls.ExcludeAccessOrgSearchableSpinner;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;
import com.lmis.util.event.StartMainFragmentEvent;
import com.rajasharan.widget.SearchableSpinner;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-9-14.
 */
public class FragmentScanBarcode extends BaseFragment implements SearchableSpinner.OnSelectionChangeListener {

    public static final String TAG = "FragmentScanBarcode";

    private static final int SCANBARCODE_CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 102;

    @Inject
    Bus mBus;

    @InjectView(R.id.edt_scan_barcode)
    EditText mEdtScanBarcode;

    //货场选择
    @InjectView(R.id.spinner_yards_select)
    Spinner mSpinnerYardsSelect;

    //分公司选择
    @InjectView(R.id.spinner_org_load_orgs_select)
    ExcludeAccessOrgSearchableSpinner mSpinnerOrgLoadOrgsSelect;

    @InjectView(R.id.txv_to_org_display)
    TextView mTxvToOrgDisplay;


    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

    @InjectView(R.id.txv_to_org_name)
    TextView mTxvToOrgName;

    @InjectView(R.id.txv_goods_num)
    TextView mTxvGoodsNum;

    @InjectView(R.id.txv_seq)
    TextView mTxvSeq;

    @InjectView(R.id.txv_barcode)
    TextView mTxvBarcode;


    @InjectView(R.id.btn_sum_goods_num)
    Button mBtnSumGoodsNum;


    @InjectView(R.id.btn_sum_bills_count)
    Button mBtnSumBillsCount;

    @InjectView(R.id.btn_all_scan)
    Button mBtnAllScan;

    @InjectView(R.id.btn_camera)
    ImageButton mBtnCamera;

    @InjectView(R.id.goods_photo)
    ImageView mImageView;

    @InjectView(R.id.btn_delete_img)
    ImageButton mImageButton;

    @InjectView(R.id.layout_goods_exception)
    View mViewGoodsException;

    @InjectView(R.id.edt_note)
    EditText mEdtNote;

    @InjectView(R.id.btn_goods_exception_ok)
    Button mBtnGoodsExceptionOk;

    @InjectView(R.id.btn_goods_exception_cancel)
    Button mBtnGoodsExceptionCancel;

    Bitmap mImg = null;


    View mView = null;
    GoodsInfo mCurGoodsInfo = null;

    public BarcodeParser getmBarcodeParser() {
        return mBarcodeParser;
    }

    public void setmBarcodeParser(BarcodeParser mBarcodeParser) {
        this.mBarcodeParser = mBarcodeParser;
    }

    public LmisDataRow getmInventoryMove() {
        return mInventoryMove;
    }

    public void setmInventoryMove(LmisDataRow mInventoryMove) {
        this.mInventoryMove = mInventoryMove;
    }

    BarcodeParser mBarcodeParser = null;
    LmisDataRow mInventoryMove = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_inventory_move_scan_barcode, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        initData();
        initControl();
        return mView;
    }

    private void initData() {
        Log.d(TAG, "FragmentScanBarcode#initData");
        if (mBarcodeParser.getmMoveId() > 0) {
            //refresh mInventoryMove
            LmisDatabase db = new InventoryMoveDB(scope.context());
            mInventoryMove = db.select(mBarcodeParser.getmMoveId());
            LmisDataRow toOrg = mInventoryMove.getM2ORecord("to_org_id").browse();

            mTxvToOrgDisplay.setText(toOrg.getString("name"));
            mBtnSumGoodsNum.setText(mBarcodeParser.sumConfirmedGoodsCount() + "" + "/" + mBarcodeParser.sumGoodsCount());
            mBtnSumBillsCount.setText(mBarcodeParser.sumBillsCount() + "");


        }

    }

    //打开其他窗口时,要关闭扫描
    @Subscribe
    public void onStartMainFragment(StartMainFragmentEvent evt){
        Log.d(TAG,"onStartMainFragment");
        mBus.unregister(this);
    }

    //打开其他窗口时,要关闭扫描
    @Subscribe
    public void onStartDetailFragment(StartMainFragmentEvent evt){
        Log.d(TAG,"onStartDetailFragment");
        mBus.unregister(this);
    }

    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        mBus.register(this);
    }

    @Subscribe
    public void onScanSuccessEvent(ScannerSuccessEvent evt){
        String barcode = evt.getStrBarcode();
        if (barcode == null || barcode.length() == 0) {
            return;
        }

        try {
            mViewGoodsException.setVisibility(View.GONE);
            mBarcodeParser.addBarcode(barcode);
            if (mInventoryMove == null) {
                LmisDatabase db = new InventoryMoveDB(scope.context());
                mInventoryMove = db.select(mBarcodeParser.getmMoveId());
            }
            DrawerListener drawer = scope.main();
            drawer.refreshDrawer(InventoryMoveList.TAG);

        } catch (InvalidBarcodeException ex) {
            Toast.makeText(scope.context(), "条码格式不正确!", Toast.LENGTH_LONG).show();
        } catch (InvalidToOrgException ex) {
            Toast.makeText(scope.context(), "到货地不匹配!", Toast.LENGTH_LONG).show();
        } catch (BarcodeDuplicateException ex) {
            setUi(ex.getmGoodsInfo());
            Toast.makeText(scope.context(), "该货物条码已扫描!", Toast.LENGTH_LONG).show();
        } catch (DBException e) {
            e.printStackTrace();
        } catch (BarcodeNotExistsException e) {
            Toast.makeText(scope.context(), "货物条码不存在!", Toast.LENGTH_LONG).show();
        } catch (BarcodeAlreadyConfirmedException ex) {
            setUi(ex.getmGoodsInfo());
            Toast.makeText(scope.context(), "该货物条码已确认!", Toast.LENGTH_LONG).show();
        }

        mEdtScanBarcode.setText(barcode);
        mEdtScanBarcode.requestFocus();


    }

    /**
     * 初始化扫描条码tab界面.
     */
    private void initControl() {

        if (mBarcodeParser.getmMoveId() > 0) {

            mSpinnerYardsSelect.setVisibility(View.GONE);
            mSpinnerYardsSelect.setVisibility(View.GONE);
            mTxvToOrgDisplay.setVisibility(View.VISIBLE);
            //refresh mInventoryMove
        } else {

            LmisDataRow toOrg = null;
            mTxvToOrgDisplay.setVisibility(View.GONE);
            //判断是何种操作optType,如果是yard_out操作,则需要选择分公司或分理处,其他操作，都是选择货场yards
            if (mBarcodeParser.getmOpType().equals(InventoryMoveOpType.YARD_OUT)) {
                mSpinnerOrgLoadOrgsSelect.setVisibility(View.VISIBLE);
                toOrg = mSpinnerOrgLoadOrgsSelect.getSelectedOrg();
            } else {
                mSpinnerOrgLoadOrgsSelect.setVisibility(View.GONE);
                mSpinnerYardsSelect.setVisibility(View.VISIBLE);
                toOrg = (LmisDataRow) mSpinnerYardsSelect.getSelectedItem();

            }
            mBarcodeParser.setmToOrgID(toOrg.getInt("id"));

        }
        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurGoodsInfo != null && mCurGoodsInfo.getmID() != -1) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, SCANBARCODE_CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                } else {
                    Toast.makeText(scope.context(), "请先扫描条码!", Toast.LENGTH_LONG).show();
                }
            }
        });
        mBtnGoodsExceptionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurGoodsInfo != null && mCurGoodsInfo.getmID() != -1) {
                    saveException();
                } else {
                    Toast.makeText(scope.context(), "请先扫描条码!", Toast.LENGTH_LONG).show();
                }

            }
        });
        mBtnGoodsExceptionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewGoodsException.setVisibility(View.GONE);
            }
        });


        mSpinnerOrgLoadOrgsSelect.setOnSelectionChangeListener(this);
//        mSpinnerOrgLoadOrgsSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                LmisDataRow toOrg = (LmisDataRow) mSpinnerOrgLoadOrgsSelect.getSelectedItem();
//                mBarcodeParser.setmToOrgID(toOrg.getInt("id"));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        mSpinnerYardsSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LmisDataRow toOrg = (LmisDataRow) mSpinnerYardsSelect.getSelectedItem();
                mBarcodeParser.setmToOrgID(toOrg.getInt("id"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mEdtScanBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {

                Log.d(TAG, "scan barcode on text changed");

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEdtScanBarcode.setSelectAllOnFocus(true);
        mEdtScanBarcode.setFocusableInTouchMode(true);
        mEdtScanBarcode.requestFocus();

        mBtnAllScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurGoodsInfo == null)
                    return;

                try {
                    mBarcodeParser.setAllGoodsScaned(mCurGoodsInfo.getmBarcode());
                    Toast.makeText(scope.context(), "整单已扫描!", Toast.LENGTH_LONG).show();
                } catch (InvalidBarcodeException e) {
                    Toast.makeText(scope.context(), "条码格式不正确!", Toast.LENGTH_LONG).show();
                } catch (BarcodeNotExistsException e) {
                    Toast.makeText(scope.context(), "货物条码不存在!", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    /**
     * 删除条形码.
     */
    private void removeBarcode() {
        try {
            String barcode = mEdtScanBarcode.getText().toString();
            mBarcodeParser.removeBarcode(barcode);
        } catch (InvalidBarcodeException ex) {
            Toast.makeText(scope.context(), "条码格式错误!", Toast.LENGTH_LONG).show();
        } catch (BarcodeNotExistsException ex) {
            Toast.makeText(scope.context(), "该条码不存在!", Toast.LENGTH_LONG).show();
        }
        clearUI();
    }

    /**
     * Clear uI.
     */
    private void clearUI() {
        mEdtScanBarcode.setText("");
        mTxvBillNo.setText("");
        mTxvToOrgName.setText("");
        mTxvGoodsNum.setText("");
        mTxvBarcode.setText("");
        mTxvSeq.setText("");
        mImageView.setImageBitmap(null);
        mViewGoodsException.setVisibility(View.GONE);
        mEdtScanBarcode.requestFocus();
    }

    /**
     * 条形码正确解析event.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onBarcodeParseSuccessEvent(BarcodeParseSuccessEvent evt) {
        setUi(evt.getmGoodsInfo());

    }

    private void setUi(GoodsInfo gs) {
        if (gs == null) {
            return;
        }
        mCurGoodsInfo = gs;
        mTxvBillNo.setText(mCurGoodsInfo.getmBillNo());
        mTxvToOrgName.setText(mCurGoodsInfo.getmToOrgName());
        mTxvGoodsNum.setText(mCurGoodsInfo.getmGoodsNum() + "");
        mTxvSeq.setText(mCurGoodsInfo.getmSeq() + "");
        mTxvBarcode.setText(mCurGoodsInfo.getmBarcode());
        mViewGoodsException.setVisibility(View.VISIBLE);
        if (mCurGoodsInfo != null && mCurGoodsInfo.getmID() > -1) {
            InventoryLineDB inventoryLineDB = new InventoryLineDB(scope.context());
            LmisDataRow line = inventoryLineDB.select(mCurGoodsInfo.getmID());
            byte[] image = (byte[]) line.get("goods_photo_1");
            if (image != null) {
                mImg = ImageUtil.getImage(image);
                mImageView.setImageBitmap(mImg);
            }

            String note = line.getString("note");
            if (note == null || note.isEmpty() || note.equals("false") || note.equals("null")) {
                note = "破损/包装破/丢失";
            }
            mEdtNote.setText(note);
        }
        mEdtScanBarcode.requestFocus();
    }

    /**
     * On scaned barcode changed event.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onScanedBarcodeChangedEvent(ScandedBarcodeChangeEvent evt) {
        mBtnSumGoodsNum.setText(evt.getmSumGoodsNum() + "");
        mBtnSumBillsCount.setText(evt.getmSumBillsCount() + "");
    }

    @Subscribe
    public void onGoodsInfoConfirmSuccessEvent(GoodsInfoConfirmSuccessEvent evt) {
        mCurGoodsInfo = evt.getmGoodsInfo();
    }

    /**
     * 条码确认发生变化.
     *
     * @param evt the evt
     */

    @Subscribe
    public void onScanedBarcodeConfirmChangedEvent(ScandedBarcodeConfirmChangeEvent evt) {
        mBtnSumGoodsNum.setText(evt.getmConfirmGoodsCount() + "/" + evt.getmGoodsCount());
    }

    /**
     * 货物信息正确添加.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onGoodsInfoAddSuccessEvent(GoodsInfoAddSuccessEvent evt) {
        mCurGoodsInfo = evt.getmGoodsInfo();
    }

    @Subscribe
    public void onBarcodeRemoveEvent(BarcodeRemoveEvent evt) {
        Toast.makeText(scope.context(), "货物已删除!", Toast.LENGTH_SHORT).show();
        clearUI();
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public Object databaseHelper(Context context) {
        return null;
    }

    @Override
    public void onSelectionChanged(String s) {
        LmisDataRow org = mSpinnerOrgLoadOrgsSelect.getSelectedOrg();
        mBarcodeParser.setmToOrgID(org.getInt("id"));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANBARCODE_CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                if (data.hasExtra("data")) {
                    Bitmap thumbnail = data.getParcelableExtra("data");
                    mImg = thumbnail;
                    mImageView.setImageBitmap(thumbnail);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }

    private void saveException() {
        InventoryLineDB inventoryLineDB = new InventoryLineDB(scope.context());

        LmisValues vals = new LmisValues();
        String note = mEdtNote.getText().toString();
        mCurGoodsInfo.setmNote(note);
        if (mImg != null) {
            byte[] b = ImageUtil.getBytes(mImg);
            vals.put("goods_photo_1", b);
        }
        if (note != null && !note.isEmpty()) {
            vals.put("note", note);

        }
        vals.put("state", "goods_exception");
        inventoryLineDB.update(vals, mCurGoodsInfo.getmID());
        Toast.makeText(scope.context(), "异常信息已保存!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG,"onDetach");
    }
}
