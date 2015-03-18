package qm.vp.kiev.qmhttplib.utils;


import android.support.v4.app.Fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import qm.vp.kiev.qmhttplib.annotations.QMReq;

public class AnnotationUtils {

    private static final String TAG = AnnotationUtils.class.toString();


    /**
     * @param mClass - class to start search annotated methods
     * @return all methods annotated with {@link qm.vp.kiev.qmhttplib.annotations.QMReq}
     * annotation in superclass hierarchy
     */
    public static List<Method> getAnnotatedMethod(Class mClass) {
        List<Method> methods = new ArrayList<>();
        Method[] declaredMethods = mClass.getDeclaredMethods();
        /**
         * Set limit of Fragment class.
         * We can't create annotation inside Fragment class :)
         */
        if (mClass.getSuperclass() != Fragment.class) {
            methods.addAll(getAnnotatedMethod(mClass.getSuperclass()));
        }
        for (int i = declaredMethods.length - 1; i >= 0; i--) {

            QMReq qmReq = declaredMethods[i].getAnnotation(QMReq.class);
            if (qmReq != null) {
                methods.add(declaredMethods[i]);
            }
        }
        return methods;
    }
}
