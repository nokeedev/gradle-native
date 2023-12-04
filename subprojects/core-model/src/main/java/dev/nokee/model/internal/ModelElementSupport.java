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
import dev.nokee.model.internal.decorators.ModelObjectIdentifierSupport;
import dev.nokee.model.internal.type.ModelTypeUtils;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class ModelElementSupport implements ModelElement, ExtensionAware {
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

	@Override
	public ModelObjectIdentifier getIdentifier() {
		return delegate.getIdentifier();
	}

	@Override
	public Stream<ModelElement> getParents() {
		return delegate.getParents();
	}

	@Override
	public boolean instanceOf(Class<?> type) {
		return delegate.instanceOf(type);
	}

	@Override
	public <T> Provider<T> safeAs(Class<T> type) {
		return delegate.safeAs(type);
	}

	// TODO: Get identifier should mark the element as single identifier else there is no way of knowing which overlapping you want

	// Passing a ModelElement because the instance can reference multiple element (overlapping)
	public static <T> T newInstance(ModelElement identifier, Factory<T> factory) {
		@Nullable final ModelElement currentIdentity = nextIdentity.get();
		try {
			nextIdentity.set(identifier);
			return ModelObjectIdentifierSupport.newInstance(identifier.getIdentifier(), factory);
		} finally {
			nextIdentity.set(currentIdentity);
		}
	}

	public static Optional<ModelElement> safeAsModelElement(Object object) {
		if (object instanceof ModelElement) {
			return Optional.of((ModelElement) object);
		} else if (object instanceof ExtensionAware) {
			return Optional.ofNullable(((ExtensionAware) object).getExtensions().findByType(ModelElement.class));
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

	// TODO: Should probably remove this API
	public static ModelObjectIdentifier nextIdentifier() {
		return nextIdentity.get().getIdentifier();
	}

	protected String getTypeName() {
		// TODO: We should use public type by default or maybe annotate the class
		//   Passing the public type directly via constructor makes things a bit more complicated
		return ModelTypeUtils.toUndecoratedType(getClass()).getSimpleName();
	}

	public String getDisplayName() {
		// TODO: Add support to include project path in the name
		return getTypeName() + " ':" + getName() + "'";
	}

	@Override
	public final String toString() {
		return getDisplayName();
	}
}
