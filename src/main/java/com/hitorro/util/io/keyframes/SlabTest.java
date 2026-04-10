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

import com.hitorro.base.filesetmanager.FileSet;
import com.hitorro.base.filesetmanager.FileSetManager;
import com.hitorro.dataaquisition.importing.twitter.Twitter2Bag;
import com.hitorro.util.bagio.BagUtil;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.constraint.IsType;
import com.hitorro.dataaquisition.twitter.TwitterUtil;

import java.io.IOException;


public class SlabTest extends EnhancedTestCase {
    public void testQeueue() {
        FileSet dq = FileSetManager.getQueue("twitterraw");
        BaseMapper mapper = dq.getIteratorMapper();
        Console.println();
    }

    public void teestBuildSlabFromBag() throws IOException {
        BaseFile inputFile = FileFileSystem.Root.getFileEnsuringDir("/ordered.ser.gz");
        BaseFile slabDir = FileFileSystem.Root.getFileEnsuringDir("/slabs/testslab");
        SlabUtil.constructSlabFromHTSerBag(inputFile, slabDir, 1, "id", 34);

        Slab<Bag> slab = new Slab(slabDir, BagUtil.buff2bag);
        SlabQuery<Bag> sq = slab.getQuery();
        KVDebug kvd = slab.getDebugKVList();
        Timer t = new Timer();
        t.start();
        for (int i = 0; i < kvd.entries; i++) {
            byte buff[] = sq.getCompressedBuffer(kvd.keys[i]);
            if (i % 10000 == 0) {
                Console.println("processed %s", i);
            }
        }

        Console.println("took %s ms", t.stop());
        Console.println();
    }

    /**
     * test that you can take a json queue and write it out as a id ordered setPhrases of bags
     *
     * @throws IOException
     */
    public void testSortAndKeyframewrite() throws Exception {
        SlabUtil.sortAndMergeQueueSegment(FileFileSystem.Root.getFile("/twitterqueue"),
                FileFileSystem.Root.getFile("/queuesorttmp"),
                new LogicalOrOperator(new IsDir(), new LogicalOrOperator(new FileEndsWith("ser", true), new FileEndsWith("ser.gz", true))),
                null,
                "htser.gz",
                200000,
                5,
                TwitterUtil.twitterHTSerBagFactory,
                new HTProperties(),
                BaseFileUtil.bf2json,
                Twitter2Bag.twitter2bag,
                new IsType("twitter"));

    }
}
