/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.model.internal.core;

import dev.nokee.model.internal.ModelElementFactory;
import lombok.val;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.model.internal.tags.ModelTags.typeOf;

public final class ModelProperties {
	private ModelProperties() {}

	public static ModelProperty<?> getProperty(Object self, String name) {
		return ModelNodes.of(self).get(DescendantNodes.class).getDirectDescendants().stream().filter(it -> it.hasComponent(typeOf(ModelPropertyTag.class))).filter(it -> it.get(ModelPathComponent.class).get().getName().equals(name)).findFirst().map(ModelProperties::newElement).orElseThrow(() -> new IllegalArgumentException("No property of name '" + name + "'. Existing properties of '" + ModelNodes.of(self).get(ModelPathComponent.class).get() + "' are: " + ModelNodes.of(self).get(DescendantNodes.class).getDirectDescendants().stream().filter(it -> it.hasComponent(typeOf(ModelPropertyTag.class))).map(it -> it.get(ModelPathComponent.class).get().getName()).collect(Collectors.joining(", "))));
	}

	public static Optional<ModelProperty<?>> findProperty(Object self, String name) {
		return ModelNodes.of(self).get(DescendantNodes.class).getDirectDescendants().stream().filter(it -> it.hasComponent(typeOf(ModelPropertyTag.class))).filter(it -> it.get(ModelPathComponent.class).get().getName().equals(name)).findFirst().map(ModelProperties::newElement);
	}

	public static boolean hasProperty(Object self, String name) {
		return findProperty(self, name).isPresent();
	}

	public static Stream<ModelProperty<?>> getProperties(Object self) {
		val result = ModelNodes.of(self).get(DescendantNodes.class).getDirectDescendants();
		return result.stream().filter(it -> it.hasComponent(typeOf(ModelPropertyTag.class))).map(ModelProperties::newElement);
	}

	private static ModelProperty<?> newElement(ModelNode entity) {
		return entity.get(ModelElementFactory.class).createProperty(entity);
	}

	public static <T extends ModelComponent & LinkedEntity> ModelProperty<?> of(Object target, Class<T> type) {
		val entity = ModelNodes.of(target);
		return entity.get(ModelElementFactory.class).createProperty(entity.get(type).get());
	}

	@SuppressWarnings("unchecked")
	public static <T> void add(ModelNode self, T element) {
		val property = self.get(GradlePropertyComponent.class).get();
		if (property instanceof ConfigurableFileCollection) {
			((ConfigurableFileCollection) property).from(element);
		} else if (property instanceof SetProperty) {
			((SetProperty<T>) property).add(element);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> void add(ModelNode self, Provider<T> element) {
		val property = self.get(GradlePropertyComponent.class).get();
		if (property instanceof ConfigurableFileCollection) {
			((ConfigurableFileCollection) property).from(element);
		} else if (property instanceof SetProperty) {
			((SetProperty<T>) property).add(element);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> void set(ModelNode self, T value) {
		val property = self.get(GradlePropertyComponent.class).get();
		if (property instanceof SetProperty) {
			assert value instanceof Iterable;
			((SetProperty<Object>) property).set((Iterable<Object>) value);
		} else if (property instanceof ListProperty) {
			assert value instanceof Iterable;
			((ListProperty<Object>) property).set((Iterable<Object>) value);
		} else if (property instanceof Property) {
			((Property<T>) property).set(value);
		}
	}

	public static void clear(ModelNode self) {
		set(self, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> Provider<T> valueOf(ModelNode self) {
		return (Provider<T>) self.get(GradlePropertyComponent.class).get();
	}
}
