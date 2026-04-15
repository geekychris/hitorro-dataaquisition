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

import com.hitorro.base.BaseBaseFileUtil;
import com.hitorro.util.bagio.BagUtil;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.DummyBaseMapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.TakeRightRowMerger;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.HTSerializableUtil;

import java.util.Comparator;


public class TwitterUtil { //@Override
    //public Bag getNewObject ()
    //{
    //    return Bag.getBagForType("twitter");
    // }

    public static BaseFileAccessingObjectFactory<Bag> twitterXMLBagFactory =
            new BaseFileAccessingObjectFactory<Bag>()
                    .setBaseFileSinkMapper(BagUtil.bf2xmlBagSink)
                    .setBaseFileToChainingMapper(BagUtil.bf2xmlbagiter)
                    .setComparitor(TweetIdComparator.comp)
                    .setMerger(TakeRightRowMerger.me);


    /**
     * We have to chain the HTSerializable mappers to a dummy me that makes an unsafe cast from htserializable to
     * Bag
     */
    public static BaseFileAccessingObjectFactory<Bag> twitterHTSerBagFactory =
            new BaseFileAccessingObjectFactory<Bag>()
                    .setBaseFileSinkMapper(HTSerializableUtil.bf2htser.combine(new DummyBaseMapper<Sink<? extends HTSerializable>, Sink<Bag>>()))
                    .setBaseFileToChainingMapper(BaseBaseFileUtil.bf2htser.combine(new DummyBaseMapper<AbstractIterator<? extends HTSerializable>, AbstractIterator<Bag>>()))
                    .setComparitor(TweetIdComparator.comp)
                    .setMerger(TakeRightRowMerger.me);

    public static class TweetIdComparator implements Comparator<Bag> {
        public static TweetIdComparator comp = new TweetIdComparator();

        @Override
        public int compare(final Bag bag, final Bag bag1) {
            Long b1Id = (Long) bag.getValue(bag, "id");


            Long b2Id = (Long) bag1.getValue(bag1, "id");
            if (b1Id == null) {
                // XXX broken????
                return -1;
            }
            if (b2Id == null) {
                // XXX broken????
                return 1;
            }
            long b1 = b1Id.longValue();
            long b2 = b2Id.longValue();
            if (b1 > b2) {
                return 1;
            }
            if (b1 < b2) {
                return -1;
            }
            return 0;
        }
    }
}
