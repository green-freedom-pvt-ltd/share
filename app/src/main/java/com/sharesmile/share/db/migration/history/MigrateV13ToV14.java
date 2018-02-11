package com.sharesmile.share.db.migration.history;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.db.migration.Migration;
import com.sharesmile.share.db.migration.MigrationImpl;

/**
 * Created by ankitmaheshwari on 12/15/17.
 */

public class MigrateV13ToV14 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {

        prepareMigration(db, currentVersion);
        db.execSQL(getSqlQueryForAddingColumn(" 'ESTIMATED_DISTANCE' REAL"));
        db.execSQL(getSqlQueryForAddingColumn(" 'ESTIMATED_STEPS' INTEGER"));
        db.execSQL(getSqlQueryForAddingColumn(" 'ESTIMATED_CALORIES' REAL"));
        db.execSQL(getSqlQueryForAddingColumn(" 'GOOGLE_FIT_STEP_COUNT' INTEGER"));
        db.execSQL(getSqlQueryForAddingColumn(" 'GOOGLE_FIT_DISTANCE' REAL"));

        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 13;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 14;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV12ToV13();
    }

    private String getSqlQueryForAddingColumn(String columnDef) {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN " + columnDef;
    }

}
