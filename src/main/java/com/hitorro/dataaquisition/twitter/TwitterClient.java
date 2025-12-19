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
package com.hitorro.dataaquisition.twitter;

import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.http.HTTPClientCore;
import com.hitorro.util.io.StoreException;
import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TwitterClient extends HTTPClientCore {
    private String baseUrl;
    private StreamHandler handler;

    public TwitterClient(String url, String credsString, StreamHandler handler) throws MalformedURLException {
        super(credsString, null);
        authScope = createAuthScope(url);
        this.handler = handler;
    }

    public void stream(String url, List<String> ids, List<String> keywords, int backfill) throws IOException, HttpException, StoreException {
        setupCreds(url);
        setupCreds(url);
        HttpPost gm;

        gm = new HttpPost(url);
        List<NameValuePair> pairs = makeRequestBody(ids, keywords, backfill);
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
        gm.setEntity(entity);
        handler.process(getReturnInputStream(url, gm));
    }

    private List<NameValuePair> makeRequestBody(List<String> ids, List<String> keywords, int backfill) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (!ListUtil.nullOrEmpty(ids)) {
            params.add(createNameValuePair("follow", ids));
        }
        if (!ListUtil.nullOrEmpty(keywords)) {
            params.add(createNameValuePair("track", keywords));
        }
        if (backfill > 0) {

            NameValuePair countParam = new BasicNameValuePair("count", Integer.toString(backfill));
            params.add(countParam);
        }
        return params;
    }


}
