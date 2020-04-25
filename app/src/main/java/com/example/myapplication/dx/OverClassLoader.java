package com.example.myapplication.dx;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OverClassLoader extends ClassLoader {
    private Set<String> blacklist;

    public OverClassLoader(ClassLoader parent, Collection<String> blacklist) {
        super(parent);
        this.blacklist = new HashSet<>(blacklist);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (blacklist.contains(name)) {
            throw new ClassNotFoundException(name + " was blocked by " + this.toString());
        }
        return super.loadClass(name, resolve);
    }
}
