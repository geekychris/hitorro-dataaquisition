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

import com.hitorro.dataaquisition.importing.mapping.Tuple;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.xml.XE;


public class XETupleCombiner implements Tuple<XE, Bag> {
    private String fields[];
    private Mapper mappers[];
    private String targetField;
    private String formatString;

    public XETupleCombiner(String fields[], Mapper mappers[], String formatString, String targetField) {
        this.fields = fields;
        this.mappers = mappers;
        this.targetField = targetField;
        this.formatString = formatString;
    }

    @Override
    public void map(final XE jsonElement, final Bag bag) {
        String target[] = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            XE e = jsonElement.get(fields[i]);
            if (e == null) {
                // we cant apply, we dont have everything we need.
                return;
            }
            if (mappers[i] != null) {
                target[i] = mappers[i].apply(e).toString();
            } else {
                target[i] = e.toString();
            }
        }
        String t = Fmt.Sargs(formatString, target);
        bag.setFromString(bag, targetField, t, false);
    }

    public void finishSetup() {

    }
}
