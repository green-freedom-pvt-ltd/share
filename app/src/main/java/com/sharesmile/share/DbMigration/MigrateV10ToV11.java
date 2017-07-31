package com.sharesmile.share.DbMigration;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;

/**
 * Created by ankitmaheshwari on 7/24/17.
 */

public class MigrateV10ToV11 extends MigrationImpl {


    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {

        prepareMigration(db, currentVersion);
        db.execSQL(getSqlStringForRemovingDuplicates());
        db.execSQL(getSqlStringForAddingIndex());
        db.execSQL(getSqlQueryForAddingColumn(" 'NUM_UPDATES' INTEGER"));
        db.execSQL(getSqlQueryForAddingColumn(" 'APP_VERSION' TEXT"));
        db.execSQL(getSqlQueryForAddingColumn(" 'OS_VERSION' INTEGER"));
        db.execSQL(getSqlQueryForAddingColumn(" 'DEVICE_ID' TEXT"));
        db.execSQL(getSqlQueryForAddingColumn(" 'DEVICE_NAME' TEXT"));

        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 11;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV9ToV10();
    }

    private String getSqlQueryForAddingColumn(String columnDef) {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN " + columnDef;
    }

    private String getSqlStringForAddingIndex(){
        // Query to create index on workoutId column
        return  "CREATE UNIQUE INDEX IF NOT EXISTS IDX_WORKOUT_WORKOUT_ID ON WORKOUT (\"WORKOUT_ID\");";
    }

    private String getSqlStringForRemovingDuplicates() {

        // Create Query to delete all duplicate records (same workoutId)
        return "delete from WORKOUT where rowid not in\n" +
                "( select min(rowid) from  WORKOUT where WORKOUT_ID is not null and WORKOUT_ID != \"\" group by WORKOUT_ID\n" +
                " union all\n" +
                " select rowid from WORKOUT where WORKOUT_ID is null or WORKOUT_ID == \"\");";

    }

}
