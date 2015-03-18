package qm.vp.kiev.qmhttplib.cache;

import org.json.JSONException;
import org.json.JSONObject;


public class QMObjectCache extends QMAbstractCache<JSONObject> {

    public QMObjectCache(String url) {
        super(url);
    }

    @Override
    public JSONObject getCache() throws JSONException {
        final String cacheString = readCacheAsString();

        if (cacheString != null && !cacheString.isEmpty()) {
            return new JSONObject(cacheString);
        } else {
            return null;
        }
    }
}
