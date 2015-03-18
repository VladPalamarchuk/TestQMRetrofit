package qm.vp.kiev.qmhttplib.cache;


import org.json.JSONException;
import org.json.JSONObject;

public interface QMCacheListener<T> {

    public void qmCache(T json) throws JSONException;
}
