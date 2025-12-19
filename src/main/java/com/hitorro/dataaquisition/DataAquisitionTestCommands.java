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
package com.hitorro.dataaquisition;


import com.hitorro.dataaquisition.twitter.TwitterUtil;
import gnu.trove.map.hash.TObjectLongHashMap;
import com.hitorro.base.BaseBaseFileUtil;
import com.hitorro.base.filesetmanager.FileQueue;
import com.hitorro.base.filesetmanager.FileSetManager;
import com.hitorro.base.filesetmanager.SinkSet;
import com.hitorro.dataaquisition.facebook.Fql;
import com.hitorro.dataaquisition.importing.twitter.Twitter2Bag;
import com.hitorro.dataaquisition.importing.wikipedia.Wiki2Bag;
import com.hitorro.dataaquisition.wiki.WikiGathererListener;
import com.hitorro.dataaquisition.wiki.WikiGatheringPrinter;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.GenericAnalyzer;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.events.cache.PoolContainer;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.opers.LogicalNotOperator;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.ResetableStringReader;
import com.hitorro.util.io.keyframes.SlabUtil;
import com.hitorro.util.io.largedata.iterator.HTSerializableSink;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.mapper.JSONIterIterMapper;
import com.hitorro.util.servicecounters.CounterSet;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.constraint.BagContainsField;
import com.hitorro.util.typesystem.constraint.IsType;
import com.hitorro.util.xml.XE;
import org.apache.http.HttpException;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wikimodel.wem.WikiParameters;
import org.wikimodel.wem.mediawiki.MediaWikiParser;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DataAquisitionTestCommands {

    @CommandDef(command = "wiki.read", description = "read wiki file")
    public static final String readWiki(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                        @DebugArgAno(keyName = "args",
                                                description = "args",
                                                defaultValue = "",
                                                argType = ArgType.Args) JVS args,
                                        @DebugArgAno(propType = StringProperty.class, keyName = "inputfile",
                                                description = "source file", defaultValue = "a") String inputfile,
                                        @DebugArgAno(propType = StringProperty.class, keyName = "outfile",
                                                description = "source file", defaultValue = "a") String outfile) throws IOException {
        Timer t = new Timer();
        Console.println("Started re-write");
        t.reset();
        SinkSet ss = FileSetManager.getSink("ht");
        BaseFile wk = FileFileSystem.Root.getFile(inputfile);
        AbstractIterator<XE> iter = BaseFileUtil.bf2Xwikipdeai_xedom.apply(wk);
        AbstractIterator<Bag> bagIter = iter.map(Wiki2Bag.wikixe2bagmapper).filter(new LogicalNotOperator(new BagContainsField("redirect")));
        CounterSet cs = new CounterSet("bftester");
        bagIter = bagIter.time(cs);
        int records = 0;

        HTSerializableSink sink = new HTSerializableSink();


        sink.init(args.getJsonNode());
        records = bagIter.sink(sink);

        return Fmt.S("took: %s for %s records", t.getTime() / 1000, records);
    }

    @CommandDef(command = "wiki.readoutcats", description = "read wiki file and output categories")
    public static final String readWikiOutputCategories(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                                        @DebugArgAno(keyName = "args",
                                                                description = "args",
                                                                defaultValue = "",
                                                                argType = ArgType.Args) JVS args,
                                                        @DebugArgAno(propType = BasefileProperty.class, keyName = "inputfile",
                                                                description = "source file", defaultValue = "a") BaseFile inputfile,
                                                        @DebugArgAno(propType = BasefileProperty.class, keyName = "outfile",
                                                                description = "source file", defaultValue = "a") BaseFile outfile) {
        Timer t = new Timer();
        Console.println("Started re-write");
        t.reset();
        SinkSet ss = FileSetManager.getSink("ht");
        AbstractIterator<XE> iter = BaseFileUtil.bf2Xwikipdeai_xedom.apply(inputfile);
        AbstractIterator<Bag> bagIter = iter.map(Wiki2Bag.wikixe2bagmapper).filter(new LogicalNotOperator(new BagContainsField("redirect")));
        CounterSet cs = new CounterSet("bftester");
        bagIter = bagIter.time(cs);
        int records = 0;

        MediaWikiParser parser = new MediaWikiParser();
        GatherCats f = new GatherCats();
        //WikiGatheringPrinter f = new WikiGatheringPrinter();
        // WikiGatherer wg = new WikiGatherer(f);
        WikiGathererListener pil = new WikiGathererListener(f);

        Set<String> include = new HashSet();
        Map<String, PrintWriter> pwMap = new HashMap();
        com.hitorro.language.Iso639Table.getInstance().addLangKeysToSet(include);
        include.add("Category");
        include.add("wikt");
        outfile.mkdir();
        int errors = 0;
        try {
            while (bagIter.hasNext()) {
                Bag bag = bagIter.next();
                try {
                    String body = bag.getValueAsString("body");
                    String title = bag.getValueAsString("title");
                    StringReader sr = new StringReader(body);
                    f.cats.clear();
                    parser.parse(sr, pil);
                    for (String cat : f.cats) {
                        String parts[] = StringUtil.splitByToken(cat, ':');
                        PrintWriter pwr = getPrintWriter(parts[0], outfile, pwMap, include);
                        if (pwr == null) {
                            continue;
                        }
                        pwr.print(parts[1]);
                        pwr.print("||");
                        pwr.print(title);
                        pwr.println();
                    }
                    records++;
                    if (records % 10000 == 0) {
                        Log.util.info(">>processed %s", records);
                    }
                } catch (Exception e) {
                    Log.util.error("Received error count=%s, %s %e", ++errors, e, e);
                }

            }
        } catch (Exception e) {
            Log.util.error("%s %e", e, e);
        } finally {

        }
        closePrintWriters(pwMap);
        return Fmt.S("took: %s for %s records", t.getTime() / 1000, records);
    }


    private static PrintWriter getPrintWriter(String prefix, BaseFile path, Map<String, PrintWriter> map, Set<String> validPrefix) {
        PrintWriter pw = map.get(prefix);
        if (pw != null) {
            return pw;
        }
        if (validPrefix.contains(prefix)) {
            pw = BaseFileUtil.bf2utf8printwriter.apply(path.getChild("%s.txt", prefix));
            map.put(prefix, pw);
            return pw;
        }
        return null;

    }

    private static void closePrintWriters(Map<String, PrintWriter> map) {
        for (PrintWriter pw : map.values()) {
            pw.flush();
            pw.close();
        }
    }

    @CommandDef(command = "wiki.readqueue", description = "read wiki file")
    public static final String readQueue(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                         @DebugArgAno(keyName = "args",
                                                 description = "args",
                                                 defaultValue = "",
                                                 argType = ArgType.Args) JVS args,
                                         @DebugArgAno(propType = StringProperty.class, keyName = "queue",
                                                 description = "source file", defaultValue = "a") String queue) throws Exception {
        FileQueue<JSONElement, Bag> dq = FileSetManager.getQueue(queue);
        AbstractIterator<Bag> iter = dq.getMappedToIterator("bag");


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

        while (iter.hasNext()) {
            Bag b = iter.next();
            Console.println("%s", b.getValueAsString("body"));
        }
        return "";
    }

    @CommandDef(command = "wiki.readht", description = "read wiki file")
    public static final String readHTWiki(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                          @DebugArgAno(keyName = "args",
                                                  description = "args",
                                                  defaultValue = "",
                                                  argType = ArgType.Args) JVS args,
                                          @DebugArgAno(propType = BasefileProperty.class, keyName = "inputfile",
                                                  description = "source file", defaultValue = "a") BaseFile inputfile) {
        Timer t = new Timer();
        Console.println("Started re-write");
        com.hitorro.language.IsoLanguage eng = com.hitorro.language.Iso639Table.english;
        if (!inputfile.exists()) {
            return "file doesnt exist";
        }
        AbstractIterator<Bag> bagIter = (AbstractIterator<Bag>) BaseBaseFileUtil.bf2htser.apply(inputfile);
        CounterSet cs = new CounterSet("bftester");
        bagIter = bagIter.time(cs);
        int records = 0;
        TObjectLongHashMap<String> names = new TObjectLongHashMap();

        MediaWikiParser parser = new MediaWikiParser();
        WikiGatheringPrinter f = new WikiGatheringPrinter();
        // WikiGatherer wg = new WikiGatherer(f);
        WikiGathererListener pil = new WikiGathererListener(f);
        FindNames fn = new FindNames(eng, names);
        try {
            while (bagIter.hasNext()) {
                Bag bag = bagIter.next();
                String body = bag.getValueAsString("body");
                String title = bag.getValueAsString("title");
                StringReader sr = new StringReader(body);
                f.clear();
                parser.parse(sr, pil);
                String txt = f.getText();
                Console.println(txt);
                getNames(txt, eng, fn);
                records++;
                if (records % 100 == 0) {
                    Log.util.info(">>another 100");
                }

            }
        } catch (Exception e) {
            Log.util.error("%s %e", e, e);
        } finally {

        }
        Console.println("Timer : seconds %s", t.getTime() / 1000);

        return Fmt.S("took: %s for %s records", t.getTime() / 1000, records);
    }

    @CommandDef(command = "json.sortqueue", description = "read wiki file")
    public static final String sortQueue(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                         @DebugArgAno(keyName = "args",
                                                 description = "args",
                                                 defaultValue = "",
                                                 argType = ArgType.Args) JVS args,
                                         @DebugArgAno(propType = StringProperty.class, keyName = "queue",
                                                 description = "source file", defaultValue = "a") String queue) throws Exception {
        FileQueue<JSONElement, Bag> dq = FileSetManager.getQueue(queue);
        AbstractIterator<Bag> iter = dq.getMappedToIterator("bag");
        /*
       AbstractIterator<Bag> iter,
                                                    BaseFile tmpDir, String fileExtOut,
                                                    int bucketSize, int mergeFactor, MapperBFAccessingObjectFactory factory,
                                                    HTProperties props
        */
        BaseFile tmpDir = FileFileSystem.Root.getFile("/Users/chris/tmpsorter");
        tmpDir.mkdir();
        HTProperties props = new HTProperties();
        BaseFile generatedFile = SlabUtil.sortAndMergeQueueSegment(iter, tmpDir, "ser.gz", 100000, 10, TwitterUtil.twitterHTSerBagFactory, props);
        return generatedFile.toString();
    }

    @CommandDef(command = "fql.status", description = "status")
    public static final String getStatus(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                         @DebugArgAno(keyName = "args",
                                                 description = "args",
                                                 defaultValue = "",
                                                 argType = ArgType.Args) JVS args,
                                         @DebugArgAno(propType = StringProperty.class, keyName = "userid",
                                                 description = "userid", defaultValue = "a") String userid) throws IOException, HttpException {
        Fql fql = new Fql();
        fql.getClientBuilder().setConnectionTimeToLive(40000, TimeUnit.MILLISECONDS);
        //String fanPageId = "73340321070";
        String fanPageId = "34936988896";
        // Stream the query results into a buffer so we can not only parse through it, we can write it to disk if we like it.
        int count = fql.select(Fql.StatusFields, "status").where("uid", "=", userid).limit(50).formatJSON().getAsArrayAndBuffer();
        Iterator<JSONElement> jsi = FileUtil.isJsonIter.apply(fql.getStreamFromBuffer()).nest(JSONIterIterMapper.jsonIterIterMapper);
        while (jsi.hasNext()) {
            JSONElement elem = jsi.next();
            Console.println(elem.toString());
        }
        return "a";
    }

    @CommandDef(command = "edit.distance", description = "status")
    public static final int editDistanceTest(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                             @DebugArgAno(keyName = "args",
                                                     description = "args",
                                                     defaultValue = "",
                                                     argType = ArgType.Args) JVS args,
                                             @DebugArgAno(propType = StringProperty.class, keyName = "a",
                                                     description = "a", defaultValue = "a") String a,
                                             @DebugArgAno(propType = StringProperty.class, keyName = "b",
                                                     description = "b", defaultValue = "b") String b) {
        EditDistance ed = new EditDistance();


        int i = ed.getDistance(a, b);
        Console.println(ed.getArrayAsString());
        return i;

    }

    @CommandDef(command = "tokenize", description = "status")
    public static final int stemText(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session,
                                     @DebugArgAno(keyName = "args",
                                             description = "args",
                                             defaultValue = "",
                                             argType = ArgType.Args) JVS args,
                                     @DebugArgAno(propType = BasefileProperty.class, keyName = "inputfile",
                                             description = "source file", defaultValue = "a") BaseFile inputfile) throws IOException {
        String text = inputfile.readString();
        GenericAnalyzer analyzer = new GenericAnalyzer("STANDARD,PORTERSTEM", com.hitorro.language.Iso639Table.english, GenericAnalyzer.Mode.Index);
        ResetableStringReader m_reader = new ResetableStringReader(null);
        m_reader.set(text);
        TokenStream ts = analyzer.tokenStream("", m_reader);
        CharTermAttribute termAttribute = ts.getAttribute(CharTermAttribute.class);
        StringBuilder sb = new StringBuilder();
        while (ts.incrementToken()) {
            sb.append(termAttribute.toString());
            sb.append(" ");
        }
        ts.close();
        Console.println(sb.toString());
        return 0;

    }


    public static final String createSlabFromBag() {
        /*SlabUtil.constructSlabFromBag (BaseFile inputFile, BaseFile slabDir,
                                                      int compressionVersion, String idField,
                                                      int splitSize,
                                                      BaseMapper<BaseFile, AbstractIterator<Bag>> me,
                                                      HTProperties props) throws IOException */

        return "";
    }


    static private void getNames(String content, com.hitorro.language.IsoLanguage lang, FindNames names) {
        PoolContainer<com.hitorro.language.IsoLanguage, com.hitorro.language.SentenceSegmenter> pool = com.hitorro.language.SentenceDetectorSingleton.singleton.get(lang);
        com.hitorro.language.SentenceSegmenter ss = null;
        try {
            ss = pool.get();
            com.hitorro.language.Sentences s = ss.getSentenceOffsets(content);

            s.visitSentences(names);
        } finally {
            pool.returnIt(ss);
        }
    }
}


