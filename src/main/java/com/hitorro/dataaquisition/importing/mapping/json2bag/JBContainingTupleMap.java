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

import com.hitorro.dataaquisition.importing.mapping.ContainingTupleMap;
import com.hitorro.dataaquisition.importing.mapping.TupleSet;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.typesystem.Bag;

/**
 * The json input element is assumed to be a graph and we are attempting to get a single child element
 */
public class JBContainingTupleMap extends ContainingTupleMap<JSONElement, Bag> {
    public JBContainingTupleMap(String fieldIn, TupleSet set) {
        super(fieldIn, set);
    }

    @Override
    public void map(final JSONElement jsonElement, final Bag bag) {
        JSONElement child = jsonElement.getFromPath(fieldIn);
        if (child != null) {
            set.map(child, bag);
        }
    }
}
