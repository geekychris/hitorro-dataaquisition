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
package com.hitorro.util.sqllike;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.http.HTTPClientCore;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.IOUtil;
import org.apache.http.HttpException;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * Base class used for performing sql like http requests ala Facebook and Yql.
 */
public abstract class SqlLike<T extends SqlLike> extends HTTPClientCore {
    protected StringBuilder sb = new StringBuilder();
    protected String accessToken;
    protected boolean formatJSON = false;
    protected String url;
    protected byte[] buff;
    protected boolean usePost = false;
    private int limit = -1;
    private int page = -1;
    private HttpClient httpClient;
    private AuthScope authScope;
    private String username;
    private String password;

    public SqlLike(String accessToken) {
        super(null, null);
        this.accessToken = accessToken;
    }

    public T select(String fields[], String from) {
        this.clearBuffer();
        sb.append("select ");
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(fields[i]);
        }
        Console.bprint(sb, " from %s ", from);
        return (T) this;
    }

    public T where(String field, String operator, int value) {
        Console.bprint(sb, "where %s %s %s ", field, operator, value);
        return (T) this;
    }

    public T where(String field, String operator, String value) {
        Console.bprint(sb, "where %s %s '%s' ", field, operator, value);
        return (T) this;
    }

    public T whereIn(String field, List<String> ids) {
        sb.append(" where ");
        return inIDS(field, ids);
    }

    public T inIDS(String field, List<String> ids) {
        Console.bprint(sb, "in %s = (", field);
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Console.bprintln(sb, "'%s'", ids.get(i));
        }
        return (T) this;
    }

    public T and(String field, String operator, String value) {
        Console.bprintln(sb, " and %s %s '%s'", field, operator, value);
        return (T) this;
    }

    public T and(String field, String operator, Date date) {
        Console.bprintln(sb, " and %s %s '%s'", field, operator, date.getTime());
        return (T) this;
    }

    public T orderBy(String field, boolean ascending) {                   //ORDER BY created_time
        if (ascending) {
            Console.bprint(sb, " ORDER BY %s ASC", field);
        } else {
            Console.bprint(sb, " ORDER BY %s DESC", field);
        }
        return (T) this;
    }

    public T limit(int limit) {
        Console.bprint(sb, " limit %s", limit);
        return (T) this;
    }

    public T formatJSON() {
        formatJSON = true;
        return (T) this;
    }

    public void clearBuffer() {
        sb.setLength(0);
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setPage(int offset) {
        this.page = offset;
    }

    public abstract String getRoot();

    public abstract String getRootS();

    public InputStream getStream() throws IOException, HttpException {
        setupCreds(getUrl());

        setupCreds(url);
        HttpRequestBase gm;

        if (usePost) {
            gm = new HttpPost(url);
        } else {
            gm = new HttpGet(url);
        }
        return getReturnInputStream(url, gm);
    }

    public int getAsArrayAndBuffer() throws IOException, HttpException {
        buff = getAsArray();
        return buff.length;
    }

    public InputStream getStreamFromBuffer() {
        return new ByteArrayInputStream(buff);
    }

    public byte[] getAsArray() throws IOException, HttpException {
        InputStream is = getStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtil.copyStream(is, baos);
        return baos.toByteArray();
    }

    public String getUrl() throws MalformedURLException, UnsupportedEncodingException {
        StringBuilder res = new StringBuilder();
        getUrl(res);
        return res.toString();
    }

    public void getUrl(StringBuilder res) throws UnsupportedEncodingException {
        if (StringUtil.nullOrEmptyString(accessToken)) {
            res.append(getRoot());
            addMisc(res);
            res.append(URLEncoder.encode(sb.toString(), "UTF-8"));
        } else {
            res.append(getRootS());
            addMisc(res);
            res.append(URLEncoder.encode(sb.toString(), "UTF-8"));
            Console.bprint(sb, "&access_token=%s", accessToken);
        }
        if (formatJSON) {
            // was JSON upper case but yahoo seems to only like json???
            Console.bprint(res, "&format=json");
        }
    }


    private void addMisc(final StringBuilder res) {
        if (limit != -1) {
            res.append("&limit=");
            res.append(limit);
        }
        if (page != -1) {
            res.append("&offset=");
            res.append(page * limit);
        }
    }
}