class FindNames extends com.hitorro.language.SimpleSentenceVisitor {
    PoolContainer<com.hitorro.language.IsoLanguage, com.hitorro.language.PartOfSpeech> posPool;
    com.hitorro.language.PartOfSpeech pos = null;
    Timer t = new Timer();
    int counter = 0;
    private com.hitorro.language.IsoLanguage lang;
    private TObjectLongHashMap<String> names;

    public FindNames(com.hitorro.language.IsoLanguage lang, TObjectLongHashMap<String> names) {
        this.lang = lang;
        this.names = names;
        posPool = com.hitorro.language.PartOfSpeechSingletonMapper.singleton.get(lang);
        pos = posPool.get();
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean start() {
        counter = 0;
        t.reset();
        return true;
    }

    @Override
    public boolean visitSimple(final String string, final int sentenceNumber) {
        pos.reset();
        com.hitorro.language.POS p = pos.getPOS(string);
        p.getTokenizedText();
        com.hitorro.language.NameFinder nf = p.getNameFinder(com.hitorro.language.IsoLanguage.NameFinderIntent.Person);
        nf.getNames(names);
        counter++;
        return true;
    }

    @Override
    public boolean end() {

        return true;
    }

    public boolean finishUse() {
        posPool.returnIt(pos);
        return true;
    }
}

class GatherCats extends WikiGatheringPrinter {
    public String prefix = "Category:";
    List<String> cats = new ArrayList();

