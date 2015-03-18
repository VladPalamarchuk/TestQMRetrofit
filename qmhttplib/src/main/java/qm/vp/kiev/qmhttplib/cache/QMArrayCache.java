package qm.vp.kiev.qmhttplib.cache;

import org.json.JSONArray;
import org.json.JSONException;


public class QMArrayCache extends QMAbstractCache<JSONArray> {

    public QMArrayCache(String url) {
        super(url);
    }

    @Override
    public JSONArray getCache() throws JSONException {
        String cacheString = readCacheAsString();
        if (cacheString != null && !cacheString.isEmpty()) {
            return new JSONArray(cacheString);
        }
        return null;
    }
}
