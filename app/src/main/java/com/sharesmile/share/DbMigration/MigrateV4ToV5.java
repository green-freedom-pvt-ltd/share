package com.sharesmile.share.DbMigration;

/**
 * Created by shine on 11/09/16.
 */

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;
public class MigrateV4ToV5 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {
        prepareMigration(db, currentVersion);

        db.execSQL(getSqlQueryForAddingColumn(" 'WORKOUT_ID' TEXT"));
        db.execSQL(getSqlQueryForAddingColumn(" 'START_POINT_LATITUDE' REAL"));
        db.execSQL(getSqlQueryForAddingColumn(" 'START_POINT_LONGITUDE' REAL"));
        db.execSQL(getSqlQueryForAddingColumn(" 'END_POINT_LATITUDE' REAL"));
        db.execSQL(getSqlQueryForAddingColumn(" 'END_POINT_LONGITUDE' REAL"));
        db.execSQL(getSqlQueryForAddingColumn(" 'BEGIN_TIME_STAMP' INTEGER"));
        db.execSQL(getSqlQueryForAddingColumn(" 'END_TIME_STAMP' INTEGER"));
        db.execSQL(getSqlQueryForAddingColumn(" 'IS_VALID_RUN' INTEGER DEFAULT 1"));
        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV3ToV4();
    }


    private String getSqlQueryForAddingColumn(String columnDef) {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN " + columnDef;
    }
}
