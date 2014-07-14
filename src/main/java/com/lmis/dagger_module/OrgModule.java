package com.lmis.dagger_module;

import android.content.Context;

import com.fizzbuzz.android.dagger.InjectingActivityModule;
import com.lmis.base.org.OrgDB;
import com.lmis.base.user_org.UserOrgDB;
import com.lmis.orm.LmisDataRow;
import com.lmis.support.LmisUser;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Qualifier;

import dagger.Module;
import dagger.Provides;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by chengdh on 14-6-17.
 * 提供org注入相关功能
 * 1 所有机构列表
 * 2 当前登录用户可访问机构列表
 * 3 中转货场列表
 * 4 总部列表
 * ------------------------------------
 * 以上对应的arrayAdapter
 */
@Module(complete = false, library = true)
public class OrgModule {

    /**
     * 获取当前登录用户可访问的组织机构列表
     *
     * @return the list
     */
    @Provides
    @AccessOrgs
    public List<LmisDataRow> provideAccessOrgs(@InjectingActivityModule.Activity Context context) {
        List<LmisDataRow> ret = new ArrayList<LmisDataRow>();
        //未登录用户不返回任何数据
        if (LmisUser.current(context) != null) {
            List<LmisDataRow> orgList = new UserOrgDB(context).select();
            for (Iterator<LmisDataRow> i = orgList.iterator(); i.hasNext(); ) {
                ret.add(i.next().getM2ORecord("org_id").browse());
            }
        }
        return ret;
    }

    @Provides
    @AllOrgs
    public List<LmisDataRow> providesAllOrgs(@InjectingActivityModule.Activity Context ctx) {
        List<LmisDataRow> ret = new ArrayList<LmisDataRow>();
        //未登录时
        if (LmisUser.current(ctx) != null) {
            return new OrgDB(ctx).select();
        }
        return ret;
    }

    @Provides
    @YardsOrgs
    public List<LmisDataRow> providesYardsOrgs(@InjectingActivityModule.Activity Context ctx) {
        List<LmisDataRow> ret = new ArrayList<LmisDataRow>();
        //未登录时
        if (LmisUser.current(ctx) != null) {
            String where = "is_yard = ?";
            String[] whereArgs = new String[]{"true"};
            return new OrgDB(ctx).select(where, whereArgs);
        }
        return ret;
    }

    //所有机构
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface AllOrgs {
    }

    //当前用户可访问机构
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface AccessOrgs {
    }

    //货场
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface YardsOrgs {
    }
}
