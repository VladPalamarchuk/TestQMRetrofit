package qm.vp.kiev.qmhttplib.abstraction;


import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import qm.vp.kiev.qmhttplib.QMType;

public class QMRequestBuilder {

    private static final String TAG = QMRequestBuilder.class.toString();

    private String url;
    private Map<String, File> fileBody;
    private Map<String, String> textBody;

    private int type = QMType.GET;

    public QMRequestBuilder() {
        fileBody = new HashMap<>();
        textBody = new HashMap<>();
    }

    public QMRequestBuilder url(String url) {

        Log.w(TAG, "QMRequestBuilder url input -> " + url);

        url = url.replace("??", "?").replace("/?", "/").replace("?/", "/");
        if (url.lastIndexOf("/") == url.length() - 1) {
            url = url.substring(0, url.length() - 1);
        }
        if (url.lastIndexOf("?") != url.length() - 1) {
            url = url + "?";
        }
        Log.w(TAG, "QMRequestBuilder url out -> " + url);

        this.url = url;
        return this;
    }

    public QMRequestBuilder simpleUrl(String url) {
        this.url = url;
        return this;
    }

    public QMRequestBuilder add(String key, String value) {
        textBody.put(key, value);
        return this;
    }

    public QMRequestBuilder add(String key, File file) {
        fileBody.put(key, file);
        return this;
    }

    public QMRequestBuilder type(int type) {
        if (type != QMType.GET && type != QMType.POST) {
            throw new RuntimeException("QMRequestBuilder undefined query type");
        }
        this.type = type;
        return this;
    }

    public String url() {
        this.url += (type == QMType.GET && !textBody.isEmpty() ? textBodyToString() : "");
        Log.d(TAG, "url() -> " + this.url);
        return this.url;
    }

    public int type() {
        return this.type;
    }

    private String textBodyToString() {
        String result = "";
        for (String key : textBody.keySet()) {
            result += "&" + key + "=" + textBody.get(key);
        }
        // skip first symbol '&'
        return result.isEmpty() ? "" : result.substring(1, result.length());
    }

    public Map<String, File> fileBody() {
        return this.fileBody;
    }

    public Map<String, String> textBody() {
        return this.textBody;
    }
}
