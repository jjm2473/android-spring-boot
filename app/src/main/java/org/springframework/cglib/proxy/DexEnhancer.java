package org.springframework.cglib.proxy;

import org.springframework.asm.ClassVisitor;
import org.springframework.cglib.core.AbstractClassGenerator;

import java.io.File;

public class DexEnhancer extends AbstractClassGenerator {
    private static File dexCacheDir;
    private static final Source SOURCE = new Source(DexEnhancer.class.getName());

    public static void setDexCacheDir(File dexCacheDir) {
        DexEnhancer.dexCacheDir = dexCacheDir;
    }

    public DexEnhancer() {
        super(SOURCE);
    }

    @Override
    protected ClassLoader getDefaultClassLoader() {
        return null;
    }

    @Override
    protected Object firstInstance(Class aClass) throws Exception {
        return null;
    }

    @Override
    protected Object nextInstance(Object o) throws Exception {
        return null;
    }

    @Override
    public void generateClass(ClassVisitor classVisitor) throws Exception {
        throw new IllegalStateException("unimpl");
    }
}
