package com.example.myapplication;

import java.util.Iterator;

public class IteratorToEnumeration<T> implements IteratorEnumeration<T> {
    private Iterator<T> delegate;

    public IteratorToEnumeration(Iterator<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        return delegate.next();
    }

    @Override
    public boolean hasMoreElements() {
        return hasNext();
    }

    @Override
    public T nextElement() {
        return next();
    }
}
