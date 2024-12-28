package com.sk.revisit;

import android.net.Uri;

public class MyUtils {
    String rootPath;

    public MyUtils(String rootPath) {
        this.rootPath = rootPath;
    }

    public String buildLocalPath(Uri uri) {
        String last = uri.getLastPathSegment();
        String localPathT = rootPath + '/' + uri.getHost() + uri.getEncodedPath();

        if (last == null) {
            return localPathT + "index.html";
        }

        if (last.contains(".")) {
            return localPathT;
        } else {
            return uri.toString().endsWith("/") ? localPathT + "index.html" : localPathT + "/index.html";
        }
    }
}
