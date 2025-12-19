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
package com.hitorro.dataaquisition.importing.wikipedia;

import com.hitorro.dataaquisition.importing.mapping.TupleSet;
import com.hitorro.dataaquisition.importing.mapping.TupleSet2BagBaseMapper;
import com.hitorro.dataaquisition.importing.mapping.xmlxe2bag.XEContainingTupleMap;
import com.hitorro.dataaquisition.importing.mapping.xmlxe2bag.XETupleSet;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.xml.XE;


/**
 *
 */
public class Wiki2Bag {
    public static Mapper<XE, Boolean> xevalueexists = new BaseMapper<XE, Boolean>() {
        public Boolean apply(XE elem) {
            if (elem != null) {
                return Boolean.TRUE;
            }
            // this path shouldnt chunkParser as we are only ever called if
            return Boolean.FALSE;
        }
    };
    public static Object[][] page = {{"title", "title"},
            {"id", "pageid"},
            {"redirect", "redirect", Wiki2Bag.xevalueexists}};

    public static Object[][] revision = {{"timestamp", "timestamp"},
            {"comment", "comment"},
            {"text", "body"}};
    public static final BaseMapper<XE, Bag> wikixe2bagmapper = new TupleSet2BagBaseMapper(getWikiMessageMappingTupleSet(), "wikipage");

    /*
    page
        page.title
        page.id
        page.redirect (tag that exists or not)
        page.revision.timestamp
        page.revision.comment
        page.revision.text
     */
    protected static TupleSet getWikiMessageMappingTupleSet() {
        TupleSet pageMap = new XETupleSet();
        pageMap.add(page);
        TupleSet revMap = new XETupleSet();
        revMap.add(revision);
        XEContainingTupleMap rev = new XEContainingTupleMap("revision", revMap);
        TupleSet ts = new XETupleSet();

        ts.add(pageMap);
        ts.add(rev);
        // combine the screen name and id together to give a unique url
        //ts.add(new JBTupleCombiner(new String[]{"user.screen_name", "id"}, new Mapper[]{null, null}, "http://twitter.com/%s/statuses/%s", "url"));
        ts.finishSetup();
        return ts;
    }
}
