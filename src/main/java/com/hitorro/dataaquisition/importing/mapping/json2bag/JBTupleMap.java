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
package com.hitorro.dataaquisition.importing.mapping.json2bag;

import com.hitorro.dataaquisition.importing.mapping.TupleMap;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.JSONType;
import com.hitorro.util.typesystem.Bag;

/**
 *
 */
public class JBTupleMap<I, O> extends TupleMap<JSONElement, Bag, JSONElement, O> {
    public JBTupleMap(String pathin, String pathOut) {
        super(pathin, pathOut);
    }

    public JBTupleMap(String pathin, String pathOut, Mapper<JSONElement, O> mapper) {
        super(pathin, pathOut, mapper);
    }

    @Override
    public void map(final JSONElement elem, Bag b) {
        JSONElement e = elem.getFromPath(pathIn);

        if (e != null && e.getJSONType() != JSONType.Null) {
            if (mapper == null) {
                b.setFromString(b, pathOut, e.toString(), false);
                return;
            }
            O out = mapper.apply(e);
            b.setValue(b, pathOut, out);
        }
    }
}
