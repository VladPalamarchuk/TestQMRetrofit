package qm.vp.kiev.qmhttplib;


import android.app.Activity;
import android.app.Application;
import android.util.Log;

import library.SQLiteSimple;
import qm.vp.kiev.qmhttplib.abstraction.*;
import qm.vp.kiev.qmhttplib.cache.QMCache;
import qm.vp.kiev.qmhttplib.cache.QMCacheDAO;
import qm.vp.kiev.qmhttplib.pool.QMPool;


public class QMApplication extends Application {

    private static final String TAG = QMApplication.class.toString();


    private Activity activity;
    private QMPool qmPool;
    private QMCacheDAO qmCacheDAO;


    private static QMApplication instance;


    public static QMApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        qmPool = new QMPool();


        initCache();
    }

    /**
     * Cache database
     */
    private void initCache() {
        SQLiteSimple sqLiteSimple = new SQLiteSimple(this);
        sqLiteSimple.create(QMCacheDAO.tableClass);

        qmCacheDAO = new QMCacheDAO(this);

        Log.i(TAG, "initCache complete");
    }

    public static QMCacheDAO getCache() {
        if (instance.qmCacheDAO == null) {
            throw new RuntimeException("Cache was not initialized");
        }
        return instance.qmCacheDAO;
    }

    public static void setCurrent(Activity activity) {
        instance.activity = activity;
        instance.qmPool.resume();

        Log.e("", "QMApplication setCurrent -> " + activity.toString());
    }

    public static void clearCurrent(Activity activity) {
//        if (instance.activity.equals(activity)) {
        instance.qmPool.pause();
        instance.activity = null;
//        }

        Log.e("", "QMApplication clearCurrent -> " + activity.toString());
    }

    public static Activity getCurrent() {
        if (instance.activity == null) {
            Log.e("", "QMApplication getCurrent NuLL");
        }
        return instance.activity;
    }

    public static void makeRequest(qm.vp.kiev.qmhttplib.abstraction.QMRequest qmRequest) {
        instance.qmPool.add(qmRequest);
    }
}
