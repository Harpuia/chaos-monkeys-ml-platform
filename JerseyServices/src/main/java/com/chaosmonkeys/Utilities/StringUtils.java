package com.chaosmonkeys.Utilities;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.UUID;

/**
 * General string manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li> check empty or blank
 * <li> other cool stuff...
 * </ul>
 * <p>
 * Note that a specific charset should be specified whenever possible.
 * Relying on the platform default means that the code is Locale-dependent.
 * Only use the default if the files are known to always use the platform default.
 * <p>
 * Portion of code origin from commons-lang
 */
public class StringUtils {


    /**
     * <p>{@code StringUtils} instances should NOT be constructed in
     * standard programming. Instead, the class should be used as
     * {@code StringUtils.trim(" foo ");}.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public StringUtils() {
        super();
    }

    /**
     * Obtain a UUID String
     * @return	return a UUID code, the length of UUID is 32
     */
    public static String getUUID(){
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        return uuid;
    }



    // Folder name
    // --------------------------------------------------
    /**
     * Generate algorithm storage folder name (valid folder name in common OS)
     * @param name which is algorithm name
     * @return
     */
    public static String genAlgrStorageFolderName(String name){
        StringBuilder strBuilder = new StringBuilder();
        String validName = FileUtils.sanitizeFilename(name);
        strBuilder.append(validName);
        strBuilder.append("-");
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MMM-d-HH-mm-ss", Locale.US)); // check locale when deploying
        strBuilder.append(dateTimeStr);

        return strBuilder.toString();
    }

    /**
     * Generate algorithm storage folder name (valid folder name in common OS)
     * @param name which is experiment name
     * @return
     */
    public static String genModelStorageFolderName(String name){
        StringBuilder strBuilder = new StringBuilder();
        String validName = FileUtils.sanitizeFilename(name);
        strBuilder.append(validName);
        strBuilder.append("-");
        strBuilder.append(System.currentTimeMillis());

        return strBuilder.toString();
    }
    // Contains
    //-------------------------------------------------------------------------
    /**
     * <p>Checks if CharSequence contains a search CharSequence irrespective of case,
     * handling {@code null}. Case-insensitivity is defined as by
     * {@link String#equalsIgnoreCase(String)}.
     *
     * <p>A {@code null} CharSequence will return {@code false}.</p>
     *
     * <pre>
     * StringUtils.containsIgnoreCase(null, *) = false
     * StringUtils.containsIgnoreCase(*, null) = false
     * StringUtils.containsIgnoreCase("", "") = true
     * StringUtils.containsIgnoreCase("abc", "") = true
     * StringUtils.containsIgnoreCase("abc", "a") = true
     * StringUtils.containsIgnoreCase("abc", "z") = false
     * StringUtils.containsIgnoreCase("abc", "A") = true
     * StringUtils.containsIgnoreCase("abc", "Z") = false
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param searchStr  the CharSequence to find, may be null
     * @return true if the CharSequence contains the search CharSequence irrespective of
     * case or false if not or {@code null} string input
     * @since 3.0 Changed signature from containsIgnoreCase(String, String) to containsIgnoreCase(CharSequence, CharSequence)
     */
    public static boolean containsIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        final int len = searchStr.length();
        final int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (regionMatches(str, true, i, searchStr, 0, len)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Green implementation of regionMatches.
     *
     * @param cs the {@code CharSequence} to be processed
     * @param ignoreCase whether or not to be case insensitive
     * @param thisStart the index to start on the {@code cs} CharSequence
     * @param substring the {@code CharSequence} to be looked for
     * @param start the index to start on the {@code substring} CharSequence
     * @param length character length of the region
     * @return whether the region matched
     */
    static boolean regionMatches(final CharSequence cs, final boolean ignoreCase, final int thisStart,
                                 final CharSequence substring, final int start, final int length)    {
        if (cs instanceof String && substring instanceof String) {
            return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
        }
        int index1 = thisStart;
        int index2 = start;
        int tmpLen = length;

        // Extract these first so we detect NPEs the same as the java.lang.String version
        final int srcLen = cs.length() - thisStart;
        final int otherLen = substring.length() - start;

        // Check for invalid parameters
        if (thisStart < 0 || start < 0 || length < 0) {
            return false;
        }

        // Check that the regions are long enough
        if (srcLen < length || otherLen < length) {
            return false;
        }

        while (tmpLen-- > 0) {
            final char c1 = cs.charAt(index1++);
            final char c2 = substring.charAt(index2++);

            if (c1 == c2) {
                continue;
            }

            if (!ignoreCase) {
                return false;
            }

            // The same check as in String.regionMatches():
            if (Character.toUpperCase(c1) != Character.toUpperCase(c2)
                    && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                return false;
            }
        }

        return true;
    }


    // Empty Checks
    //------------------------------------------------------------ -----------
    /**
     * <p>Checks if a CharSequence is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the CharSequence.
     * That functionality is available in isBlank().</p>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    /**
     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace only
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     *  not empty and not null and not whitespace only
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * <p>Checks if any of the CharSequences are empty ("") or null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isAnyBlank(null)             = true
     * StringUtils.isAnyBlank(null, "foo")      = true
     * StringUtils.isAnyBlank(null, null)       = true
     * StringUtils.isAnyBlank("", "bar")        = true
     * StringUtils.isAnyBlank("bob", "")        = true
     * StringUtils.isAnyBlank("  bob  ", null)  = true
     * StringUtils.isAnyBlank(" ", "bar")       = true
     * StringUtils.isAnyBlank(new String[] {})  = false
     * StringUtils.isAnyBlank(new String[]{""}) = true
     * StringUtils.isAnyBlank("foo", "bar")     = false
     * </pre>
     *
     * @param css  the CharSequences to check, may be null or empty
     * @return {@code true} if any of the CharSequences are empty or null or whitespace only
     */
    public static boolean isAnyBlank(final CharSequence... css) {
        if (ArrayUtils.isEmpty(css)) {
            return false;
        }
        for (final CharSequence cs : css){
            if (isBlank(cs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Checks if none of the CharSequences are empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isNoneBlank(null)             = false
     * StringUtils.isNoneBlank(null, "foo")      = false
     * StringUtils.isNoneBlank(null, null)       = false
     * StringUtils.isNoneBlank("", "bar")        = false
     * StringUtils.isNoneBlank("bob", "")        = false
     * StringUtils.isNoneBlank("  bob  ", null)  = false
     * StringUtils.isNoneBlank(" ", "bar")       = false
     * StringUtils.isNoneBlank(new String[] {})  = true
     * StringUtils.isNoneBlank(new String[]{""}) = false
     * StringUtils.isNoneBlank("foo", "bar")     = true
     * </pre>
     *
     * @param css  the CharSequences to check, may be null or empty
     * @return {@code true} if none of the CharSequences are empty or null or whitespace only
     */
    public static boolean isNoneBlank(final CharSequence... css) {
        return !isAnyBlank(css);
    }

}
