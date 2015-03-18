package qm.vp.kiev.qmhttplib.cache;


import java.util.Date;

import qm.vp.kiev.qmhttplib.QMApplication;

public class QMCacheWriter {

    public static void save(String url, String serverResponse) {
        QMCache cache = new QMCache();
        cache.serverResponse = serverResponse;
        cache.url = url;


        QMApplication.getCache().deleteWhere(QMCache.COLUMN_URL, url);
        QMApplication.getCache().create(cache);
    }
}
