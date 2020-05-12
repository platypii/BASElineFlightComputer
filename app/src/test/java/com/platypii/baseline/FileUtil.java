package com.platypii.baseline;

import androidx.annotation.NonNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class FileUtil {

    /**
     * Write string to temp file
     */
    @NonNull
    public static File makeFile(@NonNull String content) throws IOException {
        final File file = File.createTempFile("testfile", ".csv");
        final FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
        return file;
    }

    /**
     * Write string to temp file.gz
     */
    @NonNull
    public static File makeFileGz(@NonNull String content) throws IOException {
        final File file = File.createTempFile("testfile", ".csv");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));
        writer.write(content);
        writer.close();
        return file;
    }

}
