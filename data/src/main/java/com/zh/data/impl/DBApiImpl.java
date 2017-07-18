package com.zh.data.impl;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zh.data.db.DaoFactory;
import com.zh.data.db.IDBApi;
import com.zh.data.entity.DBGroupLastChatLog;
import com.zh.data.entity.DBGroupLastChatLogDao;
import com.zh.data.entity.DBUser;
import com.zh.data.entity.DBUserDao;

import java.util.List;

import de.greenrobot.dao.query.WhereCondition;

/**
 * Created by zhanghong on 2017/7/6.
 */

public class DBApiImpl implements IDBApi {

    private Gson mGson = new Gson();

    public DBApiImpl(Context context) {
        DaoFactory.getInstance().init(context);
    }

    @Override
    public List<DBUser> getDBUserByNameAndgroupName(String groupName, String nickName) {
        DBUserDao dao = DaoFactory.getInstance().getUserDao();
        if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(nickName))
            return null;
        return dao.queryBuilder().
                where(DBUserDao.Properties.SessonGroupName.eq(groupName), new WhereCondition[]{DBUserDao.Properties.WeixinNickName.eq(nickName)}).list();

    }

    @Override
    public DBUser saveDBUser(String groupName, String nickName, String weiXinName) {
        DBUserDao dao = DaoFactory.getInstance().getUserDao();
        if (TextUtils.isEmpty(groupName)) {
            return null;
        }
        List<DBUser> dbusers = dao.queryBuilder().
                where(DBUserDao.Properties.SessonGroupName.eq(groupName), new WhereCondition[]{DBUserDao.Properties.WeiXinName.eq(weiXinName)}).list();
        if (dbusers.size() > 0) {
            DBUser dbUser = dbusers.get(0);
            dbUser.setWeixinNickName(nickName);
            return dbUser;
        } else {
            DBUser dbUser = new DBUser();
            dbUser.setWeixinNickName(nickName);
            dbUser.setSessonGroupName(groupName);
            dbUser.setWeiXinName(weiXinName);
            long id = dao.insert(dbUser);
            dbUser.setId(id);
            return dbUser;
        }
    }

    @Override
    public DBGroupLastChatLog getGroupLastChatLog(String groupName) {
        if (TextUtils.isEmpty(groupName)) {
            return null;
        }
        DBGroupLastChatLogDao dao = DaoFactory.getInstance().getDBGroupLastChatLogDao();
        List<DBGroupLastChatLog> dbGroupLastChatLogs = dao.queryBuilder().
                where(DBGroupLastChatLogDao.Properties.SessonGroupName.eq(groupName)).list();
        if (dbGroupLastChatLogs.size() > 0) {
            return dbGroupLastChatLogs.get(0);
        }
        return null;
    }

    @Override
    public DBGroupLastChatLog saveGroupLastChatLog(String groupName, String sendTime, String weixinName, String weixinNickName, String sendContent, int sendContentType) {
        DBGroupLastChatLogDao dao = DaoFactory.getInstance().getDBGroupLastChatLogDao();
        if (TextUtils.isEmpty(weixinNickName)) {
            weixinName = "";
        }
        List<DBGroupLastChatLog> dbGroupLastChatLogs = dao.queryBuilder().
                where(DBGroupLastChatLogDao.Properties.SessonGroupName.eq(groupName)).list();
        if (dbGroupLastChatLogs.size() > 0) {
            DBGroupLastChatLog dbGroupLastChatLog = dbGroupLastChatLogs.get(0);
            dbGroupLastChatLog.setSendContent(sendContent);
            dbGroupLastChatLog.setSendContentType(sendContentType);
            dbGroupLastChatLog.setSendTime(sendTime);
            dbGroupLastChatLog.setSendWeiXinName(weixinName);
            dbGroupLastChatLog.setSendWeiXinNickName(weixinNickName);
            return dbGroupLastChatLog;
        } else {
            DBGroupLastChatLog dbGroupLastChatLog = new DBGroupLastChatLog();
            dbGroupLastChatLog.setSessonGroupName(groupName);
            dbGroupLastChatLog.setSendWeiXinName(weixinName);
            dbGroupLastChatLog.setSendContent(sendContent);
            dbGroupLastChatLog.setSendContentType(sendContentType);
            dbGroupLastChatLog.setSendWeiXinNickName(weixinNickName);
            dbGroupLastChatLog.setSendTime(sendTime);
            long id = dao.insert(dbGroupLastChatLog);
            dbGroupLastChatLog.setId(id);
            return dbGroupLastChatLog;
        }
    }
}
