package com.sharesmile.share.DbMigration;

/**
 * Created by shine on 11/09/16.
 */

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.CauseDao;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.v3.MessageDao;


/**
 * Migration from Version1 to Version2
 *
 * @author Jeremy
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
        return  "ALTER TABLE '" + WorkoutDao.TABLENAME + "' ADD COLUMN 'IS_VALID_RUN' BOOLEAN DEFAULT TRUE NOT NULL";

    }
}