    public void reference(String ref,
                          String label,
                          WikiParameters params) {
        if (ref.indexOf(":") != -1 && !ref.startsWith("http")) {
            cats.add(ref);
        }
    }

    @Override
    public void print(final String str) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(final String str) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

class EditDistance {
    int arrSize = 1;
    private int[][] counts = new int[arrSize][arrSize];

    /**
     * D(i,0) = i D(0,j) = j recurrence relationship for each i = 1..m for each j = 1..n
     * <p/>
     * D(i,j) = min ( D(i-1,j) + 1 D(i,j-1) + 1 D(i-1, j-1) + 2; if X(i) != Y(j) 0; if X(i) == Y(j)
     *
     * @param a
     * @param b
     * @return
     */
    private int aI;
    private int bI;

    public int getDistance(String a, String b) {

        init(a, b);
        for (int i = 1; i <= aI; i++) {
            for (int j = 1; j <= bI; j++) {
                // substitution
                int subst;
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    subst = counts[i - 1][j - 1];
                } else {
                    subst = counts[i - 1][j - 1] + 2;
                }
                int insertion = counts[i][j - 1] + 1;

                int deletion = counts[i - 1][j] + 1;
                counts[i][j] = Math.min(subst, Math.min(insertion, deletion));
            }
        }

