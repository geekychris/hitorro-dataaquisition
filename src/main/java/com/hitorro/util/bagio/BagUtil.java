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

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.keyframes.KeyFrame;
import com.hitorro.util.typesystem.Bag;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 *
 */
public class BagUtil {
    public static final com.hitorro.util.bagio.BagToString bag2bodystring = new com.hitorro.util.bagio.BagToString("body");


    public static final BaseMapper<Writer, Sink<Bag>> writer2xmlBagSink = new BaseMapper<Writer, Sink<Bag>>() {
        public Sink<Bag> apply(Writer writer) {
            return new com.hitorro.util.bagio.XMLBagWriter(writer);
        }
    };

    public static final BaseMapper<InputStream, AbstractIterator<Bag>> is2xmlbagiter = new BaseMapper<InputStream, AbstractIterator<Bag>>() {
        public AbstractIterator<Bag> apply(InputStream is) {
            try {
                return new com.hitorro.util.bagio.XMLBagIterator(is, "UTF-8");
            } catch (XMLStreamException e) {
                // XXX put logging
                return null;
            } catch (UnsupportedEncodingException e) {
                // XXX put logging too
                return null;
            }
        }
    };

    public static final BaseMapper<BaseFile, AbstractIterator<Bag>> bf2xmlbagiter = BaseFileUtil.bf2inputstream.combine(is2xmlbagiter);

    public static final BaseMapper<File, AbstractIterator<Bag>> fs2xmlbagiter = FileUtil.fsInputStream.combine(is2xmlbagiter);


    public static final BaseMapper<BaseFile, Sink<Bag>> bf2xmlBagSink = BaseFileUtil.bf2utf8Writer.combine(writer2xmlBagSink);

    public static final BaseMapper<Bag, KeyFrame> bag2kf = new Bag2FrameMapper(1, "id");
    public static final BaseMapper<KeyFrame, Bag> kf2bag = new Frame2BagMapper(1);
    public static final BaseMapper<byte[], Bag> buff2bag = new com.hitorro.util.bagio.Buff2BagMapper();

}
