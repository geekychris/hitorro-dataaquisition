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
package com.hitorro.dataaquisition.wiki;

import com.hitorro.basetext.dfindex.FPHashDict;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.longword.NamedBitsOfLong;
import com.hitorro.util.core.longword.WordBits;
import com.hitorro.util.core.map.FPHashLongMap;
import com.hitorro.util.json.keys.BasefileProperty;

import java.io.IOException;
import java.text.ParseException;

/**
 *
 */
public class WikipediaDict {
    public static final BasefileProperty DirKey = new BasefileProperty("wikipedia.dict.dir", "", "file://hthome/wiki");
    private WordBits bits;
    private NamedBitsOfLong nbol;
    private FPHashLongMap fpHashLongMap;
    private FPHashDict dict;

    public FPHashDict getDictionary() {
        return dict;
    }

    public String init() {
        bits = new WordBits();
        bits.add("iswikientry", 1);
        nbol = bits.get("iswikientry");
        bits.add("emotion", 4);
        nbol = bits.get("emotion");
        BaseFile bOut = DirKey.apply();

        try {
            fpHashLongMap = FPHashLongMap.loadFromDirectory(bits, bOut, 10, true, true);
            dict = new FPHashDict(fpHashLongMap, 0);
        } catch (IOException e) {
            return e.toString();
        } catch (ParseException e) {
            return e.toString();
        }
        return null;
    }
}

