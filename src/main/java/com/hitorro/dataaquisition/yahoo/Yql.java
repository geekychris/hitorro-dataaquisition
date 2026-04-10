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
package com.hitorro.dataaquisition.yahoo;


import com.hitorro.util.core.Constants;
import com.hitorro.util.sqllike.SqlLike;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;


public class Yql extends SqlLike<Yql> {
    //http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22YHOO%22%2C%22AAPL%22%2C%22GOOG%22%2C%22MSFT%22)%0A%09%09&diagnostics=true&env=http%3A%2F%2Fdatatables.org%2Falltables.env

    protected static String root = "http://query.yahooapis.com/v1/public/yql?q=";
    protected static String rootS = "http://query.yahooapis.com/v1/public/yql?q=";
    private StringBuilder postAppend = new StringBuilder();

    public Yql() {
        super(null);
    }

    public Yql(String accessToken) {
        super(accessToken);
    }

    public String getRoot() {
        return root;
    }

    public String getRootS() {
        return rootS;
    }

    public void clearBuffer() {
        super.clearBuffer();
        postAppend.setLength(0);
    }

    public String getUrl() throws MalformedURLException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        getUrl(sb);
        sb.append(postAppend.toString());
        return sb.toString();
    }

    public Yql whereSymbolIn(List<String> symbols) {
        sb.append(" where symbol in(");
        for (int i = 0; i < symbols.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(Constants.DoubleQuoteChar);
            sb.append(symbols.get(i));
            sb.append(Constants.DoubleQuoteChar);
        }
        sb.append(")");
        return this;
    }

    public Yql env(String e) {

        postAppend.append("&env=");
        postAppend.append(e);
        return this;
    }

    public Yql diagnostics() {
        postAppend.append("&diagnostics=true");
        return this;
    }
}