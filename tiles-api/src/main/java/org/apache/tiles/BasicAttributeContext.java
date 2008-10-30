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

package org.apache.tiles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Basic implementation for <code>AttributeContext</code>.
 *
 * @version $Rev$ $Date$
 * @since 2.1.0
 */
public class BasicAttributeContext implements AttributeContext, Serializable {

    /**
     * Template path.
     *
     * @since 2.1.0
     */
    protected String template = null;

    /**
     * The roles that can render this definition.
     *
     * @since 2.1.0
     */
    protected Set<String> roles = null;

    /**
     * Associated ViewPreparer URL or classname, if defined.
     *
     * @since 2.1.0
     */
    protected String preparer = null;

    /**
     * Template attributes.
     * @since 2.1.0
     */
    protected Map<String, Attribute> attributes = null;

    /**
     * Cascaded template attributes.
     * @since 2.1.0
     */
    protected Map<String, Attribute> cascadedAttributes = null;

    /**
     * Constructor.
     *
     * @since 2.1.0
     */
    public BasicAttributeContext() {
        super();
    }

    /**
     * Constructor.
     * Create a context and set specified attributes.
     *
     * @param attributes Attributes to initialize context.
     * @since 2.1.0
     */
    public BasicAttributeContext(Map<String, Attribute> attributes) {
        if (attributes != null) {
            this.attributes = new HashMap<String, Attribute>(attributes);
        }
    }

    /**
     * Copy constructor.
     *
     * @param context The constructor to copy.
     * @since 2.1.0
     */
    public BasicAttributeContext(AttributeContext context) {
        if (context instanceof BasicAttributeContext) {
            copyBasicAttributeContext((BasicAttributeContext) context);
        } else {
            this.template = context.getTemplate();
            Set<String> roles = context.getRoles();
            if (roles != null && !roles.isEmpty()) {
                this.roles = new HashSet<String>(roles);
            }
            this.preparer = context.getPreparer();
            this.attributes = new HashMap<String, Attribute>();
            for (String name : context.getLocalAttributeNames()) {
                attributes.put(name, context.getLocalAttribute(name));
            }
            inheritCascadedAttributes(context);
        }
    }

    /**
     * Copy constructor.
     *
     * @param context The constructor to copy.
     * @since 2.1.0
     */
    public BasicAttributeContext(BasicAttributeContext context) {
        copyBasicAttributeContext(context);
    }

    /** {@inheritDoc} */
    public String getTemplate() {
        return template;
    }

    /** {@inheritDoc} */
    public void setTemplate(String template) {
        this.template = template;
    }

    /** {@inheritDoc} */
    public String getRole() {
        String retValue = null;

        if (roles != null && !roles.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            Iterator<String> roleIt = roles.iterator();
            if (roleIt.hasNext()) {
                builder.append(roleIt.next());
                while (roleIt.hasNext()) {
                    builder.append(",");
                    builder.append(roleIt.next());
                }
                retValue = builder.toString();
            }
        }

        return retValue;
    }

    /** {@inheritDoc} */
    public Set<String> getRoles() {
        return roles;
    }

    /** {@inheritDoc} */
    public void setRole(String role) {
        if (role != null && role.trim().length() > 0) {
            String[] rolesStrings = role.split("\\s*,\\s*");
            roles = new HashSet<String>();
            for (int i = 0; i < rolesStrings.length; i++) {
                roles.add(rolesStrings[i]);
            }
        } else {
            roles = null;
        }
    }

    /** {@inheritDoc} */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /** {@inheritDoc} */
    public String getPreparer() {
        return preparer;
    }

    /** {@inheritDoc} */
    public void setPreparer(String url) {
        this.preparer = url;
    }

    /** {@inheritDoc} */
    public void inheritCascadedAttributes(AttributeContext context) {
        if (context instanceof BasicAttributeContext) {
            copyCascadedAttributes((BasicAttributeContext) context);
        } else {
            this.cascadedAttributes = new HashMap<String, Attribute>();
            for (String name : context.getCascadedAttributeNames()) {
                cascadedAttributes
                        .put(name, context.getCascadedAttribute(name));
            }
        }
    }

