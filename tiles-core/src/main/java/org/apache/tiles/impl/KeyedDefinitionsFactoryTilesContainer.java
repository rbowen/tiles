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
 *
 */

package org.apache.tiles.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.tiles.TilesException;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.definition.ComponentDefinition;
import org.apache.tiles.definition.DefinitionsFactory;
import org.apache.tiles.definition.DefinitionsFactoryException;

/**
 * Container that can be used to store multiple {@link DefinitionsFactory}
 * instances mapped to different keys.
 *
 * @version $Rev$ $Date$
 */
public class KeyedDefinitionsFactoryTilesContainer extends BasicTilesContainer {
    
    /**
     * Name of the attribute inside the request that will be used to get the key
     * of the definitions factory to be used.
     */
    public static final String DEFINITIONS_FACTORY_KEY_ATTRIBUTE_NAME =
        "org.apache.tiles.DEFINITIONS_FACTORY.key";
	
	/**
	 * Maps definition factories to their keys.
	 */
	protected Map<String, DefinitionsFactory> key2definitionsFactory;

	/**
	 * Constructor.
	 */
	public KeyedDefinitionsFactoryTilesContainer() {
		key2definitionsFactory = new HashMap<String, DefinitionsFactory>();
	}

	/**
     * Standard Getter
     *
     * @return the definitions factory used by this container. If the key is not
     * valid, the default factory will be returned.
     * @param key The key of the needed definitions factory.
     */
    public DefinitionsFactory getDefinitionsFactory(String key) {
    	DefinitionsFactory retValue = null;
    	
    	if (key != null) {
    		retValue = key2definitionsFactory.get(key);
    	}
    	if (retValue == null) {
    		retValue = getDefinitionsFactory();
    	}
    	
    	return retValue;
    }

    /**
     * Standard Getter
     *
     * @return the definitions factory used by this container. If the key is not
     * valid, <code>null</code> will be returned.
     * @param key The key of the needed definitions factory.
     */
    public DefinitionsFactory getProperDefinitionsFactory(String key) {
        DefinitionsFactory retValue = null;
        
        if (key != null) {
            retValue = key2definitionsFactory.get(key);
        }
        
        return retValue;
    }

    /**
     * Set the definitions factory. This method first ensures
     * that the container has not yet been initialized.
     *
     * @param definitionsFactory the definitions factory for this instance.
     * @throws TilesException If something goes wrong during initialization of
     * the definitions factory.
     */
    public void setDefinitionsFactory(String key,
    		DefinitionsFactory definitionsFactory,
    		Map<String, String> initParameters) throws TilesException {
    	if (key != null) {
	        key2definitionsFactory.put(key, definitionsFactory);
            initializeDefinitionsFactory(definitionsFactory,
                    getResourceString(initParameters), initParameters);
    	} else {
    		setDefinitionsFactory(definitionsFactory);
    	}
    }

    @Override
    protected ComponentDefinition getDefinition(String definitionName,
            TilesRequestContext request) throws DefinitionsFactoryException {
        ComponentDefinition retValue = null;
        String key = getDefinitionsFactoryKey(request);
        if (key != null) {
            DefinitionsFactory definitionsFactory =
                key2definitionsFactory.get(key);
            if (definitionsFactory != null) {
                retValue = definitionsFactory.getDefinition(definitionName,
                        request);
            }
        }
        if (retValue == null) {
            retValue = super.getDefinition(definitionName, request);
        }
        return retValue;
    }
    
    /**
     * Returns the definitions factory key.
     *
     * @param request The request object.
     * @return The needed factory key.
     */
    protected String getDefinitionsFactoryKey(TilesRequestContext request) {
        String retValue = null;
        Map requestScope = request.getRequestScope();
        if (requestScope != null) { // Probably the request scope does not exist
            retValue = (String) requestScope.get(
                    DEFINITIONS_FACTORY_KEY_ATTRIBUTE_NAME);
        }
        
        return retValue;
    }
}