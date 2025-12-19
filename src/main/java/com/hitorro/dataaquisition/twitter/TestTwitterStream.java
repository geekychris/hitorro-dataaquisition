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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.base.BaseBaseFileUtil;
import com.hitorro.base.docprocessing.blockqueue.QInstance;
import com.hitorro.base.filesetmanager.FileQueue;
import com.hitorro.base.filesetmanager.FileSetManager;
import com.hitorro.base.filesetmanager.SinkSet;
import com.hitorro.basedms.transformer.Log;
import com.hitorro.basetext.indexer.collector.consuming.ResultConsumer;
import com.hitorro.basetext.indexer.collector.meta.ExtendedFieldDoc;
import com.hitorro.basetext.text.indexer.DocumentIndexer;
import com.hitorro.dataaquisition.importing.twitter.Twitter2Bag;
import com.hitorro.db.DerbyService;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.bagio.Bag2RowVector;
import com.hitorro.util.bagio.BagUtil;
import com.hitorro.util.bagio.XML2BagWriter;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.fs.sinks.StringSetBaseFileStateSink;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.JsonValueSource;
import com.hitorro.util.core.iterator.LikeRowMerger;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.mappers.ConditionStringMapper;
import com.hitorro.util.core.iterator.mappers.DummyBaseMapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.csv.CSVSink;
import com.hitorro.util.io.largedata.TakeRightRowMerger;
import com.hitorro.util.io.largedata.buckets.BaseFileBucketWriter;
import com.hitorro.util.io.largedata.iterator.BaseFileSelectTreeController;
import com.hitorro.util.io.largedata.iterator.HTSerializableSink;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.keys.StaticVarProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.json.mapper.JSONFieldToStringMapper;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.HTSerializableUtil;
import com.hitorro.util.typesystem.constraint.IsType;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 *
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Test basic streaming routines against some twitter content")
public class TestTwitterStream {


    public static <E> BaseFile[] verifyFiles(final BaseFile baseDir, BaseMapper<BaseFile, AbstractIterator<E>> mapper) throws IOException {
        BaseFile files[] = baseDir.listFiles();

        for (BaseFile bf : files) {
            int counter = 0;
            try {
                AbstractIterator<E> iter = mapper.apply(bf);
                while (iter.hasNext()) {
                    counter++;
                    iter.next();
                }
                iter.close();
            } catch (Exception e) {
                Console.println("%s %s %s %e", counter, bf, e, e);
            }
        }
        return files;
    }

    @After
    public void a() {
        Console.println();
    }

    //@Test
    public void teestLangClassifier() throws IOException {
        FileQueue<JSONElement, Bag> dq = FileSetManager.getQueue("twitterraw");
        AbstractIterator<Bag> iter = dq.getMappedToIterator("bag");
        int i = 0;
        while (iter.hasNext() && i < 1000) {
            Bag b = iter.next();
            String name = b.getValue(b, "cleanusername").toString();
            Console.println("%s %s", b.getValue(b, "lang639"), name);
            i++;
        }

    }

    public void teestPhrases() throws IOException {
        BaseFile root = FileFileSystem.Root.getFile("/hthome/phrases");
        root.mkdir();
        FileQueue<JSONElement, Bag> dq = FileSetManager.getQueue("twitterraw");
        AbstractIterator<String> iter = dq.getMappedToIterator("bag").map(BagUtil.bag2bodystring);
        SinkSet ss = FileSetManager.getSink("text2phrase");
        ObjectNode on = JsonNodeFactory.instance.objectNode();
        on.put("dir", "file://phrases");
        on.put("phraseperbucket", "10000000");

        Sink sink = ss.getSink(on);
        int count = iter.sink(sink);
        /*BaseFile bf = PhraseUtil.writePhrases(iter,
                                              root,
                                              "phrase",
                                              4,
                                              "STANDARD,CASE,PORTERSTEM, NUMBER",
                                              5000000,
                                              10); */
        Console.println();

    }

    @Test
    public void testIndexTwitter() throws IOException {
        File index = new File("/twitter_stuff/index");
        FileUtil.ensureDirectoryExists(index);
        DocumentIndexer<Bag> di = new DocumentIndexer("twitter", index);

        FileQueue<JSONElement, Bag> dq = FileSetManager.getQueue("twitterraw");
        AbstractIterator<Bag> iter = dq.getMappedToIterator("bag");
        iter.sink(di);

    }

