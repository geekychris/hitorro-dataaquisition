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
package com.hitorro.dataaquisition.wiki;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.base.BaseBaseFileUtil;
import com.hitorro.base.filesetmanager.FileQueue;
import com.hitorro.base.filesetmanager.FileSetManager;
import com.hitorro.base.filesetmanager.SinkSet;
import com.hitorro.basetext.dfindex.FPHashDict;
import com.hitorro.basetext.mappers.Text2Sentences;
import com.hitorro.basetext.phrase.FPPhraseContext;
import com.hitorro.dataaquisition.importing.wikipedia.Wiki2Bag;
import com.hitorro.obj.core.FilterContext;
import com.hitorro.obj.core.GenericAnalyzer;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.events.cache.PoolContainer;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.ConditionStringMapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.opers.LogicalNotOperator;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.ResetableStringReader;
import com.hitorro.util.servicecounters.CounterSet;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.constraint.BagContainsField;
import com.hitorro.util.xml.XE;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wikimodel.wem.WikiParserException;
import org.wikimodel.wem.mediawiki.MediaWikiParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;


/**
 * ht.dataaquisition.wiki.TestWikiGathering
 */
public class TestWikiGathering extends EnhancedTestCase {
    public static final String wikiFile = "/publicsource/opw/wiki/enwiki-20110115-pages-articles.xml.bz2";

    public void testAA() {

    }

    public void teestA() throws IOException {
        FileQueue fq = FileSetManager.getQueue("tst");
        AbstractIterator<Bag> iter = fq.getInputIterator();
        //Iterator<Object> iter = fq.getIterator(FileFileSystem.Root.getJavaFile("/spinnr_single.xml"));
        //  Iterator<Object> iter = fq.getInputIterator();
        int counter = 0;
        while (iter.hasNext()) {
            Object o = iter.next();
            Console.println("%s", counter++);
        }
        Console.println();
    }

    public void teestB() {
        FileQueue fq = FileSetManager.getQueue("tst");

        BaseFile bf = FileFileSystem.Root.getFile("/2.protostream.gz");

        Iterator<Bag> bIter = fq.getIterator(bf);
        while (bIter.hasNext()) {
            Bag b = bIter.next();
            Console.println("%s", b);
        }
    }

    public void teestChunker() {
        com.hitorro.language.IsoLanguage lang = com.hitorro.language.Iso639Table.english;

    }

    public void teestReadDataMap() {
        Timer t = new Timer();
        Console.println("Started re-write");
        t.reset();
        SinkSet ss = FileSetManager.getSink("ht");
        BaseFile wk = FileFileSystem.Root.getFile(wikiFile);
        AbstractIterator<XE> iter = BaseFileUtil.bf2Xwikipdeai_xedom.apply(wk);
        AbstractIterator<Bag> bagIter = iter.map(Wiki2Bag.wikixe2bagmapper).filter(new LogicalNotOperator(new BagContainsField("redirect")));
        CounterSet cs = new CounterSet("bftester");
        bagIter = bagIter.time(cs);
        while (bagIter.hasNext()) {
            bagIter.next();
        }
        Console.println("Timer : seconds %s", t.getTime() / 1000);
    }

    public void testReadDataMap() {
        Timer t = new Timer();
        Console.println("Started re-write");
        t.reset();
        BaseFile wk = FileFileSystem.Root.getFile(wikiFile);
        AbstractIterator<XE> iter = BaseFileUtil.bf2Xwikipdeai_xedom.apply(wk);
        AbstractIterator<List<String>> bagIter = iter.map(Wiki2Bag.wikixe2bagmapper).filter(new LogicalNotOperator(new BagContainsField("redirect"))).map(new Text2Sentences(com.hitorro.language.Iso639Table.english, new String[]{"title", "body"}));
        CounterSet cs = new CounterSet("bftester");
        bagIter = bagIter.time(cs);
        PrintWriter pw = FileUtil.getBufferedPrintWriterFromFile(new File("/Users/chris/wikisents.txt"));
        while (bagIter.hasNext()) {
            List<String> sents = bagIter.next();
            for (String sent : sents) {
                pw.println(sent);
            }
        }
        pw.close();
        Console.println("Timer : seconds %s", t.getTime() / 1000);
    }

