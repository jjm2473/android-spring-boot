package com.example.myapplication;

import java.util.Enumeration;
import java.util.Iterator;

public class CompEnumeration<T> implements Enumeration<T>,Iterator<T> {
    private Iterator<Enumeration<T>> iterator;
    private Enumeration<T> current;

    public CompEnumeration(Iterable<Enumeration<T>> coll) {
        this.iterator = coll.iterator();
        nextEnum();
    }

    @Override
    public boolean hasMoreElements() {
        if (current == null) {
            return false;
        }
        if (current.hasMoreElements()) {
            return true;
        } else {
            nextEnum();
            return hasMoreElements();
        }
    }

    @Override
    public T nextElement() {
        return current.nextElement();
    }

    @Override
    public boolean hasNext() {
        return hasMoreElements();
    }

    @Override
    public T next() {
        return nextElement();
    }

    private void nextEnum() {
        if (iterator.hasNext()) {
            current = iterator.next();
        } else {
            current = null;
        }
    }
}
