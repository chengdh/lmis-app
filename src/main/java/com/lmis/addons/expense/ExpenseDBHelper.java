package com.lmis.addons.expense;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.addons.message.MessageDB;
import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;

public class ExpenseDBHelper extends LmisDatabase {
  Context mContext = null;

  public ExpenseDBHelper(Context context) {
    super(context);
    mContext = context;
  }
  //hr.expense.line
  public class  ExpenseLine extends LmisDatabase {
    Context mContext = null;

    public ExpenseLine(Context context) {
      super(context);
      mContext = context;
    }

    @Override
    public String getModelName() {
      return "hr.expense.line";
    }

    @Override
    public List<LmisColumn> getModelColumns(){
      List<LmisColumn> cols = new ArrayList<LmisColumn>();
      cols.add(new LmisColumn("expense_id","master expense", LmisFields.integer()));
      cols.add(new LmisColumn("name","Name", LmisFields.varchar(128)));
      cols.add(new LmisColumn("date_value","Date", LmisFields.varchar(20)));
      cols.add(new LmisColumn("total_amount","total amount", LmisFields.integer()));
      cols.add(new LmisColumn("unit_amount","unit amount", LmisFields.integer()));
      cols.add(new LmisColumn("unit_quantity","unit quantity", LmisFields.integer()));
      //cols.add(new LmisColumn("uom_id","unit of measure",LmisFields.varchar(20)));
      //cols.add(new LmisColumn("product_id","product",LmisFields.varchar(20)));
      cols.add(new LmisColumn("description","description", LmisFields.text()));
      return cols;
    }
  }
  //hr.department
  public class Department extends LmisDatabase {
    Context mContext = null;

    public Department(Context context) {
      super(context);
      mContext = context;
    }

    @Override
    public String getModelName() {
      return "hr.department";
    }

    @Override
    public List<LmisColumn> getModelColumns(){
      List<LmisColumn> cols = new ArrayList<LmisColumn>();
      cols.add(new LmisColumn("name","Name", LmisFields.varchar(128)));
      return cols;
    }
  }
  //hr.employee
  public class Employee extends LmisDatabase {
    Context mContext = null;

    public Employee(Context context) {
      super(context);
      mContext = context;
    }

    @Override
    public String getModelName() {
      return "hr.employee";
    }

    @Override
    public List<LmisColumn> getModelColumns(){
      List<LmisColumn> cols = new ArrayList<LmisColumn>();
      cols.add(new LmisColumn("name","Name", LmisFields.varchar(128)));
      return cols;
    }
  }

  //获取osv name
  @Override
  public String getModelName() {
    return "hr.expense.expense";
  }

  //获取columns
  @Override
  public List<LmisColumn> getModelColumns() {
    List<LmisColumn> cols = new ArrayList<LmisColumn>();
    //名称
    cols.add(new LmisColumn("name","Name", LmisFields.varchar(128)));
    cols.add(new LmisColumn("date","Date", LmisFields.varchar(20)));
    cols.add(new LmisColumn("employee_id","Employee", LmisFields.manyToOne(new Employee(mContext))));
    cols.add(new LmisColumn("date_confirm","Date Confirm", LmisFields.varchar(20)));
    cols.add(new LmisColumn("date_valid","Date Valid", LmisFields.varchar(20)));
    cols.add(new LmisColumn("line_ids","lines", LmisFields.oneToMany(new ExpenseLine(mContext))));
    cols.add(new LmisColumn("note","note", LmisFields.varchar(200)));
    cols.add(new LmisColumn("amount","amount", LmisFields.integer()));
    cols.add(new LmisColumn("department_id","department", LmisFields.manyToOne(new Department(mContext))));
    cols.add(new LmisColumn("state","state", LmisFields.varchar(20)));
    cols.add(new LmisColumn("next_workflow_signal","next_signal", LmisFields.varchar(20)));
    cols.add(new LmisColumn("processed","is processed", LmisFields.varchar(20)));

    cols.add(new LmisColumn("message_ids","messages", LmisFields.oneToMany(new MessageDB(mContext))));

    return cols;
  }

}