    /** {@inheritDoc} */
    public void inherit(AttributeContext parent) {
        if (parent instanceof BasicAttributeContext) {
            inherit((BasicAttributeContext) parent);
        } else {
            // Inheriting template, roles and preparer.
            if (template == null) {
                template = parent.getTemplate();
            }
            Set<String> parentRoles = parent.getRoles();
            if ((roles == null || roles.isEmpty()) && parentRoles != null
                    && !parentRoles.isEmpty()) {
                roles = new HashSet<String>(parentRoles);
            }
            if (preparer == null) {
                preparer = parent.getPreparer();
            }

            // Inheriting attributes.
            Set<String> names = parent.getCascadedAttributeNames();
            if (names != null && !names.isEmpty()) {
                for (String name : names) {
                    Attribute attribute = parent.getCascadedAttribute(name);
                    Attribute destAttribute = getCascadedAttribute(name);
                    if (destAttribute == null) {
                        putAttribute(name, attribute, true);
                    } else if (attribute instanceof ListAttribute
                            && destAttribute instanceof ListAttribute
                            && ((ListAttribute) destAttribute).isInherit()) {
                        ((ListAttribute) destAttribute).inherit((ListAttribute) attribute);
                    }
                }
            }
            names = parent.getLocalAttributeNames();
            if (names != null && !names.isEmpty()) {
                for (String name : names) {
                    Attribute attribute = parent.getLocalAttribute(name);
                    Attribute destAttribute = getLocalAttribute(name);
                    if (destAttribute == null) {
                        putAttribute(name, attribute, false);
                    } else if (attribute instanceof ListAttribute
                            && destAttribute instanceof ListAttribute
                            && ((ListAttribute) destAttribute).isInherit()) {
                        ((ListAttribute) destAttribute).inherit((ListAttribute) attribute);
                    }
                }
            }
        }
    }

    /**
     * Inherits the attribute context, inheriting, i.e. copying if not present,
     * the attributes.
     *
     * @param parent The attribute context to inherit.
     * @since 2.1.0
     */
    public void inherit(BasicAttributeContext parent) {
        // Set template, roles and preparer if not set.
        if (template == null) {
            template = parent.template;
        }
        if ((roles == null || roles.isEmpty()) && parent.roles != null
                && !parent.roles.isEmpty()) {
            roles = new HashSet<String>(parent.roles);
        }
        if (preparer == null) {
            preparer = parent.preparer;
        }

        // Sets attributes.
        cascadedAttributes = addMissingAttributes(
                ((BasicAttributeContext) parent).cascadedAttributes,
                cascadedAttributes);
        attributes = addMissingAttributes(
                ((BasicAttributeContext) parent).attributes, attributes);
    }

    /**
     * Add all attributes to this context.
     * Copies all of the mappings from the specified attribute map to this context.
     * New attribute mappings will replace any mappings that this context had for any of the keys
     * currently in the specified attribute map.
     *
     * @param newAttributes Attributes to add.
     * @since 2.1.0
     */
    public void addAll(Map<String, Attribute> newAttributes) {
        if (newAttributes == null) {
            return;
        }

        if (attributes == null) {
            attributes = new HashMap<String, Attribute>(newAttributes);
            return;
        }

        attributes.putAll(newAttributes);
    }

