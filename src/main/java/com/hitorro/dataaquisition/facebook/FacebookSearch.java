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

import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.sqllike.SqlLike;

/**
 *
 */
public class FacebookSearch extends SqlLike<FacebookSearch> {
    String root = "http://graph.facebook.com/search?type=%s&q=%s";
    String rootS = "http://graph.facebook.com/search?type=%s&q=%s";
    private String query;
    private String type;
    private String fields[] = null;

    public FacebookSearch(String accessToken) {
        super(accessToken);
        //usePost = true;
    }

    public void setFields(String fields[]) {
        this.fields = fields;
    }

    public void setQuery(String type, String query) {
        this.type = type;
        this.query = query;
    }

    @Override
    public String getRoot() {
        return aux(root);
    }

    @Override
    public String getRootS() {
        return aux(rootS);
    }

    private String aux(String r) {
        StringBuilder sb = new StringBuilder();

        Console.bprint(sb, r, type, query);
        if (!ArrayUtil.nullOrEmpty(fields)) {
            sb.append("&fields=");
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(fields[i]);
            }
        }
        return sb.toString();
    }
}
