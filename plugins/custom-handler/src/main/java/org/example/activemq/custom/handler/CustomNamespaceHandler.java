/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example.activemq.custom.handler;

import java.net.URL;
import java.util.Set;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableValueMetadata;
import org.example.activemq.custom.plugin.MetadataBrokerPlugin;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.ValueMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class CustomNamespaceHandler implements org.apache.aries.blueprint.NamespaceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomNamespaceHandler.class);

    // Namespaces supported
    public static final String CUSTOM_NAMESPACE_V1_0 = "http://activemq.example.org/custom/handler/v1.0";

    // Attributes and Elements supported
    public static final String BEAN = "bean";
    public static final String METADATA_PLUGIN = "metadataPlugin";
    public static final String ID_ATTRIBUTE = "id";
    public static final String SEND_TO_ATTRIBUTE = "sendTo";
    public static final String STORE_PROPERTY = "store";

    private int idCounter = 0;

	@Override
	public ComponentMetadata decorate(Node node, ComponentMetadata component, ParserContext context) {
        LOGGER.debug("Decorate node {{}}{}", node.getNamespaceURI(), node.getLocalName());

        if (node instanceof Element && nodeNameEquals(node, BEAN)) {
            return context.parseElement(BeanMetadata.class, component, (Element)node);
        } else {
            throw new ComponentDefinitionException("Unsupported node: " + node.getNodeName());
        }
	}

	@Override
    @SuppressWarnings("rawtypes")
	public Set<Class> getManagedClasses() {
		// no compatibility checks
		return null;
	}

	@Override
	public URL getSchemaLocation(String namespace) {
        if (CUSTOM_NAMESPACE_V1_0.equals(namespace)) {
            return getClass().getResource("custom-handler-1.0.xsd");
          }
          return null;
	}

	@Override
	public Metadata parse(Element element, ParserContext context) {
        LOGGER.debug("Parsing element {{}}{}", element.getNamespaceURI(), element.getLocalName());

        if (nodeNameEquals(element, BEAN)) {
            return context.parseElement(BeanMetadata.class, context.getEnclosingComponent(), element);
        } else if (nodeNameEquals(element, METADATA_PLUGIN)) {
            return parseMetadataPlugin(element, context);
        } else {
            throw new ComponentDefinitionException("Unsupported element: " + element.getNodeName());
        }
	}

    private static boolean nodeNameEquals(Node node, String name) {
        return (name.equals(node.getNodeName()) || name.equals(node.getLocalName()));
    }

    private Metadata parseMetadataPlugin(Element element, ParserContext context) {
        MutableBeanMetadata metadata = context.createMetadata(MutableBeanMetadata.class);
        metadata.setProcessor(true);
        metadata.setActivation(MutableBeanMetadata.ACTIVATION_EAGER);
        metadata.setRuntimeClass(MetadataBrokerPlugin.class);
        metadata.addProperty(STORE_PROPERTY, createValue(context, element.getAttribute(SEND_TO_ATTRIBUTE)));
        return metadata;
    }

    private static ValueMetadata createValue(ParserContext context, String value) {
        return createValue(context, value, null);
    }

    private static ValueMetadata createValue(ParserContext context, String value, String type) {
        MutableValueMetadata m = context.createMetadata(MutableValueMetadata.class);
        m.setStringValue(value);
        m.setType(type);
        return m;
    }

}

