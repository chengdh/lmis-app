package com.lmis.addons.inventory;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.util.barcode.BarcodeDuplicateException;
import com.lmis.util.barcode.BarcodeParseSuccessEvent;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.GoodsInfo;
import com.lmis.util.barcode.GoodsInfoAddSuccessEvent;
import com.lmis.util.barcode.InvalidBarcodeException;
import com.lmis.util.barcode.InvalidToOrgException;
import com.lmis.util.barcode.ScandedBarcodeChangeEvent;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-6-17.
 * 调拨出库单新增或修改界面
 */
public class InventoryOut extends BaseFragment {

    public static final String TAG = "InventoryOut";
    @Inject
    Bus mBus;

    @InjectView(R.id.start_page)
    LinearLayout mStartPage;

    @InjectView(R.id.edt_scan_barcode)
    EditText mEdtScanBarcode;

    @InjectView(R.id.spinner_yards_select)
    Spinner mSpinnerYardsSelect;

    @InjectView(R.id.txv_bill_no)
    TextView mTxvBillNo;

    @InjectView(R.id.txv_to_org_name)
    TextView mTxvToOrgName;

    @InjectView(R.id.txv_seq)
    TextView mTxvSeq;

    @InjectView(R.id.txv_goods_num)
    TextView mTxvGoodsNum;

    @InjectView(R.id.btn_all_scaned)
    ImageButton mBtnAllScaned;

    @InjectView(R.id.txv_sum_goods_num)
    TextView mTxvSumGoodsNum;


    @InjectView(R.id.txv_sum_bills_count)
    TextView mTxvSumBillsCount;

    @InjectView(R.id.txv_scan_message)
    TextView mTxvScanMessage;

    View mView = null;
    Integer mInventoryOutId = null;
    LmisDataRow mInventoryOut = null;

    BarcodeParser mBarcodeParser;

    /**
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_inventory_out, container, false);
        ButterKnife.inject(this, mView);
        mBarcodeParser = new BarcodeParser(scope.context(), null, false, null);
        mBus.register(this);
        initControls();
        return mView;
    }

    /**
     * 初始化控件.
     */
    private void initControls() {
        mEdtScanBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {
                mStartPage.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                String barcode = mEdtScanBarcode.getText().toString();

                if (barcode.length() == 16) {
                    try {
                        mBarcodeParser.addBarcode(s.toString());
                    } catch (InvalidBarcodeException ex) {
                        mTxvScanMessage.setText("条码格式不正确");

                    } catch (InvalidToOrgException ex) {
                        mTxvScanMessage.setText("到货地不匹配");
                    } catch (BarcodeDuplicateException ex) {
                        mTxvScanMessage.setText("重复扫描货物");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public Object databaseHelper(Context context) {
        return new InventoryMoveDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
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
        mTxvScanMessage.setText("");
    }

    /**
     * 货物信息正确添加.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onGoodsInfoAddSuccessEvent(GoodsInfoAddSuccessEvent evt) {
        mTxvScanMessage.setText("barcode is added!");
    }

    @Subscribe
    public void onScanedBarcodeChangedEvent(ScandedBarcodeChangeEvent evt) {
        mTxvSumGoodsNum.setText(evt.getmSumGoodsNum() + "");
        mTxvSumBillsCount.setText(evt.getmSumBillsCount() + "");
    }
}
