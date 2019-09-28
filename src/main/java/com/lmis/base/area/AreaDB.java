package com.lmis.base.area;

import android.content.Context;

import com.lmis.base.org.OrgDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

import java.util.ArrayList;
import java.util.List;

public class AreaDB extends LmisDatabase {
    /**
     * Instantiates a new Carrying bill dB.
     *
     * @param context the context
     */
    public AreaDB(Context context) {
        super(context);
    }

    /**
     * Gets model name.
     *
     * @return the model name
     */
    @Override
    public String getModelName() {
        return "Area";
    }

    /**
     * Gets model columns.
     *
     * @return the model columns
     */
    @Override
    public List<LmisColumn> getModelColumns() {
        List<LmisColumn> cols = new ArrayList<LmisColumn>();

        //名称
        LmisColumn colName = new LmisColumn("name", "Name", LmisFields.varchar(60));
        cols.add(colName);

        //名称
        LmisColumn colOrderBy = new LmisColumn("order_by", "Order By", LmisFields.integer());
        cols.add(colOrderBy);

        LmisColumn colIsActive = new LmisColumn("is_active", "Order By", LmisFields.varchar(20));
        cols.add(colIsActive);

        LmisColumn colNote = new LmisColumn("note", "Note", LmisFields.varchar(60));
        cols.add(colNote );

        LmisColumn colSimpName= new LmisColumn("simp_name", "Simple Name", LmisFields.varchar(60));
        cols.add(colSimpName);

        return cols;
    }

}
