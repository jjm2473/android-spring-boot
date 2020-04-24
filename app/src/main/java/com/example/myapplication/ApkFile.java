package com.example.myapplication;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;

/**
 * try to take over {@link org.hibernate.boot.archive.internal.JarFileBasedArchiveDescriptor#visitArchive(org.hibernate.boot.archive.spi.ArchiveContext)}
 */
public class ApkFile extends JarFile {
    private List<JarEntry> clzEntries;
    private Map<String, ApkEntry> clzMap;
    public ApkFile(String name) throws IOException {
        super(name);
        List<String> classes = AndroidClassResource.getClasses(null);
        clzEntries = new ArrayList<>(classes.size());
        clzMap = new HashMap<>(classes.size());
        for (String clz:classes) {
            ApkEntry apkEntry = new ApkEntry(clz);
            clzEntries.add(apkEntry);
            clzMap.put(apkEntry.getName(), apkEntry);
        }
    }

    @Override
    public ZipEntry getEntry(String name) {
        ApkEntry apkEntry = clzMap.get(name);
        if (apkEntry == null) {
            return super.getEntry(name);
        } else {
            return apkEntry;
        }
    }

    @Override
    public Enumeration<JarEntry> entries() {
        return mergedEntries();
    }

    @Override
    public int size() {
        return super.size() + clzEntries.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Stream<JarEntry> stream() {
        return StreamSupport.stream(Spliterators.spliterator(
                mergedEntries(), size(),
                Spliterator.ORDERED | Spliterator.DISTINCT |
                        Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    @Override
    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
        if (ze instanceof ApkEntry) {
            return new ByteArrayInputStream(((ApkEntry) ze).data);
        }
        return super.getInputStream(ze);
    }

    private IteratorEnumeration<JarEntry> mergedEntries() {
        return new CompEnumeration<>(Arrays.asList(super.entries(), new IteratorToEnumeration<>(clzEntries.iterator())));
    }

    private static class ApkEntry extends JarEntry {
        private static final byte[] MAGIC = "APK/".getBytes(StandardCharsets.US_ASCII);
        private byte[] data;
        public ApkEntry(String clz) {
            super(clz.replace('.', '/') + ".class");

            byte[] bytes = clz.getBytes(StandardCharsets.UTF_8);
            this.data = new byte[bytes.length + 4];
            System.arraycopy(MAGIC, 0, this.data, 0, 4);
            System.arraycopy(bytes, 0, this.data, 4, bytes.length);
        }

    }
}
