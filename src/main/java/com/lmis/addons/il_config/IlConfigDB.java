package com.lmis.addons.il_config;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDataRow;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengdh on 14/11/26.
 */
public class IlConfigDB extends LmisDatabase {

    /**
     * The constant SYSTEM_TITILE.
     */
    public static final String SYSTEM_TITILE = "system_title";
    /**
     * The constant CLIENT_NAME.
     */
    public static final String CLIENT_NAME = "client_name";
    /**
     * The constant INSURED_FEE.
     */
    public static final String INSURED_FEE = "insured_fee";
    /**
     * The constant CARRYING_FEE_GTE_ON_INSURED_FEE.
     */
    public static final String CARRYING_FEE_GTE_ON_INSURED_FEE = "carrying_fee_gte_on_insured_fee";
    /**
     * The constant MAX_HAND_FEE.
     */
    public static final String MAX_HAND_FEE = "max_hand_fee";

    /**
     * Instantiates a new Il config dB.
     *
     * @param context the context
     */
    public IlConfigDB(Context context) {
        super(context);
    }

    /**
     * Gets model name.
     *
     * @return the model name
     */
    @Override
    public String getModelName() {
        return "il_config";
    }

    /**
     * Gets model columns.
     *
     * @return the model columns
     */
    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        cols.add(new LmisColumn("key", "Key", LmisFields.varchar(30)));
        cols.add(new LmisColumn("title", "Title", LmisFields.varchar(30)));
        cols.add(new LmisColumn("value", "Value", LmisFields.varchar(30)));
        return cols;
    }

    /**
     * Get int value.
     *
     * @param key         the key
     * @param default_val the default _ val
     * @return the int
     */
    private int getIntValue(String key, int default_val) {
        int ret = default_val;
        String where = "key = ?";
        String[] whereArgs = {key};
        List<LmisDataRow> rows = select(where, whereArgs, null, null, null);
        if (rows.size() > 0) {
            ret = Integer.parseInt(rows.get(0).getString("value"));

        }
        return ret;
    }

    /**
     * Get insured fee.
     *
     * @return the float
     */
    public int getInsuredFee() {
        return getIntValue(INSURED_FEE, 2);
    }
}
