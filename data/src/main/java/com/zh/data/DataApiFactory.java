package com.zh.data;

import android.content.Context;

import com.zh.data.db.IDBApi;
import com.zh.data.impl.DBApiImpl;


/**
 * Created by zhanghong on 2017/7/6.
 */

public class DataApiFactory {

    private IDBApi mIDBApi;

    private static DataApiFactory sDataApiFactory;

    public static DataApiFactory getInstance() {
        if (sDataApiFactory == null) {
            synchronized (DataApiFactory.class) {
                if (sDataApiFactory == null) {
                    sDataApiFactory = new DataApiFactory();
                }
            }
        }
        return sDataApiFactory;
    }

    public void initDBApi(Context context) {
        mIDBApi = new DBApiImpl(context);
    }


    public IDBApi getIDBApi() {
        return mIDBApi;
    }

}
