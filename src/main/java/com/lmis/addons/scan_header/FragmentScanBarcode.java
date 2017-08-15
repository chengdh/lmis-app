package com.lmis.addons.scan_header;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisValues;
import com.lmis.support.BaseFragment;
import com.lmis.util.barcode_scan_header.BarcodeDuplicateException;
import com.lmis.util.barcode_scan_header.BarcodeNotExistsException;
import com.lmis.util.barcode_scan_header.BarcodeParseSuccessEventForScanHeader;
import com.lmis.util.barcode_scan_header.BarcodeParser;
import com.lmis.util.barcode_scan_header.BarcodeRemoveEventForScanHeader;
import com.lmis.util.barcode_scan_header.DBException;
import com.lmis.util.barcode_scan_header.GoodsInfo;
import com.lmis.util.barcode_scan_header.GoodsInfoChangeEvent;
import com.lmis.util.barcode_scan_header.ScanHeaderGoodsInfoAddSuccessEvent;
import com.lmis.util.barcode_scan_header.InvalidBarcodeException;
import com.lmis.util.barcode_scan_header.InvalidToOrgException;
import com.lmis.util.barcode_scan_header.ScanHeaderOpType;
import com.lmis.util.barcode_scan_header.ScandedBarcodeChangeEventForScanHeader;
import com.lmis.util.barcode_scan_header.ScandedBarcodeConfirmChangeEvent;
import com.lmis.util.controls.GoodsStatus;
import com.lmis.util.controls.GoodsStatusSpinner;
import com.lmis.util.controls.OrgLoadOrgSpinner;
import com.lmis.util.controls.OrgSortingOrgSpinner;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-9-14.
 */
public class FragmentScanBarcode extends BaseFragment {

    public static final String TAG = "FragmentScanBarcode";

    String mOpType = ScanHeaderOpType.SORTING_IN;

    @Inject
    Bus mBus;

    @InjectView(R.id.edt_scan_barcode)
    EditText mEdtScanBarcode;

    @InjectView(R.id.txv_from_org)
    TextView mTxvFromOrg;

    @InjectView(R.id.txv_to_org)
    TextView mTxvToOrg;

    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

    @InjectView(R.id.txv_goods_no)
    TextView mTxvGoodsNo;

    @InjectView(R.id.txv_pay_type)
    TextView mTxvPayType;

    @InjectView(R.id.txv_carrying_fee)
    TextView mTxvCarryingFee;

    @InjectView(R.id.txv_goods_fee)
    TextView mTxvGoodsFee;

    @InjectView(R.id.txv_goods_info)
    TextView mTxvGoodsInfo;

    @InjectView(R.id.txv_goods_num)
    TextView mTxvGoodsNum;

    @InjectView(R.id.txv_state_des)
    TextView mTxvStateDes;


    @InjectView(R.id.btn_sum_goods_num)
    Button mBtnSumGoodsNum;


    @InjectView(R.id.btn_sum_bills_count)
    Button mBtnSumBillsCount;

    @InjectView(R.id.spinner_load_org_select)
    OrgLoadOrgSpinner mLoadOrgSpinner;


    @InjectView(R.id.spinner_sorting_org_select)
    OrgSortingOrgSpinner mSortingOrgSpinner;

    @InjectView(R.id.spinner_goods_status)
    GoodsStatusSpinner mGoodsStatusSpinner;

    GoodsInfo mCurrentGoodsInfo = null;


    View mView = null;

    public BarcodeParser getmBarcodeParser() {
        return mBarcodeParser;
    }

    public void setmBarcodeParser(BarcodeParser mBarcodeParser) {
        this.mBarcodeParser = mBarcodeParser;
    }

    public LmisDataRow getmScanHeader() {
        return mScanHeader;
    }

    public void setmScanHeader(LmisDataRow mScanHeader) {
        this.mScanHeader = mScanHeader;
    }

