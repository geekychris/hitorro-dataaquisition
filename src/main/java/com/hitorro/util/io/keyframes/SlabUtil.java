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

import com.hitorro.base.BaseBaseFileUtil;
import com.hitorro.dataaquisition.importing.twitter.Twitter2Bag;
import com.hitorro.util.bagio.BagKeyFrameSink;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.mappers.DummyBaseMapper;
import com.hitorro.util.core.opers.AlwaysTrueOperator;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.datefilters.DateRangeFilterIntf;
import com.hitorro.util.datefilters.NoOpFilter;
import com.hitorro.util.io.compressedmap.CompressedPointerMap;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.buckets.BaseFileBucketWriter;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.iterator.BaseFileSelectTreeController;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.constraint.IsType;
import com.hitorro.dataaquisition.twitter.TwitterUtil;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Utilities for constructing slabs from ordered queues
 */
public class SlabUtil {
    /**
     * given a bag iterator ordered by its ID field, create a slab file (keyframe file) that has compressed content
     * using the compression version provided. Once created a compressedpointermap is created from that to be used in
     * lookups.
     *
     * @param inputFile
     * @param slabDir
     * @param compressionVersion
     * @param idField
     * @param splitSize
     * @return
     * @throws IOException
     */
    public static final boolean constructSlabFromHTSerBag(BaseFile inputFile, BaseFile slabDir,
                                                          int compressionVersion, String idField,
                                                          int splitSize) throws IOException {
        return constructSlabFromBag(inputFile, slabDir, compressionVersion, idField, splitSize, BaseBaseFileUtil.bf2htser.combine(new DummyBaseMapper<AbstractIterator<? extends HTSerializable>, AbstractIterator<Bag>>()), null);
    }

    /**
     * given a bag iterator ordered by its ID field, create a slab file (keyframe file) that has compressed content
     * using the compression version provided. Once created a compressedpointermap is created from that to be used in
     * lookups.
     *
     * @param inputFile
     * @param slabDir
     * @param compressionVersion
     * @param idField
     * @param splitSize
     * @return
     * @throws IOException
     */
    public static final boolean constructSlabFromBag(BaseFile inputFile, BaseFile slabDir,
                                                     int compressionVersion, String idField,
                                                     int splitSize,
                                                     BaseMapper<BaseFile, AbstractIterator<Bag>> mapper,
                                                     HTProperties props) throws IOException {
        AbstractIterator<Bag> iter = mapper.apply(inputFile);
        slabDir.mkdir();
        BaseFile kfFile = slabDir.getChild(Slab.KFSlab);
        DataOutputStream dos = kfFile.getDataOutputStream();
        Timer t = new Timer();
        t.start();
        BagKeyFrameSink sink = new BagKeyFrameSink(dos, compressionVersion, idField);
        int i = iter.sink(sink);
        long sinkTime = t.stop();

        // we have now created the slab from the input file.  Now we can create the lookup table.
        createCompressedPointerMapFromKeyFrameFile(slabDir, compressionVersion, splitSize, kfFile, sinkTime, props);
        return true;
    }

    private static void createCompressedPointerMapFromKeyFrameFile(final BaseFile slabDir, final int compressionVersion,
                                                                   final int splitSize, final BaseFile kfFile,
                                                                   long sinkTime,
                                                                   HTProperties props) throws IOException {
        CInputStream is = kfFile.getCInputStream();
        CompressedPointerMap cpm = new CompressedPointerMap(slabDir, splitSize);
        cpm.openForWrite();
        KeyFrame kf = new KeyFrame();
        int counter = 0;
        Timer t = new Timer();
        t.start();
        while (kf.read(is)) {
            if (kf.key == 0) {
                continue;
            }
            cpm.add(kf.key, kf.recordPointer);
            counter++;
        }
        cpm.finish();
        if (props == null) {
            props = new HTProperties();
        }
        Env.addMachineInfoToProps(props);
        props.put("slab_write_ms", sinkTime);

        props.put("comp_map_write_ms", t.stop());
        props.put(Slab.Compression, compressionVersion);
        props.put(Slab.Count, counter);
        props.put(Slab.SplitSize, splitSize);
        slabDir.getChild(Slab.PropsFileKey).writeProperties(props);
    }

    /**
     * Given a queue of docs, sort the items and apply them into one file.
     *
     * @param queue
     * @param tmpDir
     * @param lo
     * @param drFilter
     * @param fileExtOut
     * @param bucketSize
     * @param factory
     * @param props
     * @param baseFileIterMapper - maps from base file to an iterator of objects
     * @param objectMapping      - maps from queue format object to desired target (eg, json to bag)
     * @return
     * @throws IOException
     */
    public static BaseFile sortAndMergeQueueSegment(BaseFile queue, BaseFile tmpDir, HTPredicate<BaseFile> lo,
                                                    DateRangeFilterIntf drFilter, String fileExtOut,
                                                    int bucketSize, int mergeFactor, BaseFileAccessingObjectFactory factory,
                                                    HTProperties props,
                                                    BaseMapper baseFileIterMapper,
                                                    Mapper objectMapping,
                                                    HTPredicate filter) throws Exception {
        BaseFile sortDir = tmpDir.getChild("sort");
        sortDir.mkdir();
        if (drFilter == null) {
            drFilter = new NoOpFilter();
        }
        AbstractIterator<BaseFile> dvi = new DirectoryVisitorIterator(queue, lo, drFilter);
        BaseFileBucketWriter bWriter = new BaseFileBucketWriter(bucketSize, sortDir,
                fileExtOut, factory);
        Timer t = new Timer();
        t.start();
        if (filter == null) {
            filter = AlwaysTrueOperator.oper;
        }
        int count = dvi.nest(baseFileIterMapper).map(objectMapping).filter(filter).sink(bWriter);
        props.put("sort_docs_time_ms", t.stop());
        props.put("sort_docs_count", count);
        t.start();
        BaseFile files[] = sortDir.listFiles();
        BaseFileSelectTreeController controller = new BaseFileSelectTreeController(tmpDir, files, 10, factory, false, fileExtOut, true);
        BaseFile bf = controller.merge();
        props.put("merge_docs_time_ms", t.stop());
        return bf;
    }

    public static BaseFile sortAndMergeQueueSegment(AbstractIterator<Bag> iter,
                                                    BaseFile tmpDir, String fileExtOut,
                                                    int bucketSize, int mergeFactor, BaseFileAccessingObjectFactory factory,
                                                    HTProperties props) throws Exception {
        BaseFile sortDir = tmpDir.getChild("sort");
        sortDir.mkdir();

        BaseFileBucketWriter bWriter = new BaseFileBucketWriter(bucketSize, sortDir,
                fileExtOut, factory);
        Timer t = new Timer();
        t.start();

        int count = iter.sink(bWriter);
        props.put("sort_docs_time_ms", t.stop());
        props.put("sort_docs_count", count);
        t.start();
        BaseFile files[] = sortDir.listFiles();
        BaseFileSelectTreeController controller = new BaseFileSelectTreeController(tmpDir, files, mergeFactor, factory, true, fileExtOut, true);
        BaseFile bf = controller.merge();
        props.put("merge_docs_time_ms", t.stop());
        return bf;
    }

    public void t() throws Exception {
        sortAndMergeQueueSegment(FileFileSystem.Root.getFile("/twitterqueue"),
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
