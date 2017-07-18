package com.zh.data.entity;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dBUserDaoConfig;
    private final DaoConfig dBGroupLastChatLogDaoConfig;

    private final DBUserDao dBUserDao;
    private final DBGroupLastChatLogDao dBGroupLastChatLogDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dBUserDaoConfig = daoConfigMap.get(DBUserDao.class).clone();
        dBUserDaoConfig.initIdentityScope(type);

        dBGroupLastChatLogDaoConfig = daoConfigMap.get(DBGroupLastChatLogDao.class).clone();
        dBGroupLastChatLogDaoConfig.initIdentityScope(type);

        dBUserDao = new DBUserDao(dBUserDaoConfig, this);
        dBGroupLastChatLogDao = new DBGroupLastChatLogDao(dBGroupLastChatLogDaoConfig, this);

        registerDao(DBUser.class, dBUserDao);
        registerDao(DBGroupLastChatLog.class, dBGroupLastChatLogDao);
    }
    
    public void clear() {
        dBUserDaoConfig.getIdentityScope().clear();
        dBGroupLastChatLogDaoConfig.getIdentityScope().clear();
    }

    public DBUserDao getDBUserDao() {
        return dBUserDao;
    }

    public DBGroupLastChatLogDao getDBGroupLastChatLogDao() {
        return dBGroupLastChatLogDao;
    }

}
