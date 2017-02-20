/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.lmis.orm;

import android.util.Base64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class LmisDataRow {
    HashMap<String, Object> _data = new HashMap<String, Object>();

    public void put(String key, Object value) {
        _data.put(key, value);
    }

    public Object get(String key) {
        return _data.get(key);
    }

    public Integer getInt(String key) {
        return Integer.parseInt(_data.get(key).toString());
    }

    public Float getFloat(String key) {
        return Float.parseFloat(_data.get(key).toString());
    }

    public String getString(String key) {
        if (_data.containsKey(key) && _data.get(key) != null)
            return _data.get(key).toString();
        else
            return "false";
    }

    public Boolean getBoolean(String key) {
        Object v = _data.get(key);
        if(v == null){
            return false;
        }
        return Boolean.parseBoolean(_data.get(key).toString());
    }

    public IdName getIdName(String key) {
        String data = getString(key);
        IdName val = null;
        try {
            JSONArray arr = new JSONArray(data);
            if (arr.get(0) instanceof JSONArray) {
                if (arr.getJSONArray(0).length() == 2) {
                    val = new IdName(Integer.parseInt(arr.getJSONArray(0)
                            .getString(0)), arr.getJSONArray(0).getString(1));
                }
            } else {
                if (arr.length() == 2) {
                    val = new IdName(Integer.parseInt(arr.getString(0)), arr.getString(1));
                }
            }
        } catch (Exception e) {
        }
        return val;
    }

    public LmisM2ORecord getM2ORecord(String key) {
        return (LmisM2ORecord) _data.get(key);
    }

    public LmisM2MRecord getM2MRecord(String key) {
        return (LmisM2MRecord) _data.get(key);
    }

    public LmisO2MRecord getO2MRecord(String key) {
        return (LmisO2MRecord) _data.get(key);
    }


    public List<String> keys() {
        List<String> list = new ArrayList<String>();
        list.addAll(_data.keySet());
        return list;
    }

    @Override
    public String toString() {
        if (_data.containsKey("name"))
            return _data.get("name").toString();

        return _data.toString();
    }

    public class IdName {
        Integer id;
        String name;

        public IdName(Integer id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    /**
     * 将数据转换为json
     *
     * @param includeId 导出数据时是否包含id
     * @return the jSON object
     * @throws JSONException the jSON exception
     */
    public JSONObject exportAsJSON(Boolean includeId) throws JSONException {
        JSONObject ret = new JSONObject();
        for (Map.Entry<String, Object> o : _data.entrySet()) {
            Object value = o.getValue();
            String key = o.getKey();
            if (key == "id" || key == "oea_name")
                continue;

            if (value instanceof LmisM2ORecord) {
                ret.put(key, ((LmisM2ORecord) value).browse().getInt("id"));
            } else if (value instanceof LmisO2MRecord) {
                //对O2M循环处理
                int i = 0;
                for (LmisDataRow line : ((LmisO2MRecord) value).browseEach()) {
                    JSONObject jsonLine = line.exportAsJSON(includeId);
                    ret.append(key + "_attributes", jsonLine);

                }

            } else if (value instanceof LmisM2MRecord) {
                int i = 0;
                //对M2M循环处理
                for (LmisDataRow line : ((LmisM2MRecord) value).browseEach()) {
                    JSONObject jsonLine = line.exportAsJSON(includeId);
                    ret.append(key + "_attributes", jsonLine);
                }
            } else if (value instanceof byte[]) {
                //使用data uri的方式标示base64 encode后的图片,在服务端paperclip会自动解析
                ret.put(key, "data:image/png;base64," + Base64.encodeToString((byte[]) value, 0));
            } else {
                ret.put(key, value);
            }

        }
        return ret;
    }
}
