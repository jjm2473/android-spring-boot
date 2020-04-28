package com.example.myapplication.util;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    public static void rm(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f:file.listFiles()) {
                    rm(f);
                }
            }
            if (!file.delete()) {
                throw new IOException("delete file failed: " + file.getAbsolutePath());
            }
        }
    }
}
