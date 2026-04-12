package com.mynote.util;

@FunctionalInterface
public interface Convert<T, R> {
    R convert(T source);
}