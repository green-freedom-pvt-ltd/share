package com.dao;

import de.greenrobot.daogenerator.Schema;

/**
 * Created by shine on 15/11/16.
 */
/* A Version of the Schema.
        *
        * @author Jeremy
        */
public abstract class SchemaVersion {

    public static final String CURRENT_SCHEMA_PACKAGE = "com.sharesmile.share";

    private final Schema schema;

    private final boolean current;

    /**
     * Constructor
     *
     * @param current indicating if this is the current schema.
     */
    public SchemaVersion(boolean current) {
        int version = getVersionNumber();
        String packageName = CURRENT_SCHEMA_PACKAGE;
        if (!current) {
            packageName += ".v" + version;
        }
        this.schema = new Schema(version, packageName);
        this.schema.enableKeepSectionsByDefault();
        this.current = current;
    }

    /**
     * @return the GreenDAO schema.
     */
    protected Schema getSchema() {
        return schema;
    }

    /**
     * @return boolean indicating if this is the highest or current schema version.
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * @return unique integer schema version identifier.
     */
    public abstract int getVersionNumber();
}
