package com.sharesmile.share.db.migration.history;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.db.migration.Migration;
import com.sharesmile.share.db.migration.MigrationImpl;

/**
 * Created by ankitmaheshwari
 */

public class MigrateV12ToV13 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {

        prepareMigration(db, currentVersion);
        db.execSQL(getSqlQueryForAddingColumn(" 'CAUSE_ID' INTEGER"));

        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 12;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 13;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV11ToV12();
    }

    private String getSqlQueryForAddingColumn(String columnDef) {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN " + columnDef;
    }

}

