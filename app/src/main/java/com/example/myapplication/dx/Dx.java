package com.example.myapplication.dx;

import android.os.Build;

import com.android.dex.DexFormat;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.cf.direct.StdAttributeFactory;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.example.myapplication.util.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * 将多个.class文件转换成单个.dex文件
 */
public class Dx {
    private static File tempCache;

    public static void init(File tempCache) throws IOException {
        Dx.tempCache = tempCache;

        for (File f:tempCache.listFiles()) {
            FileUtils.rm(f);
        }

        tempCache.deleteOnExit();
        seq.set(0);
    }

    private static AtomicInteger seq = new AtomicInteger(0);
    // Only warn about not being able to deal with blacklisted methods once. Often this is no
    // problem and warning on every class load is too spammy.
    private static boolean didWarnBlacklistedMethods;
    private static boolean didWarnNonBaseDexClassLoader;

    private DxContext context;
    private DexOptions dexOptions;
    private CfOptions cfOptions;
    private ClassLoader sharedClassLoader;
    private DexFile outputDex;
    private boolean markAsTrusted;

    public Dx() {
        context = new DxContext();
        dexOptions = new DexOptions();
        dexOptions.minSdkVersion = Build.VERSION.SDK_INT;
        outputDex = new DexFile(dexOptions);
        cfOptions = new CfOptions();
    }

    public void addClass(byte[] jvmClass, String name) {
        DirectClassFile cf = new DirectClassFile(jvmClass, name.replace('.', '/') + ".class", cfOptions.strictNameCheck);
        cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        cf.getMagic(); // triggers the actual parsing
        ClassDefItem classDef = CfTranslator.translate(context, cf, jvmClass, cfOptions, dexOptions, outputDex);
        outputDex.add(classDef);
    }

