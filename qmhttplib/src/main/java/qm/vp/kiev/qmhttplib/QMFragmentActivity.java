package qm.vp.kiev.qmhttplib;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import qm.vp.kiev.qmhttplib.abstraction.*;
import qm.vp.kiev.qmhttplib.cache.QMCacheListener;


public abstract class QMFragmentActivity<T> extends FragmentActivity implements QMComplete<T>, QMError {

    private static final String TAG = QMFragmentActivity.class.toString();

    private QMProgress qmProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        QMApplication.setCurrent(this);
        qmProgress = new QMProgress(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        QMApplication.clearCurrent(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        QMProgressHide();
        super.onStop();
    }

    @Override
    protected void onResume() {
        QMApplication.setCurrent(this);
        super.onResume();
    }

    public void QMRequest(QMObjectRequest qmRequest, QMComplete<JSONObject> qmComplete, QMError qmError) {
        qmRequest.setListeners(qmComplete, qmError);
        QMApplication.makeRequest(qmRequest);
    }

    public void QMRequest(QMArrayRequest qmRequest, QMComplete<JSONArray> qmComplete, QMError qmError) {
        qmRequest.setListeners(qmComplete, qmError);
        QMApplication.makeRequest(qmRequest);
    }


    public void QMProgressShow() {
        qmProgress.show();
    }

    public void QMProgressHide() {
        if (qmProgress.isShowing()) {
            Log.e(TAG, "QMFragmentActivity qmProgress dismiss");
            qmProgress.dismiss();
        }
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
}
