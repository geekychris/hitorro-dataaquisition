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
package com.hitorro.dataaquisition.importing.mapping.xmlxe2bag;

import com.hitorro.dataaquisition.importing.mapping.TupleMap;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.xml.XE;

/**
 *
 */
public class XETupleMap<I, O> extends TupleMap<XE, Bag, XE, O> {
    public XETupleMap(String pathin, String pathOut) {
        super(pathin, pathOut);
    }

    public XETupleMap(String pathin, String pathOut, Mapper<XE, O> mapper) {
        super(pathin, pathOut, mapper);
    }

    @Override
    public void map(final XE elem, Bag b) {
        XE e = elem.get(pathIn);

        if (e != null) {
            if (mapper == null) {
                // currently dont know how to handle attributes
                b.setFromString(b, pathOut, e.getValue(), false);
                return;
            }
            O out = mapper.apply(e);
            b.setValue(b, pathOut, out);
        }
    }
}