        return counts[aI][bI];
    }

    private void init(final String a, final String b) {
        aI = a.length();
        bI = b.length();
        int max = Math.max(aI, bI) + 1;
        if (max > arrSize) {
            arrSize = max;
            counts = new int[arrSize][arrSize];
            for (int i = 0; i < arrSize; i++) {
                counts[i][0] = i;
            }
            for (int j = 0; j < arrSize; j++) {
                counts[0][j] = j;
            }
        }
    }

    public String getArrayAsString() {
        return ArrayUtil.printIntArray(counts, 3, true);
    }
}

/*

   Porter stemmer in Java. The original paper is in

       Porter, 1980, An algorithm for suffix stripping, Program, Vol. 14,
       no. 3, pp 130-137,

   See also http://www.tartarus.org/~martin/PorterStemmer

   History:

   Release 1

   Bug 1 (reported by Gonzalo Parra 16/10/99) fixed as marked below.
   The words 'aed', 'eed', 'oed' leave k at 'a' for step 3, and b[k-1]
   is then out outside the bounds of b.

   Release 2

   Similarly,

   Bug 2 (reported by Steve Dyrdahl 22/2/00) fixed as marked below.
   'ion' by itself leaves j = -1 in the test for 'ion' in step 5, and
   b[j] is then outside the bounds of b.

   Release 3

   Considerably revised 4/9/00 in the light of many helpful suggestions
   from Brian Goetz of Quiotix Corporation (brian@quiotix.com).

   Release 4

*/


/**
 * Stemmer, implementing the Porter Stemming Algorithm
 * <p/>
 * The Stemmer class transforms a word into its root form.  The input word can be provided a character at time (by
 * calling put()), or at once by calling one of the various stem(something) methods.
 */

class Stemmer {
    private static final int INC = 50;
    private char[] b;
    private int i,     /* offset into b */
            i_end, /* offset to end of stemmed word */
            j, k;

