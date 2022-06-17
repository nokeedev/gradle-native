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

import dev.gradleplugins.dockit.dsl.docbook.model.ClassDoc;
import dev.gradleplugins.dockit.dsl.docbook.model.ClassExtensionMetaData;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;

public class ClassDocBuilder {
	private final GenerationListener listener = new DefaultGenerationListener();
	private final ClassDocPropertiesBuilder propertiesBuilder;
	private final ClassDocMethodsBuilder methodsBuilder;
	private final ClassDocExtensionsBuilder extensionsBuilder;
	private final ClassMetaData classMetaData;

	public ClassDocBuilder(DslDocModel model, JavadocConverter javadocConverter, ClassMetaData classMetaData, DslDocModel.ClassFile classFile, ClassExtensionMetaData classExtensionMetaData) {
		this.classMetaData = classMetaData;
		propertiesBuilder = new ClassDocPropertiesBuilder(javadocConverter, listener, classFile, model, classMetaData);
		methodsBuilder = new ClassDocMethodsBuilder(javadocConverter, listener, classFile, classMetaData, model);
		extensionsBuilder = new ClassDocExtensionsBuilder(model, listener, classExtensionMetaData, classMetaData);
	}

	void build(ClassDoc.Builder classDoc) {
		listener.start(String.format("class %s", classMetaData.getClassName()));
		try {
			propertiesBuilder.build(classDoc);
			methodsBuilder.build(classDoc);
			extensionsBuilder.build(classDoc);
		} finally {
			listener.finish();
		}
	}
}
