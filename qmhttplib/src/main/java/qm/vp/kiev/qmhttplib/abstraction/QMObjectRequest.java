package qm.vp.kiev.qmhttplib.abstraction;

import org.json.JSONException;
import org.json.JSONObject;

import qm.vp.kiev.qmhttplib.cache.QMAbstractCache;
import qm.vp.kiev.qmhttplib.cache.QMCacheListener;
import qm.vp.kiev.qmhttplib.cache.QMObjectCache;


public class QMObjectRequest extends QMRequest<JSONObject> {

    public QMObjectRequest(QMRequestBuilder qmRequestBuilder) {
        super(qmRequestBuilder);
    }

    @Override
    public JSONObject obtainResult() throws JSONException {
        return new JSONObject(serverResponse);
    }

    @Override
    public QMAbstractCache<JSONObject> getCacheInstance() {
        return new QMObjectCache(qmRequestBuilder.url());
    }

    @Override
    public QMRequest<JSONObject> setListeners(QMComplete<JSONObject> qmComplete, QMError qmError) {
        super.setListeners(qmComplete, qmError);

        return this;
    }

    @Override
    public QMRequest<JSONObject> setCacheListener(QMCacheListener<JSONObject> cacheListener, QMError qmError) {
        super.setCacheListener(cacheListener, qmError);
        return this;
    }
}