    /* unit of size whereby b is increased */
    public Stemmer() {
        b = new char[INC];
        i = 0;
        i_end = 0;
    }

    /**
     * Test program for demonstrating the Stemmer.  It reads text from a a list of files, stems each word, and writes
     * the result to standard output. Note that the word stemmed is expected to be in lower case: forcing lower case
     * must be done outside the Stemmer class. Usage: Stemmer file-name file-name ...
     */
    public static void main(String[] args) {
        char[] w = new char[501];
        Stemmer s = new Stemmer();
        for (int i = 0; i < args.length; i++) {
            try {
                FileInputStream in = new FileInputStream(args[i]);

                try {
                    while (true) {
                        int ch = in.read();
                        if (Character.isLetter((char) ch)) {
                            int j = 0;
                            while (true) {
                                ch = Character.toLowerCase((char) ch);
                                w[j] = (char) ch;
                                if (j < 500) {
                                    j++;
                                }
                                ch = in.read();
                                if (!Character.isLetter((char) ch)) {
                                    /* to test put(char ch) */
                                    for (int c = 0; c < j; c++) {
                                        s.add(w[c]);
                                    }

                                    /* or, to test put(char[] w, int j) */
                                    /* s.put(w, j); */

                                    s.stem();
                                    {
                                        String u;

                                        /* and now, to test toString() : */
                                        u = s.toString();

                                        /* to test getResultBuffer(), getResultLength() : */
                                        /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */

                                        System.out.print(u);
                                    }
                                    break;
                                }
                            }
                        }
                        if (ch < 0) {
                            break;
                        }
                        System.out.print((char) ch);
                    }
                } catch (IOException e) {
                    System.out.println("error reading " + args[i]);
                    break;
                }
            } catch (FileNotFoundException e) {
                System.out.println("file " + args[i] + " not found");
                break;
            }
        }
    }

