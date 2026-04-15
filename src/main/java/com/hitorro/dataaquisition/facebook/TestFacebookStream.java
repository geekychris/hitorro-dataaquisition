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
package com.hitorro.dataaquisition.facebook;

import com.hitorro.language.Iso639Table;
import com.hitorro.language.IsoLanguage;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.JSONList;
import com.hitorro.util.json.iterators.GetJSONIteratorFromPath;
import com.hitorro.util.json.mapper.JSONIterIterMapper;
import com.hitorro.util.json.mapper.LongMinMaxJSONMapper;
import com.hitorro.util.testframework.EnhancedTestCase;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
//ht.dataaquisition.facebook.TestFacebookStream

/**
 * At the moment these are not really tests, but more code to play with ideas. amiga page= 34936988896
 * <p/>
 * http://www.facebook.com/search/opensearch_typeahead.php?format=xml&q=amiga
 * <p/>
 * Bunch of example search calls
 * <p/>
 * http://ncong.wordpress.com/2010/07/27/search-provider-xml/
 * <p/>
 * strpos instead of like:
 * <p/>
 * http://forum.developers.facebook.net/viewtopic.php?id=30775
 * <p/>
 * includes "search":
 * <p/>
 * http://developers.facebook.com/docs/api/
 */
public class TestFacebookStream extends EnhancedTestCase {
    public void testFaceBook() throws IOException, HttpException {

        FacebookSearch search = new FacebookSearch(null);

        search.setQuery("page", "amiga");
        search.setFields(new String[]{"name", "category", "likes"});
        search.setLimit(100);
        int page = 0;
        boolean hasResults;
        int result = 0;
        Iso639Table table = Iso639Table.getInstance();
        Set<String> ids = new HashSet();
        do {
            search.setPage(page);
            search.getAsArrayAndBuffer();
            Iterator<JSONElement> iter = FileUtil.isJsonIter.apply(search.getStreamFromBuffer()).nest(new GetJSONIteratorFromPath("data"));
            hasResults = false;
            while (iter.hasNext()) {
                hasResults = true;
                JSONElement elem = iter.next();
                JSONElement e = elem.getFromPath("id");
                IsoLanguage lang = table.getLanguageFromContent(elem.getFromPath("name").toString());
                ids.add(e.toString());
                Console.println("%s %s %s %s", result++, page, lang, elem.toString());
            }
            page++;
            Console.println(">>> Counts: %s, mapSize:%s", result, ids.size());
        }
        while (hasResults);

        //String s = new String(bytes);
        Fql fql = new Fql();
        fql.getClientBuilder().setConnectionTimeToLive(40000, TimeUnit.MILLISECONDS);
        LongMinMaxJSONMapper minmax = new LongMinMaxJSONMapper("created_time", "created_time");
        //String fanPageId = "73340321070";
        String fanPageId = "34936988896";
        // Stream the query results into a buffer so we can not only parse through it, we can write it to disk if we like it.
        int count = fql.select(Fql.StreamFields, "stream").where("source_id", "=", fanPageId).orderBy("created_time", true).limit(50).formatJSON().getAsArrayAndBuffer();


        // / we have a set of documents that come back in a json listFiles.  We remove the items one by one from the listFiles.  This should
        // allow decoding of many arrays of
        Iterator<JSONElement> jsi = FileUtil.isJsonIter.apply(fql.getStreamFromBuffer()).nest(JSONIterIterMapper.jsonIterIterMapper).map(minmax);
        while (jsi.hasNext()) {
            JSONElement elem = jsi.next();
            Console.println(elem.toString());
        }
        Console.println("Min: %s, Max: %s", minmax.getMin(), minmax.getMax());
        // actual page info

        count = fql.select(Fql.PageFields, "page").where("strpos(name,'jeep')", ">", 0).formatJSON().getAsArrayAndBuffer();
        jsi = FileUtil.isJsonIter.apply(fql.getStreamFromBuffer()).nest(JSONIterIterMapper.jsonIterIterMapper);
        while (jsi.hasNext()) {
            JSONElement elem = jsi.next();
            Console.println(elem.toString());
        }

        count = fql.select(Fql.PageFields, "page").where("page_id", "=", fanPageId).formatJSON().getAsArrayAndBuffer();
        jsi = FileUtil.isJsonIter.apply(fql.getStreamFromBuffer()).nest(JSONIterIterMapper.jsonIterIterMapper);
        while (jsi.hasNext()) {
            JSONElement elem = jsi.next();
            Console.println(elem.toString());
        }


        // some comments
        count = fql.select(Fql.CommentsFields, "comment").where("post_id", "=", "73340321070_10150096339456071").limit(50).formatJSON().getAsArrayAndBuffer();
        jsi = FileUtil.isJsonIter.apply(fql.getStreamFromBuffer()).nest(JSONIterIterMapper.jsonIterIterMapper);
        while (jsi.hasNext()) {
            JSONElement elem = jsi.next();
            Console.println(elem.toString());
        }
    }
}

class GetIteratorFromPath extends BaseMapper<JSONElement, AbstractIterator<JSONElement>> {
    public static GetIteratorFromPath mapper = new GetIteratorFromPath("data");
    private String path;

    public GetIteratorFromPath(String path) {
        this.path = path;
    }

    @Override
    public AbstractIterator<JSONElement> apply(final JSONElement e) {
        JSONElement e2 = e.getFromPath(path);
        if (e2 instanceof JSONList) {
            // just give back a one element iterator (its probably an error)
            return new CollectionIterator(((JSONList) e2).get());

        }
        return null;
    }
}

