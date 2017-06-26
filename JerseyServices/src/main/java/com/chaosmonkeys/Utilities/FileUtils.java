package com.chaosmonkeys.Utilities;

import java.io.File;

/**
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li> create datasets folder
 * <li> create new folder under specific folder
 * <li> other cool stuff...
 * </ul>
 * <p>
 * Note that a specific charset should be specified whenever possible.
 * Relying on the platform default means that the code is Locale-dependent.
 * Only use the default if the files are known to always use the platform default.
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 *
 */
public class FileUtils {

    // Constants operating with dataset upload and storage
    public static final String DATA_SET_PATH = "./Datasets/";
    public static final String EXECUTION_DATA = "Execution";
    public static final String TRAINING_DATA = "Predications";

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileUtils() {
        super();
    }

    //* Methods used to manage data set file and folder *//
    /**
     * Create datasets folder if it does not exist
     *
     * @return DataSets root folder as File Object
     */
    public static File createDatasetFolder() {
        File rootFolder = new File(DATA_SET_PATH);
        if (!rootFolder.exists()) {
            rootFolder.mkdir();
        }
        return rootFolder;
    }

    /**
     * Create a folder with specified name under parent folder
     *  return the File object of folder created
     * @param folderName
     * @param parentFolder
     * @return
     */
    public static File createNewFolderUnder(String folderName, File parentFolder) {
        File folder = new File(parentFolder, folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }
}
