package com.sharesmile.share;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "WORKOUT".
*/
public class WorkoutDao extends AbstractDao<Workout, Long> {

    public static final String TABLENAME = "WORKOUT";

    /**
     * Properties of entity Workout.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Distance = new Property(1, float.class, "distance", false, "DISTANCE");
        public final static Property ElapsedTime = new Property(2, String.class, "elapsedTime", false, "ELAPSED_TIME");
        public final static Property Steps = new Property(3, Integer.class, "steps", false, "STEPS");
        public final static Property RecordedTime = new Property(4, float.class, "recordedTime", false, "RECORDED_TIME");
        public final static Property AvgSpeed = new Property(5, float.class, "avgSpeed", false, "AVG_SPEED");
        public final static Property CauseBrief = new Property(6, String.class, "causeBrief", false, "CAUSE_BRIEF");
        public final static Property Date = new Property(7, java.util.Date.class, "date", false, "DATE");
        public final static Property RunAmount = new Property(8, Float.class, "runAmount", false, "RUN_AMOUNT");
        public final static Property Is_sync = new Property(9, Boolean.class, "is_sync", false, "IS_SYNC");
        public final static Property WorkoutId = new Property(10, String.class, "workoutId", false, "WORKOUT_ID");
        public final static Property StartPointLatitude = new Property(11, Double.class, "startPointLatitude", false, "START_POINT_LATITUDE");
        public final static Property StartPointLongitude = new Property(12, Double.class, "startPointLongitude", false, "START_POINT_LONGITUDE");
        public final static Property EndPointLatitude = new Property(13, Double.class, "endPointLatitude", false, "END_POINT_LATITUDE");
        public final static Property EndPointLongitude = new Property(14, Double.class, "endPointLongitude", false, "END_POINT_LONGITUDE");
        public final static Property BeginTimeStamp = new Property(15, Long.class, "beginTimeStamp", false, "BEGIN_TIME_STAMP");
        public final static Property EndTimeStamp = new Property(16, Long.class, "endTimeStamp", false, "END_TIME_STAMP");
        public final static Property IsValidRun = new Property(17, Boolean.class, "isValidRun", false, "IS_VALID_RUN");
        public final static Property Version = new Property(18, Long.class, "version", false, "VERSION");
        public final static Property Calories = new Property(19, Double.class, "calories", false, "CALORIES");
        public final static Property TeamId = new Property(20, Integer.class, "teamId", false, "TEAM_ID");
        public final static Property NumSpikes = new Property(21, Integer.class, "numSpikes", false, "NUM_SPIKES");
        public final static Property NumUpdates = new Property(22, Integer.class, "numUpdates", false, "NUM_UPDATES");
        public final static Property AppVersion = new Property(23, String.class, "appVersion", false, "APP_VERSION");
        public final static Property OsVersion = new Property(24, Integer.class, "osVersion", false, "OS_VERSION");
        public final static Property DeviceId = new Property(25, String.class, "deviceId", false, "DEVICE_ID");
        public final static Property DeviceName = new Property(26, String.class, "deviceName", false, "DEVICE_NAME");
        public final static Property ShouldSyncLocationData = new Property(27, Boolean.class, "shouldSyncLocationData", false, "SHOULD_SYNC_LOCATION_DATA");
        public final static Property CauseId = new Property(28, Integer.class, "causeId", false, "CAUSE_ID");
    };


    public WorkoutDao(DaoConfig config) {
        super(config);
    }
    
    public WorkoutDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"WORKOUT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"DISTANCE\" REAL NOT NULL ," + // 1: distance
                "\"ELAPSED_TIME\" TEXT NOT NULL ," + // 2: elapsedTime
                "\"STEPS\" INTEGER," + // 3: steps
                "\"RECORDED_TIME\" REAL NOT NULL ," + // 4: recordedTime
                "\"AVG_SPEED\" REAL NOT NULL ," + // 5: avgSpeed
                "\"CAUSE_BRIEF\" TEXT," + // 6: causeBrief
                "\"DATE\" INTEGER," + // 7: date
                "\"RUN_AMOUNT\" REAL," + // 8: runAmount
                "\"IS_SYNC\" INTEGER," + // 9: is_sync
                "\"WORKOUT_ID\" TEXT," + // 10: workoutId
                "\"START_POINT_LATITUDE\" REAL," + // 11: startPointLatitude
                "\"START_POINT_LONGITUDE\" REAL," + // 12: startPointLongitude
                "\"END_POINT_LATITUDE\" REAL," + // 13: endPointLatitude
                "\"END_POINT_LONGITUDE\" REAL," + // 14: endPointLongitude
                "\"BEGIN_TIME_STAMP\" INTEGER," + // 15: beginTimeStamp
                "\"END_TIME_STAMP\" INTEGER," + // 16: endTimeStamp
                "\"IS_VALID_RUN\" INTEGER," + // 17: isValidRun
                "\"VERSION\" INTEGER," + // 18: version
                "\"CALORIES\" REAL," + // 19: calories
                "\"TEAM_ID\" INTEGER," + // 20: teamId
                "\"NUM_SPIKES\" INTEGER," + // 21: numSpikes
                "\"NUM_UPDATES\" INTEGER," + // 22: numUpdates
                "\"APP_VERSION\" TEXT," + // 23: appVersion
                "\"OS_VERSION\" INTEGER," + // 24: osVersion
                "\"DEVICE_ID\" TEXT," + // 25: deviceId
                "\"DEVICE_NAME\" TEXT," + // 26: deviceName
                "\"SHOULD_SYNC_LOCATION_DATA\" INTEGER," + // 27: shouldSyncLocationData
                "\"CAUSE_ID\" INTEGER);"); // 28: causeId
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_WORKOUT_WORKOUT_ID ON WORKOUT" +
                " (\"WORKOUT_ID\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"WORKOUT\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Workout entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindDouble(2, entity.getDistance());
        stmt.bindString(3, entity.getElapsedTime());
 
        Integer steps = entity.getSteps();
        if (steps != null) {
            stmt.bindLong(4, steps);
        }
        stmt.bindDouble(5, entity.getRecordedTime());
        stmt.bindDouble(6, entity.getAvgSpeed());
 
        String causeBrief = entity.getCauseBrief();
        if (causeBrief != null) {
            stmt.bindString(7, causeBrief);
        }
 
        java.util.Date date = entity.getDate();
        if (date != null) {
            stmt.bindLong(8, date.getTime());
        }
 
        Float runAmount = entity.getRunAmount();
        if (runAmount != null) {
            stmt.bindDouble(9, runAmount);
        }
 
        Boolean is_sync = entity.getIs_sync();
        if (is_sync != null) {
            stmt.bindLong(10, is_sync ? 1L: 0L);
        }
 
        String workoutId = entity.getWorkoutId();
        if (workoutId != null) {
            stmt.bindString(11, workoutId);
        }
 
        Double startPointLatitude = entity.getStartPointLatitude();
        if (startPointLatitude != null) {
            stmt.bindDouble(12, startPointLatitude);
        }
 
        Double startPointLongitude = entity.getStartPointLongitude();
        if (startPointLongitude != null) {
            stmt.bindDouble(13, startPointLongitude);
        }
 
        Double endPointLatitude = entity.getEndPointLatitude();
        if (endPointLatitude != null) {
            stmt.bindDouble(14, endPointLatitude);
        }
 
        Double endPointLongitude = entity.getEndPointLongitude();
        if (endPointLongitude != null) {
            stmt.bindDouble(15, endPointLongitude);
        }
 
        Long beginTimeStamp = entity.getBeginTimeStamp();
        if (beginTimeStamp != null) {
            stmt.bindLong(16, beginTimeStamp);
        }
 
        Long endTimeStamp = entity.getEndTimeStamp();
        if (endTimeStamp != null) {
            stmt.bindLong(17, endTimeStamp);
        }
 
        Boolean isValidRun = entity.getIsValidRun();
        if (isValidRun != null) {
            stmt.bindLong(18, isValidRun ? 1L: 0L);
        }
 
        Long version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(19, version);
        }
 
        Double calories = entity.getCalories();
        if (calories != null) {
            stmt.bindDouble(20, calories);
        }
 
        Integer teamId = entity.getTeamId();
        if (teamId != null) {
            stmt.bindLong(21, teamId);
        }
 
        Integer numSpikes = entity.getNumSpikes();
        if (numSpikes != null) {
            stmt.bindLong(22, numSpikes);
        }
 
        Integer numUpdates = entity.getNumUpdates();
        if (numUpdates != null) {
            stmt.bindLong(23, numUpdates);
        }
 
        String appVersion = entity.getAppVersion();
        if (appVersion != null) {
            stmt.bindString(24, appVersion);
        }
 
        Integer osVersion = entity.getOsVersion();
        if (osVersion != null) {
            stmt.bindLong(25, osVersion);
        }
 
        String deviceId = entity.getDeviceId();
        if (deviceId != null) {
            stmt.bindString(26, deviceId);
        }
 
        String deviceName = entity.getDeviceName();
        if (deviceName != null) {
            stmt.bindString(27, deviceName);
        }
 
        Boolean shouldSyncLocationData = entity.getShouldSyncLocationData();
        if (shouldSyncLocationData != null) {
            stmt.bindLong(28, shouldSyncLocationData ? 1L: 0L);
        }
 
        Integer causeId = entity.getCauseId();
        if (causeId != null) {
            stmt.bindLong(29, causeId);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Workout readEntity(Cursor cursor, int offset) {
        Workout entity = new Workout( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getFloat(offset + 1), // distance
            cursor.getString(offset + 2), // elapsedTime
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // steps
            cursor.getFloat(offset + 4), // recordedTime
            cursor.getFloat(offset + 5), // avgSpeed
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // causeBrief
            cursor.isNull(offset + 7) ? null : new java.util.Date(cursor.getLong(offset + 7)), // date
            cursor.isNull(offset + 8) ? null : cursor.getFloat(offset + 8), // runAmount
            cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0, // is_sync
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // workoutId
            cursor.isNull(offset + 11) ? null : cursor.getDouble(offset + 11), // startPointLatitude
            cursor.isNull(offset + 12) ? null : cursor.getDouble(offset + 12), // startPointLongitude
            cursor.isNull(offset + 13) ? null : cursor.getDouble(offset + 13), // endPointLatitude
            cursor.isNull(offset + 14) ? null : cursor.getDouble(offset + 14), // endPointLongitude
            cursor.isNull(offset + 15) ? null : cursor.getLong(offset + 15), // beginTimeStamp
            cursor.isNull(offset + 16) ? null : cursor.getLong(offset + 16), // endTimeStamp
            cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0, // isValidRun
            cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18), // version
            cursor.isNull(offset + 19) ? null : cursor.getDouble(offset + 19), // calories
            cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20), // teamId
            cursor.isNull(offset + 21) ? null : cursor.getInt(offset + 21), // numSpikes
            cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22), // numUpdates
            cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23), // appVersion
            cursor.isNull(offset + 24) ? null : cursor.getInt(offset + 24), // osVersion
            cursor.isNull(offset + 25) ? null : cursor.getString(offset + 25), // deviceId
            cursor.isNull(offset + 26) ? null : cursor.getString(offset + 26), // deviceName
            cursor.isNull(offset + 27) ? null : cursor.getShort(offset + 27) != 0, // shouldSyncLocationData
            cursor.isNull(offset + 28) ? null : cursor.getInt(offset + 28) // causeId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Workout entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDistance(cursor.getFloat(offset + 1));
        entity.setElapsedTime(cursor.getString(offset + 2));
        entity.setSteps(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setRecordedTime(cursor.getFloat(offset + 4));
        entity.setAvgSpeed(cursor.getFloat(offset + 5));
        entity.setCauseBrief(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setDate(cursor.isNull(offset + 7) ? null : new java.util.Date(cursor.getLong(offset + 7)));
        entity.setRunAmount(cursor.isNull(offset + 8) ? null : cursor.getFloat(offset + 8));
        entity.setIs_sync(cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0);
        entity.setWorkoutId(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setStartPointLatitude(cursor.isNull(offset + 11) ? null : cursor.getDouble(offset + 11));
        entity.setStartPointLongitude(cursor.isNull(offset + 12) ? null : cursor.getDouble(offset + 12));
        entity.setEndPointLatitude(cursor.isNull(offset + 13) ? null : cursor.getDouble(offset + 13));
        entity.setEndPointLongitude(cursor.isNull(offset + 14) ? null : cursor.getDouble(offset + 14));
        entity.setBeginTimeStamp(cursor.isNull(offset + 15) ? null : cursor.getLong(offset + 15));
        entity.setEndTimeStamp(cursor.isNull(offset + 16) ? null : cursor.getLong(offset + 16));
        entity.setIsValidRun(cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0);
        entity.setVersion(cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18));
        entity.setCalories(cursor.isNull(offset + 19) ? null : cursor.getDouble(offset + 19));
        entity.setTeamId(cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20));
        entity.setNumSpikes(cursor.isNull(offset + 21) ? null : cursor.getInt(offset + 21));
        entity.setNumUpdates(cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22));
        entity.setAppVersion(cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23));
        entity.setOsVersion(cursor.isNull(offset + 24) ? null : cursor.getInt(offset + 24));
        entity.setDeviceId(cursor.isNull(offset + 25) ? null : cursor.getString(offset + 25));
        entity.setDeviceName(cursor.isNull(offset + 26) ? null : cursor.getString(offset + 26));
        entity.setShouldSyncLocationData(cursor.isNull(offset + 27) ? null : cursor.getShort(offset + 27) != 0);
        entity.setCauseId(cursor.isNull(offset + 28) ? null : cursor.getInt(offset + 28));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Workout entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Workout entity) {
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
