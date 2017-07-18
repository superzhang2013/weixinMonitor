package com.zh.data.server.entity;

import java.io.Serializable;

/**
 * Created by zhanghong on 2017/7/5.
 */

public class UserBean extends BaseEntity implements Serializable {

    public String sessonGroupName;

    public String userWeiXinName;

    public String weixinNickName;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("chatBean{");
        sb.append("sessonGroupName='").append(sessonGroupName).append('\'');
        sb.append(", userWeiXinName='").append(userWeiXinName).append('\'');
        sb.append(",weixinNickName='").append(weixinNickName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
