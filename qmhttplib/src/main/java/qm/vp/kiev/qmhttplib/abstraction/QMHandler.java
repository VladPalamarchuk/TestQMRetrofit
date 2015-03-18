package qm.vp.kiev.qmhttplib.abstraction;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import qm.vp.kiev.qmhttplib.QMApplication;
import qm.vp.kiev.qmhttplib.QMErrors;
import qm.vp.kiev.qmhttplib.QMProgress;


public class QMHandler<T> extends Handler {
    private static final String TAG = QMHandler.class.toString();

    private static final int SUCCESS = 1;
    private static final int ERROR = 2;
    private static final int NETWORK_ERROR = 3;
    private static final int PROGRESS_SHOW = 4;


    private QMComplete<T> qmComplete;
    private QMError qmError;
    private QMRequest<T> qmRequest;


    private QMProgress qmProgress;


    public QMHandler(QMRequest<T> qmRequest, QMComplete<T> qmComplete, QMError qmError) {
        this.qmComplete = qmComplete;
        this.qmError = qmError;
        this.qmRequest = qmRequest;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        try {
            switch (msg.what) {
                case SUCCESS:
                    qmComplete.qmComplete(qmRequest.obtainResult());
                    break;

                case ERROR:
                    qmError.qmError((Integer) msg.obj);
                    break;

                case NETWORK_ERROR:
                    qmError.qmNetworkError();
                    break;

                case PROGRESS_SHOW:
                    handlerProgressShow();
                    return;
            }

        } catch (JSONException e) {
            Log.w(TAG, e.toString());
            qmError.qmError(QMErrors.INVALID_JSON);
        }

        handleProgressHide();
    }


    public void networkError() {
        Message message = new Message();
        message.what = NETWORK_ERROR;

        sendMessage(message);
    }

    public void error(int error) {
        Message message = new Message();
        message.what = ERROR;
        message.obj = error;

        sendMessage(message);
    }

    private void handleProgressHide() {
        if (qmProgress != null) {
            qmProgress.dismiss();
        }
    }

    public void progress() {
        sendEmptyMessage(PROGRESS_SHOW);
    }

    private void handlerProgressShow() {
        Activity current = QMApplication.getCurrent();
        if (current != null) {
            qmProgress = new QMProgress(current);
            qmProgress.show();
        } else {
            Log.e(QMHandler.class.toString(), "Activity is NuLL");
        }
    }


    public void complete() {
        Message message = new Message();
        message.what = SUCCESS;

        sendMessage(message);
    }
}
