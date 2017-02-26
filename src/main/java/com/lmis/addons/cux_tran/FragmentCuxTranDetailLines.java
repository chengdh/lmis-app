package com.lmis.addons.cux_tran;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lmis.R;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.BaseFragment;
import com.lmis.support.listview.LmisListAdapter;
import com.lmis.util.drawer.DrawerItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chengdh on 2017/2/25.
 */

public class FragmentCuxTranDetailLines extends BaseFragment {
    public static final String TAG = "FragmentCuxTranDetailLine";

    LmisDataRow mCuxTranData;

    @InjectView(R.id.lst_cux_tran_lines)
    ListView mListLines;

    LmisListAdapter mLinesAdapter = null;
    View mView = null;


    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class LineViewHolder {
        @InjectView(R.id.txv_item_dec)
        TextView txvItemDescription;

        @InjectView(R.id.txv_item_spc)
        TextView txvItemSpc;

        @InjectView(R.id.txv_uom)
        TextView txvUom;


        @InjectView(R.id.txv_require_qty)
        TextView txvRequireQty;

        @InjectView(R.id.txv_cost)
        TextView txvCost;
//        @InjectView(R.id.txv_expense_type)
//        TextView txvExpenseType;


        public LineViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    public LmisDataRow getmCuxTranData() {
        return mCuxTranData;
    }

    public void setmCuxTranData(LmisDataRow mCuxTranData) {
        this.mCuxTranData = mCuxTranData;
    }

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
        mView = inflater.inflate(R.layout.fragment_cux_tran_detail_lines, container, false);
        ButterKnife.inject(this, mView);
        initLines();
        return mView;
    }

    private void initLines() {
        //设置子表内容
        List lines = mCuxTranData.getO2MRecord("cux_tran_lines").browseEach();
        mLinesAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_cux_tran_detail_line_item, lines) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getActivity().getLayoutInflater().inflate(getResource(), parent, false);
                    view.setTag(new LineViewHolder(view));
                }
                view = handleRowView(view, position);
                return view;
            }
        };
        mListLines.setAdapter(mLinesAdapter);
    }

    private View handleRowView(View mView, final int position) {
        LineViewHolder holder = (LineViewHolder) mView.getTag();

        List<LmisDataRow> lines = mCuxTranData.getO2MRecord("cux_tran_lines").browseEach();
        if (lines.size() > 0) {
            LmisDataRow line = (LmisDataRow) lines.get(position);

            String lineNumber = line.getString("line_number");
            String itemDec = line.getString("item_dec");
            String itemSpc = line.getString("item_spc");
            String uom = line.getString("uom");
            String requireQty = line.getString("required_qty");
            String cost = line.getString("cost");
            String expenseType = line.getString("expense_type");
            holder.txvItemDescription.setText(itemDec);
            holder.txvItemSpc.setText(itemSpc);
            holder.txvUom.setText(uom);
            holder.txvRequireQty.setText(requireQty);
            holder.txvCost.setText(cost);
//            holder.txvExpenseType.setText(expenseType);
        }

        return mView;
    }
}
