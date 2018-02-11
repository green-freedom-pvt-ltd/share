package com.sharesmile.share.db.migration.history;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.db.migration.Migration;
import com.sharesmile.share.db.migration.MigrationImpl;

/**
 * Created by ankitmaheshwari on 7/24/17.
 */

public class MigrateV11ToV12 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {

        prepareMigration(db, currentVersion);
        db.execSQL(getSqlQueryForAddingColumn(" 'SHOULD_SYNC_LOCATION_DATA' INTEGER"));

        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 11;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 12;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV10ToV11();
    }

    private String getSqlQueryForAddingColumn(String columnDef) {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN " + columnDef;
    }

}
