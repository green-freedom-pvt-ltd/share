package com.sharesmile.share.v11;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.sharesmile.share.v11.Message;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MESSAGE".
*/
public class MessageDao extends AbstractDao<Message, Long> {

    public static final String TABLENAME = "MESSAGE";

    /**
     * Properties of entity Message.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Message_image = new Property(1, String.class, "message_image", false, "MESSAGE_IMAGE");
        public final static Property Message_title = new Property(2, String.class, "message_title", false, "MESSAGE_TITLE");
        public final static Property MessageBrief = new Property(3, String.class, "messageBrief", false, "MESSAGE_BRIEF");
        public final static Property Message_description = new Property(4, String.class, "message_description", false, "MESSAGE_DESCRIPTION");
        public final static Property Message_date = new Property(5, String.class, "message_date", false, "MESSAGE_DATE");
        public final static Property ShareTemplate = new Property(6, String.class, "shareTemplate", false, "SHARE_TEMPLATE");
        public final static Property VideoId = new Property(7, String.class, "videoId", false, "VIDEO_ID");
        public final static Property Is_read = new Property(8, Boolean.class, "is_read", false, "IS_READ");
    };


    public MessageDao(DaoConfig config) {
        super(config);
    }
    
    public MessageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MESSAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"MESSAGE_IMAGE\" TEXT NOT NULL ," + // 1: message_image
                "\"MESSAGE_TITLE\" TEXT," + // 2: message_title
                "\"MESSAGE_BRIEF\" TEXT," + // 3: messageBrief
                "\"MESSAGE_DESCRIPTION\" TEXT NOT NULL ," + // 4: message_description
                "\"MESSAGE_DATE\" TEXT NOT NULL ," + // 5: message_date
                "\"SHARE_TEMPLATE\" TEXT NOT NULL ," + // 6: shareTemplate
                "\"VIDEO_ID\" TEXT NOT NULL ," + // 7: videoId
                "\"IS_READ\" INTEGER);"); // 8: is_read
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MESSAGE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Message entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getMessage_image());
 
        String message_title = entity.getMessage_title();
        if (message_title != null) {
            stmt.bindString(3, message_title);
        }
 
        String messageBrief = entity.getMessageBrief();
        if (messageBrief != null) {
            stmt.bindString(4, messageBrief);
        }
        stmt.bindString(5, entity.getMessage_description());
        stmt.bindString(6, entity.getMessage_date());
        stmt.bindString(7, entity.getShareTemplate());
        stmt.bindString(8, entity.getVideoId());
 
        Boolean is_read = entity.getIs_read();
        if (is_read != null) {
            stmt.bindLong(9, is_read ? 1L: 0L);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Message readEntity(Cursor cursor, int offset) {
        Message entity = new Message( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // message_image
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // message_title
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // messageBrief
            cursor.getString(offset + 4), // message_description
            cursor.getString(offset + 5), // message_date
            cursor.getString(offset + 6), // shareTemplate
            cursor.getString(offset + 7), // videoId
            cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0 // is_read
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Message entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMessage_image(cursor.getString(offset + 1));
        entity.setMessage_title(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setMessageBrief(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setMessage_description(cursor.getString(offset + 4));
        entity.setMessage_date(cursor.getString(offset + 5));
        entity.setShareTemplate(cursor.getString(offset + 6));
        entity.setVideoId(cursor.getString(offset + 7));
        entity.setIs_read(cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Message entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Message entity) {
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