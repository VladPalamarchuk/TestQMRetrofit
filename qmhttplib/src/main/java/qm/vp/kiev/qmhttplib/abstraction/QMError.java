package qm.vp.kiev.qmhttplib.abstraction;


public interface QMError {
    public void qmError(int qmErrorCode);
    public void qmNetworkError();
}
