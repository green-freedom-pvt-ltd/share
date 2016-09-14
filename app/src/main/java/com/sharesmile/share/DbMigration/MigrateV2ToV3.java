package com.sharesmile.share.DbMigration;

/**
 * Created by shine on 11/09/16.
 */

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sharesmile.share.LeaderBoardDao;
import com.sharesmile.share.Message;
import com.sharesmile.share.MessageDao;


/**
 * Migration from Version1 to Version2
 *
 * @author Jeremy
 */
public class MigrateV2ToV3 extends MigrationImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public int applyMigration(@NonNull SQLiteDatabase db,
                              int currentVersion) {
        prepareMigration(db, currentVersion);

        db.execSQL(getSqlStringForMigration());

        //created LeaderBoard table
        LeaderBoardDao.createTable(db, true);


        return getMigratedVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetVersion() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMigratedVersion() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Migration getPreviousMigration() {
        return new MigrateV1ToV2();
    }

    private String getSqlStringForMigration() {
        return  "ALTER TABLE '" + MessageDao.TABLENAME + "' ADD COLUMN 'VIDEO_ID' TEXT DEFAULT NULL";
    }
}
