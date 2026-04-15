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
package com.hitorro.util.io.keyframes;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.compressedmap.CompressedPointerMap;
import com.hitorro.util.io.compression.DictCompressor;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.json.keys.IntegerProperty;

import java.io.IOException;

/**
 * Encapsulates a keyfile containing key-> buffer
 */
public class Slab<T> {
    public static final String KFSlab = "keyframe.slab";
    public static final String Compression = "compression_version";
    public static final String SplitSize = "split_size";
    public static final String Count = "row_count";
    public static final String Build_Date = "build_date";

    public static final String PropsFileKey = "stats.properties";


    public static IntegerProperty CompressionProp = new IntegerProperty(Compression, "", 1);
    public static IntegerProperty CounterProp = new IntegerProperty(Count, "", 1);
    public static IntegerProperty LoweBitsProp = new IntegerProperty(SplitSize, "", 1);
    BaseFile dir;

    // for read
    CompressedPointerMap cpm;
    BaseMapper<byte[], T> mapper;
    private boolean openedForRead = false;
    private JsonNode props;

    public Slab(BaseFile dir, BaseMapper<byte[], T> mapper) throws IOException {
        this.dir = dir;
        this.mapper = mapper;
        props = dir.getChild(PropsFileKey).getPropertiesAsJsonNode();
    }

    public DictCompressor getCompressor() throws IOException {
        DictCompressor compressor = new DictCompressor();
        compressor.init(CompressionProp.apply(props));
        return compressor;
    }

    /**
     * Debug method to get the keys and the pointer offsets out of the slab so one can exercise the slab in perf and
     * debugging exercises.
     *
     * @return
     * @throws IOException
     */
    public KVDebug getDebugKVList() throws IOException {
        CInputStream is = dir.getChild(KFSlab).getCInputStream();
        int counter = 0;
        int ptrNotFound = 0;
        int recordNotFound = 0;

        int number = CounterProp.apply(props);
        long key[] = new long[number];

        long value[] = new long[number];
        counter = 0;
        KeyFrame kf = new KeyFrame();
        while (kf.read(is)) {
            if (kf.key != 0) {
                key[counter] = kf.key;
                value[counter++] = kf.recordPointer;
            }
        }
        KVDebug kvd = new KVDebug();
        kvd.entries = number;
        kvd.keys = key;
        kvd.values = value;
        return kvd;
    }

    public synchronized SlabQuery getQuery() throws IOException {
        openForRead();
        return new SlabQuery(this);
    }

    public boolean openForRead() {
        if (openedForRead) {
            return true;
        }

        cpm = new CompressedPointerMap(dir, LoweBitsProp.apply(props));
        openedForRead = true;
        return true;
    }
}
