/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.dataaquisition.importing.mapping;

import com.hitorro.util.core.iterator.Mapper;

/**
 *
 */
public abstract class TupleMap<E, E1, I, O> implements Tuple<E, E1> {
    protected String pathIn;
    protected String pathOut;
    protected Mapper<I, O> mapper = null;

    public TupleMap(String pathin, String pathOut) {
        set(pathin, pathOut, null);
    }

    public TupleMap(String pathin, String pathOut, Mapper<I, O> mapper) {
        set(pathin, pathOut, mapper);
    }

    private void set(final String pathin, final String pathOut, final Mapper<I, O> mapper) {
        this.pathIn = pathin;
        this.pathOut = pathOut;
        this.mapper = mapper;
    }

    public abstract void map(E elem, E1 elemTarget);

    public void finishSetup() {

    }
}
