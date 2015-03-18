package qm.vp.kiev.qmhttplib.abstraction;


import android.os.Looper;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import qm.vp.kiev.qmhttplib.QMApplication;
import qm.vp.kiev.qmhttplib.QMErrors;
import qm.vp.kiev.qmhttplib.QMType;
import qm.vp.kiev.qmhttplib.QMUtils;
import qm.vp.kiev.qmhttplib.cache.*;

public abstract class QMRequest<T> implements Runnable {

    private static final String TAG = QMRequest.class.toString();

    private QMHandler<T> qmHandler;
    private QMCacheHandler<T> qmCacheHandler;

    protected QMRequestBuilder qmRequestBuilder;

    private DefaultHttpClient httpClient;

    protected String serverResponse;

    public QMRequest<?> setListeners(QMComplete<T> qmComplete, QMError qmError) {
        qmHandler = new QMHandler<>(this, qmComplete, qmError);
        return this;
    }

    public QMRequest<?> setCacheListener(QMCacheListener<T> cacheListener, QMError qmError) {
        this.qmCacheHandler = new QMCacheHandler<>(getCacheInstance(), cacheListener, qmError);
        return this;
    }

    public QMRequest(QMRequestBuilder qmRequestBuilder) {
        this.qmRequestBuilder = qmRequestBuilder;
    }

    @Override
    public void run() {

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        if (cacheEnabled()) {
            qmCacheHandler.readCache();
        }

        if (!QMUtils.isNetworkOnline(QMApplication.getInstance())) {
            qmHandler.networkError();
            return;
        }

        qmHandler.progress();

        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                10000);


        httpClient = new DefaultHttpClient(httpParams);
//        String respJSON;

        try {
            switch (qmRequestBuilder.type()) {
                case QMType.GET:
                    serverResponse = get();
                    break;
                case QMType.POST:
                    serverResponse = post();
                    break;
                default:
                    throw new RuntimeException("Unknown request type");
            }

//            Log.e(QMRequest.class.toString(), "DONE");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR: " + e.toString());

            qmHandler.error(QMErrors.UNKNOWN);
            return;
        }

        Log.d(TAG, serverResponse);

        saveCache(serverResponse);

        qmHandler.complete();
    }

    private boolean cacheEnabled() {
        return qmCacheHandler != null;
    }

    public void saveCache(String serverResponse) {
        if (qmRequestBuilder.type() == QMType.GET) {
            QMCacheWriter.save(qmRequestBuilder.url(), serverResponse);
        }
    }

    private String get() throws IOException {
        HttpGet httpGet = new HttpGet(qmRequestBuilder.url());


        Log.e(TAG, "get return:" + toString(httpClient.execute(httpGet).getEntity().getContent()));

        return toString(httpClient.execute(httpGet).getEntity().getContent());
    }

    private String post() throws IOException {
        HttpPost post = new HttpPost(qmRequestBuilder.url());
        post.setEntity(multipart(qmRequestBuilder.fileBody(), qmRequestBuilder.textBody()));
        return toString(httpClient.execute(post).getEntity().getContent());
    }

    public static String toString(InputStream stream) throws IOException {
        if (stream != null) {
            int n = 0;
            char[] buffer = new char[1024 * 4];
            InputStreamReader reader = new InputStreamReader(stream, "UTF8");
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
            return writer.toString();
        }

        return null;
    }

    public static HttpEntity multipart(Map<String, File> fileBody, Map<String, String> textBody) {
        MultipartEntity entityBuilder = new MultipartEntity();
        for (String key : fileBody.keySet()) {
            entityBuilder.addPart(key, value(fileBody.get(key)));
            Log.w(TAG, "fileBody: key -> " + key + ", value -> " + value(fileBody.get(key)));
        }
        for (String key : textBody.keySet()) {
            entityBuilder.addPart(key, value(textBody.get(key)));
            Log.w(TAG, "textBody: key -> " + key + ", value -> " + value(textBody.get(key)));
        }
        return entityBuilder;
    }

    private static ContentBody value(File file) {
        return new FileBody(file);
    }

    private static ContentBody value(String text) {
        try {
            return new StringBody(text);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    abstract public T obtainResult() throws JSONException;

    abstract public QMAbstractCache<T> getCacheInstance();

}
