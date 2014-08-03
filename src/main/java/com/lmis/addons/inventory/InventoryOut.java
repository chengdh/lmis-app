package com.lmis.addons.inventory;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.LmisDialog;
import com.lmis.support.LmisUser;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.barcode.BarcodeDuplicateException;
import com.lmis.util.barcode.BarcodeNotExistsException;
import com.lmis.util.barcode.BarcodeParseSuccessEvent;
import com.lmis.util.barcode.BarcodeParser;
import com.lmis.util.barcode.BarcodeQueryListener;
import com.lmis.util.barcode.BarcodeRemoveEvent;
import com.lmis.util.barcode.DBException;
import com.lmis.util.barcode.GoodsInfo;
import com.lmis.util.barcode.GoodsInfoAddSuccessEvent;
import com.lmis.util.barcode.InvalidBarcodeException;
import com.lmis.util.barcode.InvalidToOrgException;
import com.lmis.util.barcode.MultiChoiceBarcodeListener;
import com.lmis.util.barcode.ScandedBarcodeChangeEvent;
import com.lmis.util.drawer.DrawerItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 14-6-17.
 * 调拨出库单新增或修改界面
 */
public class InventoryOut extends BaseFragment implements AdapterView.OnItemLongClickListener{

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

    @InjectView(R.id.btn_remove_barcode)
    ImageButton mBtnRemoveBarcode;

    @InjectView(R.id.btn_sum_goods_num)
    Button mBtnSumGoodsNum;


    @InjectView(R.id.btn_sum_bills_count)
    Button mBtnSumBillsCount;

    @InjectView(R.id.tabHost)
    TabHost mTabHost;

    //已扫描的票据列表
    @InjectView(R.id.lst_view_bills)
    ListView mLstBills;

    LmisListAdapter mBillsAdapter = null;

    //已扫描的票据列表
    List<Object> mBillsObjects = null;


    //已扫描的条码列表
    @InjectView(R.id.lst_view_barcodes)
    ListView mListBarcodes;
    LmisListAdapter mBarcodesAdapter = null;
    List<Object> mBarcodesObjects = null;


    View.OnClickListener mRemoveBarcodeListener = null;
    View.OnClickListener mAddBillListener = null;

    View mView = null;
    Integer mInventoryOutId = null;
    LmisDataRow mInventoryOut = null;

    //条码解析器
    BarcodeParser mBarcodeParser;

    //上传到服务器处理
    Upload uploadSync = null;

    //search view
    @InjectView(R.id.search_view_bill_list)
    SearchView mSearchViewBillList;

