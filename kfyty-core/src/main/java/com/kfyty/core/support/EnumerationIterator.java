package com.kfyty.core.support;

import lombok.RequiredArgsConstructor;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * 描述: {@link java.util.Enumeration} 迭代支持
 *
 * @author kfyty725
 * @date 2023/5/22 9:11
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class EnumerationIterator<T> implements Iterable<T> {
    private final Enumeration<T> enumeration;

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
        };
    }
}
