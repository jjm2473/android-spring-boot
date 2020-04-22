package com.example.myapplication;

import org.hibernate.boot.archive.internal.JarFileBasedArchiveDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * try to take over {@link JarFileBasedArchiveDescriptor#visitArchive(org.hibernate.boot.archive.spi.ArchiveContext)}
 */
public class ApkFile extends JarFile {
    public ApkFile(String name) throws IOException {
        super(name);
    }

    @Override
    public JarEntry getJarEntry(String name) {
        return super.getJarEntry(name);
    }

    @Override
    public ZipEntry getEntry(String name) {
        return super.getEntry(name);
    }

    @Override
    public Enumeration<JarEntry> entries() {
        return super.entries();
    }

    @Override
    public Stream<JarEntry> stream() {
        return super.stream();
    }

    @Override
    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
        return super.getInputStream(ze);
    }
}