    @InjectView(R.id.search_view_list_barcodes)
    SearchView mSearchViewBarcodeList;

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
        mBus.register(this);
        initData();
        initControls();
        return mView;
    }

    /**
     * 初始化数据.
     */
    private void initData() {
        Log.d(TAG, "inventory_out#initData");
        LmisUser currentUser = scope.User();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mInventoryOutId = bundle.getInt("inventory_out_id");
            mInventoryOut = new InventoryMoveDB(scope.context()).select(mInventoryOutId);
            LmisDataRow fromOrg = mInventoryOut.getM2ORecord("from_org_id").browse();
            LmisDataRow toOrg = mInventoryOut.getM2ORecord("to_org_id").browse();
            mBarcodeParser = new BarcodeParser(scope.context(), mInventoryOutId, fromOrg.getInt("id"), toOrg.getInt("id"), false);
            ArrayAdapter adapter = (ArrayAdapter) mSpinnerYardsSelect.getAdapter();
            int pos = adapter.getPosition(toOrg);
            mSpinnerYardsSelect.setSelection(pos);
            mBtnSumGoodsNum.setText(mInventoryOut.getInt("sum_goods_count") + "");
            mBtnSumBillsCount.setText(mInventoryOut.getInt("sum_bills_count") + "");
            mStartPage.setVisibility(View.GONE);
        } else
            mBarcodeParser = new BarcodeParser(scope.context(), -1, currentUser.getDefault_org_id(), -1, false);

    }

    /**
     * 初始化控件.
     */
    private void initControls() {
        initTabs();
        initScanTab();
        initBillsListTab();
        initBarcodesListTab();
    }

    /**
     * 初始化tabs.
     */
    private void initTabs() {
        mTabHost.setup();
        //TODO 此处加上tab图标
        mTabHost.addTab(mTabHost.newTabSpec("TAB_SCAN_BARCODE").setIndicator("扫描条码").setContent(R.id.scan_tab));
        mTabHost.addTab(mTabHost.newTabSpec("TAB_BILLS_LIST").setIndicator("票据明细").setContent(R.id.lst_bills));
        mTabHost.addTab(mTabHost.newTabSpec("TAB_BARCODES_LIST").setIndicator("条码明细").setContent(R.id.lst_barcodes));
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
                mStartPage.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                String barcode = mEdtScanBarcode.getText().toString();

                if (barcode.length() == 16) {
                    try {
                        mBarcodeParser.addBarcode(s.toString());
                        if (mInventoryOutId == null) {
                            mInventoryOutId = mBarcodeParser.getmMoveId();
                        }
                        mEdtScanBarcode.selectAll();


                    } catch (InvalidBarcodeException ex) {
                        Toast.makeText(scope.context(), "条码格式不正确!", Toast.LENGTH_LONG).show();
                    } catch (InvalidToOrgException ex) {
                        Toast.makeText(scope.context(), "到货地不匹配!", Toast.LENGTH_LONG).show();
                    } catch (BarcodeDuplicateException ex) {
                        Toast.makeText(scope.context(), "该货物条码已扫描!", Toast.LENGTH_LONG).show();
                    } catch (DBException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEdtScanBarcode.requestFocus();

        //删除条形码
        mRemoveBarcodeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeBarcode();
            }
        };
        mBtnRemoveBarcode.setOnClickListener(mRemoveBarcodeListener);
    }



    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolderForBillsList {
        //发货地
        @InjectView(R.id.txv_bill_no)
        TextView txvBillNo;
        //描述信息
        @InjectView(R.id.txv_barcode_count)
        TextView txvBarcodeCount;

        public ViewHolderForBillsList(View view) {
            ButterKnife.inject(this, view);
        }
    }


    /**
     * 初始化票据列表tab.
     */
    private void initBillsListTab() {
        mBillsObjects = new ArrayList<Object>(mBarcodeParser.getBillsList());
        mBillsAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_inventory_out_list_bills_item, mBillsObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolderForBillsList holder;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_inventory_out_list_bills_item, parent, false);
                    holder = new ViewHolderForBillsList(mView);
                    mView.setTag(holder);
                } else {
                    holder = (ViewHolderForBillsList) mView.getTag();
                }
                Map.Entry billHash = (Map.Entry) mBillsObjects.get(position);
                String billNo = billHash.getKey().toString();
                String count = billHash.getValue().toString();
                holder.txvBillNo.setText(billNo);
                holder.txvBarcodeCount.setText("已扫" + count + "件");
                return mView;
            }

        };
        mLstBills.setAdapter(mBillsAdapter);
        mSearchViewBillList.setIconifiedByDefault(false);
        mSearchViewBillList.setOnQueryTextListener(new BarcodeQueryListener(mBillsAdapter));
        mLstBills.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mLstBills.setOnItemLongClickListener(this);
        //mLstBills.setMultiChoiceModeListener(new MultiChoiceBarcodeListener(scope.context(),mBarcodesObjects,mBarcodeParser));
    }

    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class ViewHolderForBarcodesList {
        //发货地
        @InjectView(R.id.txv_bill_no)
        TextView txvBillNo;
        //描述信息
        @InjectView(R.id.txv_barcode)
        TextView txvBarcode;

        public ViewHolderForBarcodesList(View view) {
            ButterKnife.inject(this, view);
        }
    }

    /**
     * 初始化条码列表.
     */
    private void initBarcodesListTab() {

        mBarcodesObjects = new ArrayList<Object>(mBarcodeParser.getmScanedBarcode());
        mBarcodesAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_inventory_out_list_barcodes_item, mBarcodesObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                ViewHolderForBarcodesList holder;
                if (mView == null) {
                    mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_inventory_out_list_barcodes_item, parent, false);
                    holder = new ViewHolderForBarcodesList(mView);
                    mView.setTag(holder);
                } else {
                    holder = (ViewHolderForBarcodesList) mView.getTag();
                }
                GoodsInfo gs = (GoodsInfo) mBarcodesObjects.get(position);
                String billNo = gs.getmBillNo();
                String barcode = gs.getmBarcode();
                holder.txvBillNo.setText(billNo);
                holder.txvBarcode.setText(barcode);
                if (position > 0) {
                    GoodsInfo prevGs = (GoodsInfo) mBarcodesObjects.get(position - 1);
                    if (prevGs.getmBillNo().equals(billNo))
                        holder.txvBillNo.setText("");
                }
                return mView;
            }

        };
        mListBarcodes.setAdapter(mBarcodesAdapter);
        mSearchViewBarcodeList.setIconifiedByDefault(false);
        mSearchViewBarcodeList.setOnQueryTextListener(new BarcodeQueryListener(mBarcodesAdapter));
        mListBarcodes.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListBarcodes.setOnItemLongClickListener(this);
        mListBarcodes.setMultiChoiceModeListener(new MultiChoiceBarcodeListener(scope.context(),mBarcodesObjects,mBarcodeParser));
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
        mTxvSeq.setText("");
        mEdtScanBarcode.requestFocus();
    }

    @Override
    public Object databaseHelper(Context context) {
        return new InventoryMoveDB(context);
    }

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_inventory_out, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_inventory_out_upload:
                uploadSync = new Upload();
                uploadSync.execute((Void) null);
                return true;
            case R.id.menu_inventory_out_delete:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        //mTxvScanMessage.setText("");
    }

    /**
     * 货物信息正确添加.
     *
     * @param evt the evt
     */
    @Subscribe
    public void onGoodsInfoAddSuccessEvent(GoodsInfoAddSuccessEvent evt) {
        //mTxvScanMessage.setText("barcode is added!");
    }

    @Subscribe
    public void onScanedBarcodeChangedEvent(ScandedBarcodeChangeEvent evt) {
        mBtnSumGoodsNum.setText(evt.getmSumGoodsNum() + "");
        mBtnSumBillsCount.setText(evt.getmSumBillsCount() + "");
        //通知billListTab数据变化
        mBillsObjects.clear();
        mBillsObjects.addAll(mBarcodeParser.getBillsList());
        mBillsAdapter.notifiyDataChange(mBillsObjects);
        //通知barcodesListTab数据变化
        mBarcodesObjects.clear();
        mBarcodesObjects.addAll(mBarcodeParser.getmScanedBarcode());
        mBarcodesAdapter.notifiyDataChange(mBarcodesObjects);

        //清除选中状态
        mListBarcodes.clearChoices();
        mLstBills.clearChoices();
    }

    @Subscribe
    public void onBarcodeRemoveEvent(BarcodeRemoveEvent evt) {
        Toast.makeText(scope.context(), "货物已删除!", Toast.LENGTH_SHORT).show();
        clearUI();
    }

    /**
     * 上传数据.
     */
    private class Upload extends AsyncTask<Void, Void, Boolean> {

        LmisDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = new LmisDialog(getActivity(), false, "正在上传数据...");
            pdialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ((InventoryMoveDB) db()).save2server(mInventoryOutId);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                uploadSync.cancel(true);
                Toast.makeText(scope.context(), "上传数据成功!", Toast.LENGTH_SHORT).show();
                //返回已处理界面
                InventoryOutList list = new InventoryOutList();
                Bundle arg = new Bundle();
                arg.putString("type", "processed");
                list.setArguments(arg);
                scope.main().startMainFragment(list, true);
            } else {
                Toast.makeText(scope.context(), "上传数据失败!", Toast.LENGTH_SHORT).show();
            }

            pdialog.dismiss();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }
}
