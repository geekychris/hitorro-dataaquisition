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

import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.compression.DictCompressor;
import com.hitorro.util.io.keyframes.KeyFrame;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.HTSerializableUtil;

import java.io.IOException;

/**
 *
 */
public class Bag2FrameMapper extends BaseMapper<Bag, KeyFrame> {
    private DictCompressor comp;
    private String idField;
    private int version;

    public Bag2FrameMapper(int version, String idField) {
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
    public KeyFrame apply(final Bag e) {
        byte buff[] = HTSerializableUtil.getHTSerializableBuffer(e);
        KeyFrame kf = new KeyFrame();
        Object o = e.getValue(e, idField);
        if (o == null) {
            return null;
        }
        if (!(o instanceof Long)) {
            return null;
        }
        kf.key = ((Long) o).longValue();
        int size = comp.compressBytes(buff);
        kf.buff = new byte[size];
        System.arraycopy(comp.getCompressionBuffer(), 0, kf.buff, 0, size);
        kf.compressionVersion = version;
        kf.payloadByteCount = kf.buff.length;
        return kf;
    }
}

