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
package com.hitorro.dataaquisition.fetcher;

import com.hitorro.base.docprocessing.blockqueue.QInstance;
import com.hitorro.dataaquisition.BaseDataAquisitionService;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.json.keys.ClassInstantiationProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

/**
 * Fetch data from a source (at the moment twitter, but needs a little rework for other places).
 * <p/>
 * Construct a queue with the appropriate segmentation mechanism and writer, provide the fetch handler that has a
 * runnable on it and your done. The magic of course is in the queue writing semantics and the handler that fetches the
 * data and has to keep up and running.
 */
@ServiceDefinition(dependentService = {BaseDataAquisitionService.class},
        shortName = "fetcher",
        description = "Fetcher service",
        debugCommands = {},
        typeManagedClasses = {})
public class FetcherService {
    public final static StringProperty ConfigPath = new StringProperty("configpath", "where to pickup the configuration options for this fetcher", "fetcher.twitter");
    public final static ClassInstantiationProperty<FetchServiceHandler> FetchServiceHandlerKey = new ClassInstantiationProperty("fetcher.fetchhandler", "", null, FetchServiceHandler.class);

    private QInstance qInst;
    private FetchServiceHandler fsHandler;

    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        fsHandler = FetchServiceHandlerKey.apply();
        if (fsHandler == null) {
            return "No fetch handler defined or not constructable";
        }
        try {
            initQueueWriter(fsHandler.getFileExtension(), ConfigPath.apply());
        } catch (PropaccessError e) {
            return Fmt.S("Unable to initalize FetcherService %s %e", e, e);
        }
        fsHandler.init(qInst);
        return null;
    }

    public String start(boolean dbInit) {
        RestartableService rs = new RestartableService("fetcher", "fetcher", 100, fsHandler, true);
        RestartableServiceDaemon.addService(rs);
        return null;
    }

    public String deInit() {
        return null;
    }

    private boolean initQueueWriter(String extension, String configPath) throws PropaccessError {
        JVS jvs = new JVS(JVSProperties.getProperties().get(configPath));
        jvs = jvs.clone();
        jvs.set("partitioner.class", fsHandler.getPartitioner());
        jvs.set("writer.class", fsHandler.getWriter());
        qInst = new QInstance();
        qInst.init(extension, jvs.getJsonNode());
        return true;
    }
}
