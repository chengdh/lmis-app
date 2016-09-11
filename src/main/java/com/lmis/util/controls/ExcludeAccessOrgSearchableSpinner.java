package com.lmis.util.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import com.fizzbuzz.android.dagger.Injector;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.lmis.dagger_module.OrgModule;
import com.lmis.orm.LmisDataRow;
import com.rajasharan.widget.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by chengdh on 16/9/10.
 */
public class ExcludeAccessOrgSearchableSpinner extends SearchableSpinner {
    @Inject
    @OrgModule.ExcludeAccessOrgs
    List<LmisDataRow> mAccessOrgs;
    ArrayAdapter<LmisDataRow> mAdapter = null;

    public ExcludeAccessOrgSearchableSpinner(Context context) {
        super(context);
        ((Injector) context).inject(this);
    }

    public ExcludeAccessOrgSearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Injector) context).inject(this);
        String[] names = getOrgNames();
        setList(names);

    }

    public ExcludeAccessOrgSearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((Injector) context).inject(this);

        String[] names = getOrgNames();
        setList(names);
    }

    /**
     * 获取选择的org.
     *
     * @return the lmis data row
     */
    public LmisDataRow getSelectedOrg() {
        String selectedText = getSelectedItem();
        if (selectedText == null) {
            return null;
        }
        int pos = selectedText.indexOf("<");
        if (pos > 0) {
            String orgName = selectedText.substring(0, pos);
            for (int i = 0; i < mAccessOrgs.size(); i++) {
                if (mAccessOrgs.get(i).getString("name").equals(orgName)) {
                    return mAccessOrgs.get(i);
                }

            }
        }

        return null;

    }

    private String[] getOrgNames() {
        List<String> orgNames = new ArrayList<>();
        for (int i = 0; i < mAccessOrgs.size(); i++) {
            LmisDataRow org = mAccessOrgs.get(i);
            String pinyin = "";
            try {
                pinyin = PinyinHelper.getShortPinyin(org.getString("name"));
            } catch (PinyinException e) {
                e.printStackTrace();
            }
            orgNames.add(org.getString("name") + "<" + pinyin + ">");
        }
        return orgNames.toArray(new String[0]);
    }
}
