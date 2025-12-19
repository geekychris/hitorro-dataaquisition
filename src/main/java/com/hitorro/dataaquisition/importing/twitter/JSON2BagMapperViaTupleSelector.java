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
package com.hitorro.dataaquisition.importing.twitter;

import com.hitorro.dataaquisition.importing.mapping.TupleSet;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.date.DateResolution;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.opers.LogicalSelector;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.typesystem.Bag;

import java.text.ParseException;
import java.util.Date;

/**
 *
 */
public class JSON2BagMapperViaTupleSelector extends BaseMapper<JSONElement, Bag> {
    public static Mapper<JSONElement, Long> jsonDateToLong = new BaseMapper<JSONElement, Long>() {
        DateResolution dr = DateResolution.json;
        private Long Zero = new Long(0);

        public Long apply(JSONElement elem) {
            try {
                Date d = dr.parseThreadLocal(elem.toString());
                return d.getTime();
            } catch (ParseException e) {
                return Zero;
            }
        }
    };

    protected LogicalSelector<JSONElement, GenericKeyValue<String, TupleSet>> ts;

    public JSON2BagMapperViaTupleSelector(LogicalSelector<JSONElement, GenericKeyValue<String, TupleSet>> ts) {
        this.ts = ts;
    }

    public Bag apply(JSONElement elem) {
        GenericKeyValue<String, TupleSet> gk = ts.select(elem);
        if (gk == null) {
            return null;
        }
        Bag bag = Bag.getBagForType(gk.getKey());
        if (bag == null) {
            return null;
        }
        gk.getValue().map(elem, bag);
        return bag;
    }
}
