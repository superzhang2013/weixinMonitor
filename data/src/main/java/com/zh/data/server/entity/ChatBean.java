package com.zh.data.server.entity;

import java.io.Serializable;

/**
 * Created by zhanghong on 2017/7/5.
 */

public class ChatBean extends BaseEntity implements Serializable {

    public String sendTime;

    public String sendContent;

    public int contentType;

    public UserBean chatBean;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("sendTime='").append(sendTime).append('\'');
        sb.append("sendContent='").append(sendContent).append('\'');
        sb.append("contentType=").append(contentType);
        sb.append(", chatUserBean='").append(chatBean).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
