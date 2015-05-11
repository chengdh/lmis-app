package com.lmis.dagger_module;

import android.content.Context;

import com.fizzbuzz.android.dagger.InjectingActivityModule;
import com.lmis.Lmis;
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
        return getAccessOrgs(context);
    }

    /**
     * 获取当前用户可访问的org列表.
     *
     * @param context the context
     * @return the list
     */
    private List<LmisDataRow> getAccessOrgs(Context context) {
        List<LmisDataRow> ret = new ArrayList<LmisDataRow>();
        //未登录用户不返回任何数据
        if (LmisUser.current(context) != null) {
            String where = "is_active = ?";
            String[] whereArgs = new String[]{"true"};
            List<LmisDataRow> orgList = new UserOrgDB(context).select(where,whereArgs);
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
            String where = "is_active = ?";
            String[] whereArgs = new String[]{"true"};

            return new OrgDB(ctx).select(where,whereArgs);
        }
        return ret;
    }

    @Provides
    @OrgModule.ExcludeAccessOrgs
    public List<LmisDataRow> providesExcludeAccessOrgs(@InjectingActivityModule.Activity Context ctx) {
        //未登录时
        LmisUser curUser = LmisUser.current(ctx);
        if (curUser != null) {
            OrgDB orgDB = new OrgDB(ctx);
            List<LmisDataRow> allOrgs = providesAllOrgs(ctx);

            List<LmisDataRow> excludeOrgs = new ArrayList<LmisDataRow>();
            List<LmisDataRow> removeOrgs = new ArrayList<LmisDataRow>();

            int defaultOrgID = curUser.getDefault_org_id();
            LmisDataRow defaultOrg = orgDB.select(defaultOrgID);

            excludeOrgs.add(defaultOrg);
            //如果当前用户所属机构是顶级机构,则选择其他机构
            Object parentOrgIdObj =   defaultOrg.get("parent_id");
            if(parentOrgIdObj != null && !parentOrgIdObj.equals("null")) {
                Integer parentOrgID = defaultOrg.getInt("parent_id");
                LmisDataRow parentOrg = orgDB.select(parentOrgID);
                excludeOrgs.add(parentOrg);
                excludeOrgs.addAll(orgDB.getChildrenOrgs(parentOrgID));
            }
            else
            {
                excludeOrgs.addAll(orgDB.getChildrenOrgs(defaultOrgID));
            }

            for(LmisDataRow o : allOrgs){
                for(LmisDataRow exOrg : excludeOrgs){
                    if(o.getInt("id").equals(exOrg.getInt("id"))){
                        removeOrgs.add(o);

                    }
                }

            }
            allOrgs.removeAll(removeOrgs);
            return allOrgs;
        }
        return null;
    }

    @Provides
    @YardsOrgs
    public List<LmisDataRow> providesYardsOrgs(@InjectingActivityModule.Activity Context ctx) {
        List<LmisDataRow> ret = new ArrayList<LmisDataRow>();
        //未登录时
        if (LmisUser.current(ctx) != null) {
            String where = "is_active = ? AND is_yard = ?";
            String[] whereArgs = new String[]{"true","true"};
            return new OrgDB(ctx).select(where, whereArgs);
        }
        return ret;
    }


    //总部机构列表
    @Provides
    @SummaryChildrenOrgs
    public List<LmisDataRow> providesSummaryOrgs(@InjectingActivityModule.Activity Context ctx) {
        List<LmisDataRow> ret = new ArrayList<LmisDataRow>();
        //未登录时
        if (LmisUser.current(ctx) != null) {
            String where = "is_active = ? AND is_summary = ?";
            String[] whereArgs = new String[]{"true","true"};
            OrgDB db = new OrgDB(ctx);
            List<LmisDataRow> summaryOrgs = db.select(where, whereArgs);
            if (summaryOrgs.size() > 0) {
                ret.addAll(summaryOrgs);
            }

            //获取总部子机构
            for (LmisDataRow o : summaryOrgs) {
                String w = "parent_id = ?";
                String[] wa = new String[]{o.getInt("id") + ""};
                List<LmisDataRow> children = db.select(w, wa);
                if (children.size() > 0) {
                    ret.addAll(children);
                }
            }

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

    //当前用户可访问机构以外的机构
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface ExcludeAccessOrgs {
    }

    //货场
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface YardsOrgs {
    }

    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface SummaryChildrenOrgs {
    }
}
