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

import com.hitorro.util.basefile.tools.queue.writer.serializer.ByteArrayWrapper;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.StoreException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Take a stream of twitter data and send it to some kind of put.  This would typically be a block writer.
 */
public class SinkStreamHandler implements StreamHandler {
    private boolean runningState = true;
    private int maxResults;
    private Sink<ByteArrayWrapper> writer;

    public SinkStreamHandler(int maxResults, Sink<ByteArrayWrapper> writer) {
        this.maxResults = maxResults;
        this.writer = writer;
    }

    @Override
    public void process(final InputStream is) throws IOException, StoreException {
        BufferedInputStream bis = new BufferedInputStream(is);
        DataInputStream dis = new DataInputStream(bis);
        int counter = 0;
        ByteArrayWrapper wrapper = new ByteArrayWrapper();
        while (runningState) {
            String sizeString = dis.readLine();
            if (StringUtil.nullOrEmptyOrBlankString(sizeString)) {
                continue;
            }
            wrapper.size = Integer.parseInt(sizeString);
            wrapper.buff = new byte[wrapper.size];

            readDocument(wrapper.buff, dis);
            counter++;

            writer.add(wrapper);

            if (maxResults > 0 && maxResults < counter) {
                runningState = false;
            }
        }
    }

    private void readDocument(byte[] b, InputStream is) throws IOException {
        int bytesRead = 0;
        int attemptCount = 0;
        while (bytesRead < b.length) {
            int count = is.read(b, bytesRead, b.length - bytesRead);
            if (count == -1) {
                is.close();
                throw new IOException(Fmt.S("Stream returned -1 %s read %s bytes of bytes: %s", is, bytesRead, b.length));
            } else if (count == 0) {
                attemptCount++;
                Env.sleepMillis(100);
            } else {
                attemptCount = 0;
            }

            bytesRead += count;

            if (attemptCount == 1000) {
                throw new IOException("No new data");
            }
        }
    }
}
