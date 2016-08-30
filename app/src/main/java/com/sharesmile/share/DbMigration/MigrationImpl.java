package com.sharesmile.share.DbMigration;

/**
* Created by samvedana on 24/11/14.
*/

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;


/**
* An abstract base class with the basic implementation to promote reuse.
*
* @author Jeremy
*/
public abstract class MigrationImpl implements Migration {

    /**
     * A helper method which helps the migration prepare by passing the call up the chain.
     *
     * @param db
     * @param currentVersion
     */
    protected void prepareMigration(@NonNull SQLiteDatabase db,
                                    int currentVersion) {
        if (db == null) {
            throw new IllegalArgumentException(
                    "db cannot be null");
        }
        if (currentVersion < 1) {
            throw new IllegalArgumentException(
                    "Lowest supported schema version is 1, unable to prepare for migration from version: "
                            + currentVersion);
        }

        if (currentVersion < getTargetVersion()) {
            Migration previousMigration = getPreviousMigration();

            if (previousMigration == null) {
                // This is the first migration
                if (currentVersion != getTargetVersion()) {
                    throw new IllegalStateException(
                            "Unable to apply migration as Version: "
                                    + currentVersion
                                    + " is not suitable for this Migration.");
                }
            }
            if (previousMigration.applyMigration(db, currentVersion) != getTargetVersion()) {
                // For all other migrations ensure that after the earlier
                // migration has been applied the expected version matches
                throw new IllegalStateException(
                        "Error, expected migration parent to update database to appropriate version");
            }
        }
    }
}