    public byte[] generate() {
        try {
            return outputDex.toDex(null, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set shared class loader to use.
     *
     * <p>If a class wants to call package private methods of another class they need to share a
     * class loader. One common case for this requirement is a mock class wanting to mock package
     * private methods of the original class.
     *
     * <p>If the classLoader is not a subclass of {@code dalvik.system.BaseDexClassLoader} this
     * option is ignored.
     *
     * @param classLoader the class loader the new class should be loaded by
     */
    public void setSharedClassLoader(ClassLoader classLoader) {
        this.sharedClassLoader = classLoader;
    }

    public void markAsTrusted() {
        this.markAsTrusted = true;
    }

    private ClassLoader generateClassLoader(File result, File dexCache, ClassLoader parent) {
        try {
            boolean shareClassLoader = sharedClassLoader != null;

            ClassLoader preferredClassLoader = null;
            if (parent != null) {
                preferredClassLoader = parent;
            } else if (sharedClassLoader != null) {
                preferredClassLoader = sharedClassLoader;
            }

            Class baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");

            if (shareClassLoader) {
                if (!baseDexClassLoaderClass.isAssignableFrom(preferredClassLoader.getClass())) {
                    if (!preferredClassLoader.getClass().getName().equals(
                            "java.lang.BootClassLoader")) {
                        if (!didWarnNonBaseDexClassLoader) {
                            System.err.println("Cannot share classloader as shared classloader '"
                                    + preferredClassLoader + "' is not a subclass of '"
                                    + baseDexClassLoaderClass
                                    + "'");
                            didWarnNonBaseDexClassLoader = true;
                        }
                    }

                    shareClassLoader = false;
                }
            }

            // Try to load the class so that it can call hidden APIs. This is required for spying
            // on system classes as real-methods of these classes might call blacklisted APIs
            if (markAsTrusted) {
                try {
                    if (shareClassLoader) {
                        preferredClassLoader.getClass().getMethod("addDexPath", String.class,
                                Boolean.TYPE).invoke(preferredClassLoader, result.getPath(), true);
                        return preferredClassLoader;
                    } else {
                        return (ClassLoader) baseDexClassLoaderClass
                                .getConstructor(String.class, File.class, String.class,
                                        ClassLoader.class, Boolean.TYPE)
                                .newInstance(result.getPath(), dexCache.getAbsoluteFile(), null,
                                        preferredClassLoader, true);
                    }
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof SecurityException) {
                        if (!didWarnBlacklistedMethods) {
                            System.err.println("Cannot allow to call blacklisted super methods. "
                                    + "This might break spying on system classes." + e.getCause());
                            didWarnBlacklistedMethods = true;
                        }
                    } else {
                        throw e;
                    }
                }
            }

            if (shareClassLoader) {
                preferredClassLoader.getClass().getMethod("addDexPath", String.class).invoke(
                        preferredClassLoader, result.getPath());
                return preferredClassLoader;
            } else {
                return (ClassLoader) Class.forName("dalvik.system.DexClassLoader")
                        .getConstructor(String.class, String.class, String.class, ClassLoader.class)
                        .newInstance(result.getPath(), dexCache.getAbsolutePath(), null,
                                preferredClassLoader);
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("load() requires a Dalvik VM", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (InstantiationException e) {
            throw new AssertionError();
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        }
    }

    /**
     * Generates a dex file and loads its types into the current process.
     *
     * <h3>Picking a dex cache directory</h3>
     * The {@code dexCache} should be an application-private directory. If
     * you pass a world-writable directory like {@code /sdcard} a malicious app
     * could inject code into your process. Most applications should use this:
     * <pre>   {@code
     *
     *     File dexCache = getApplicationContext().getDir("dx", Context.MODE_PRIVATE);
     * }</pre>
     * If the {@code dexCache} is null, this method will consult the {@code
     * dexmaker.dexcache} system property. If that exists, it will be used for
     * the dex cache. If it doesn't exist, this method will attempt to guess
     * the application's private data directory as a last resort. If that fails,
     * this method will fail with an unchecked exception. You can avoid the
     * exception by either providing a non-null value or setting the system
     * property.
     *
     * @param parent the parent ClassLoader to be used when loading our
     *     generated types (if set, overrides
     *     {@link #setSharedClassLoader(ClassLoader) shared class loader}.
     * @param dexCache the destination directory where generated and optimized
     *     dex files will be written. If null, this class will try to guess the
     *     application's private data dir.
     */
    public ClassLoader generateAndLoad(ClassLoader parent, File dexCache) throws IOException {
        if (dexCache == null) {
            String property = System.getProperty("dx.dexcache");
            if (property != null) {
                dexCache = new File(property);
            } else {
                dexCache = tempCache;
                if (dexCache == null) {
                    throw new IllegalArgumentException("dexcache == null (and no default could be"
                            + " found; consider setting the 'dexmaker.dexcache' system property)");
                }
            }
        }

        File result = new File(dexCache, generateFileName());
        // Check that the file exists. If it does, return a DexClassLoader and skip all
        // the dex bytecode generation.
        if (result.exists()) {
            return generateClassLoader(result, dexCache, parent);
        }

        byte[] dex = generate();

        /*
         * This implementation currently dumps the dex to the filesystem. It
         * jars the emitted .dex for the benefit of Gingerbread and earlier
         * devices, which can't load .dex files directly.
         *
         * TODO: load the dex from memory where supported.
         */
        result.createNewFile();

        JarOutputStream jarOut =
                new JarOutputStream(new BufferedOutputStream(new FileOutputStream(result)));
        try {
            JarEntry entry = new JarEntry(DexFormat.DEX_IN_JAR_NAME);
            entry.setSize(dex.length);
            jarOut.putNextEntry(entry);
            try {
                jarOut.write(dex);
            } finally {
                jarOut.closeEntry();
            }
        } finally {
            jarOut.close();
        }

        return generateClassLoader(result, dexCache, parent);
    }

    DexFile getDexFile() {
        return outputDex;
    }

    private String generateFileName() {
        return "Dx_" + seq.incrementAndGet() +".jar";
    }
}
