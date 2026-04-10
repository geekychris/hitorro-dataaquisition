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

import com.hitorro.dataaquisition.wiki.WikiGathererListener;
import com.hitorro.dataaquisition.wiki.WikiGatheringPrinter;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.ResetableStringReader;
import org.wikimodel.wem.WikiParserException;
import org.wikimodel.wem.mediawiki.MediaWikiParser;


public class WikiTextMapper extends BaseMapper<String, String> {
    private MediaWikiParser parser = new MediaWikiParser();
    private ResetableStringReader sr = new ResetableStringReader("text");
    private WikiGatheringPrinter f = new WikiGatheringPrinter();
    private WikiGathererListener wgl = new WikiGathererListener(f);


    public WikiTextMapper() {

    }

    public boolean isThreadSafe() {
        return false;
    }

    public BaseMapper getCopy() {
        return new WikiTextMapper();
    }

    @Override
    public String apply(final String s) {
        sr.set(s);
        f.clear();
        try {
            parser.parse(sr, wgl);
        } catch (WikiParserException e) {
            return null;
        } catch (NullPointerException e) {
            // could write out example of a bad mapping
            return null;
        }
        return f.getText();
    }
}