    public void teestReadData() {
        Timer t = new Timer();
        Console.println("Started re-write");

        BaseFile wk = FileFileSystem.Root.getFile("/hthome/wiki.ht.gz");
        if (!wk.exists()) {
            return;
        }
        AbstractIterator<Bag> bagIter = (AbstractIterator<Bag>) BaseBaseFileUtil.bf2htser.apply(wk);
        CounterSet cs = new CounterSet("bftester");
        bagIter = bagIter.time(cs);
        while (bagIter.hasNext()) {
            bagIter.next();
        }
        Console.println("Timer : seconds %s", t.getTime() / 1000);
    }


    public void teestReWriteWikiData() throws IOException {
        Timer t = new Timer();
        Console.println("Started re-write");
        t.reset();
        SinkSet ss = FileSetManager.getSink("ht");
        BaseFile wk = FileFileSystem.Root.getFile("/Users/chris/opendata/wikipedia/enwiki-20110115-pages-articles.xml.bz2");
        AbstractIterator<XE> iter = BaseFileUtil.bf2Xwikipdeai_xedom.apply(wk);
        AbstractIterator<Bag> bagIter = iter.map(Wiki2Bag.wikixe2bagmapper).filter(new LogicalNotOperator(new BagContainsField("redirect")));
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("outfile", "file://hthome/wiki.ht.gz");
        Sink sink = ss.getSink(node);
        bagIter.sink(sink);
        t.stop();
        Console.println("Timer : seconds %s", t.getTime() / 1000);
    }

    public void teestWiki() throws IOException, WikiParserException {


        String s = StringUtil.readFileIntoString(new File("/hthome/amiga.txt"));
        MediaWikiParser parser = new MediaWikiParser();
        StringReader sr = new StringReader(s);
        WikiGatheringPrinter f = new WikiGatheringPrinter();
        WikiGathererListener wgl = new WikiGathererListener(f);
        //WikiGatherer wg = new WikiGatherer(f);
        parser.parse(sr, wgl);
        String text = f.getText();
        Console.println("Text: %s", text);
        Console.println("Refs: %s", f.getRefs());

        for (String box : f.infoBoxes) {
            Console.println("Info: %s", box);
        }


        com.hitorro.language.IsoLanguage lang = com.hitorro.language.Iso639Table.english;
        PoolContainer<com.hitorro.language.IsoLanguage, com.hitorro.language.SentenceSegmenter> pool = com.hitorro.language.SentenceDetectorSingleton.singleton.get(lang);
        com.hitorro.language.SentenceSegmenter ss = pool.get();


        PoolContainer<com.hitorro.language.IsoLanguage, com.hitorro.language.PartOfSpeech> posPool = com.hitorro.language.PartOfSpeechSingletonMapper.singleton.get(lang);
        com.hitorro.language.PartOfSpeech pos = posPool.get();

        SentenceEnumerator se = new SentenceEnumerator(pos);
        com.hitorro.language.Sentences sentences = ss.getSentenceOffsets(text);

        sentences.visitSentences(se);
        posPool.returnIt(pos);
        pool.returnIt(ss);
    }

