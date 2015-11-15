package com.lmis.orm;

import android.util.Log;

import com.lmis.util.Inflector;

import java.util.ArrayList;
import java.util.List;

public class SQLHelper {

    public List<String> createTable(LmisDBHelper db) {
        List<String> queries = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(modelToTable(db.getModelName()));
        sql.append(" (");
        for (LmisColumn col : db.getModelColumns()) {
            if (col.getName().equals("id") || col.getName().equals("oea_name")) {
                continue;
            }
            if (col.getType() instanceof String) {
                sql.append(col.getName());
                sql.append(" ");
                sql.append(col.getType());
                sql.append(", ");
            }
            if (col.getType() instanceof LmisManyToOne) {
                LmisManyToOne manyToOne = (LmisManyToOne) col.getType();
                List<String> many2one = createTable(manyToOne.getDBHelper());
                for (String query : many2one) {
                    queries.add(query);
                }
                sql.append(col.getName());
                sql.append(" ");
                sql.append(LmisFields.integer());
                sql.append(", ");

            }
            if (col.getType() instanceof LmisManyToMany) {
                LmisManyToMany manyTomany = (LmisManyToMany) col.getType();
                List<String> many2many = createTable(manyTomany.getDBHelper());
                for (String query : many2many) {
                    queries.add(query);
                    queries.add(createMany2ManyRel(db.getModelName(),
                            manyTomany.getDBHelper().getModelName()));
                }
            }
            //one to many字段处理
            if (col.getType() instanceof LmisOneToMany) {
                LmisOneToMany oneTomany = (LmisOneToMany) col.getType();
                List<String> one2many = createTable(oneTomany.getDBHelper());
                for (String query : one2many) {
                    queries.add(query);
                }
            }
        }
        sql.append(defaultColumns());
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append(");");
        queries.add(sql.toString());
        Log.d("SQLHelper", "Table created : " + modelToTable(db.getModelName()));
        return queries;
    }

    public String createMany2ManyRel(String model_first, String model_second) {
        String column_first = modelToTable(model_first);
        String column_second = modelToTable(model_second);
        String rel_table = column_first + "_" + column_second;
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(rel_table);
        sql.append("(");
        sql.append(column_first + "_id ");
        sql.append(LmisFields.integer());
        sql.append(", ");
        sql.append(column_second + "_id ");
        sql.append(LmisFields.integer());
        sql.append(defaultRelColumns());
        sql.append(");");
        Log.d("SQLHelper", "Table created : " + rel_table);
        return sql.toString();
    }

    public String dropMany2ManyRel(String model_first, String model_second) {
        String column_first = modelToTable(model_first);
        String column_second = modelToTable(model_second);
        String rel_table = column_first + "_" + column_second;
        StringBuffer sql = new StringBuffer();
        sql.append("DROP TABLE IF EXISTS ");
        sql.append(rel_table + ";");
        Log.d("SQLHelper", "Table dropped : " + rel_table);
        return sql.toString();
    }

    public List<String> dropTable(LmisDBHelper db) {
        List<String> queries = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        String table = modelToTable(db.getModelName());
        sql.append("DROP TABLE IF EXISTS " + table + ";");
        Log.d("SQHelper", "Table droped : " + table);
        queries.add(sql.toString());
        for (LmisColumn col : db.getModelColumns()) {
            if (col.getType() instanceof LmisManyToMany) {
                LmisManyToMany m2mDb = (LmisManyToMany) col.getType();
                for (String que : dropTable(m2mDb.getDBHelper())) {
                    queries.add(que);
                }
                queries.add(dropMany2ManyRel(table, modelToTable(m2mDb
                        .getDBHelper().getModelName())));
            }
            if (col.getType() instanceof LmisManyToOne) {
                sql = new StringBuffer();
                LmisManyToOne m2oDb = (LmisManyToOne) col.getType();
                for (String que : dropTable(m2oDb.getDBHelper())) {
                    queries.add(que);
                }
            }
            //处理one to many字段
            if (col.getType() instanceof LmisOneToMany) {
                LmisOneToMany o2mDb = (LmisOneToMany) col.getType();
                for (String que : dropTable(o2mDb.getDBHelper())) {
                    queries.add(que);
                }
            }

        }
        return queries;
    }

    private String defaultColumns() {
        StringBuffer defaultCols = new StringBuffer();
        defaultCols.append("id ");
        defaultCols.append(LmisFields.integer());
        defaultCols.append(" PRIMARY KEY");
        defaultCols.append(", ");
        defaultCols.append("oea_name ");
        defaultCols.append(LmisFields.varchar(50));
        defaultCols.append(", ");
        return defaultCols.toString();
    }

    private String defaultRelColumns() {
        StringBuffer defaultCols = new StringBuffer();
        defaultCols.append(", ");
        defaultCols.append("oea_name ");
        defaultCols.append(LmisFields.varchar(50));
        return defaultCols.toString();
    }

    public String modelToTable(String model) {
        return Inflector.tableize(model);
    }
}
