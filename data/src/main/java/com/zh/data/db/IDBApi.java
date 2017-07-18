package com.zh.data.db;

import com.zh.data.entity.DBGroupLastChatLog;
import com.zh.data.entity.DBUser;

import java.util.List;

/**
 * Created by zhanghong on 2017/7/6.
 */

public interface IDBApi {

    /**
     * 根据用户所在群以及昵称查找对应的微信名
     *
     * @param groupName
     * @param nickName
     * @return
     */
    public List<DBUser> getDBUserByNameAndgroupName(String groupName, String nickName);


    /**
     * 保存用户信息至数据库中
     *
     * @param groupName
     * @param nickName
     * @param weiXinName
     * @return
     */
    public DBUser saveDBUser(String groupName, String nickName, String weiXinName);

    /**
     * 获取群聊天中的最近一条消息
     *
     * @param groupName
     * @return
     */
    public DBGroupLastChatLog getGroupLastChatLog(String groupName);

    public DBGroupLastChatLog saveGroupLastChatLog(String groupName, String sendTime, String weixinName, String weixinNickName, String sendContent, int sendContentType);
}
