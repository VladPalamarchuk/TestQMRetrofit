package qm.vp.kiev.qmhttplib.abstraction;


import org.json.JSONException;

public interface QMComplete<T> {

    public void qmComplete(T result) throws JSONException;
}