    /**
     * Add a character to the word being stemmed.  When you are finished adding characters, you can call stem(void) to
     * stem the word.
     */

    public void add(char ch) {
        if (i == b.length) {
            char[] new_b = new char[i + INC];
            for (int c = 0; c < i; c++) {
                new_b[c] = b[c];
            }
            b = new_b;
        }
        b[i++] = ch;
    }

    /**
     * Adds wLen characters to the word being stemmed contained in a portion of a char[] array. This is like repeated
     * calls of put(char ch), but faster.
     */

    public void add(char[] w, int wLen) {
        if (i + wLen >= b.length) {
            char[] new_b = new char[i + wLen + INC];
            for (int c = 0; c < i; c++) {
                new_b[c] = b[c];
            }
            b = new_b;
        }
        for (int c = 0; c < wLen; c++) {
            b[i++] = w[c];
        }
    }

    /**
     * After a word has been stemmed, it can be retrieved by toString(), or a reference to the internal buffer can be
     * retrieved by getResultBuffer and getResultLength (which is generally more efficient.)
     */
    public String toString() {
        return new String(b, 0, i_end);
    }

    /**
     * Returns the length of the word resulting from the stemming process.
     */
    public int getResultLength() {
        return i_end;
    }

    /* cons(i) is true <=> b[i] is a consonant. */

    /**
     * Returns a reference to a character buffer containing the results of the stemming process.  You also need to
     * consult getResultLength() to determine the length of the result.
     */
    public char[] getResultBuffer() {
        return b;
    }

    /* m() measures the number of consonant sequences between 0 and j. if c is
       a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
       presence,

          <c><v>       gives 0
          <c>vc<v>     gives 1
          <c>vcvc<v>   gives 2
          <c>vcvcvc<v> gives 3
          ....
    */

    private final boolean cons(int i) {
        switch (b[i]) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return false;
            case 'y':
                return (i == 0) || !cons(i - 1);
            default:
                return true;
        }
    }

    /* vowelinstem() is true <=> 0,...j contains a vowel */

    private final int m() {
        int n = 0;
        int i = 0;
        while (true) {
            if (i > j) {
                return n;
            }
            if (!cons(i)) {
                break;
            }
            i++;
        }
        i++;
        while (true) {
            while (true) {
                if (i > j) {
                    return n;
                }
                if (cons(i)) {
                    break;
                }
                i++;
            }
            i++;
            n++;
            while (true) {
                if (i > j) {
                    return n;
                }
                if (!cons(i)) {
                    break;
                }
                i++;
            }
            i++;
        }
    }

    /* doublec(j) is true <=> j,(j-1) contain a double consonant. */

    private final boolean vowelinstem() {
        int i;
        for (i = 0; i <= j; i++) {
            if (!cons(i)) {
                return true;
            }
        }
        return false;
    }

