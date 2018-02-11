package com.sharesmile.share.db.migration.history;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.LeaderBoardDao;
import com.sharesmile.share.db.migration.Migration;
import com.sharesmile.share.db.migration.MigrationImpl;

/**
 * Created by ankitmaheshwari on 3/31/17.
 */

public class MigrateV5ToV6 extends MigrationImpl {

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
        return 5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV4ToV5();
    }

    private String getSqlStringForMigration() {
        return "ALTER TABLE '" + LeaderBoardDao.TABLENAME + "' ADD COLUMN 'RANK' INTEGER";
    }

}
