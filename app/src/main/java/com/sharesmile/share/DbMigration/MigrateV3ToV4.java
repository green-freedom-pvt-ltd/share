package com.sharesmile.share.DbMigration;

/**
 * Created by shine on 11/09/16.
 */

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.Cause;
import com.sharesmile.share.CauseDao;
import com.sharesmile.share.LeaderBoardDao;
import com.sharesmile.share.MessageDao;


/**
 * Migration from Version1 to Version2
 *
 * @author Jeremy
 */
public class MigrateV3ToV4 extends MigrationImpl {

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
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV2ToV3();
    }

    private String getSqlStringForMigration() {
        return "ALTER TABLE '" + CauseDao.TABLENAME + "' ADD COLUMN 'ORDER_PRIORITY' INTEGER DEFAULT 0";
    }
}
