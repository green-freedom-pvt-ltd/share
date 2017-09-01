package com.sharesmile.share.DbMigration;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sharesmile.share.DaoMaster;


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
