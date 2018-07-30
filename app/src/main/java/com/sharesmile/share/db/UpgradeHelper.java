package com.sharesmile.share.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sharesmile.share.DaoMaster;
import com.sharesmile.share.db.migration.history.MigrateV10ToV11;
import com.sharesmile.share.db.migration.history.MigrateV11ToV12;
import com.sharesmile.share.db.migration.history.MigrateV12ToV13;
import com.sharesmile.share.db.migration.history.MigrateV13ToV14;
import com.sharesmile.share.db.migration.history.MigrateV14ToV15;
import com.sharesmile.share.db.migration.history.MigrateV15ToV16;
import com.sharesmile.share.db.migration.history.MigrateV1ToV2;
import com.sharesmile.share.db.migration.history.MigrateV2ToV3;
import com.sharesmile.share.db.migration.history.MigrateV3ToV4;
import com.sharesmile.share.db.migration.history.MigrateV4ToV5;
import com.sharesmile.share.db.migration.history.MigrateV5ToV6;
import com.sharesmile.share.db.migration.history.MigrateV6ToV7;
import com.sharesmile.share.db.migration.history.MigrateV7ToV8;
import com.sharesmile.share.db.migration.history.MigrateV8ToV9;
import com.sharesmile.share.db.migration.history.MigrateV9ToV10;


/**
 * Created by Shine on 27/08/16.
 */
public class UpgradeHelper extends DaoMaster.OpenHelper {

    private static final String TAG = "Upgrade Helper";

    public UpgradeHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "UpgradeHelper onUpgrade from " + oldVersion + " to " + newVersion);
        switch (newVersion) {
            case 2:
                new MigrateV1ToV2().applyMigration(db, oldVersion);
                break;
            case 3:
                new MigrateV2ToV3().applyMigration(db, oldVersion);
                break;
            case 4:
                new MigrateV3ToV4().applyMigration(db, oldVersion);
                break;
            case 5:
                new MigrateV4ToV5().applyMigration(db, oldVersion);
                break;
            case 6:
                new MigrateV5ToV6().applyMigration(db, oldVersion);
                break;
            case 7:
                new MigrateV6ToV7().applyMigration(db, oldVersion);
                break;
            case 8:
                new MigrateV7ToV8().applyMigration(db, oldVersion);
                break;
            case 9:
                new MigrateV8ToV9().applyMigration(db, oldVersion);
                break;
            case 10:
                new MigrateV9ToV10().applyMigration(db, oldVersion);
                break;
            case 11:
                new MigrateV10ToV11().applyMigration(db, oldVersion);
                break;
            case 12:
                new MigrateV11ToV12().applyMigration(db, oldVersion);
                break;
            case 13:
                new MigrateV12ToV13().applyMigration(db, oldVersion);
                break;
            case 14:
                new MigrateV13ToV14().applyMigration(db, oldVersion);
                break;
            case 15:
                new MigrateV14ToV15().applyMigration(db, oldVersion);
                break;
            case 16:
                new MigrateV15ToV16().applyMigration(db, oldVersion);
                break;
            default:
                return;
        }
    }

    public static boolean columnExists(SQLiteDatabase db, String table, String column) {
        Cursor res = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        int value = res.getColumnIndex(column);

        return (value >= 0);
    }
}
