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

package dev.nokee.model.internal;

import dev.nokee.internal.Factory;
import org.gradle.api.plugins.ExtensionAware;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ModelElementSupport implements ModelElement {
	private static final ThreadLocal<ModelElement> nextIdentity = new ThreadLocal<>();
	private final ModelElement delegate;

	protected ModelElementSupport() {
		assert getClass().getSimpleName().endsWith("_Decorated") : "must be instantiated via Gradle's ObjectFactory";
		assert nextIdentity.get() != null : "must have identity, user ModelObjectSupport.newInstance(...)";
		this.delegate = nextIdentity.get();
	}

	public String getName() {
		return delegate.getName();
	}

	// TODO: Get identifier should mark the element as single identifier else there is no way of knowing which overlapping you want

	// Passing a ModelElement because the instance can reference multiple element (overlapping)
	public static <T> T newInstance(ModelElement identifier, Factory<T> factory) {
		@Nullable final ModelElement currentIdentity = nextIdentity.get();
		try {
			nextIdentity.set(identifier);
			return factory.create();
		} finally {
			nextIdentity.set(currentIdentity);
		}
	}

	public static Optional<ModelElement> safeAsModelElement(Object object) {
		if (object instanceof ModelElement) {
			return Optional.of((ModelElement) object);
		} else if (object instanceof ExtensionAware) {
			ModelElement id = ((ExtensionAware) object).getExtensions().findByType(ModelElement.class);
			if (id != null) {
				return Optional.of(id);
			}
		}

		return Optional.empty();
	}

	public static ModelElement asModelElement(Object object) {
		return safeAsModelElement(object).orElseThrow(() -> new UnsupportedOperationException("object is not a ModelElement"));
	}

	public static <T> void injectIdentifier(T target, ModelElement identity) {
		if (target instanceof ExtensionAware) {
			((ExtensionAware) target).getExtensions().add(ModelElement.class, "$identity", identity);
		}
	}
}
