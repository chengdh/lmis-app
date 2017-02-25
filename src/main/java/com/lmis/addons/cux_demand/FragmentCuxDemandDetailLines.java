package com.lmis.addons.cux_demand;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

public class FragmentCuxDemandDetailLines extends BaseFragment {
    public static final String TAG = "FragmentCuxDemandDetailHeader";

    LmisDataRow mCuxDemandData;

    @InjectView(R.id.lst_cux_demand_lines)
    ListView mListLines;

    LmisListAdapter mLinesAdapter = null;
    View mView = null;


    /**
     * The type View holder.
     * butterKnife viewholder
     */
    static class LineViewHolder {
        //        @InjectView(R.id.txv_line_number)
//        TextView txvLineNumber;
        @InjectView(R.id.txv_item_description)
        TextView txvItemDescription;
//        @InjectView(R.id.txv_item_spec)
//        TextView txvItemSpec;

        @InjectView(R.id.txv_item_price)
        TextView txvItemPrice;

        @InjectView(R.id.txv_demand_quantiry)
        TextView txvDemandQuantiry;
        @InjectView(R.id.txv_line_bugdet)
        TextView txvLineBugdet;


        public LineViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    public LmisDataRow getmCuxDemandData() {
        return mCuxDemandData;
    }

    public void setmCuxDemandData(LmisDataRow mCuxDemandData) {
        this.mCuxDemandData = mCuxDemandData;
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
        mView = inflater.inflate(R.layout.fragment_cux_demand_detail_lines, container, false);
        ButterKnife.inject(this, mView);
        initLines();
        return mView;
    }

    private void initLines() {
        //设置子表内容
        List lines = mCuxDemandData.getO2MRecord("cux_demand_lines").browseEach();
        mLinesAdapter = new LmisListAdapter(scope.context(), R.layout.fragment_cux_demand_detail_line_item, lines) {
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

        List<LmisDataRow> lines = mCuxDemandData.getO2MRecord("cux_demand_lines").browseEach();
        if (lines.size() > 0) {
            LmisDataRow line = (LmisDataRow) lines.get(position);

            String lineNumber = line.getString("line_number");
            String itemDescription = line.getString("item_description");
            String itemSpec = line.getString("item_spec");
            String itemPrice = line.getString("item_price");
            String demandQuantiry = line.getString("demand_quantiry");
            String lineBugdet = line.getString("line_bugdet");
//            holder.txvLineNumber.setText(lineNumber);
            holder.txvItemDescription.setText(itemSpec);
//            holder.txvItemSpec.setText(itemSpec);
            holder.txvItemPrice.setText(itemPrice);
            holder.txvDemandQuantiry.setText(demandQuantiry);
            holder.txvLineBugdet.setText(lineBugdet);
        }

        return mView;
    }

}
