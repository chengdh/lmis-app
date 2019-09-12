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
import com.lmis.util.barcode.InventoryMoveOpType;
import com.lmis.util.barcode.ScandedBarcodeChangeEvent;
import com.lmis.util.barcode.ScandedBarcodeConfirmChangeEvent;
import com.lmis.util.controls.ExcludeAccessOrgSearchableSpinner;
import com.lmis.util.drawer.DrawerItem;
import com.lmis.util.drawer.DrawerListener;
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
        initScanTab();
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

    /**
     * 初始化扫描条码tab界面.
     */
    private void initScanTab() {
        if (mBarcodeParser.getmMoveId() > 0) {

            mSpinnerYardsSelect.setVisibility(View.GONE);
            mSpinnerYardsSelect.setVisibility(View.GONE);
            mTxvToOrgDisplay.setVisibility(View.VISIBLE);
            //refresh mInventoryMove
        }
        else {

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
                String barcode = mEdtScanBarcode.getText().toString();

                if (barcode.length() == 21) {
                    try {
                        mBarcodeParser.addBarcode(s.toString());
                        if (mInventoryMove == null) {
                            LmisDatabase db = new InventoryMoveDB(scope.context());
                            mInventoryMove = db.select(mBarcodeParser.getmMoveId());
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
        mEdtScanBarcode.requestFocus();
    }

    /**
     * 条形码正确解析event.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onBarcodeParseSuccessEvent(BarcodeParseSuccessEvent evt) {
        mCurGoodsInfo = evt.getmGoodsInfo();
        mTxvBillNo.setText(mCurGoodsInfo.getmBillNo());
        mTxvToOrgName.setText(mCurGoodsInfo.getmToOrgName());
        mTxvGoodsNum.setText(mCurGoodsInfo.getmGoodsNum() + "");
        mTxvSeq.setText(mCurGoodsInfo.getmSeq() + "");
        mTxvBarcode.setText(mCurGoodsInfo.getmBarcode());
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
}
