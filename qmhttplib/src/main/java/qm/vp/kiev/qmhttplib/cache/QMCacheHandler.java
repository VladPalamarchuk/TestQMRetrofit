package qm.vp.kiev.qmhttplib.cache;

import android.os.Handler;
import android.os.Message;

import org.json.JSONException;

import qm.vp.kiev.qmhttplib.QMErrors;
import qm.vp.kiev.qmhttplib.abstraction.QMError;


public class QMCacheHandler<T> extends Handler {

    public static final int SUCCESS = 1;


    private final QMAbstractCache<T> qmAbstractCache;
    private QMCacheListener<T> qmCacheListener;

    private QMError qmErrorListener;

    public QMCacheHandler(QMAbstractCache<T> qmAbstractCache, QMCacheListener<T> qmCacheListener,
                          QMError qmErrorListener) {
        this.qmCacheListener = qmCacheListener;
        this.qmErrorListener = qmErrorListener;
        this.qmAbstractCache = qmAbstractCache;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        try {
            if (msg.what == SUCCESS) {
                T result = qmAbstractCache.getCache();
                if (result != null) {
                    qmCacheListener.qmCache(result);
                }
            }
        } catch (JSONException e) {
            qmErrorListener.qmError(QMErrors.INVALID_JSON);
        }
    }

    public void readCache() {
        Message message = new Message();
        message.what = SUCCESS;

        sendMessage(message);
    }
}
