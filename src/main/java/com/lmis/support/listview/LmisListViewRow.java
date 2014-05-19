/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.lmis.support.listview;

import com.lmis.orm.LmisDataRow;

/**
 * The Class OEListViewRows.
 */
public class LmisListViewRow {

    /**
     * The row_id.
     */
    private int row_id;

    /**
     * The row_data.
     */
    private LmisDataRow row_data;

    /**
     * Instantiates a new oE list view rows.
     *
     * @param row_id   the row_id
     * @param row_data the row_data
     */
    public LmisListViewRow(int row_id, LmisDataRow row_data) {
        super();
        this.row_id = row_id;
        this.row_data = row_data;
    }

    /**
     * Gets the row_id.
     *
     * @return the row_id
     */
    public int getRow_id() {
        return row_id;
    }

    /**
     * Sets the row_id.
     *
     * @param row_id the new row_id
     */
    public void setRow_id(int row_id) {
        this.row_id = row_id;
    }

    /**
     * Gets the row_data.
     *
     * @return the row_data
     */
    public LmisDataRow getRow_data() {
        return row_data;
    }

    /**
     * Sets the row_data.
     *
     * @param row_data the row_data
     */
    public void setRow_data(LmisDataRow row_data) {
        this.row_data = row_data;
    }

    @Override
    public String toString() {
        return getRow_data().toString();
    }

}
