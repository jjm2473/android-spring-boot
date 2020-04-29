package com.example.myapplication.scan;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.myapplication.CompEnumeration;
import com.example.myapplication.IteratorEnumeration;
import com.example.myapplication.IteratorToEnumeration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
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
    public ApkFile(String name) throws IOException {
        super(name);
    }

    @Override
    public ZipEntry getEntry(String name) {
        ApkEntry apkEntry = ApkClassesCache.clzMap.get(name);
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
        return super.size() + ApkClassesCache.clzEntries.size();
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
//        return new IteratorToEnumeration<>(ApkClassesCache.clzEntries.iterator());
        return new CompEnumeration<>(Arrays.asList(super.entries(), new IteratorToEnumeration<>(ApkClassesCache.clzEntries.iterator())));
    }
}
