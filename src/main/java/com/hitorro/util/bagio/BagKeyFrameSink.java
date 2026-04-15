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
package com.hitorro.util.bagio;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.compression.DictCompressor;
import com.hitorro.util.io.keyframes.KeyFrame;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.HTSerializableUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Write bags to disk as keyframes.  This compresses the serialized form of the bag in an attempt to make the disk usage
 * less.
 */
public class BagKeyFrameSink implements Sink<Bag> {
    public static BasefileProperty OutFileKey = new BasefileProperty("dir", "output directory using basefile format");
    public static IntegerProperty VersionKey = new IntegerProperty("version", "version number of file format", 1);
    public static StringProperty IdField = new StringProperty("idfield", "field that contains the unique id", "id");


    private int counter = 0;
    private Timer timer = new Timer();
    private java.io.DataOutputStream os;
    private DictCompressor comp;
    private String idField;
    private int version;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private KeyFrame kf = new KeyFrame();
    private long uncompressed;
    private long compressed;
    private boolean stopped = false;

    public BagKeyFrameSink(java.io.DataOutputStream os, int version, String idField) {
        initAux(os, version, idField);
    }

    public double getCompressionRatio() {
        if (uncompressed == 0) {
            return -1;
        }
        double uncD = uncompressed;
        double compD = compressed;
        return compD / uncD;
    }

    private void initAux(final DataOutputStream os, final int version, final String idField) {
        this.os = os;
        this.version = version;
        this.idField = idField;
        comp = new DictCompressor();

        try {
            comp.init(version);
        } catch (IOException e) {
            Log.compression.error("Unable to construct compressor %s %e", e, e);
        }
    }

    @Override
    public boolean init(JsonNode node) {
        try {
            initAux(OutFileKey.apply(node).getDataOutputStream(),
                    VersionKey.apply(node),
                    IdField.apply(node));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean start() {
        timer.start();
        return true;
    }

    @Override
    public boolean add(final Bag bag) {
        counter++;
        if (counter % 10000 == 0) {
            long time = timer.stop();
            Log.compression.info("Compressed another 10000 in %s seconds(so far %s)", time / 1000, counter);
            timer.start();
        }
        byte buff[] = HTSerializableUtil.getHTSerializableBuffer(bag, baos);
        uncompressed += buff.length;

        Object o = bag.getValue(bag, idField);
        if (o == null) {
            return false;
        }
        if (!(o instanceof Long)) {
            return false;
        }
        kf.key = ((Long) o).longValue();
        return !compressAndWrite(buff);
    }

    private boolean compressAndWrite(final byte[] buff) {
        try {
            int size = comp.compressBytes(buff, buff.length);
            compressed += size;
            kf.compressionVersion = version;
            kf.payloadByteCount = size;
            kf.buff = comp.getCompressionBuffer();
            kf.writeToJavaOs(os);
        } catch (IOException e) {
            Log.compression.error("Unable compress compressor %s %e", e, e);
            return true;
        }
        return false;
    }

    @Override
    public boolean stop() throws IOException {
        if (stopped) {
            return true;
        }
        stopped = true;
        kf.closeJavaOS(os);
        return true;
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}

