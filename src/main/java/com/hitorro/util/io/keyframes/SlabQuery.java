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

import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.compressedmap.CompressedPointerMapQuery;
import com.hitorro.util.io.compression.DictCompressor;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;

import java.io.IOException;

/**
 * Encapsulates all that is not thread safe for accessing a slab of data
 */
public class SlabQuery<T> {
    protected Slab<T> slab;
    protected CompressedPointerMapQuery cpmq;
    protected CInputStream slabIS;
    protected DictCompressor compressor;
    protected BaseMapper<byte[], T> mapper;
    protected KeyFrame kf = new KeyFrame();

    protected SlabQuery(Slab slab) throws IOException {
        this.slab = slab;
        mapper = slab.mapper;
        cpmq = slab.cpm.getQuery();
        compressor = new DictCompressor();
        slabIS = getSlabInputStream();
    }

    /**
     * Private input stream for the
     *
     * @return
     * @throws IOException
     */
    private CInputStream getSlabInputStream() throws IOException {
        return slab.dir.getChild(Slab.KFSlab).getCInputStream();
    }

    public T getDeserialized(long key) throws IOException {
        byte buff[] = getCompressedBuffer(key);
        if (buff == null) {
            return null;
        }
        byte buffNew[] = compressor.decompressBytes(buff);
        return mapper.apply(buffNew);
    }

    public byte[] getCompressedBuffer(long key) throws IOException {
        long ptr = cpmq.getPtr(key);
        if (ptr == -1) {
            return null;
        }
        slabIS.seek(ptr);
        kf.read(slabIS);
        return kf.buff;
    }
}
