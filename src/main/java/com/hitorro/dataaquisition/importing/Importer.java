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
package com.hitorro.dataaquisition.importing;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.dataaquisition.BaseDataAquisitionService;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.basefile.tools.queue.reader.GoodFileKeyWriter;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.json.keys.ClassInstantiationProperty;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.json.keys.ResolvableStringProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.io.File;
import java.io.IOException;

/**
 *
 */
@ServiceDefinition(dependentService = {BaseDataAquisitionService.class},
        shortName = "importer",
        description = "importer",
        debugCommands = {},
        typeManagedClasses = {})
public class Importer {
    public static final FileProperty LgkProperty = new FileProperty("importer.lgkname", "", "${ht_home}/queuekeys/importer.lgk");
    public static final ResolvableStringProperty QueueRootProperty = new ResolvableStringProperty("inputqueue", "", "file://${ht_home}/queuepath/");
    public static final StringProperty ConfigPath = new StringProperty("importer.configpath", "where to look in the config path for importer options", null);
    public final static ClassInstantiationProperty<ImporterServiceHandler> ImporterServiceHandlerKey = new ClassInstantiationProperty("importerhandler", "", null, ImporterServiceHandler.class);

    private String rootString;
    private BaseFile root;
    private DirectoryVisitorIterator dirIter;
    private ImporterServiceHandler handler;


    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        JsonNode map = null;
        try {
            map = JVSProperties.getProperties().get(ConfigPath.apply());
        } catch (PropaccessError propaccessError) {
            return "Unable to init importer";
        }
        handler = ImporterServiceHandlerKey.apply(map);
        if (handler == null) {
            return "No fetch handler defined or not constructable";
        }
        File lgkFile = LgkProperty.apply(map);
        rootString = QueueRootProperty.apply(map);
        try {
            root = BaseFileSystem.getFileFromPath(QueueRootProperty, map);
        } catch (IOException e) {
            String err = Fmt.S("Unable to get filepath for queue path:%s error %s %e", rootString, e, e);
            Log.queue.error(err);
            return err;
        }

        GoodFileKeyWriter lgk = new GoodFileKeyWriter(lgkFile);
        FileEndsWith filter = new FileEndsWith("json.gz", true);
        root.mkdir();

        try {
            dirIter = new DirectoryVisitorIterator(root, 30, lgk, new LogicalOrOperator(new IsDir(), filter));
        } catch (IOException e) {
            return e.getMessage();
        }
        handler.init(map, dirIter);
        return null;
    }

    public String start(boolean dbInit) {
        RestartableService rs = new RestartableService("Importer", "Importer", 100, handler, true);
        RestartableServiceDaemon.addService(rs);
        return null;
    }
}
