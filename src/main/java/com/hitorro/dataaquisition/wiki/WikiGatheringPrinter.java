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

import com.hitorro.util.core.Console;
import org.wikimodel.wem.IWikiPrinter;
import org.wikimodel.wem.WikiParameters;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class WikiGatheringPrinter implements IWikiPrinter {
    StringBuilder sb = new StringBuilder();
    StringBuilder refsBuffer = new StringBuilder();
    int depth = 0;
    StringBuilder sBuilder = new StringBuilder();

    List<String> infoBoxes = new ArrayList();
    StringBuilder infoBoxSb = new StringBuilder();


    public void incrementDepth() {
        depth++;
        infoBoxSb.append("{");

    }

    public void decrementDepth() {
        depth--;
        infoBoxSb.append("}");
        if (depth == 0) {
            infoBoxes.add(infoBoxSb.toString());
            infoBoxSb.setLength(0);
        }
    }

    public void printSpecialSymbol(String symbol) {
        // do nothing here at the moment we dont want to print special symbols in this gatherer.
        char c = symbol.charAt(0);
        if (c == '<' || c == '>') {
            return;
        }
        print(symbol);
    }

    public void clear() {
        sb.setLength(0);
        sBuilder.setLength(0);
        refsBuffer.setLength(0);
        infoBoxes.clear();
        infoBoxSb.setLength(0);
    }

    public String getText() {
        return sb.toString();
    }

    public String getRefs() {
        return refsBuffer.toString();
    }

    public void printFormat(String format) {
    }

    @Override
    public void print(String str) {
        if (depth > 0) {
            infoBoxSb.append(str);
            String strT = str.trim();
            if (strT.equals("=")) {
                //key = sBuilder.toString();
                sBuilder.setLength(0);
                // assignment
                //Console.println("equals");
            } else if (strT.equals("|")) {
                // Console.println("bar");
                processKV();
            } else {
                sBuilder.append(str);
            }
            // Console.bprint(sb, str);
        } else {
            sb.append(str);
        }
    }

    private void processKV() {
        String value = sBuilder.toString();
        sBuilder.setLength(0);

    }

    @Override
    public void println(final String str) {
        if (depth > 0) {
            infoBoxSb.append(str);
            String strT = str.trim();
            if (strT.equals("=")) {
                //key = sBuilder.toString();
                sBuilder.setLength(0);
                // assignment
                //Console.println("equals");
            } else if (strT.equals("|")) {
                // Console.println("bar");
                processKV();
            } else {
                sBuilder.append(str);
            }
            // Console.bprint(sb, str);
        } else {
            Console.bprintln(sb, str);
        }
    }

    /**
     * encode language references, category references, etc
     *
     * @param ref
     * @param label
     * @param params
     */
    public void reference(String ref,
                          String label,
                          WikiParameters params) {
        Console.bprintln(refsBuffer, "REF: %s Label: %s Params:%s", ref, label, params);
        print("[[");
        print(label);
        print("]]");

    }
}
