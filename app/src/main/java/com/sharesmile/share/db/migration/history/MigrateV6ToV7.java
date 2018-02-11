package com.sharesmile.share.db.migration.history;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.db.migration.Migration;
import com.sharesmile.share.db.migration.MigrationImpl;

/**
 * Created by ankitmaheshwari on 4/26/17.
 */

public class MigrateV6ToV7 extends MigrationImpl {

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
        return 6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 7;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV5ToV6();
    }

    private String getSqlStringForMigration() {
        return "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN 'VERSION' INTEGER";
    }

}
