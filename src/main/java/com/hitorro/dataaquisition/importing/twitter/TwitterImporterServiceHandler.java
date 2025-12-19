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
package com.hitorro.dataaquisition.importing.twitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.base.docprocessing.blockqueue.QInstance;
import com.hitorro.dataaquisition.importing.ImporterServiceHandler;
import com.hitorro.util.bagio.BF2TwitterBagSetting;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.typesystem.Bag;

import java.io.IOException;

/**
 * Simple importer for twitter objects that reads a queue of json tweets including deletes and outputs them into a new
 * queue.  Each document in this case has been ordered by its twitter id using the sorter.
 */
public class TwitterImporterServiceHandler implements ImporterServiceHandler {
    private DirectoryVisitorIterator iter;
    private QInstance qInst;

    private BaseMapper<BaseFile, AbstractIterator<Bag>> bf2objectIterator = new BaseMapper<BaseFile, AbstractIterator<Bag>>() {
        public AbstractIterator<Bag> apply(BaseFile bf) {
            return BaseFileUtil.bf2json.apply(bf).map(Twitter2Bag.twitter2bag);
        }
    };

    public String init(JsonNode map, DirectoryVisitorIterator iter) {
        this.iter = iter;
        initQueueWriter("ser", map);
        return null;
    }

    private boolean initQueueWriter(String extension, JsonNode map) {
        JsonNode copy = map.deepCopy();
        //copy.put("partitioner.class", DoNothingPartitioner.class.getCanonicalName());
        //copy.put("writer.class", HTSerializableWriterWritable.class.getCanonicalName());
        qInst = new QInstance();
        qInst.init(extension, copy);
        return true;
    }

    @Override
    public void run() {
        BF2TwitterBagSetting setter = new BF2TwitterBagSetting("srcfile", "created");
        while (true) {
            try {
                // We currently cant use the nested iterator as we dont have a way to marry
                // the object from the stream to the correct file.  This is a shortcoming
                // of some of the iterators that buffer an object, thus several chained iterators
                // will have a sequence of objects scattered throughout the iterator chain as they are
                // fed through select tree's or filters
                // Instead we feed one file at a time.
                iter.sinkWithSettable(qInst.getWriter(), bf2objectIterator, setter);
            } catch (IOException e) {
                Log.importer.error("Error importing %s %e", e, e);
            }
        }
    }
}
