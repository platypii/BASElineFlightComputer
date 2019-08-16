package com.platypii.baseline;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Gradle doesn't want to share, so duplicated FileUtil into common
 */
public class FileUtilCommon {

    /**
     * Write string to temp file.gz
     */
    public static File makeFileGz(String content) throws IOException {
        final File file = File.createTempFile("testfile", ".csv");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));
        writer.write(content);
        writer.close();
        return file;
    }

}
