package qm.vp.kiev.qmhttplib.cache;


import android.content.Context;

import library.SQLiteSimpleDAO;

public class QMCacheDAO extends SQLiteSimpleDAO<QMCache> {

    public static final Class<QMCache> tableClass = QMCache.class;

    public QMCacheDAO(Context context) {
        super(tableClass, context);
    }
}
