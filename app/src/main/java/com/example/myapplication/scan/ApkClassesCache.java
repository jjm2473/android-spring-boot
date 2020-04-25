package com.example.myapplication.scan;

import com.example.myapplication.AndroidClassResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

class ApkClassesCache {
    static List<JarEntry> clzEntries;
    static Map<String, ApkEntry> clzMap;
    static {
        try {
            List<String> classes = AndroidClassResource.getClasses(null);
            clzEntries = new ArrayList<>(classes.size());
            clzMap = new HashMap<>(classes.size());
            for (String clz : classes) {
                ApkEntry apkEntry = new ApkEntry(clz);
                clzEntries.add(apkEntry);
                clzMap.put(apkEntry.getName(), apkEntry);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
