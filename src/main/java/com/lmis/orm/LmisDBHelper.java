package com.lmis.orm;

import java.util.List;

public interface LmisDBHelper {
    public String getModelName();

    public List<LmisColumn> getModelColumns();
}