    BarcodeParser mBarcodeParser = null;
    LmisDataRow mScanHeader = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_scan_header_scan_barcode, container, false);
        ButterKnife.inject(this, mView);
        Bundle args = getArguments();
        if (args != null && args.containsKey("type"))
            mOpType = args.getString("type");
        switch (mOpType) {
            case ScanHeaderOpType.SORTING_IN:
                break;
            case ScanHeaderOpType.LOAD_IN:
                break;
            case ScanHeaderOpType.LOAD_OUT:
//                mLoadOrgSpinner.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        mBus.register(this);
        initData();
        initScanTab();
        return mView;
    }

    private void initData() {
        Log.d(TAG, "FragmentScanBarcode#initData");
        clearUI();
        if (mBarcodeParser.getmId() > 0) {
            //refresh mInventoryMove
            LmisDatabase db = new ScanHeaderDB(scope.context());
            mScanHeader = db.select(mBarcodeParser.getmId());
            mBtnSumGoodsNum.setText(mBarcodeParser.sumGoodsCount() + "");
            mBtnSumBillsCount.setText(mBarcodeParser.sumBillsCount() + "");
        }

    }

    /**
     * 初始化扫描条码tab界面.
     */
    private void initScanTab() {

        mEdtScanBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                String barcode = mEdtScanBarcode.getText().toString();

                if (barcode.length() == 7 || barcode.length() == 8) {
                    mCurrentGoodsInfo = null;
                    try {
                        mBarcodeParser.addBarcode(s.toString());
                        if (mScanHeader == null) {
                            LmisDatabase db = new ScanHeaderDB(scope.context());
                            mScanHeader = db.select(mBarcodeParser.getmId());
                        }
                        mEdtScanBarcode.setText("");

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
        mLoadOrgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LmisDataRow org = (LmisDataRow) mLoadOrgSpinner.getItemAtPosition(position);
                mBarcodeParser.setmToOrgID(org.getInt("id"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mGoodsStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                          @Override
                                                          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                              if (mCurrentGoodsInfo == null) {
                                                                  return;
                                                              }
                                                              Map.Entry gsType = (Map.Entry<Integer, String>) mGoodsStatusSpinner.getItemAtPosition(position);
                                                              Integer goodsStatusType = (Integer) gsType.getKey();
                                                              int lineID = mCurrentGoodsInfo.getmID();
                                                              LmisValues vals = new LmisValues();
                                                              vals.put("goods_status_type", goodsStatusType);
                                                              updateScanLine(vals, lineID);

                                                              if (goodsStatusType == GoodsStatus.GOODS_STATUS_INPUT) {
                                                                  openGoodsStatusInput();
                                                              }

                                                          }

                                                          @Override
                                                          public void onNothingSelected(AdapterView<?> parent) {

                                                          }
                                                      }
        );

        mEdtScanBarcode.requestFocus();
    }

    private void openGoodsStatusInput() {
        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(scope.context());

        //AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        // Setting Dialog Title
//        alertDialog.setTitle("录入备注信息");

        // Setting Dialog Message
        alertDialog.setMessage("录入备注");
        final EditText input = new EditText(scope.context());
        alertDialog.setView(input);

        // Setting Icon to Dialog
//        alertDialog.setIcon(R.drawable.key);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        if (mCurrentGoodsInfo == null) {
                            return;
                        }
                        int lineID = mCurrentGoodsInfo.getmID();
                        String goodsStatusNote = input.getText().toString();
                        LmisValues vals = new LmisValues();
                        vals.put("goods_status_note", goodsStatusNote);
                        updateScanLine(vals, lineID);
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        // closed

        // Showing Alert Message
        alertDialog.show();

    }

    private int updateScanLine(LmisValues vals, int id) {

        if (vals.contains("goods_status_type")) {
            mCurrentGoodsInfo.setmGoodsStatusType(vals.getInt("goods_status_type"));
        }
        if (vals.contains("goods_status_note")) {

            mCurrentGoodsInfo.setmGoodsStatusNote(vals.getString("goods_status_note"));
        }
        ScanLineDB db = new ScanLineDB(scope.context());
        mBus.post(new GoodsInfoChangeEvent(mCurrentGoodsInfo));
        return db.update(vals, id);
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
        mTxvGoodsNo.setText("");
        mTxvFromOrg.setText("");
        mTxvToOrg.setText("");
        mTxvCarryingFee.setText("0");
        mTxvGoodsFee.setText("0");
        mTxvGoodsInfo.setText("");
        mTxvGoodsNum.setText("");
        mTxvPayType.setText("");
        mTxvStateDes.setText("");
        mEdtScanBarcode.requestFocus();
    }

    /**
     * 设置界面显示
     */
    private void setUI(GoodsInfo gs) {
        mTxvBillNo.setText(gs.getmBillNo());
        mTxvGoodsNo.setText(gs.getmGoodsNo());
        mTxvFromOrg.setText(gs.getmFromOrgName());
        mTxvToOrg.setText(gs.getmToOrgName());
        mTxvCarryingFee.setText(gs.getmCarryingFee() + "");
        mTxvGoodsFee.setText(gs.getmGoodsFee() + "");
        mTxvGoodsInfo.setText(gs.getmGoodsInfo());
        mTxvPayType.setText(gs.getmPayTypeDes());
        mTxvGoodsNum.setText(gs.getmGoodsNum() + "");
        mTxvStateDes.setText(gs.getmStateDes());
        mEdtScanBarcode.requestFocus();


    }

    /**
     * 条形码正确解析event.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onBarcodeParseSuccessEvent(BarcodeParseSuccessEventForScanHeader evt) {
    }

    /**
     * On scaned barcode changed event.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onScanedBarcodeChangedEvent(ScandedBarcodeChangeEventForScanHeader evt) {
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
    public void onGoodsInfoAddSuccessEvent(ScanHeaderGoodsInfoAddSuccessEvent evt) {
        mCurrentGoodsInfo = evt.getmGoodsInfo();
        setUI(evt.getmGoodsInfo());
    }

    @Subscribe
    public void onBarcodeRemoveEventForScanHeader(BarcodeRemoveEventForScanHeader evt) {
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
