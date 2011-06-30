/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tiles.definition.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.tiles.Definition;
import org.apache.tiles.definition.DefinitionsFactoryException;
import org.apache.tiles.definition.DefinitionsReader;
import org.apache.tiles.definition.RefreshMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstract class for a DAO that is based on URLs and locale as a
 * customization key.
 *
 * @version $Rev$ $Date$
 * @since 2.1.0
 */
public abstract class BaseLocaleUrlDefinitionDAO implements
        DefinitionDAO<Locale>, RefreshMonitor, URLReader {

    /**
     * The logging object.
     */
    private final Logger log = LoggerFactory
            .getLogger(BaseLocaleUrlDefinitionDAO.class);

    /**
     * Contains the URL objects identifying where configuration data is found.
     *
     * @since 2.1.0
     */
    protected List<URL> sourceURLs;

    /**
     * Contains the dates that the URL sources were last modified.
     *
     * @since 2.1.0
     */
    protected Map<String, Long> lastModifiedDates;

    /**
     * Reader used to get definitions from the sources.
     *
     * @since 2.1.0
     */
    protected DefinitionsReader reader;

    /**
     * Constructor.
     */
    public BaseLocaleUrlDefinitionDAO() {
        lastModifiedDates = new HashMap<String, Long>();
    }

    /**  {@inheritDoc}*/
    public void setSourceURLs(List<URL> sourceURLs) {
        this.sourceURLs = sourceURLs;
    }

    /**  {@inheritDoc}*/
    public void setReader(DefinitionsReader reader) {
        this.reader = reader;
    }

    /** {@inheritDoc} */
    public boolean refreshRequired() {
        boolean status = false;

        Set<String> urls = lastModifiedDates.keySet();

        try {
            for (String urlPath : urls) {
                Long lastModifiedDate = lastModifiedDates.get(urlPath);
                URL url = new URL(urlPath);
                URLConnection connection = url.openConnection();
                connection.connect();
                long newModDate = connection.getLastModified();
                if (newModDate != lastModifiedDate) {
                    status = true;
                    break;
                }
            }
        } catch (IOException e) {
            log.warn("Exception while monitoring update times.", e);
            return true;
        }
        return status;
    }

    /**
     * Loads definitions from an URL without loading from "parent" URLs.
     *
     * @param url The URL to read.
     * @return The definition map that has been read.
     */
    protected Map<String, Definition> loadDefinitionsFromURL(URL url) {
        Map<String, Definition> defsMap = null;

        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            // File not found. continue.
            if (log.isDebugEnabled()) {
                log.debug("I/O exception thrown when opening URL connection to "
                        + url.toString() + ", continue");
            }
            return null;
        }

        InputStream stream = null;
        try {
            connection.connect();
            lastModifiedDates.put(url.toExternalForm(), connection
                    .getLastModified());

            // Definition must be collected, starting from the base
            // source up to the last localized file.
            stream = connection.getInputStream();
            defsMap = reader.read(stream);
        } catch (FileNotFoundException e) {
            // File not found. continue.
            if (log.isDebugEnabled()) {
                log.debug("File " + url.toString() + " not found, continue");
            }
        } catch (IOException e) {
            throw new DefinitionsFactoryException(
                    "I/O error processing configuration.", e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                throw new DefinitionsFactoryException(
                        "I/O error closing " + url.toString(), e);
            }
        }

        return defsMap;
    }
}