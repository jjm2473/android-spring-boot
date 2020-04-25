package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Build;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

@SuppressLint("ObsoleteSdkInt")
public class AndroidClassesScanner {
    private static final Class baseDexClassLoaderClass;
    private static final DexGetter dexGetter;

    static {
        try {
            baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError();
        }
        if (Build.VERSION.SDK_INT >= 19) {
            dexGetter = new V19();
        } else if (Build.VERSION.SDK_INT >= 14) {
            dexGetter = new V14();
        } else {
            dexGetter = new V4();
        }
    }

    public static List<DexFile> dexs(ClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
        return dexGetter.dexs(loader);
    }

    public static void classes(ClassLoader loader, Consumer consumer) throws NoSuchFieldException, IllegalAccessException {
        try {
            ClassLoader current = loader;
            while (current != null) {
//                "java.lang.BootClassLoader".equals(current.getClass().getName())
                if (baseDexClassLoaderClass.isInstance(current)) {
                    for (DexFile dexFile : dexs(current)) {
                        Enumeration<String> entries = dexFile.entries();
                        while (entries.hasMoreElements()) {
                            consumer.accept(dexFile, entries.nextElement());
                        }
                    }
                }
                current = current.getParent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static interface Consumer {
        void accept(DexFile dexFile, String className);
    }

    private interface DexGetter {
        List<DexFile> dexs(ClassLoader loader) throws NoSuchFieldException, IllegalAccessException;
    }

    private static class V19 implements DexGetter {
        public List<DexFile> dexs(ClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            Object dexElements = getField(dexPathList, "dexElements");
            Field dexFileField = null;
            int length = Array.getLength(dexElements);
            List<DexFile> result = new ArrayList<>(length);
            for (int i=0;i<length;++i) {
                Object ele = Array.get(dexElements, i);
                if (dexFileField == null)  {
                    dexFileField = findField(ele, "dexFile");
                }
                result.add((DexFile) dexFileField.get(ele));
            }
            return result;
        }
    }
    private static class V14 implements DexGetter {
        public List<DexFile> dexs(ClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            Object dexElements = null;
            try {
                dexElements = getField(dexPathList, "dexElements");
            } catch (NoSuchFieldException e) {
                dexElements = getField(dexPathList, "pathElements");
            }
            Field dexFileField = null;
            int length = Array.getLength(dexElements);
            List<DexFile> result = new ArrayList<>(length);
            for (int i=0;i<length;++i) {
                Object ele = Array.get(dexElements, i);
                if (dexFileField == null)  {
                    dexFileField = findField(ele, "dexFile");
                }
                result.add((DexFile) dexFileField.get(ele));
            }
            return result;
        }
    }
    private static class V4 implements DexGetter{
        public List<DexFile> dexs(ClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
            Object mDexs = getField(loader, "mDexs");
            int length = Array.getLength(mDexs);
            List<DexFile> result = new ArrayList<>(length);
            for (int i=0;i<length;++i) {
                Object ele = Array.get(mDexs, i);
                result.add((DexFile)ele);
            }
            return result;
        }
    }


    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);


                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    /**
     * Replace the value of a field containing a non null array, by a new array containing the
     * elements of the original array plus the elements of extraElements.
     * @param instance the instance whose field is to be modified.
     * @param fieldName the field to modify.
     * @return
     */
    private static Object getField(Object instance, String fieldName) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        return jlrField.get(instance);
    }
}
