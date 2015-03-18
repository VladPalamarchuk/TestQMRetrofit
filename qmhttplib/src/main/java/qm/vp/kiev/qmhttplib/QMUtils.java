package qm.vp.kiev.qmhttplib;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import qm.vp.kiev.qmhttplib.abstraction.QMError;

public class QMUtils {
    public static boolean isNetworkOnline(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }

    public static void toast(String message) {
        Activity activity = QMApplication.getCurrent();
        if (activity != null) {
            toast(activity, message);
        }
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public static void toast(int messageResource) {
        Activity activity = QMApplication.getCurrent();
        if (activity != null) {
            toast(activity.getString(messageResource));
        }
    }

    public static boolean isSuccess(JSONObject jsonObject) {
        return jsonObject.optString("status").equalsIgnoreCase("success");
    }

    public static void toast(Context context, int resID) {
        toast(context, context.getString(resID));
    }

    public static AlertDialog alert(String message) {
        Activity activity = QMApplication.getCurrent();
        if (activity != null) {
            return new AlertDialog.Builder(activity).setMessage(message).show();
        }
        return null;
    }

    public static void QMError(int code) {
        switch (code) {
            case QMErrors.INVALID_JSON:
                QMUtils.toast(R.string.qm_invalid_json);
                break;
            default:
                QMUtils.toast(R.string.qm_error);
        }
    }

    public static void QMNetworkError() {
        QMUtils.toast(R.string.qm_check_network_connection);
    }

    public static void handleError(int qmErrorCode) {
        if (qmErrorCode == QMErrors.INVALID_JSON) {
            QMUtils.toast("Invalid json, look logcat to find more details");
        } else {
            QMUtils.toast("Some error while making query, logcat helps you:) ");
        }
    }
}
