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

import java.util.ArrayList;
import java.util.List;


public abstract class TupleSet<I, O> implements Tuple<I, O> {
    protected Tuple[] tuples;
    protected List<Tuple> list = new ArrayList();

    public TupleSet() {

    }

    public void add(Tuple tp) {
        list.add(tp);
    }

    public abstract TupleMap getInstance(String in, String out, Mapper mapper);

    public void add(String in, String out, Mapper mapper) {
        add(getInstance(in, out, mapper));
    }

    public void add(String in, String out) {
        add(getInstance(in, out, null));
    }

    /**
     * Load from a 2d array consisting of: {in(java.lang.String), out(java.lang.String), me(com.hitorro.util.iter.Mapper)}
     *
     * @param rows
     */
    public void add(Object rows[][]) {
        for (Object row[] : rows) {
            switch (row.length) {
                case 2:
                    add((String) row[0], (String) row[1], null);
                    break;
                case 3:
                    add((String) row[0], (String) row[1], (Mapper) row[2]);
                    break;
            }
        }
        finishSetup();
    }

    public void finishSetup() {
        Tuple ts[] = new Tuple[list.size()];
        tuples = list.toArray(ts);
        for (Tuple t : tuples) {
            t.finishSetup();
        }
    }

    public void set(TupleMap[] tuples) {
        this.tuples = tuples;
    }

    public void map(I i, O o) {
        for (Tuple tp : tuples) {
            tp.map(i, o);
        }
    }
}

