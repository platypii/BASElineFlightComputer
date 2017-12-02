package com.platypii.baseline.util;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {

    public static String toString(@NonNull InputStream input) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final byte buffer[] = new byte[1024];
        int bytesRead;
        while((bytesRead = input.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, bytesRead));
        }
        return builder.toString();
    }

}
