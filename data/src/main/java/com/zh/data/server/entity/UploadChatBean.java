package com.zh.data.server.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhanghong on 2017/7/14.
 */

public class UploadChatBean extends BaseEntity implements Serializable {

    public String sendTime;     //会话开始时间，格式  YYYY-MM-DD HH:MM

    public String sessonGroupName;  // 群名，一般是学员手机号_考试id

    public String sessonGroupId;    //群名md5值

    public int sendChatContentCount;    //消息条数

    public List<ChatBean> sendContent;    // 消息内容，数组

}
