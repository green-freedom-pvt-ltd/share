package com.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.daogenerator.DaoGenerator;
import sun.rmi.runtime.Log;

public class SchemaGenerator {

    /**
     * Generator main application which builds all of the schema versions
     * (including older versions used for migration test purposes) and ensures
     * business rules are met; these include ensuring we only have a single
     * current schema instance and the version numbering is correct.
     *
     * @param args
     * @throws Exception
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        List<SchemaVersion> versions = new ArrayList<SchemaVersion>();
        //Schema Starts from version1
        versions.add(new Version1(false));
        versions.add(new Version2(false));
        versions.add(new Version3(false));
        versions.add(new Version4(false));
        versions.add(new Version5(false));
        versions.add(new Version6(false));
        versions.add(new Version7(false));
        versions.add(new Version8(false));
        versions.add(new Version9(false));
        versions.add(new Version10(false));
        versions.add(new Version11(false));
        versions.add(new Version12(false));
        versions.add(new Version13(false));
        versions.add(new Version14(false));
        versions.add(new Version15(true));

        validateSchemas(versions);
        toFileForceExists("../../Share/app/src/main/java-gen");

        for (SchemaVersion version : versions) {
            // NB: Test output creates stubs, we have an established testing
            // standard which should be followed in preference to generating
            // these stubs.
            new DaoGenerator().generateAll(version.getSchema(),
                    "../../Share/app/src/main/java-gen");
        }
    }

    /**
     * Validate the schema, throws
     *
     * @param versions
     * @throws IllegalArgumentException if data is invalid
     */
    public static void validateSchemas(List<SchemaVersion> versions)
            throws IllegalArgumentException {
        int numCurrent = 0;
        Set<Integer> versionNumbers = new HashSet<Integer>();

        for (SchemaVersion version : versions) {
            if (version.isCurrent()) {
                numCurrent++;
            }

            int versionNumber = version.getVersionNumber();
            if (versionNumbers.contains(versionNumber)) {
                throw new IllegalArgumentException(
                        "Unable to process schema versions, multiple instances with version number : "
                                + version.getVersionNumber());
            }
            versionNumbers.add(versionNumber);
        }

        if (numCurrent != 1) {
            throw new IllegalArgumentException(
                    "Unable to generate schema, exactly one schema marked as current is required.");
        }
    }

    protected static File toFileForceExists(String filename) throws IOException {
        File file = new File(filename);
        Log.getLog("path : ", file.getCanonicalPath(), 1);
        if (!file.exists()) {
            throw new IOException(filename
                    + " does not exist. This check is to prevent accidental file generation into a wrong path.");
        }
        return file;
    }

}
