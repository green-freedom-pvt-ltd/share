package com.sharesmile.share.db.migration.history;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.db.migration.Migration;
import com.sharesmile.share.db.migration.MigrationImpl;

/**
 * Created by ankitmaheshwari on 6/15/17.
 */

public class MigrateV8ToV9 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {
        prepareMigration(db, currentVersion);

        db.execSQL(getSqlQueryForAddingColumn(" 'TEAM_ID' INTEGER"));

        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 9;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV7ToV8();
    }

    private String getSqlQueryForAddingColumn(String columnDef) {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN " + columnDef;
    }

}