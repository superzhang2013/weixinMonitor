package com.zh.data.server.entity;

import com.google.gson.Gson;

/**
 * Created by zhanghanguo@yy.com on 2015/11/12.
 */
public class BaseEntity implements ICheckable, IJsonable {

    private static Gson sGson = new Gson();

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String writeJson() {
        return sGson.toJson(this);
    }
}
