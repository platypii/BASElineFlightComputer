package com.platypii.baseline.cloud;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.util.Callback;
import java.util.List;

public class TheCloud {
    static final String baselineServer = "https://base-line.ws";

    public static void list(String auth, Callback<List<CloudData>> cb) {
        new ListTask(auth, cb).execute();
    }

    public static void upload(Jump jump, String auth, Callback<CloudData> cb) {
        new UploadTask(jump, auth, cb).execute();
    }

}
