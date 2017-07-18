package com.zh.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zh.data.entity.DBGroupLastChatLogDao;
import com.zh.data.entity.DBUserDao;
import com.zh.data.entity.DaoMaster;
import com.zh.data.entity.DaoSession;

/**
 * Created by zhanghong on 2017/7/6.
 */

public class DaoFactory {

    private boolean mInited = false;

    private DaoSession mDaoSession;

    private DBGroupLastChatLogDao mDBGroupLastChatLogDao;

    private static class SingleHolder {
        private static final DaoFactory DAO_FACTORY = new DaoFactory();
    }

    public static DaoFactory getInstance() {
        return SingleHolder.DAO_FACTORY;
    }

    public void init(Context context) {
        if (!mInited) {
            DaoMaster.OpenHelper helper = new ReleaseOpenHelper(context, "weixin.db", null);
            DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
            mDaoSession = daoMaster.newSession();
            mInited = true;
        }
    }

    public DBUserDao getUserDao() {
        return mDaoSession.getDBUserDao();
    }

    public DBGroupLastChatLogDao getDBGroupLastChatLogDao(){
        return mDaoSession.getDBGroupLastChatLogDao();
    }

    public class ReleaseOpenHelper extends DaoMaster.OpenHelper {

        public ReleaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == newVersion) {
                return;
            }
        }
    }

}
