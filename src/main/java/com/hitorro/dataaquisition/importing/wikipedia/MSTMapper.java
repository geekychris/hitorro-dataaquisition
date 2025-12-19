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
package com.hitorro.dataaquisition.importing.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.analysis.cky.ling.PennTreeRenderer;
import com.hitorro.analysis.cky.ling.Tree;
import com.hitorro.analysis.mstparser.DependencyResult;
import com.hitorro.analysis.mstparser.MSTParser;
import com.hitorro.analysis.mstparser.MSTParserSingleton;
import com.hitorro.util.core.events.cache.PoolContainer;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.mappers.JsonInitableMapper;

import java.util.List;

/**
 *
 */
public class MSTMapper extends JsonInitableMapper<JsonNode, ArrayNode> {
    PoolContainer<com.hitorro.language.IsoLanguage, MSTParser> mstp;
    MSTParser mstParser;
    private com.hitorro.language.IsoLanguage lang;
    private PoolContainer<com.hitorro.language.IsoLanguage, com.hitorro.language.PartOfSpeech> pool = null;
    private com.hitorro.language.PartOfSpeech ss;

    public MSTMapper() {
        this(com.hitorro.language.Iso639Table.english);
    }

    public MSTMapper(com.hitorro.language.IsoLanguage lang) {
        this.lang = lang;
        pool = com.hitorro.language.PartOfSpeechSingletonMapper.singleton.get(lang);
        ss = pool.get();

        mstp = MSTParserSingleton.singleton.get(com.hitorro.language.Iso639Table.english);
        mstParser = mstp.get();
    }

    public boolean isThreadSafe() {
        return false;
    }

    public BaseMapper getCopy() {
        return new MSTMapper(lang);
    }

    @Override
    public ArrayNode apply(final JsonNode e) {

        ArrayNode retM = JsonNodeFactory.instance.arrayNode();
        if (e.isArray()) {
            ArrayNode an = (ArrayNode) e;
            for (JsonNode jn : an) {
                getAux(jn, retM);
            }
        } else {
            getAux(e, retM);
        }
        return retM;
    }

    private void getAux(final JsonNode e, ArrayNode retM) {
        String txt = e.textValue();

        com.hitorro.language.POS pos = ss.getPOS(txt);

        List<String>[] arr = pos.getTags();
        List<String> tagRow = arr[0];
        String posArr[] = new String[tagRow.size()];
        posArr = tagRow.toArray(posArr);

        DependencyResult di = mstParser.get(pos.getTokenizedText(), posArr, false);
        ObjectNode on = JsonNodeFactory.instance.objectNode();
        retM.add(on);
        ArrayNode labels = JsonNodeFactory.instance.arrayNode();
        on.set("deps", labels);
        for (int i = 0; i < di.labels.length; i++) {
            labels.add(di.labels[i]);
        }
        Tree<String> tree = di.getTree();
        on.put("tree", PennTreeRenderer.render(tree));
    }

    public void finishSetup() {
        if (ss != null) {
            pool.returnIt(ss);
        }

        if (mstParser != null) {
            mstp.returnIt(mstParser);
        }
    }

}