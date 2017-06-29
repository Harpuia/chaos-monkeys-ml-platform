package com.chaosmonkeys.Utilities;

import java.io.File;
import java.io.IOException;

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
 *
 */
public class FileUtils {

    // Constants operating with dataset upload and storage
    public static final String DATA_SET_PATH = "./Datasets/";
    public static final String EXECUTION_DATA = "Execution";
    public static final String TRAINING_DATA = "Predications";

    // Constants operating with algorithm upload and storage;
    public static final String ALGR_PATH = "./Algorithms/";


    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileUtils() {
        super();
    }

    // Common methods for all services, including:
    // file name validator, invalid name sanitizer...
    // TODO: add new common features in the future

    /**
     * Check whether the file name is valid
     * @param filename
     * @return
     */
    public static boolean isFileNameValid(String filename) {
        File f = new File(filename);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * replace illegal characters in a filename with "_"
     * illegal characters :
     *           : \ / * ? | < >
     * @param name
     * @return
     */
    public static String sanitizeFilename(String name) {
        return name.replaceAll("[:\\\\/*?|<>\"]", "_");
    }

    public static void main(String args[]) throws Exception {

        String test = "invalid : file ? name.txt";
        System.out.println(test + " -> " + FileUtils.sanitizeFilename(test));
    /* output :
     *
     *   invalid : file ? name. -> invalid _ file _ name.txt
     *
     */
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

    /**
     * Create folder in specific path
     * when the application is deployed in production environment
     * please replace the path to absolute path
     * @param path
     * @return
     */
    public static File createFolderInPath(String path){
        File rootFolder = new File(path);
        if (!rootFolder.exists()) {
            rootFolder.mkdir();
        }
        return rootFolder;
    }

    //* Methods used to manage data set file and folder *//
    /**
     * Create datasets folder if it does not exist
     *
     * @return DataSets root folder as File Object
     */
    public static File createDatasetFolder() {
        return createFolderInPath(DATA_SET_PATH);
    }

    public static File createAlgorithmFolder(){
        return createFolderInPath(ALGR_PATH);
    }
}
