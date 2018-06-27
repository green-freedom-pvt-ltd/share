package com.sharesmile.share;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.sharesmile.share.AchievedBadge;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ACHIEVED_BADGE".
*/
public class AchievedBadgeDao extends AbstractDao<AchievedBadge, Long> {

    public static final String TABLENAME = "ACHIEVED_BADGE";

    /**
     * Properties of entity AchievedBadge.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ServerId = new Property(1, long.class, "serverId", false, "SERVER_ID");
        public final static Property UserId = new Property(2, long.class, "userId", false, "USER_ID");
        public final static Property CauseId = new Property(3, long.class, "causeId", false, "CAUSE_ID");
        public final static Property CauseName = new Property(4, String.class, "causeName", false, "CAUSE_NAME");
        public final static Property BadgeIdInProgress = new Property(5, long.class, "badgeIdInProgress", false, "BADGE_ID_IN_PROGRESS");
        public final static Property BadgeIdAchieved = new Property(6, long.class, "badgeIdAchieved", false, "BADGE_ID_ACHIEVED");
        public final static Property BadgeIdAchievedDate = new Property(7, java.util.Date.class, "badgeIdAchievedDate", false, "BADGE_ID_ACHIEVED_DATE");
        public final static Property NoOfStarAchieved = new Property(8, Integer.class, "noOfStarAchieved", false, "NO_OF_STAR_ACHIEVED");
        public final static Property BadgeType = new Property(9, String.class, "badgeType", false, "BADGE_TYPE");
        public final static Property Category = new Property(10, String.class, "category", false, "CATEGORY");
        public final static Property CategoryStatus = new Property(11, String.class, "categoryStatus", false, "CATEGORY_STATUS");
        public final static Property ParamDone = new Property(12, double.class, "paramDone", false, "PARAM_DONE");
        public final static Property IsSync = new Property(13, boolean.class, "isSync", false, "IS_SYNC");
    };


    public AchievedBadgeDao(DaoConfig config) {
        super(config);
    }
    
    public AchievedBadgeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ACHIEVED_BADGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"SERVER_ID\" INTEGER NOT NULL ," + // 1: serverId
                "\"USER_ID\" INTEGER NOT NULL ," + // 2: userId
                "\"CAUSE_ID\" INTEGER NOT NULL ," + // 3: causeId
                "\"CAUSE_NAME\" TEXT NOT NULL ," + // 4: causeName
                "\"BADGE_ID_IN_PROGRESS\" INTEGER NOT NULL ," + // 5: badgeIdInProgress
                "\"BADGE_ID_ACHIEVED\" INTEGER NOT NULL ," + // 6: badgeIdAchieved
                "\"BADGE_ID_ACHIEVED_DATE\" INTEGER," + // 7: badgeIdAchievedDate
                "\"NO_OF_STAR_ACHIEVED\" INTEGER," + // 8: noOfStarAchieved
                "\"BADGE_TYPE\" TEXT NOT NULL ," + // 9: badgeType
                "\"CATEGORY\" TEXT NOT NULL ," + // 10: category
                "\"CATEGORY_STATUS\" TEXT NOT NULL ," + // 11: categoryStatus
                "\"PARAM_DONE\" REAL NOT NULL ," + // 12: paramDone
                "\"IS_SYNC\" INTEGER NOT NULL );"); // 13: isSync
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ACHIEVED_BADGE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, AchievedBadge entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getServerId());
        stmt.bindLong(3, entity.getUserId());
        stmt.bindLong(4, entity.getCauseId());
        stmt.bindString(5, entity.getCauseName());
        stmt.bindLong(6, entity.getBadgeIdInProgress());
        stmt.bindLong(7, entity.getBadgeIdAchieved());
 
        java.util.Date badgeIdAchievedDate = entity.getBadgeIdAchievedDate();
        if (badgeIdAchievedDate != null) {
            stmt.bindLong(8, badgeIdAchievedDate.getTime());
        }
 
        Integer noOfStarAchieved = entity.getNoOfStarAchieved();
        if (noOfStarAchieved != null) {
            stmt.bindLong(9, noOfStarAchieved);
        }
        stmt.bindString(10, entity.getBadgeType());
        stmt.bindString(11, entity.getCategory());
        stmt.bindString(12, entity.getCategoryStatus());
        stmt.bindDouble(13, entity.getParamDone());
        stmt.bindLong(14, entity.getIsSync() ? 1L: 0L);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public AchievedBadge readEntity(Cursor cursor, int offset) {
        AchievedBadge entity = new AchievedBadge( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // serverId
            cursor.getLong(offset + 2), // userId
            cursor.getLong(offset + 3), // causeId
            cursor.getString(offset + 4), // causeName
            cursor.getLong(offset + 5), // badgeIdInProgress
            cursor.getLong(offset + 6), // badgeIdAchieved
            cursor.isNull(offset + 7) ? null : new java.util.Date(cursor.getLong(offset + 7)), // badgeIdAchievedDate
            cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8), // noOfStarAchieved
            cursor.getString(offset + 9), // badgeType
            cursor.getString(offset + 10), // category
            cursor.getString(offset + 11), // categoryStatus
            cursor.getDouble(offset + 12), // paramDone
            cursor.getShort(offset + 13) != 0 // isSync
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, AchievedBadge entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setServerId(cursor.getLong(offset + 1));
        entity.setUserId(cursor.getLong(offset + 2));
        entity.setCauseId(cursor.getLong(offset + 3));
        entity.setCauseName(cursor.getString(offset + 4));
        entity.setBadgeIdInProgress(cursor.getLong(offset + 5));
        entity.setBadgeIdAchieved(cursor.getLong(offset + 6));
        entity.setBadgeIdAchievedDate(cursor.isNull(offset + 7) ? null : new java.util.Date(cursor.getLong(offset + 7)));
        entity.setNoOfStarAchieved(cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8));
        entity.setBadgeType(cursor.getString(offset + 9));
        entity.setCategory(cursor.getString(offset + 10));
        entity.setCategoryStatus(cursor.getString(offset + 11));
        entity.setParamDone(cursor.getDouble(offset + 12));
        entity.setIsSync(cursor.getShort(offset + 13) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(AchievedBadge entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(AchievedBadge entity) {
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
