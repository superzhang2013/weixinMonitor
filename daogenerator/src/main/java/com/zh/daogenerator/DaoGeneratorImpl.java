package com.zh.daogenerator;
import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by zhanghong on 2017/7/6.
 */

public class DaoGeneratorImpl {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(6, "com.demo.zh.data.entity");

        addUserBean(schema);
        addChatBean(schema);

        new DaoGenerator().generateAll(schema, "./data/src/main/java");

    }

    public static void addUserBean(Schema schema) {
        Entity entity = schema.addEntity("DBUser");

        entity.addLongProperty("id").primaryKey().autoincrement();
        entity.addStringProperty("sessonGroupName");
        entity.addStringProperty("weiXinName");
        entity.addStringProperty("weixinNickName");
    }

    public static void addChatBean(Schema schema) {
        Entity entity = schema.addEntity("DBGroupLastChatLog");

        entity.addLongProperty("id").primaryKey().autoincrement();
        entity.addStringProperty("sendTime");
        entity.addStringProperty("sessonGroupName");
        entity.addStringProperty("sendWeiXinName");
        entity.addStringProperty("sendWeiXinNickName");
        entity.addStringProperty("sendContent");
        entity.addIntProperty("sendContentType");
    }
}
