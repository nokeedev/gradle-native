/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.scripts;

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtensionsSchema;
import org.gradle.api.reflect.TypeOf;

import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * Programmatically import types in a project build script scope.
 * The "default imports" Gradle feature is internal and isn't available to plugin authors.
 * To workaround this limitation, we fake a default import by registering the type as an extension.
 * We can then access the type via the normal delegation process for Groovy DSL and Kotlin DSL.
 */
public final class DefaultImporter {
	private final ExtensionContainer extensionContainer;

	private DefaultImporter(ExtensionContainer extensionContainer) {
		this.extensionContainer = extensionContainer;
	}

	/**
	 * Returns the default importer for the specified {@link Project}.
	 *
	 * @param project  the project to import types
	 * @return the default importer for the specified project, never null.
	 */
	public static DefaultImporter forProject(Project project) {
		return new DefaultImporter(project.getExtensions());
	}

	/**
	 * Imports the specified type in the current project.
	 *
	 * @param type  the type to import
	 * @param <T>  the type to import
	 * @return this default importer, never null.
	 */
	public <T> DefaultImporter defaultImport(Class<T> type) {
		val defaultImportExtensionType = new TypeOf<Class<T>>() {};
		try {
			extensionContainer.add(defaultImportExtensionType, type.getSimpleName(), type);
		} catch (Throwable ex) {
			if (!hasExtension(ofName(type.getSimpleName()).and(ofType(defaultImportExtensionType)))) {
				throw new IllegalArgumentException(String.format("Could not default import type '%s'.", type.getCanonicalName()));
			}
		}
		return this;
	}

	private Predicate<ExtensionsSchema.ExtensionSchema> ofName(String typeSimpleName) {
		return extensionSchema -> extensionSchema.getName().equals(typeSimpleName);
	}

	private Predicate<ExtensionsSchema.ExtensionSchema> ofType(TypeOf<?> publicType) {
		return extensionSchema -> extensionSchema.getPublicType().equals(publicType);
	}

	private boolean hasExtension(Predicate<ExtensionsSchema.ExtensionSchema> predicate) {
		return StreamSupport.stream(extensionContainer.getExtensionsSchema().getElements().spliterator(), false).anyMatch(predicate);
	}
}