    /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
       and also if the second c is not w,x or y. this is used when trying to
       restore an e at the end of a short word. e.g.

          cav(e), lov(e), hop(e), crim(e), but
          snow, box, tray.

    */

    private final boolean doublec(int j) {
        if (j < 1) {
            return false;
        }
        if (b[j] != b[j - 1]) {
            return false;
        }
        return cons(j);
    }

    private final boolean cvc(int i) {
        if (i < 2 || !cons(i) || cons(i - 1) || !cons(i - 2)) {
            return false;
        }
        {
            int ch = b[i];
            return ch != 'w' && ch != 'x' && ch != 'y';
        }
    }

    /* setto(s) sets (j+1),...k to the characters in the string s, readjusting
   k. */

    private final boolean ends(String s) {
        int l = s.length();
        int o = k - l + 1;
        if (o < 0) {
            return false;
        }
        for (int i = 0; i < l; i++) {
            if (b[o + i] != s.charAt(i)) {
                return false;
            }
        }
        j = k - l;
        return true;
    }

    /* r(s) is used further down. */

    private final void setto(String s) {
        int l = s.length();
        int o = j + 1;
        for (int i = 0; i < l; i++) {
            b[o + i] = s.charAt(i);
        }
        k = j + l;
    }

    /* step1() gets rid of plurals and -ed or -ing. e.g.

           caresses  ->  caress
           ponies    ->  poni
           ties      ->  ti
           caress    ->  caress
           cats      ->  cat

           feed      ->  feed
           agreed    ->  agree
           disabled  ->  disable

           matting   ->  mat
           mating    ->  mate
           meeting   ->  meet
           milling   ->  mill
           messing   ->  mess

           meetings  ->  meet

    */

    private final void r(String s) {
        if (m() > 0) {
            setto(s);
        }
    }

    /* step2() turns terminal y to i when there is another vowel in the stem. */

    private final void step1() {
        if (b[k] == 's') {
            if (ends("sses")) {
                k -= 2;
            } else if (ends("ies")) {
                setto("i");
            } else if (b[k - 1] != 's') {
                k--;
            }
        }
        if (ends("eed")) {
            if (m() > 0) {
                k--;
            }
        } else if ((ends("ed") || ends("ing")) && vowelinstem()) {
            k = j;
            if (ends("at")) {
                setto("ate");
            } else if (ends("bl")) {
                setto("ble");
            } else if (ends("iz")) {
                setto("ize");
            } else if (doublec(k)) {
                k--;
                {
                    int ch = b[k];
                    if (ch == 'l' || ch == 's' || ch == 'z') {
                        k++;
                    }
                }
            } else if (m() == 1 && cvc(k)) {
                setto("e");
            }
        }
    }

    /* step3() maps double suffices to single ones. so -ization ( = -ize plus
 -ation) maps to -ize etc. note that the string before the suffix must give
 m() > 0. */

    private final void step2() {
        if (ends("y") && vowelinstem()) {
            b[k] = 'i';
        }
    }

    /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

    private final void step3() {
        if (k == 0) {
            return; /* For Bug 1 */
        }
        switch (b[k - 1]) {
            case 'a':
                if (ends("ational")) {
                    r("ate");
                    break;
                }
                if (ends("tional")) {
                    r("tion");
                    break;
                }
                break;
            case 'c':
                if (ends("enci")) {
                    r("ence");
                    break;
                }
                if (ends("anci")) {
                    r("ance");
                    break;
                }
                break;
            case 'e':
                if (ends("izer")) {
                    r("ize");
                    break;
                }
                break;
            case 'l':
                if (ends("bli")) {
                    r("ble");
                    break;
                }
                if (ends("alli")) {
                    r("al");
                    break;
                }
                if (ends("entli")) {
                    r("ent");
                    break;
                }
                if (ends("eli")) {
                    r("e");
                    break;
                }
                if (ends("ousli")) {
                    r("ous");
                    break;
                }
                break;
            case 'o':
                if (ends("ization")) {
                    r("ize");
                    break;
                }
                if (ends("ation")) {
                    r("ate");
                    break;
                }
                if (ends("ator")) {
                    r("ate");
                    break;
                }
                break;
            case 's':
                if (ends("alism")) {
                    r("al");
                    break;
                }
                if (ends("iveness")) {
                    r("ive");
                    break;
                }
                if (ends("fulness")) {
                    r("ful");
                    break;
                }
                if (ends("ousness")) {
                    r("ous");
                    break;
                }
                break;
            case 't':
                if (ends("aliti")) {
                    r("al");
                    break;
                }
                if (ends("iviti")) {
                    r("ive");
                    break;
                }
                if (ends("biliti")) {
                    r("ble");
                    break;
                }
                break;
            case 'g':
                if (ends("logi")) {
                    r("log");
                    break;
                }
        }
    }

