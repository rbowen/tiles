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

package org.apache.tiles.velocity.template;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tiles.servlet.context.ServletUtil;
import org.apache.tiles.template.PutAttributeModel;
import org.apache.tiles.velocity.context.VelocityUtil;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.Renderable;

/**
 * Wraps {@link PutAttributeModel} to be used in Velocity. For the list of
 * parameters, see
 * {@link PutAttributeModel#start(java.util.Stack)}
 * , {@link PutAttributeModel#end(org.apache.tiles.TilesContainer, java.util.Stack, String, Object, String, String, String, String, boolean, Object...)} and
 * {@link PutAttributeModel#execute(org.apache.tiles.TilesContainer, java.util.Stack, String, Object, String, String, String, String, boolean, Object...)}.
 * 
 * @version $Rev$ $Date$
 * @since 2.2.0
 */
public class PutAttributeVModel implements Executable, BodyExecutable {

    /**
     * The template model.
     */
    private PutAttributeModel model;

    /**
     * The Servlet context.
     */
    private ServletContext servletContext;
    
    /**
     * Constructor.
     * 
     * @param model The template model.
     * @param servletContext The servlet context.
     * @since 2.2.0
     */
    public PutAttributeVModel(PutAttributeModel model,
            ServletContext servletContext) {
        this.model = model;
        this.servletContext = servletContext;
    }
    
    /** {@inheritDoc} */
    public Renderable execute(HttpServletRequest request,
            HttpServletResponse response, Context velocityContext,
            Map<String, Object> params) {
        model.execute(ServletUtil.getCurrentContainer(request,
                servletContext), ServletUtil.getComposeStack(request),
                (String) params.get("name"), params.get("value"),
                (String) params.get("expression"), null,
                (String) params.get("role"), (String) params
                        .get("type"), VelocityUtil.toSimpleBoolean(
                        (Boolean) params.get("cascade"), false),
                velocityContext, request, response);

        return VelocityUtil.EMPTY_RENDERABLE;
    }

    /** {@inheritDoc} */
    public Renderable end(HttpServletRequest request, HttpServletResponse response,
            Context velocityContext) {
        Map<String, Object> params = VelocityUtil.getParameterStack(
                velocityContext).pop();
        model.end(ServletUtil.getCurrentContainer(request, servletContext), ServletUtil.getComposeStack(request),
                (String) params.get("name"), params.get("value"),
                (String) params.get("expression"), null, (String) params.get("role"),
                (String) params.get("type"), VelocityUtil.toSimpleBoolean((Boolean) params.get("value"), false),
                velocityContext, request, response);
        return VelocityUtil.EMPTY_RENDERABLE;
    }

    /** {@inheritDoc} */
    public void start(HttpServletRequest request, HttpServletResponse response,
            Context velocityContext, Map<String, Object> params) {
        VelocityUtil.getParameterStack(velocityContext).push(params);
        model.start(ServletUtil.getComposeStack(request));
    }
}