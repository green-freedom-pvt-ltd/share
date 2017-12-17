package com.sharesmile.share.DbMigration;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;

/**
 * Created by ankitmaheshwari on 12/17/17.
 */

public class MigrateV14ToV15 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {

        prepareMigration(db, currentVersion);
        db.execSQL(getSqlQueryForAddingColumn(" 'USAIN_BOLT_COUNT' INTEGER"));

        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 14;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 15;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV13ToV14();
    }

    private String getSqlQueryForAddingColumn(String columnDef) {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN " + columnDef;
    }

}
