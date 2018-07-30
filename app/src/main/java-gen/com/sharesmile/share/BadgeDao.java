package com.sharesmile.share;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.sharesmile.share.Badge;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "BADGE".
*/
public class BadgeDao extends AbstractDao<Badge, Long> {

    public static final String TABLENAME = "BADGE";

    /**
     * Properties of entity Badge.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property BadgeId = new Property(1, int.class, "badgeId", false, "BADGE_ID");
        public final static Property Type = new Property(2, String.class, "type", false, "TYPE");
        public final static Property Category = new Property(3, String.class, "category", false, "CATEGORY");
        public final static Property Name = new Property(4, String.class, "name", false, "NAME");
        public final static Property NoOfStars = new Property(5, int.class, "noOfStars", false, "NO_OF_STARS");
        public final static Property ImageUrl = new Property(6, String.class, "imageUrl", false, "IMAGE_URL");
        public final static Property Description1 = new Property(7, String.class, "description1", false, "DESCRIPTION1");
        public final static Property Description2 = new Property(8, String.class, "description2", false, "DESCRIPTION2");
        public final static Property Description3 = new Property(9, String.class, "description3", false, "DESCRIPTION3");
        public final static Property Description_inprogress = new Property(10, String.class, "description_inprogress", false, "DESCRIPTION_INPROGRESS");
        public final static Property Share_badge_content = new Property(11, String.class, "share_badge_content", false, "SHARE_BADGE_CONTENT");
        public final static Property BadgeParameter = new Property(12, double.class, "badgeParameter", false, "BADGE_PARAMETER");
        public final static Property BadgeParameterCheck = new Property(13, String.class, "badgeParameterCheck", false, "BADGE_PARAMETER_CHECK");
    };


    public BadgeDao(DaoConfig config) {
        super(config);
    }
    
    public BadgeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BADGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"BADGE_ID\" INTEGER NOT NULL ," + // 1: badgeId
                "\"TYPE\" TEXT NOT NULL ," + // 2: type
                "\"CATEGORY\" TEXT NOT NULL ," + // 3: category
                "\"NAME\" TEXT NOT NULL ," + // 4: name
                "\"NO_OF_STARS\" INTEGER NOT NULL ," + // 5: noOfStars
                "\"IMAGE_URL\" TEXT," + // 6: imageUrl
                "\"DESCRIPTION1\" TEXT NOT NULL ," + // 7: description1
                "\"DESCRIPTION2\" TEXT," + // 8: description2
                "\"DESCRIPTION3\" TEXT," + // 9: description3
                "\"DESCRIPTION_INPROGRESS\" TEXT," + // 10: description_inprogress
                "\"SHARE_BADGE_CONTENT\" TEXT," + // 11: share_badge_content
                "\"BADGE_PARAMETER\" REAL NOT NULL ," + // 12: badgeParameter
                "\"BADGE_PARAMETER_CHECK\" TEXT);"); // 13: badgeParameterCheck
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BADGE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Badge entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getBadgeId());
        stmt.bindString(3, entity.getType());
        stmt.bindString(4, entity.getCategory());
        stmt.bindString(5, entity.getName());
        stmt.bindLong(6, entity.getNoOfStars());
 
        String imageUrl = entity.getImageUrl();
        if (imageUrl != null) {
            stmt.bindString(7, imageUrl);
        }
        stmt.bindString(8, entity.getDescription1());
 
        String description2 = entity.getDescription2();
        if (description2 != null) {
            stmt.bindString(9, description2);
        }
 
        String description3 = entity.getDescription3();
        if (description3 != null) {
            stmt.bindString(10, description3);
        }
 
        String description_inprogress = entity.getDescription_inprogress();
        if (description_inprogress != null) {
            stmt.bindString(11, description_inprogress);
        }
 
        String share_badge_content = entity.getShare_badge_content();
        if (share_badge_content != null) {
            stmt.bindString(12, share_badge_content);
        }
        stmt.bindDouble(13, entity.getBadgeParameter());
 
        String badgeParameterCheck = entity.getBadgeParameterCheck();
        if (badgeParameterCheck != null) {
            stmt.bindString(14, badgeParameterCheck);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Badge readEntity(Cursor cursor, int offset) {
        Badge entity = new Badge( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // badgeId
            cursor.getString(offset + 2), // type
            cursor.getString(offset + 3), // category
            cursor.getString(offset + 4), // name
            cursor.getInt(offset + 5), // noOfStars
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // imageUrl
            cursor.getString(offset + 7), // description1
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // description2
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // description3
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // description_inprogress
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // share_badge_content
            cursor.getDouble(offset + 12), // badgeParameter
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13) // badgeParameterCheck
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Badge entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setBadgeId(cursor.getInt(offset + 1));
        entity.setType(cursor.getString(offset + 2));
        entity.setCategory(cursor.getString(offset + 3));
        entity.setName(cursor.getString(offset + 4));
        entity.setNoOfStars(cursor.getInt(offset + 5));
        entity.setImageUrl(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setDescription1(cursor.getString(offset + 7));
        entity.setDescription2(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setDescription3(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setDescription_inprogress(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setShare_badge_content(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setBadgeParameter(cursor.getDouble(offset + 12));
        entity.setBadgeParameterCheck(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Badge entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Badge entity) {
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
