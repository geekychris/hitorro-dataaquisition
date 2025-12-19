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

import com.hitorro.base.docprocessing.blockqueue.QInstance;
import com.hitorro.dataaquisition.fetcher.FetchServiceHandler;
import com.hitorro.util.basefile.tools.queue.writer.partitioners.DoNothingPartitioner;
import com.hitorro.util.basefile.tools.queue.writer.serializer.SimpleByteBufferWriterWritable;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.http.HttpException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 *
 */
public class TwitterFetchHandler implements FetchServiceHandler {
    public final static StringProperty CredentialsKey = new StringProperty("credentials", "'u1:password1,u2:password2 ...'", "chrisxyz11:ariba1");
    public final static StringProperty StreamURLKey = new StringProperty("url", "base url for streaming query", "http://stream.twitter.com/1/statuses/sample.json?delimited=length");
    public final static StringProperty StreamFollowURLKey = new StringProperty("url_follow", "base url for streaming query", "http://stream.twitter.com/1/statuses/filter.json");

    public final static FileProperty IdFile = new FileProperty("idfile", "file containing twitter ids to follow");

    private SinkStreamHandler handler;
    private TwitterClient tw;
    private List<String> ids = null;
    private String url;

    public String getFileExtension() {
        return "json";
    }

    public String init(QInstance qInst) {
        String creds = CredentialsKey.apply();
        String url = StreamURLKey.apply();
        handler = new SinkStreamHandler(-1, qInst.getWriter());
        try {
            File f = IdFile.apply();
            if (FileUtil.notNullAndExists(f)) {
                ids = FileUtil.getLinesFromFile(f);
            }
            if (ListUtil.notNullAndContainsRows(ids)) {
                url = StreamFollowURLKey.apply();
            }

            tw = new TwitterClient(url, creds, handler);

        } catch (FileNotFoundException e) {
            return e.getMessage();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getPartitioner() {
        return DoNothingPartitioner.class.getCanonicalName();
    }

    public String getWriter() {
        return SimpleByteBufferWriterWritable.class.getCanonicalName();
    }

    public void run() {
        try {
            tw.stream(url, ids, null, -1);
        } catch (IOException e) {
            Log.fetcher.error("Unable to continue streaming twitter data %s %e", e, e);
        } catch (StoreException e) {
            Log.fetcher.error("Unable to continue streaming twitter data %s %e", e, e);
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }
}
