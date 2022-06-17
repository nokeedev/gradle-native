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

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.dockit.dsl.docbook.model.ClassDoc;
import dev.gradleplugins.dockit.dsl.docbook.model.ClassDocSuperTypes;
import dev.gradleplugins.dockit.dsl.docbook.model.ExtraAttributeDoc;
import dev.gradleplugins.dockit.dsl.docbook.model.PropertyDoc;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import dev.gradleplugins.dockit.dsl.source.model.PropertyMetaData;
import lombok.AllArgsConstructor;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@AllArgsConstructor
public class ClassDocPropertiesBuilder {
	private static final Logger LOG = Logging.getLogger(ClassDocPropertiesBuilder.class);
	private final JavadocConverter javadocConverter;
	private final GenerationListener listener;
	private final DslDocModel.ClassFile classFile;
	private final DslDocModel model;
	private final ClassMetaData classMetaData;

	/**
	 * Builds the properties of the given class
	 */
	void build(ClassDoc.Builder builder) {
		List<ClassDoc> superTypes = ClassDocSuperTypes.of(classMetaData, model).getSuperTypes();

		//adding the properties from the super class onto the inheriting class
		Map<String, PropertyDoc> props = new TreeMap<>();
		for (ClassDoc superType : superTypes) {
			LOG.info("Getting properties for {}", superType.getName());
			for (PropertyDoc propertyDoc : superType.getClassProperties()) {
				Map<String, ExtraAttributeDoc> additionalValues = new LinkedHashMap<>();
				for (ExtraAttributeDoc attributeDoc : propertyDoc.getAdditionalValues()) {
					String key = attributeDoc.getKey();
//                    if (inheritedValueTitleMapping.get(key) != null) {
//                        ExtraAttributeDoc newAttribute = new ExtraAttributeDoc(inheritedValueTitleMapping.get(key), attributeDoc.getValueCell());
//                        additionalValues.put(newAttribute.getKey(), newAttribute);
//                    } else {
					additionalValues.put(key, attributeDoc);
//                    }
				}

				props.put(propertyDoc.getName(), propertyDoc.forClass(classMetaData, additionalValues.values()));
			}
		}

		for (String propName : classFile.getProperties()) {
			PropertyMetaData property = classMetaData.findProperty(propName);
			if (property == null) {
				throw new RuntimeException(String.format("No metadata for property '%s.%s'. Available properties: %s", classMetaData.getClassName(), propName, classMetaData.getPropertyNames()));
			}

			Map<String, ExtraAttributeDoc> additionalValues = new LinkedHashMap<>();

			if (!superTypes.isEmpty()) {
				PropertyDoc overriddenProp = props.get(propName);
				if (overriddenProp != null) {
					for (ExtraAttributeDoc attributeDoc : overriddenProp.getAdditionalValues()) {
						additionalValues.put(attributeDoc.getKey(), attributeDoc);
					}
				}
			}

//            for (int i = 1; i < header.size(); i++) {
//                if (cells.get(i).getFirstChild() == null) {
//                    continue;
//                }
//                ExtraAttributeDoc attributeDoc = new ExtraAttributeDoc(valueTitles.get(i - 1), cells.get(i));
//                additionalValues.put(attributeDoc.getKey(), attributeDoc);
//            }
			PropertyDoc propertyDoc = new PropertyDoc(classMetaData, property, javadocConverter.parse(property, listener), ImmutableList.copyOf(additionalValues.values()));
			// TODO: The following is probably needed
//            if (propertyDoc.getDescription() == null) {
//                throw new RuntimeException(String.format("Docbook content for '%s.%s' does not contain a description paragraph.", classDoc.getName(), propName));
//            }

			props.put(propName, propertyDoc);
		}

		for (PropertyDoc propertyDoc : props.values()) {
			builder.withClassProperty(propertyDoc);
		}
	}
}
