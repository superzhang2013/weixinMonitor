package com.zh.weixinmonitor;

import android.os.Environment;

import java.io.File;

/**
 * Created by zhanghong on 2017/7/14.
 */

public class PathUtils {

    public static final String APP_ROOT_PATH = "/weixinMonitor";
    public static final String UPLOAD_FILE_PATH = APP_ROOT_PATH + "/upload/log";


    public static String getAppUploadLogPath() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + PathUtils.UPLOAD_FILE_PATH;

        File result = new File(rootPath);
        if (!result.exists())
            result.mkdirs();
        return rootPath;
    }

}
