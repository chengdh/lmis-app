package com.lmis.addons.voucher;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lmis.orm.LmisColumn;
import com.lmis.orm.LmisDatabase;
import com.lmis.orm.LmisFields;
import com.lmis.addons.message.MessageDB;
import com.lmis.base.res.ResPartnerDB;

//供应商付款凭证
public class VoucherDB extends LmisDatabase {
  Context mContext = null;

  public VoucherDB(Context context) {
    super(context);
    mContext = context;
  }
  //获取osv name
  @Override
  public String getModelName() {
    return "account.voucher";
  }

  //获取columns
  @Override
  public List<LmisColumn> getModelColumns() {
    List<LmisColumn> cols = new ArrayList<LmisColumn>();
    //名称
    cols.add(new LmisColumn("partner_id","partner", LmisFields.manyToOne(new ResPartnerDB(mContext))));
    cols.add(new LmisColumn("date","Date", LmisFields.varchar(20)));
    cols.add(new LmisColumn("state","state", LmisFields.varchar(20)));
    cols.add(new LmisColumn("amount","amount", LmisFields.integer(20)));
    cols.add(new LmisColumn("type","amount", LmisFields.varchar(20)));
    cols.add(new LmisColumn("processed","is processed", LmisFields.varchar(20)));
    cols.add(new LmisColumn("next_workflow_signal","next_signal", LmisFields.varchar(20)));
    cols.add(new LmisColumn("message_ids","messages", LmisFields.oneToMany(new MessageDB(mContext))));
    //明细
    cols.add(new LmisColumn("line_ids","voucher lines", LmisFields.oneToMany(new VoucherLineDB(mContext))));
    return cols;
  }

  //付款单明细
  public class VoucherLineDB extends LmisDatabase {
    Context mContext = null;

    public VoucherLineDB(Context context) {
      super(context);
      mContext = context;
    }

    //获取osv name
    @Override
    public String getModelName() {
      return "account.voucher.line";
    }

    //获取columns
    @Override
    public List<LmisColumn> getModelColumns() {
      List<LmisColumn> cols = new ArrayList<LmisColumn>();
      //主表id
      cols.add(new LmisColumn("voucher_id","master voucher", LmisFields.integer()));
      //名称
      cols.add(new LmisColumn("name","Description", LmisFields.varchar(200)));
      //对应的会计分录
      //cols.add(new LmisColumn("account_id","Account",LmisFields.manyToOne(new AccountDB(mContext))));
      //业务发生日期
      cols.add(new LmisColumn("date_original","Date Original", LmisFields.varchar(20)));
      //本次付款金额
      cols.add(new LmisColumn("amount","amount", LmisFields.integer()));
      return cols;
    }

  }

  //会计分录
  public class AccountDB extends LmisDatabase {
    Context mContext = null;

    public AccountDB(Context context) {
      super(context);
      mContext = context;
    }

    //获取osv name
    @Override
    public String getModelName() {
      return "account.account";
    }

    //获取columns
    @Override
    public List<LmisColumn> getModelColumns() {
      List<LmisColumn> cols = new ArrayList<LmisColumn>();
      //名称
      cols.add(new LmisColumn("name","name", LmisFields.varchar(200)));
      //分录类型
      cols.add(new LmisColumn("type","Type", LmisFields.varchar(200)));
      //贷方金额
      cols.add(new LmisColumn("credit","credit", LmisFields.integer()));
      cols.add(new LmisColumn("debit","debit", LmisFields.integer()));
      cols.add(new LmisColumn("balance","balance", LmisFields.integer()));
      return cols;
    }
  }
}