    /**
     * Add all missing attributes to this context.
     * Copies all of the mappings from the specified attributes map to this context.
     * New attribute mappings will be added only if they don't already exist in
     * this context.
     *
     * @param defaultAttributes Attributes to add.
     * @since 2.1.0
     */
    public void addMissing(Map<String, Attribute> defaultAttributes) {
        if (defaultAttributes == null) {
            return;
        }

        if (attributes == null) {
            attributes = new HashMap<String, Attribute>(defaultAttributes);
            if (cascadedAttributes == null || cascadedAttributes.isEmpty()) {
                return;
            }
        }

        Set<Map.Entry<String, Attribute>> entries = defaultAttributes.entrySet();
        for (Map.Entry<String, Attribute> entry : entries) {
            String key = entry.getKey();
            if (!attributes.containsKey(key)
                    && (cascadedAttributes == null || cascadedAttributes
                            .containsKey(key))) {
                attributes.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /** {@inheritDoc} */
    public Attribute getAttribute(String name) {
        Attribute retValue = null;
        if (attributes != null) {
            retValue = attributes.get(name);
        }

        if (retValue == null && cascadedAttributes != null) {
            retValue = cascadedAttributes.get(name);
        }

        return retValue;
    }

    /** {@inheritDoc} */
    public Attribute getLocalAttribute(String name) {
        if (attributes == null) {
            return null;
        }

        return attributes.get(name);
    }

    /** {@inheritDoc} */
    public Attribute getCascadedAttribute(String name) {
        if (cascadedAttributes == null) {
            return null;
        }

        return cascadedAttributes.get(name);
    }

    /** {@inheritDoc} */
    public Iterator<String> getAttributeNames() {
        Set<String> attributeSet = null;

        if (attributes != null && !attributes.isEmpty()) {
            attributeSet = new HashSet<String>(attributes
                    .keySet());
            if (cascadedAttributes != null && !cascadedAttributes.isEmpty()) {
                attributeSet.addAll(cascadedAttributes.keySet());
            }
        } else if (cascadedAttributes != null && !cascadedAttributes.isEmpty()) {
            attributeSet = new HashSet<String>(cascadedAttributes.keySet());
        }

        if (attributeSet != null) {
            return attributeSet.iterator();
        } else {
            return new ArrayList<String>().iterator();
        }
    }

    /** {@inheritDoc} */
    public Set<String> getLocalAttributeNames() {
        if (attributes != null && !attributes.isEmpty()) {
            return attributes.keySet();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public Set<String> getCascadedAttributeNames() {
        if (cascadedAttributes != null && !cascadedAttributes.isEmpty()) {
            return cascadedAttributes.keySet();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public void putAttribute(String name, Attribute value) {
        if (attributes == null) {
            attributes = new HashMap<String, Attribute>();
        }

        attributes.put(name, value);
    }

    /** {@inheritDoc} */
    public void putAttribute(String name, Attribute value, boolean cascade) {
        Map<String, Attribute> mapToUse;
        if (cascade) {
            if (cascadedAttributes == null) {
                cascadedAttributes = new HashMap<String, Attribute>();
            }
            mapToUse = cascadedAttributes;
        } else {
            if (attributes == null) {
                attributes = new HashMap<String, Attribute>();
            }
            mapToUse = attributes;
        }
        mapToUse.put(name, value);
    }

    /** {@inheritDoc} */
    public void clear() {
        template = null;
        preparer = null;
        roles = null;
        attributes.clear();
        cascadedAttributes.clear();
    }

    /**
     * Copies a BasicAttributeContext in an easier way.
     *
     * @param context The context to copy.
     */
    private void copyBasicAttributeContext(BasicAttributeContext context) {
        template = context.template;
        Set<String> roles = context.roles;
        if (roles != null && !roles.isEmpty()) {
            this.roles = new HashSet<String>(roles);
        }
        preparer = context.preparer;
        if (context.attributes != null && !context.attributes.isEmpty()) {
            attributes = new HashMap<String, Attribute>(context.attributes);
        }
        copyCascadedAttributes(context);
    }

    /**
     * Copies the cascaded attributes to the current context.
     *
     * @param context The context to copy from.
     */
    private void copyCascadedAttributes(BasicAttributeContext context) {
        if (context.cascadedAttributes != null
                && !context.cascadedAttributes.isEmpty()) {
            cascadedAttributes = new HashMap<String, Attribute>(
                    context.cascadedAttributes);
        }
    }

    /**
     * Adds missing attributes to the destination map.
     *
     * @param source The source attribute map.
     * @param destination The destination attribute map.
     * @return The destination attribute map if not null, a new one otherwise.
     */
    private Map<String, Attribute> addMissingAttributes(Map<String, Attribute> source,
            Map<String, Attribute> destination) {
        if (source != null && !source.isEmpty()) {
            if (destination == null) {
                destination = new HashMap<String, Attribute>();
            }
            for (Map.Entry<String, Attribute> entry : source.entrySet()) {
                String key = entry.getKey();
                Attribute destAttribute = destination.get(key);
                if (destAttribute == null) {
                    destination.put(key, entry.getValue());
                } else if (destAttribute instanceof ListAttribute
                        && entry.getValue() instanceof ListAttribute
                        && ((ListAttribute) destAttribute).isInherit()) {
                    ((ListAttribute) destAttribute)
                            .inherit((ListAttribute) entry.getValue());
                }
            }
        }

        return destination;
    }
}