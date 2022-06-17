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
import dev.gradleplugins.dockit.dsl.docbook.model.*;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import dev.gradleplugins.dockit.dsl.source.model.MethodMetaData;
import dev.gradleplugins.dockit.dsl.source.model.PropertyMetaData;
import dev.gradleplugins.dockit.dsl.source.model.TypeMetaData;
import groovy.lang.Closure;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class ClassDocMethodsBuilder {
	private final JavadocConverter javadocConverter;
	private final GenerationListener listener;
	private final DslDocModel.ClassFile classFile;
	private final ClassMetaData classMetaData;
	private final DslDocModel model;

	/**
	 * Builds the methods and script blocks of the given class. Assumes properties have already been built.
	 */
	public void build(ClassDoc.Builder builder) {
		Set<String> signatures = new HashSet<String>();

		for (String methodName : classFile.getMethods()) {
			Collection<MethodMetaData> methods = classMetaData.findDeclaredMethods(methodName);
			if (methods.isEmpty()) {
				throw new RuntimeException(String.format("No metadata for method '%s.%s()'. Available methods: %s", classMetaData.getClassName(), methodName, classMetaData.getDeclaredMethodNames()));
			}
			for (MethodMetaData method : methods) {
				DocComment docComment = javadocConverter.parse(method, listener);
				MethodDoc methodDoc = new MethodDoc(classMetaData, method, docComment);
				// Closure specific type should be invisible (weaved into the bytecode).
				if (methodDoc.getDescription() == null && methodDoc.getMetaData().getParameters().stream().noneMatch(it -> it.getType().equals(new TypeMetaData("groovy.lang.Closure")))) {
					throw new RuntimeException(String.format("Docbook content for '%s %s' does not contain a description paragraph.", classMetaData.getClassName(), method.getSignature()));
				}
				PropertyMetaData property = classMetaData.findProperty(methodName);
				boolean multiValued = false;
				if (property != null && method.getParameters().size() == 1 && method.getParameters().get(0).getType().getSignature().equals(Closure.class.getName())) {
					TypeMetaData type = property.getType();
					if (type.getName().equals("java.util.List")
						|| type.getName().equals("java.util.Collection")
						|| type.getName().equals("java.util.Set")
						|| type.getName().equals("java.util.Iterable")) {
						type = type.getTypeArgs().get(0);
						multiValued = true;
					}
					// TODO: Careful, here the property is recreated and is not aligned with original
					builder.withClassBlock(new BlockDoc(methodDoc, new PropertyDoc(classMetaData, property, javadocConverter.parse(property, listener), ImmutableList.of()), type, multiValued));
				} else {
					builder.withClassMethod(methodDoc);
					signatures.add(method.getOverrideSignature());
				}
			}
		}

		List<ClassDoc> superTypes = ClassDocSuperTypes.of(classMetaData, model).getSuperTypes();
		for (ClassDoc supertype : superTypes) {
			for (MethodDoc method : supertype.getClassMethods()) {
				if (signatures.add(method.getMetaData().getOverrideSignature())) {
					builder.withClassMethod(method);
				}
			}
		}
	}
}