    public void teestWikiMap() {
        BaseFile wk = FileFileSystem.Root.getFile("/wiki/enwiki-20110115-pages-articles.xml.bz2");
        AbstractIterator<XE> iter = BaseFileUtil.bf2Xwikipdeai_xedom.apply(wk);
        AbstractIterator<Bag> bagIter = iter.map(Wiki2Bag.wikixe2bagmapper).filter(new LogicalNotOperator(new BagContainsField("redirect")));
        MediaWikiParser parser = new MediaWikiParser();
        WikiGatheringPrinter f = new WikiGatheringPrinter();
        // WikiGatherer wg = new WikiGatherer(f);
        WikiGathererListener pil = new WikiGathererListener(f);
        int counter = 0;
        PrintWriter pw = FileUtil.getBufferedPrintWriterFromFile(new File("/wiki.csv"));
        Console.println(pw, "key, iswikientry");
        while (bagIter.hasNext()) {
            Bag bag = bagIter.next();
            counter++;
            if (counter % 1000 == 0) {
                Console.println("counter %s", counter);
            }
            if (bag != null) {
                //Console.println("%s", bag);
                String o = bag.getValueAsString(bag, "body");
                if (o == null) {
                    continue;
                }
                String t = bag.getValueAsString(bag, "title");
                if (t == null) {
                    continue;
                }
                int index = t.indexOf("(");
                if (index != -1) {
                    t = t.substring(0, index);
                }
                t = ConditionStringMapper.alpha.apply(t);

                Console.println(pw, "%s, 1", t);
                //  Console.println("%s", t);
                String title = t;
                String bridge = "Amiga";

                if (!title.equalsIgnoreCase(bridge)) {
                    continue;
                }
                //if (txt.length() > 5000)
                {
                    f.clear();
                    StringReader sr = new StringReader(o);

                    try {
                        parser.parse(sr, pil);
                        //parser.parse(sr, wg);
                        //String s = f.getText();

                        //Console.println("==============================================================================");
                        //Console.println(s);
                    } catch (WikiParserException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            }
        }
        pw.flush();
        pw.close();
        Console.println("%s", counter);
    }
}


class SentenceEnumerator extends com.hitorro.language.SimpleSentenceVisitor {
    private com.hitorro.language.PartOfSpeech pos;
    private StringBuilder toks = new StringBuilder();
    private WikipediaDict dict;

    private Context context;
    private FilterContext fc;
    private ResetableStringReader reader = new ResetableStringReader("");
    private GenericAnalyzer ga = new GenericAnalyzer("htstandard,emitphrase", com.hitorro.language.Iso639Table.english, GenericAnalyzer.Mode.Index);

    public SentenceEnumerator(com.hitorro.language.PartOfSpeech pos) {
        this.pos = pos;
        dict = new WikipediaDict();
        dict.init();
        context = new Context(4, dict.getDictionary(), true);
        fc = new FilterContext();
        fc.fpPhraseContext = context;
        fc.baseIndex = dict.getDictionary();
    }

    @Override
    public boolean visitSimple(final String string, final int sentenceNumber) {
        reader.set(string);
        TokenStream ts = ga.tokenStream("phrase", reader);
        CharTermAttribute termAttLocal = ts.addAttribute(CharTermAttribute.class);
        try {
            context.reset();

            while (ts.incrementToken()) {
                String tok = termAttLocal.toString();
            }
            ts.close();
            if (context.getOccurences() > 0) {
                return visitAux(string);
            } else {
                // nada
                Console.println("Nada");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;

    }

    private boolean visitAux(final String string) {
        com.hitorro.language.PennAndTreebankBase patb = com.hitorro.language.Iso639Table.english.getPennAndTreebank();
        pos.reset();
        com.hitorro.language.POS p = pos.getPOS(string);
        List<String>[] tt = p.getTags();
        if (tt != null) {
            List<String> tags = tt[0];
            String t[] = p.getTokenizedText();
            for (int i = 0; i < t.length; i++) {
                String tag = tags.get(i);
                com.hitorro.language.PATElem pat = patb.getValue(tag);
                if (pat != null) {
                    pat = pat.getRoot();

                    if (toks.length() > 0) {
                        Console.println(toks.toString());
                        toks.setLength(0);
                    }

                }

                //Console.print("%s(%s) ", t[i], tags[i]);
            }
            //Console.println();
        }
        if (toks.length() > 0) {
            Console.println(toks.toString());
            toks.setLength(0);
        }

        return true;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean start() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean end() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean finishUse() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

class Context extends FPPhraseContext {
    private int occurences = 0;

    public Context(int maxDepth, FPHashDict baseIndex, boolean recordStrings) {
        super(maxDepth, baseIndex, recordStrings);
    }

    public void phraseMatch(long fingerPrint, int startPosition, int size, String txt, long val) {
        //Console.println(">>> %s %s %s %s val:%s <%s>", fingerPrint, startPosition, size, txt, val, getVal(val));
        occurences++;
    }

    public int getOccurences() {
        return occurences;
    }

    public void reset() {
        occurences = 0;
    }
}



