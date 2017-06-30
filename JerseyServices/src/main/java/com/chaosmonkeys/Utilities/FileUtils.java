package com.chaosmonkeys.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

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

    // Filename case sensitive
    private static boolean sensitive = false;

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



    //-----------------------------------------------------------------------
    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
     */
    public static void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            final String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
     * </ul>
     *
     * @param file file or directory to delete, can be {@code null}
     * @return {@code true} if the file or directory was deleted, otherwise
     * {@code false}
     *
     * @since 1.4
     */
    public static boolean deleteQuietly(final File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (final Exception ignored) {
        }

        try {
            return file.delete();
        } catch (final Exception ignored) {
            return false;
        }
    }

    /**
     * Determines whether the {@code parent} directory contains the {@code child} element (a file or directory).
     * <p>
     * The files names are expected to be normalized.
     * </p>
     *
     * Edge cases:
     * <ul>
     * <li>A {@code directory} must not be null: if null, throw IllegalArgumentException</li>
     * <li>A directory does not contain itself: return false</li>
     * <li>A null child file is not contained in any parent: return false</li>
     * </ul>
     *
     * @param canonicalParent
     *            the file to consider as the parent.
     * @param canonicalChild
     *            the file to consider as the child.
     * @return true is the candidate leaf is under by the specified composite. False otherwise.
     * @throws IOException
     *             if an IO error occurs while checking the files.
     * @since 2.2
     * @see FileUtils#directoryContains(File, File)
     */
    public static boolean directoryContains(final String canonicalParent, final String canonicalChild)
            throws IOException {

        // Fail fast against NullPointerException
        if (canonicalParent == null) {
            throw new IllegalArgumentException("Directory must not be null");
        }

        if (canonicalChild == null) {
            return false;
        }

        if (checkEquals(canonicalParent, canonicalChild)) {
            return false;
        }

        return checkStartsWith(canonicalChild, canonicalParent);
    }



    /**
     * Compares two strings using the case-sensitivity rule.
     * <p>
     * This method mimics {@link String#equals} but takes case-sensitivity
     * into account.
     *
     * @param str1  the first string to compare, not null
     * @param str2  the second string to compare, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    public static boolean checkEquals(final String str1, final String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("The strings must not be null");
        }
        return sensitive ? str1.equals(str2) : str1.equalsIgnoreCase(str2);
    }


    /**
     * case insensitive compare
     * @param str
     * @param start
     * @return
     */
    public static boolean checkStartsWith(final String str, final String start) {
        return str.regionMatches(!sensitive, 0, start, 0, start.length());
    }

    /**
     * Determines whether the {@code parent} directory contains the {@code child} element (a file or directory).
     * <p>
     * Files are normalized before comparison.
     * </p>
     *
     * Edge cases:
     * <ul>
     * <li>A {@code directory} must not be null: if null, throw IllegalArgumentException</li>
     * <li>A {@code directory} must be a directory: if not a directory, throw IllegalArgumentException</li>
     * <li>A directory does not contain itself: return false</li>
     * <li>A null child file is not contained in any parent: return false</li>
     * </ul>
     *
     * @param directory the file to consider as the parent.
     * @param child     the file to consider as the child.
     * @return true is the candidate leaf is under by the specified composite. False otherwise.
     * @throws IOException              if an IO error occurs while checking the files.
     * @throws IllegalArgumentException if {@code directory} is null or not a directory.
     * @since 2.2
     */
    public static boolean directoryContains(final File directory, final File child) throws IOException {

        // Fail fast against NullPointerException
        if (directory == null) {
            throw new IllegalArgumentException("Directory must not be null");
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        if (child == null) {
            return false;
        }

        if (!directory.exists() || !child.exists()) {
            return false;
        }

        // Canonicalize paths (normalizes relative paths)
        final String canonicalParent = directory.getCanonicalPath();
        final String canonicalChild = child.getCanonicalPath();

        return directoryContains(canonicalParent, canonicalChild);
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException              in case cleaning is unsuccessful
     * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
     */
    public static void cleanDirectory(final File directory) throws IOException {
        final File[] files = verifiedListFiles(directory);

        IOException exception = null;
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Lists files in a directory, asserting that the supplied directory satisfies exists and is a directory
     * @param directory The directory to list
     * @return The files in the directory, never null.
     * @throws IOException if an I/O error occurs
     */
    private static File[] verifiedListFiles(File directory) throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        return files;
    }
    //-----------------------------------------------------------------------
    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException  if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            final boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                final String message =
                        "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Schedules a file to be deleted when JVM exits.
     * If file is directory delete it and all sub-directories.
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException if the file is {@code null}
     * @throws IOException          in case deletion is unsuccessful
     */
    public static void forceDeleteOnExit(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Schedules a directory recursively for deletion on JVM exit.
     *
     * @param directory directory to delete, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case deletion is unsuccessful
     */
    private static void deleteDirectoryOnExit(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case cleaning is unsuccessful
     */
    private static void cleanDirectoryOnExit(final File directory) throws IOException {
        final File[] files = verifiedListFiles(directory);

        IOException exception = null;
        for (final File file : files) {
            try {
                forceDeleteOnExit(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Determines whether the specified file is a Symbolic Link rather than an actual file.
     * <p>
     * Will not return true if there is a Symbolic Link anywhere in the path,
     * only if the specific file is.
     * <p>
     * When using jdk1.7, this method delegates to {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
     *
     * <b>Note:</b> the current implementation always returns {@code false} if running on
     * jkd1.6 and the system is detected as Windows using {@code FilenameUtils#isSystemWindows()}
     * <p>
     * For code that runs on Java 1.7 or later, use the following method instead:
     * <br>
     * {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
     * @param file the file to check
     * @return true if the file is a Symbolic Link
     * @throws IOException if an IO error occurs while checking the file
     */
    public static boolean isSymlink(final File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        return Files.isSymbolicLink(file.toPath());
    }
}
