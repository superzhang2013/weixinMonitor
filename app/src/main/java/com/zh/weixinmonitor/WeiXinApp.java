package com.zh.weixinmonitor;

import android.app.Application;

import com.zh.data.DataApiFactory;


/**
 * Created by zhanghong on 2017/7/6.
 */

public class WeiXinApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DataApiFactory.getInstance().initDBApi(this);
    }

}