    /* step5() takes off -ant, -ence etc., in compContext <c>vcvc<v>. */

    private final void step4() {
        switch (b[k]) {
            case 'e':
                if (ends("icate")) {
                    r("ic");
                    break;
                }
                if (ends("ative")) {
                    r("");
                    break;
                }
                if (ends("alize")) {
                    r("al");
                    break;
                }
                break;
            case 'i':
                if (ends("iciti")) {
                    r("ic");
                    break;
                }
                break;
            case 'l':
                if (ends("ical")) {
                    r("ic");
                    break;
                }
                if (ends("ful")) {
                    r("");
                    break;
                }
                break;
            case 's':
                if (ends("ness")) {
                    r("");
                    break;
                }
                break;
        }
    }

    /* step6() removes a final -e if m() > 1. */

    private final void step5() {
        if (k == 0) {
            return; /* for Bug 1 */
        }
        switch (b[k - 1]) {
            case 'a':
                if (ends("al")) {
                    break;
                }
                return;
            case 'c':
                if (ends("ance")) {
                    break;
                }
                if (ends("ence")) {
                    break;
                }
                return;
            case 'e':
                if (ends("er")) {
                    break;
                }
                return;
            case 'i':
                if (ends("ic")) {
                    break;
                }
                return;
            case 'l':
                if (ends("able")) {
                    break;
                }
                if (ends("ible")) {
                    break;
                }
                return;
            case 'n':
                if (ends("ant")) {
                    break;
                }
                if (ends("ement")) {
                    break;
                }
                if (ends("ment")) {
                    break;
                }
                /* element etc. not stripped before the m */
                if (ends("ent")) {
                    break;
                }
                return;
            case 'o':
                if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) {
                    break;
                }
                /* j >= 0 fixes Bug 2 */
                if (ends("ou")) {
                    break;
                }
                return;
            /* takes care of -ous */
            case 's':
                if (ends("ism")) {
                    break;
                }
                return;
            case 't':
                if (ends("ate")) {
                    break;
                }
                if (ends("iti")) {
                    break;
                }
                return;
            case 'u':
                if (ends("ous")) {
                    break;
                }
                return;
            case 'v':
                if (ends("ive")) {
                    break;
                }
                return;
            case 'z':
                if (ends("ize")) {
                    break;
                }
                return;
            default:
                return;
        }
        if (m() > 1) {
            k = j;
        }
    }

    private final void step6() {
        j = k;
        if (b[k] == 'e') {
            int a = m();
            if (a > 1 || a == 1 && !cvc(k - 1)) {
                k--;
            }
        }
        if (b[k] == 'l' && doublec(k) && m() > 1) {
            k--;
        }
    }

    /**
     * Stem the word placed into the Stemmer buffer through calls to put(). Returns true if the stemming process
     * resulted in a word different from the input.  You can retrieve the result with
     * getResultLength()/getResultBuffer() or toString().
     */
    public void stem() {
        k = i - 1;
        if (k > 1) {
            step1();
            step2();
            step3();
            step4();
            step5();
            step6();
        }
        i_end = k + 1;
        i = 0;
    }
}
