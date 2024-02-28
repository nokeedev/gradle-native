/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.internal.services;

import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.reflect.TypeOf;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public final class ExtensionBackedServiceLookup implements ServiceLookup {
	private final ExtensionContainer extensionContainer;

	public ExtensionBackedServiceLookup(ExtensionContainer extensionContainer) {
		this.extensionContainer = extensionContainer;
	}

	@Nullable
	@Override
	public Object find(Type serviceType) {
		return extensionContainer.findByType(TypeOf.typeOf(serviceType));
	}
}
