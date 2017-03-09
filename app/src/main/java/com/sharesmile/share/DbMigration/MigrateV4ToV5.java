package com.sharesmile.share.DbMigration;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;

/**
 * Created by ankitmaheshwari on 3/9/17.
 */

public class MigrateV4ToV5 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {
        prepareMigration(db, currentVersion);

        db.execSQL(getSqlStringForMigration());

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

    private String getSqlStringForMigration() {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD " +
                " 'WORKOUT_ID' TEXT NOT NULL ," +
                " 'START_POINT_LATITUDE' REAL ," +
                " 'START_POINT_LONGITUDE' REAL," +
                " 'END_POINT_LATITUDE' REAL ," +
                " 'END_POINT_LONGITUDE' REAL ," +
                " 'BEGIN_TIME_STAMP' INTEGER," +
                " 'END_TIME_STAMP' INTEGER";
    }

}
