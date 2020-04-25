package com.example.myapplication.dx;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.FieldId;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OverDexMaker {
    private DexMaker dexMaker;
    private List<String> defined;

    public OverDexMaker() {
        this.dexMaker = new DexMaker();
        this.defined = new ArrayList<>(1);
    }

    public void declare(TypeId<?> type, String sourceFile, int flags, TypeId<?> supertype, TypeId<?>... interfaces) {
        dexMaker.declare(type, sourceFile, flags, supertype, interfaces);
        String name = type.getName();
        defined.add(name.substring(1, name.length()-1).replace("/", "."));
    }

    public Code declare(MethodId<?, ?> method, int flags) {
        return dexMaker.declare(method, flags);
    }

    public void declare(FieldId<?, ?> fieldId, int flags, Object staticValue) {
        dexMaker.declare(fieldId, flags, staticValue);
    }

    public byte[] generate() {
        return dexMaker.generate();
    }

    public void setSharedClassLoader(ClassLoader classLoader) {
        dexMaker.setSharedClassLoader(classLoader);
    }

    public void markAsTrusted() {
        dexMaker.markAsTrusted();
    }

    public ClassLoader generateAndLoad(ClassLoader parent, File dexCache) throws IOException {
        return dexMaker.generateAndLoad(new OverClassLoader(parent, defined), dexCache);
    }
}