    public void teestCSVIterator() throws Exception {
        BaseFile inFile = FileFileSystem.Root.getFile("/ht/platform/data/tableloadexample/TableOne.csv");
        AbstractIterator<JsonValueSource> csvIter = BaseFileUtil.csv_bf2json.apply(inFile);
        while (csvIter.hasNext()) {
            JsonValueSource elem = csvIter.next();
            Console.println("%s", elem);
        }
        csvIter.close();

        BaseFile nan = FileFileSystem.Root.getFile("/ht/platform/data/statemachine/nanny.xls");
        AbstractIterator<JsonValueSource> nanIter = BaseFileUtil.csv_bf2json.apply(nan);
        while (nanIter.hasNext()) {
            JsonValueSource elem = nanIter.next();
            Console.println("%m", elem);
        }
        csvIter.close();
    }

    public void teestDB() throws SQLException {
        Connection conn = DerbyService.getConnection();
        /* This ArrayList usage may cause a warning when compiling this class
         * with a compiler for J2SE 5.0 or newer. We are not using generics
         * because we want the source to support J2SE 1.4.2 environments. */
        ArrayList statements = new ArrayList(); // listFiles of Statements, PreparedStatements
        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;
        Statement s = null;
        ResultSet rs = null;
        try {

            conn.setAutoCommit(false);

            /* Creating a statement object that we can use for running various
             * SQL statements commands against the database.*/
            s = conn.createStatement();
            statements.add(s);

            // We create a table...
            s.execute("create table location(num int, addr varchar(40))");
            System.out.println("Created table location");

            // and put a few rows...

            /* It is recommended to use PreparedStatements when you are
             * repeating execution of an SQL statement. PreparedStatements also
             * allows you to parameterize variables. By using PreparedStatements
             * you may increase performance (because the Derby engine does not
             * have to recompile the SQL statement each time it is executed) and
             * improve security (because of Java type checking).
             */
            // parameter 1 is num (int), parameter 2 is addr (varchar)
            psInsert = conn.prepareStatement(
                    "insert into location values (?, ?)");
            statements.add(psInsert);

            psInsert.setInt(1, 1956);
            psInsert.setString(2, "Webster St.");
            psInsert.executeUpdate();
            //foo(conn);
            System.out.println("Inserted 1956 Webster");

            psInsert.setInt(1, 1910);
            psInsert.setString(2, "Union St.");
            psInsert.executeUpdate();
            System.out.println("Inserted 1910 Union");

            // Let's update some rows as well...

            // parameter 1 and 3 are num (int), parameter 2 is addr (varchar)
            psUpdate = conn.prepareStatement(
                    "update location set num=?, addr=? where num=?");
            statements.add(psUpdate);

            psUpdate.setInt(1, 180);
            psUpdate.setString(2, "Grand Ave.");
            psUpdate.setInt(3, 1956);
            psUpdate.executeUpdate();
            System.out.println("Updated 1956 Webster to 180 Grand");

            psUpdate.setInt(1, 300);
            psUpdate.setString(2, "Lakeshore Ave.");
            psUpdate.setInt(3, 180);
            psUpdate.executeUpdate();
            System.out.println("Updated 180 Grand to 300 Lakeshore");

            /*
              We select the rows and verify the results.
            */
            rs = s.executeQuery(
                    "SELECT num, addr FROM location ORDER BY num");

            /* we expect the first returned column to be an integer (num),
             * and second to be a String (addr). Rows are sorted by street
             * number (num).
             *
             * Normally, it is best to use a pattern of
             *  while(rs.next()) {
             *    // do something with the result set
             *  }
             * to process all returned rows, but we are only expecting two rows
             * this time, and want the verification code to be easy to
             * comprehend, so we use a different pattern.
             */

            int number; // street number retreived from the database
            boolean failure = false;
            if (!rs.next()) {
                failure = true;
                Console.println("No rows in ResultSet");
            }

            if ((number = rs.getInt(1)) != 300) {
                failure = true;
                Console.println(
                        "Wrong row returned, expected num=300, got " + number);
            }

            if (!rs.next()) {
                failure = true;
                Console.println("Too few rows");
            }

            if ((number = rs.getInt(1)) != 1910) {
                failure = true;
                Console.println(
                        "Wrong row returned, expected num=1910, got " + number);
            }

            if (rs.next()) {
                failure = true;
                Console.println("Too many rows");
            }

            if (!failure) {
                Console.println("Verified the rows");
            }
            if (1 == 1) {
                return;
            }
            // delete the table
            s.execute("drop table location");
            Console.println("Dropped table location");

            /*
               We commit the transaction. Any changes will be persisted to
               the database now.
             */
            conn.commit();
            Console.println("Committed the transaction");

            /*
             * In embedded mode, an application should shut down the database.
             * If the application fails to shut down the database,
             * Derby will not perform a checkpoint when the JVM shuts down.
             * This means that it will take longer to boot (connect to) the
             * database the next time, because Derby needs to perform a recovery
             * operation.
             *
             * It is also possible to shut down the Derby system/engine, which
             * automatically shuts down all booted databases.
             *
             * Explicitly shutting down the database or the Derby engine with
             * the connection URL is preferred. This style of shutdown will
             * always throw an SQLException.
             *
             * Not shutting down when in a client environment, see method
             * Javadoc.
             */

        } catch (SQLException sqle) {
            Log.db.error(sqle);
        } finally {
            // release all open resources to avoid unnecessary memory usage

            // ResultSet
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException sqle) {
                Log.db.error(sqle);
            }

            // Statements and PreparedStatements
            int i = 0;
            while (!statements.isEmpty()) {
                // PreparedStatement extend Statement
                Statement st = (Statement) statements.remove(i);
                try {
                    if (st != null) {
                        st.close();
                        st = null;
                    }
                } catch (SQLException sqle) {
                    Log.db.error(sqle);
                }
            }

            //Connection
            try {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            } catch (SQLException sqle) {
                Log.db.error(sqle);
            }
        }
    }

    /* public void teestReadAndCountJSONFields () throws IOException
  {
      HTPredicate<BaseFile> lo = new LogicalOrOperator(new IsDir(), new LogicalOrOperator(new FileEndsWith("ser", true), new FileEndsWith("ser.gz", true)));
      AbstractIterator<BaseFile> dvi = new DirectoryVisitorIterator(FileFileSystem.Root.getFile("/twitterqueue"), lo);

      Iterator<JsonValueSource> iter = dvi.nest(BaseFileUtil.bf2json);
      NodeCounter nc = new NodeCounter();
      while (iter.hasNext())
      {
          JSONElement elem = iter.next();
          nc.visit(elem, 0);
      }
      List<GenericKeyValue<String, Integer>> list = nc.getDumpOfCounts();

      ColumnComparator comp = new ColumnComparator(new IntComp(), 1);
      Collections.sort(list, comp);
      for (GenericKeyValue<String, Integer> e : list)
      {
          Console.println("%s %s", e.getKey(), e.apply());
      }

  }  */

    public void teestQeueue() throws IOException {

        FileQueue<JSONElement, Bag> dq = FileSetManager.getQueue("twitterraw");


        FileQueue<JSONElement, Bag> fb = FileSetManager.getQueue("facebookraw");
        QInstance qi = fb.getQueueInstance();
        // queue writer for facebook queue

        AbstractIterator<Bag> iter = dq.getMappedToIterator("bag");
        while (iter.hasNext()) {
            Bag elem = iter.next();
            Console.println("%s", elem);
        }
        Console.println();
    }

    public void teestReadFromQueue2UniqueNames() throws IOException, PropaccessError {
        StaticVarProperty<Mapper> svp = new StaticVarProperty("mapper", "", false, BaseFileUtil.bf2json, Mapper.class);
        JVS jvs = new JVS();


        jvs.set("mapper", "ht.util.file.fs.BaseFileUtil#bf2json");
        Mapper m = svp.apply(jvs);
        HTPredicate<BaseFile> lo = new LogicalOrOperator(new IsDir(), new LogicalOrOperator(new FileEndsWith("ser", true), new FileEndsWith("ser.gz", true)));
        BaseFile outFile = FileFileSystem.Root.getFile("/user_names.txt");

        JSONFieldToStringMapper jsonSingleFieldMapp = new JSONFieldToStringMapper("user.name");
        BaseMapper comb = jsonSingleFieldMapp.combine(new ConditionStringMapper(true, true, ConditionStringMapper.Keep.Alpha));
        StringSetBaseFileStateSink sink = new StringSetBaseFileStateSink(outFile, true);
        new DirectoryVisitorIterator(FileFileSystem.Root.getFile("/twitterqueue"), lo).nest(BaseFileUtil.bf2json).map(comb).sink(sink);
    }

    /**
     * Read in json representation from a queue of tweets and write out a unique ordered profile url to disk.
     *
     * @throws IOException
     */
    public void teestReadFromQueue2UniqueUrls() throws IOException, PropaccessError {
        StaticVarProperty<Mapper> svp = new StaticVarProperty("mapper", "", false, BaseFileUtil.bf2json, Mapper.class);
        JVS jvs = new JVS();
        jvs.set("mapper", "ht.util.file.fs.BaseFileUtil#bf2json");
        Mapper m = svp.apply(jvs);
        HTPredicate<BaseFile> lo = new LogicalOrOperator(new IsDir(), new LogicalOrOperator(new FileEndsWith("ser", true), new FileEndsWith("ser.gz", true)));
        BaseFile outFile = FileFileSystem.Root.getFile("/urls.txt");

        JSONFieldToStringMapper jsonSingleFieldMapp = new JSONFieldToStringMapper("user.profile_image_url");
        StringSetBaseFileStateSink sink = new StringSetBaseFileStateSink(outFile, true);
        new DirectoryVisitorIterator(FileFileSystem.Root.getFile("/twitterqueue"), lo).nest(BaseFileUtil.bf2json).map(jsonSingleFieldMapp).sink(sink);
    }

    /**
     * example where we take a queue of json encoded files representing tweets.  We apply them to a bag filter out the
     * deletes, apply the bag to a vector for output to a csvwriter.
     *
     * @throws IOException
     */
    public void teestReadFromQueue2CSV() throws IOException {
        HTPredicate<BaseFile> lo = new LogicalOrOperator(new IsDir(), new LogicalOrOperator(new FileEndsWith("ser", true), new FileEndsWith("ser.gz", true)));
        AbstractIterator<BaseFile> dvi = new DirectoryVisitorIterator(FileFileSystem.Root.getFile("/twitterqueue"), lo);

        BaseFile outFile = FileFileSystem.Root.getFile("/foo.csv");

        String src[] = {"username", "location", "lang"};
        Bag2RowVector vectorMap = new Bag2RowVector(src, src);
        CSVSink sink = new CSVSink(src, BaseFileUtil.bf2outputstream.apply(outFile), ',');
        int count = dvi.nest(BaseFileUtil.bf2json).map(Twitter2Bag.twitter2bag).filter(new IsType("twitter")).map(vectorMap).sink(sink);
        Console.println("Ordered %s", count);
    }

    /**
     * We take a queue of json encoded tweets, convert them to Bags, bucket them up in to 200k buckets, sort by tweetid
     * and output them in HTserialized format.
     *
     * @throws IOException
     */
    public void teestReadFromQueue() throws IOException {
        HTPredicate<BaseFile> lo = new LogicalOrOperator(new IsDir(), new LogicalOrOperator(new FileEndsWith("ser", true), new FileEndsWith("ser.gz", true)));
        AbstractIterator<BaseFile> dvi = new DirectoryVisitorIterator(FileFileSystem.Root.getFile("/twitterqueue"), lo);

        BaseFile baseDir = FileFileSystem.Root.getFileEnsuringDir("/twitterdays");

        //,

        BaseFileBucketWriter bWriter = new BaseFileBucketWriter(200000, baseDir, "htser.gz", TwitterUtil.twitterXMLBagFactory);

        int count = dvi.nest(BaseFileUtil.bf2json).map(Twitter2Bag.twitter2bag).filter(new IsType("twitter")).sink(bWriter);
        Console.println("Ordered %s", count);

    }

    public void teestReadJSONWriteHTReadHT() throws IOException {
        File input = EnhancedTestCase.getOutputFileRelativeForClass("twitter.ser", this.getClass(), true);
        File outS = EnhancedTestCase.getOutputFileRelativeForClass("twitter.htser", this.getClass(), true);
        outS.delete();
        HTSerializableSink htSink = null;
        try {
            htSink = new HTSerializableSink(FileUtil.fsOutputStream.apply(outS));
        } catch (Exception e) {
            Console.println("%s %e", e, e);
        }
        int counter = FileUtil.fsJsonIter.apply(input).map(Twitter2Bag.twitter2bag).sink(htSink);
        AbstractIterator<? extends HTSerializable> htIter = HTSerializableUtil.fs2htser.apply(outS);
        int c = 0;

        while (htIter.hasNext()) {
            c++;

            Bag b = (Bag) htIter.next();
            Console.println();
        }
    }

    public void teestReadAndWrite() throws IOException {
        File input = EnhancedTestCase.getOutputFileRelativeForClass("twitter.ser", this.getClass(), true);
        File out = EnhancedTestCase.getOutputFileRelativeForClass("twitter.xml", this.getClass(), true);
        out.delete();
        out.delete();
        // get me a bagxml writer Sink object, using a writer we created from a file.
        XML2BagWriter writer = new XML2BagWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
        /// magic line that reads from file, converting json based twitter into a twitter type Bag object and then writes it back out as
        // xml representation of a bag
        int counter = FileUtil.fsJsonIter.apply(input).map(Twitter2Bag.twitter2bag).sink(writer, 145, 10);

        Iterator<Bag> iter = BagUtil.fs2xmlbagiter.apply(out);
        int c = 0;
        try {
            while (iter.hasNext()) {
                c++;
                iter.next();
            }
        } catch (Exception e) {
            Console.println("%s %s %e", c, e, e);
        }

        FileFileSystem ffs = new FileFileSystem(new File("/"));
        BaseFile baseDir = ffs.getFileEnsuringDir("/basebuckettest");
        Comparator<Bag> comp = TwitterUtil.TweetIdComparator.comp;
        LikeRowMerger<Bag> merger = TakeRightRowMerger.me;
        BaseFileBucketWriter bWriter = new BaseFileBucketWriter(1000, baseDir, "xml", TwitterUtil.twitterXMLBagFactory);

        EnhancedTestCase.assertEquals(2980, counter);
    }

    public void teestSelectTree() throws Exception {

        FileFileSystem ffs = new FileFileSystem(new File("/"));
        BaseFile baseDir = ffs.getFileEnsuringDir("/basebuckettest");
        BaseFile mergeDir = ffs.getFileEnsuringDir("/basemerge");
        mergeDir.mkdir();
        BaseFile[] files = verifyFiles(baseDir, BagUtil.bf2xmlbagiter);
        BaseFileSelectTreeController controller = new BaseFileSelectTreeController(mergeDir, files, 5, TwitterUtil.twitterXMLBagFactory, false, "json", true);
        BaseFile merged = controller.merge();
        Console.println("%s ", merged);
    }

    /**
     * Read in a json file, sort it by status id and spew them out!
     *
     * @throws IOException
     */
    public void teestHTSerializableSelectTree() throws Exception {
        File input = EnhancedTestCase.getOutputFileRelativeForClass("twitter.ser", this.getClass(), true);
        File outS = EnhancedTestCase.getOutputFileRelativeForClass("twitter.htser", this.getClass(), true);
        outS.delete();

        IsType<Bag> isType = new IsType("twitter");

        FileFileSystem ffs = new FileFileSystem(new File("/"));
        BaseFile baseDir = ffs.getFileEnsuringDir("/basebuckettest");
        BaseFile mergeDir = ffs.getFileEnsuringDir("/basemerge");

        BaseFileBucketWriter bWriter = new BaseFileBucketWriter(1000, baseDir, "htser", TwitterUtil.twitterXMLBagFactory);

        int cnt = FileUtil.fsJsonIter.apply(input).map(Twitter2Bag.twitter2bag).filter(isType).sink(bWriter);
        BaseFile files[] = baseDir.listFiles();

        verifyFiles(baseDir, BaseBaseFileUtil.bf2htser.combine(new DummyBaseMapper<AbstractIterator<? extends HTSerializable>, AbstractIterator<Bag>>()));


        mergeDir.mkdir();
        BaseFileSelectTreeController controller = new BaseFileSelectTreeController(mergeDir, files, 5, TwitterUtil.twitterHTSerBagFactory, false, "htser", true);
        BaseFile merged = controller.merge();
        Console.println("%s ", merged);
    }

}


class IntComp implements Comparator<Integer> {

    @Override
    public int compare(final Integer integer, final Integer integer1) {
        return integer - integer1;
    }
}


/**
 * Non re-entrant me (reuses a stringbuilder)
 */

class TestResultConsumer implements ResultConsumer {

    @Override
    public void consume(final ExtendedFieldDoc[] docs, final int max) {
        for (int i = 0; i < max; i++) {
            Console.println("%s", docs[i]);
        }
    }
}


