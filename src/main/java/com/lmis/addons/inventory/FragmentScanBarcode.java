package com.lmis.addons.inventory;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.support.BaseFragment;
import com.lmis.util.barcode.BarcodeDuplicateException;
import com.lmis.util.barcode.BarcodeNotExistsException;
import com.lmis.util.barcode.BarcodeParseSuccessEvent;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.BarcodeRemoveEvent;
import com.lmis.util.barcode.DBException;
import com.lmis.util.barcode.GoodsInfo;
import com.lmis.util.barcode.GoodsInfoAddSuccessEvent;
import com.lmis.util.barcode.InvalidBarcodeException;
import com.lmis.util.barcode.InvalidToOrgException;
import com.lmis.util.barcode.ScandedBarcodeChangeEvent;
import com.lmis.util.barcode.ScandedBarcodeConfirmChangeEvent;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-9-14.
 */
public class FragmentScanBarcode extends BaseFragment {

    public static final String TAG = "FragmentScanBarcode";

    @Inject
    Bus mBus;

    @InjectView(R.id.edt_scan_barcode)
    EditText mEdtScanBarcode;

    @InjectView(R.id.spinner_yards_select)
    Spinner mSpinnerYardsSelect;

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


    View mView = null;

    public BarcodeParser getmBarcodeParser() {
        return mBarcodeParser;
    }

    public void setmBarcodeParser(BarcodeParser mBarcodeParser) {
        this.mBarcodeParser = mBarcodeParser;
    }

    public LmisDataRow getmInventoryOut() {
        return mInventoryOut;
    }

    public void setmInventoryOut(LmisDataRow mInventoryOut) {
        this.mInventoryOut = mInventoryOut;
    }

    BarcodeParser mBarcodeParser = null;
    LmisDataRow mInventoryOut = null;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_inventory_move_scan_barcode, container, false);
        ButterKnife.inject(this, mView);
        mBus.register(this);
        initData();
        initScanTab();
        return mView;
    }

    private void initData() {
        Log.d(TAG, "FragmentScanBarcode#initData");
        if(mBarcodeParser.getmMoveId() > 0) {
            //refresh mInventoryMove
            LmisDatabase db = new InventoryMoveDB(scope.context());
            mInventoryOut = db.select(mBarcodeParser.getmMoveId());
            LmisDataRow toOrg = mInventoryOut.getM2ORecord("to_org_id").browse();
            ArrayAdapter adapter = (ArrayAdapter) mSpinnerYardsSelect.getAdapter();
            int pos = adapter.getPosition(toOrg);
            mSpinnerYardsSelect.setSelection(pos);
            mBtnSumGoodsNum.setText(mBarcodeParser.sumConfirmedGoodsCount() + "" + "/" + mBarcodeParser.sumGoodsCount());
            mBtnSumBillsCount.setText(mBarcodeParser.sumBillsCount() + "");
        }

    }

    /**
     * 初始化扫描条码tab界面.
     */
    private void initScanTab() {

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
                String barcode = mEdtScanBarcode.getText().toString();

                if (barcode.length() == 16) {
                    try {
                        mBarcodeParser.addBarcode(s.toString());
                        if(mInventoryOut == null){
                            LmisDatabase db = new InventoryMoveDB(scope.context());
                            mInventoryOut = db.select(mBarcodeParser.getmMoveId());
                        }
                        mEdtScanBarcode.setText("");
                        DrawerListener drawer = scope.main();
                        drawer.refreshDrawer(InventoryMoveList.TAG);

                    } catch (InvalidBarcodeException ex) {
                        Toast.makeText(scope.context(), "条码格式不正确!", Toast.LENGTH_LONG).show();
                    } catch (InvalidToOrgException ex) {
                        Toast.makeText(scope.context(), "到货地不匹配!", Toast.LENGTH_LONG).show();
                    } catch (BarcodeDuplicateException ex) {
                        Toast.makeText(scope.context(), "该货物条码已扫描!", Toast.LENGTH_LONG).show();
                    } catch (DBException e) {
                        e.printStackTrace();
                    } catch (BarcodeNotExistsException e) {
                        Toast.makeText(scope.context(), "货物条码不存在!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEdtScanBarcode.requestFocus();

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
        mEdtScanBarcode.requestFocus();
    }

    /**
     * 条形码正确解析event.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onBarcodeParseSuccessEvent(BarcodeParseSuccessEvent evt) {
        GoodsInfo gs = evt.getmGoodsInfo();
        mTxvBillNo.setText(gs.getmBillNo());
        mTxvToOrgName.setText(gs.getmToOrgName());
        mTxvGoodsNum.setText(gs.getmGoodsNum() + "");
        mTxvSeq.setText(gs.getmSeq() + "");
        mTxvBarcode.setText(gs.getmBarcode());
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

    /**
     * 条码确认发生变化.
     *
     * @param evt the evt
     */

    @Subscribe
    public void onScanedBarcodeConfirmChangedEvent(ScandedBarcodeConfirmChangeEvent evt){
        mBtnSumGoodsNum.setText(evt.getmConfirmGoodsCount() + "/" +evt.getmGoodsCount());
     }

    /**
     * 货物信息正确添加.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onGoodsInfoAddSuccessEvent(GoodsInfoAddSuccessEvent evt) {
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

}
