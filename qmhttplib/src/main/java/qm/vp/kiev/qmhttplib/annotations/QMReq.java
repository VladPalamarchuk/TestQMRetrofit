package qm.vp.kiev.qmhttplib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import qm.vp.kiev.qmhttplib.QMType;
import qm.vp.kiev.qmhttplib.annotations.emun.QMAnswer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface QMReq {

    public String value();

    public int type() default QMType.GET;
}
