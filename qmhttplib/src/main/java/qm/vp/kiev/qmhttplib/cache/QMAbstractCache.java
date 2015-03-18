package qm.vp.kiev.qmhttplib.cache;

import org.json.JSONException;

import qm.vp.kiev.qmhttplib.QMApplication;

public abstract class QMAbstractCache<T> {

    private static final QMCacheDAO qmCacheDAO;

    static {
        qmCacheDAO = QMApplication.getCache();
    }

    private final String url;

    public QMAbstractCache(String url) {
        this.url = url;
    }

    protected String readCacheAsString() {
        QMCache qmCache = qmCacheDAO.readWhere(QMCache.COLUMN_URL, url);
        return qmCache != null ? qmCache.serverResponse : null;
    }

    public abstract T getCache() throws JSONException;
}
