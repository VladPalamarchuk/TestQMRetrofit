package qm.vp.kiev.qmhttplib.cache;


import java.util.Date;

import library.annotations.Column;
import library.annotations.Table;

@Table
public class QMCache {

    public static final String COLUMN_URL = "url";

    @Column(isPrimaryKey = true, name = COLUMN_URL)
    public String url;

    @Column
    public String serverResponse;
}
