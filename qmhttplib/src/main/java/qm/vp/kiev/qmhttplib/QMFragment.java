package qm.vp.kiev.qmhttplib;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.List;

import qm.vp.kiev.qmhttplib.abstraction.QMArrayRequest;
import qm.vp.kiev.qmhttplib.abstraction.QMComplete;
import qm.vp.kiev.qmhttplib.abstraction.QMError;
import qm.vp.kiev.qmhttplib.abstraction.QMObjectRequest;
import qm.vp.kiev.qmhttplib.abstraction.QMRequestBuilder;
import qm.vp.kiev.qmhttplib.annotations.QMReq;
import qm.vp.kiev.qmhttplib.cache.QMCacheListener;
import qm.vp.kiev.qmhttplib.utils.AnnotationUtils;


public class QMFragment<T> extends Fragment implements QMComplete<T>, QMError,
        QMCacheListener<T> {

    private static final String TAG = QMFragment.class.toString();

    protected QMProgress qmProgress;

    @Override
    public void onAttach(Activity activity) {
        qmProgress = new QMProgress(activity);
        super.onAttach(activity);

        query();
    }

    @Override
    public void onDestroyView() {
        QMProgressHide();
        super.onDestroyView();
    }

    /**
     * ANNOTATION PROCESSING
     */

    protected void query() {

        List<Method> methods = AnnotationUtils.getAnnotatedMethod(getClass());
        for (final Method method : methods) {

            QMReq qmReq = method.getAnnotation(QMReq.class);
            assert qmReq != null;

            Class[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new InvalidParameterException(String.format("Method %s must have one parameter only", method.getName()));
            } else {
                Class parameter = parameterTypes[0];

                if (!parameter.isAssignableFrom(JSONObject.class) && !parameter.isAssignableFrom(JSONArray.class)) {
                    throw new InvalidParameterException(String.format("Method %s must have one parameter with JSONObject or JSONArray class type", method.getName()));
                }

                /**
                 * BEGIN MAKING QUERY
                 */
                QMRequestBuilder builder = new QMRequestBuilder()
                        .url(qmReq.value());

                final boolean isJSONObject = parameter.isAssignableFrom(JSONObject.class);
                if (isJSONObject) {
                    /**
                     * JSONObject server response
                     */
                    QMRequest(new QMObjectRequest(builder), new QMComplete<JSONObject>() {
                        @Override
                        public void qmComplete(JSONObject result) throws JSONException {
                            try {
                                method.invoke(QMFragment.this, result);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
                            }
                        }
                    }, this);
                } else {

                    /**
                     * JSONArray servers response
                     */
                    QMRequest(new QMArrayRequest(builder), new QMComplete<JSONArray>() {
                        @Override
                        public void qmComplete(JSONArray result) throws JSONException {
                            try {
                                method.invoke(QMFragment.this, result);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
                            }
                        }
                    }, this);
                }
            }
        }
    }

    /**
     * Without cache
     */
    public void QMRequest(QMObjectRequest qmRequest, QMComplete<JSONObject> qmComplete, QMError qmError) {
        qmRequest.setListeners(qmComplete, qmError);
        QMApplication.makeRequest(qmRequest);
    }

    /**
     * With cache
     */
    public void QMRequest(QMObjectRequest qmRequest, QMComplete<JSONObject> qmComplete, QMError qmError, QMCacheListener<JSONObject> qmCacheListener) {
        qmRequest.setCacheListener(qmCacheListener, qmError);

        QMRequest(qmRequest, qmComplete, qmError);
    }

    /**
     * Without cache
     */
    public void QMRequest(QMArrayRequest qmRequest, QMComplete<JSONArray> qmComplete, QMError qmError) {
        qmRequest.setListeners(qmComplete, qmError);
        QMApplication.makeRequest(qmRequest);
    }

    /**
     * With cache
     */
    public void QMRequest(QMArrayRequest qmRequest, QMComplete<JSONArray> qmComplete, QMError qmError, QMCacheListener<JSONArray> qmCacheListener) {
        qmRequest.setCacheListener(qmCacheListener, qmError);

        QMRequest(qmRequest, qmComplete, qmError);
    }

    public void QMProgressHide() {
        if (qmProgress.isShowing()) {
            qmProgress.dismiss();
        }
    }

    public void toast(String message) {
        QMUtils.toast(getActivity(), message);
    }

    public void toast(int resourceID) {
        this.toast(getActivity().getString(resourceID));
    }

    @Override
    public void qmComplete(T result) throws JSONException {

    }

    @Override
    public void qmError(int qmErrorCode) {
        switch (qmErrorCode) {
            case QMErrors.INVALID_JSON:
                QMUtils.toast(R.string.qm_invalid_json);
                break;
            default:
                QMUtils.toast(R.string.qm_error);
        }
    }

    @Override
    public void qmNetworkError() {
        QMUtils.toast(R.string.qm_check_network_connection);
    }

    @Override
    public void qmCache(T json) throws JSONException {

    }
}
