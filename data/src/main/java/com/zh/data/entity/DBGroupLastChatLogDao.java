package com.zh.data.entity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DBGROUP_LAST_CHAT_LOG".
*/
public class DBGroupLastChatLogDao extends AbstractDao<DBGroupLastChatLog, Long> {

    public static final String TABLENAME = "DBGROUP_LAST_CHAT_LOG";

    /**
     * Properties of entity DBGroupLastChatLog.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property SendTime = new Property(1, String.class, "sendTime", false, "SEND_TIME");
        public final static Property SessonGroupName = new Property(2, String.class, "sessonGroupName", false, "SESSON_GROUP_NAME");
        public final static Property SendWeiXinName = new Property(3, String.class, "sendWeiXinName", false, "SEND_WEI_XIN_NAME");
        public final static Property SendWeiXinNickName = new Property(4, String.class, "sendWeiXinNickName", false, "SEND_WEI_XIN_NICK_NAME");
        public final static Property SendContent = new Property(5, String.class, "sendContent", false, "SEND_CONTENT");
        public final static Property SendContentType = new Property(6, Integer.class, "sendContentType", false, "SEND_CONTENT_TYPE");
    };


    public DBGroupLastChatLogDao(DaoConfig config) {
        super(config);
    }
    
    public DBGroupLastChatLogDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DBGROUP_LAST_CHAT_LOG\" (" + //
                "\"ID\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"SEND_TIME\" TEXT," + // 1: sendTime
                "\"SESSON_GROUP_NAME\" TEXT," + // 2: sessonGroupName
                "\"SEND_WEI_XIN_NAME\" TEXT," + // 3: sendWeiXinName
                "\"SEND_WEI_XIN_NICK_NAME\" TEXT," + // 4: sendWeiXinNickName
                "\"SEND_CONTENT\" TEXT," + // 5: sendContent
                "\"SEND_CONTENT_TYPE\" INTEGER);"); // 6: sendContentType
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DBGROUP_LAST_CHAT_LOG\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DBGroupLastChatLog entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String sendTime = entity.getSendTime();
        if (sendTime != null) {
            stmt.bindString(2, sendTime);
        }
 
        String sessonGroupName = entity.getSessonGroupName();
        if (sessonGroupName != null) {
            stmt.bindString(3, sessonGroupName);
        }
 
        String sendWeiXinName = entity.getSendWeiXinName();
        if (sendWeiXinName != null) {
            stmt.bindString(4, sendWeiXinName);
        }
 
        String sendWeiXinNickName = entity.getSendWeiXinNickName();
        if (sendWeiXinNickName != null) {
            stmt.bindString(5, sendWeiXinNickName);
        }
 
        String sendContent = entity.getSendContent();
        if (sendContent != null) {
            stmt.bindString(6, sendContent);
        }
 
        Integer sendContentType = entity.getSendContentType();
        if (sendContentType != null) {
            stmt.bindLong(7, sendContentType);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public DBGroupLastChatLog readEntity(Cursor cursor, int offset) {
        DBGroupLastChatLog entity = new DBGroupLastChatLog( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // sendTime
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // sessonGroupName
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // sendWeiXinName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // sendWeiXinNickName
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // sendContent
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6) // sendContentType
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DBGroupLastChatLog entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setSendTime(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSessonGroupName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setSendWeiXinName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setSendWeiXinNickName(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setSendContent(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setSendContentType(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(DBGroupLastChatLog entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(DBGroupLastChatLog entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
