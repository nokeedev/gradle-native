/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.dockit.dsl.docbook;

import dev.gradleplugins.dockit.dsl.docbook.model.*;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import dev.gradleplugins.dockit.dsl.source.model.MethodMetaData;
import dev.gradleplugins.dockit.dsl.source.model.PropertyMetaData;
import dev.gradleplugins.dockit.dsl.source.model.TypeMetaData;
import groovy.lang.Closure;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ClassDocExtensionsBuilder {
	private final DslDocModel model;
	private final GenerationListener listener;
	private final ClassExtensionMetaData classExtensionMetaData;
	private final ClassMetaData classMetaData;

	/**
	 * Builds the extension meta-data for the given class.
	 */
	public void build(ClassDoc.Builder builder) {
		Map<String, ClassExtensionDoc> plugins = new HashMap<String, ClassExtensionDoc>();
		for (MixinMetaData mixin : classExtensionMetaData.getMixinClasses()) {
			String pluginId = mixin.getPluginId();
			ClassExtensionDoc classExtensionDoc = plugins.get(pluginId);
			if (classExtensionDoc == null) {
				classExtensionDoc = new ClassExtensionDoc(pluginId, classMetaData);
				plugins.put(pluginId, classExtensionDoc);
			}
			classExtensionDoc.getMixinClasses().add(model.getClassDoc(mixin.getMixinClass()));
		}
		for (ExtensionMetaData extension : classExtensionMetaData.getExtensionClasses()) {
			String pluginId = extension.getPluginId();
			ClassExtensionDoc classExtensionDoc = plugins.get(pluginId);
			if (classExtensionDoc == null) {
				classExtensionDoc = new ClassExtensionDoc(pluginId, classMetaData);
				plugins.put(pluginId, classExtensionDoc);
			}
			classExtensionDoc.getExtensionClasses().put(extension.getExtensionId(), model.getClassDoc(extension.getExtensionClass()));
		}
		for (ClassExtensionDoc extensionDoc : plugins.values()) {
			build(extensionDoc);
			builder.withClassExtension(extensionDoc);
		}
	}

	@SneakyThrows
	private void build(ClassExtensionDoc extensionDoc) {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		DocLinkBuilder docLinkBuilder = new DocLinkBuilder(model);
		for (Map.Entry<String, ClassDoc> entry : extensionDoc.getExtensionClasses().entrySet()) {
			String id = entry.getKey();
			ClassDoc type = entry.getValue();
			PropertyMetaData propertyMetaData = new PropertyMetaData(id, extensionDoc.getTargetClassMetaData());
			propertyMetaData.setType(new TypeMetaData(type.getName()));

			Element para = doc.createElement("para");
			para.appendChild(doc.createTextNode("The "));
			para.appendChild(docLinkBuilder.link(propertyMetaData.getType(), listener).getDocbook());
			para.appendChild(doc.createTextNode(String.format(" added by the %s plugin.", extensionDoc.getPluginId())));
			DocComment commentProperty = new JavadocConverter.DocCommentImpl(Collections.singletonList(para));

			PropertyDoc propertyDoc = new PropertyDoc(classMetaData, propertyMetaData, commentProperty, Collections.<ExtraAttributeDoc>emptyList());
			extensionDoc.getExtraProperties().add(propertyDoc);

			para = doc.createElement("para");
			para.appendChild(doc.createTextNode("Configures the "));
			para.appendChild(docLinkBuilder.link(propertyMetaData.getType(), listener).getDocbook());
			para.appendChild(doc.createTextNode(String.format(" added by the %s plugin.", extensionDoc.getPluginId())));
			DocComment commentMethod = new JavadocConverter.DocCommentImpl(Collections.singletonList(para));

			MethodMetaData methodMetaData = new MethodMetaData(id, extensionDoc.getTargetClassMetaData());
			methodMetaData.addParameter("configClosure", new TypeMetaData(Closure.class.getName()));
			MethodDoc methodDoc = new MethodDoc(classMetaData, methodMetaData, commentMethod);
			extensionDoc.getExtraBlocks().add(new BlockDoc(methodDoc, propertyDoc, propertyMetaData.getType(), false));
		}
	}
}
