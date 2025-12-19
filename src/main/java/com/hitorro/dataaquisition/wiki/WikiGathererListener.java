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

import org.wikimodel.wem.ReferenceHandler;
import org.wikimodel.wem.WikiFormat;
import org.wikimodel.wem.WikiPageUtil;
import org.wikimodel.wem.WikiParameters;
import org.wikimodel.wem.util.WikiEntityUtil;

/**
 *
 */
public class WikiGathererListener extends WikiGatherer {
    /**
     *
     */
    public WikiGathererListener(WikiGatheringPrinter printer) {
        super(printer);
    }

    public WikiGathererListener(WikiGatheringPrinter printer, boolean supportImage, boolean supportDownload) {
        super(printer, supportImage, supportDownload);
    }

    /**
     * @see org.wikimodel.wem.IWemListener#beginFormat(org.wikimodel.wem.WikiFormat)
     */
    @Override
    public void beginFormat(WikiFormat format) {
        printer.printFormat(format.getTags(true));
        if (format.getParams().size() > 0) {
            printer.printFormat("<span class='wikimodel-parameters'" + format.getParams() + ">");
        }
    }

    /**
     * @see org.wikimodel.wem.IWemListener#beginPropertyInline(java.lang.String)
     */
    @Override
    public void beginPropertyInline(String str) {
        printer.printFormat("<span class='wikimodel-property' url='"
                + WikiPageUtil.escapeXmlAttribute(str)
                + "'>");
    }

    /**
     * @see org.wikimodel.wem.IWemListener#endFormat(org.wikimodel.wem.WikiFormat)
     */
    @Override
    public void endFormat(WikiFormat format) {
        if (format.getParams().size() > 0) {
            printer.printFormat("</span>");
        }
        printer.printFormat(format.getTags(false));
    }

    /**
     * @see org.wikimodel.wem.IWemListener#endPropertyInline(java.lang.String)
     */
    @Override
    public void endPropertyInline(String inlineProperty) {
        printer.printFormat("</span>");
    }

    /**
     * Returns <code>true</code> if special ht.Wiki entities should be represented as the corresponding HTML entities or
     * they should be visualized using the corresponding XHTML codes (like &amp;amp; and so on). This method can be
     * overloaded in subclasses to re-define the visualization style.
     *
     * @return <code>true</code> if special ht.Wiki entities should be represented as the corresponding HTML entities or
     * they should be visualized using the corresponding XHTML codes (like &amp;amp; and so on).
     */
    protected boolean isHtmlEntities() {
        return true;
    }

    @Override
    protected ReferenceHandler newReferenceHandler() {
        return new ReferenceHandler(isSupportImage(), isSupportDownload()) {
            @Override
            protected void handleImage(
                    String ref,
                    String label,
                    WikiParameters params) {
                printer.printFormat("<img src='"
                        + WikiPageUtil.escapeXmlAttribute(ref)
                        + "'"
                        + params
                        + "/>");
            }

            @Override
            protected void handleReference(
                    String ref,
                    String label,
                    WikiParameters params) {
                printer.reference(ref, label, params);
            }

        };
    }

    /**
     * @see org.wikimodel.wem.IWemListener#onEscape(java.lang.String)
     */
    @Override
    public void onEscape(String str) {
    }

    @Override
    public void onExtensionInline(String extensionName, WikiParameters params) {
        /*print("<span class='wikimodel-extension' extension='"
                      + extensionName
                      + "'"
                      + params
                      + "/>");  */
        int i = 0;
    }

    /**
     * @see org.wikimodel.wem.IWemListener#onLineBreak()
     */

    @Override
    public void onLineBreak() {
        /*print("<br />");  */
    }

    /**
     * @see org.wikimodel.wem.IWemListener#onSpecialSymbol(java.lang.String)
     */
    @Override
    public void onSpecialSymbol(String str) {
        if (str.equals("{")) {
            printer.incrementDepth();
        } else if (str.equals("}")) {
            printer.decrementDepth();
        } else {
            String entity1 = null;
            if (isHtmlEntities()) {
                entity1 = WikiEntityUtil.getHtmlSymbol(str);
            } else {
                int code = WikiEntityUtil.getHtmlCodeByWikiSymbol(str);
                if (code > 0) {
                    entity1 = "#" + Integer.toString(code);
                }
            }
            if (entity1 != null) {
                entity1 = "&" + entity1 + ";";
                if (str.startsWith(" --")) {
                    entity1 = "&#160;" + entity1 + " ";
                }
            }
            String entity = entity1;
            if (entity == null) {
                entity = WikiPageUtil.escapeXmlString(str);
            }

            printer.printSpecialSymbol(entity);
        }

    }

    /**
     * @see org.wikimodel.wem.IWemListener#onVerbatimInline(java.lang.String, WikiParameters)
     */
    @Override
    public void onVerbatimInline(String str, WikiParameters params) {
        print("<tt class=\"wikimodel-verbatim\""
                + params
                + ">"
                + WikiPageUtil.escapeXmlString(str)
                + "</tt>");
    }

}
