package qm.vp.kiev.qmhttplib.abstraction;

import org.json.JSONArray;
import org.json.JSONException;

import qm.vp.kiev.qmhttplib.cache.QMAbstractCache;
import qm.vp.kiev.qmhttplib.cache.QMArrayCache;
import qm.vp.kiev.qmhttplib.cache.QMCacheListener;


public class QMArrayRequest extends QMRequest<JSONArray> {

    public QMArrayRequest(QMRequestBuilder qmRequestBuilder) {
        super(qmRequestBuilder);
    }

    @Override
    public JSONArray obtainResult() throws JSONException {
        return new JSONArray(serverResponse);
    }

    @Override
    public QMAbstractCache<JSONArray> getCacheInstance() {
        return new QMArrayCache(qmRequestBuilder.url());
    }

    @Override
    public QMRequest<JSONArray> setListeners(QMComplete<JSONArray> qmComplete, QMError qmError) {
        super.setListeners(qmComplete, qmError);
        return this;
    }

    @Override
    public QMRequest<JSONArray> setCacheListener(QMCacheListener<JSONArray> cacheListener, QMError qmError) {
        super.setCacheListener(cacheListener, qmError);
        return this;
    }
}
