package com.platypii.baseline;

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
    public static File makeFile(String content) throws IOException {
        final File file = File.createTempFile("testfile", ".csv");
        final FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
        return file;
    }

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
