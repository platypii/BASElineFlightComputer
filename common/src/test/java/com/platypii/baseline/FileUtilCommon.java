package com.platypii.baseline;

import androidx.annotation.NonNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

/**
 * Gradle doesn't want to share, so duplicated FileUtil into common
 */
public class FileUtilCommon {

    /**
     * Write string to temp file.gz
     */
    @NonNull
    public static File makeFileGz(String content) throws IOException {
        final File file = File.createTempFile("testfile", ".csv");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));
        writer.write(content);
        writer.close();
        return file;
    }

}
