/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskTypeComponent;
import lombok.val;
import org.gradle.api.Task;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class DomainObjectEntities {
	private static final Consumer<Builder> DO_NOTHING = __ -> {};

	public static <T> ModelRegistration newEntity(ModelObjectIdentifier identifier, Class<T> type) {
		return newEntity(identifier, type, DO_NOTHING);
	}

	public static <T> ModelRegistration newEntity(ModelObjectIdentifier identifier, Class<T> type, Consumer<? super Builder> builderConsumer) {
		return newEntity(identifier.getName(), type, builder -> {
			builder.withComponent(new IdentifierComponent(identifier));
			builderConsumer.accept(builder);
		});
	}

	// TODO: Should bound type to something that is common between domain object and model
	public static <T> ModelRegistration newEntity(String elementName, Class<T> type) {
		return newEntity(elementName, type, DO_NOTHING);
	}

	public static <T> ModelRegistration newEntity(String elementName, Class<T> type, Consumer<? super Builder> builderConsumer) {
		if (Task.class.isAssignableFrom(type)) {
			return newEntity(TaskName.of(elementName), type, builderConsumer);
		} else {
			return newEntity(ElementName.of(elementName), type, builderConsumer);
		}
	}

	public static <T> ModelRegistration newEntity(ElementName elementName, Class<T> type, Consumer<? super Builder> builderConsumer) {
		val result = ModelRegistration.builder();
		val builder = new Builder(result);
		builderConsumer.accept(builder);

		result.withComponent(new ElementNameComponent(elementName));
		result.mergeFrom(entityOf(type));
		return result.build();
	}

	public static <T> ModelRegistration tagsOf(Class<T> type) {
		val builder = ModelRegistration.builder();
		val tagAnnotations = new LinkedHashSet<Tag>();
		findAnnotations(type, Tag.class, tagAnnotations);
		for (Tag tags : tagAnnotations) {
			for (Class<? extends ModelTag> tag : tags.value()) {
				builder.withComponentTag(tag);
			}
		}
		return builder.build();
	}

	public static <T> ModelRegistration entityOf(Class<T> type) {
		val builder = ModelRegistration.builder();
		val tagAnnotations = new LinkedHashSet<Tag>();
		findAnnotations(type, Tag.class, tagAnnotations);
		for (Tag tags : tagAnnotations) {
			for (Class<? extends ModelTag> tag : tags.value()) {
				builder.withComponentTag(tag);
			}
		}

		if (Task.class.isAssignableFrom(type)) {
			builder.withComponentTag(IsTask.class);
			builder.withComponentTag(ConfigurableTag.class);

			@SuppressWarnings("unchecked")
			val taskType = (Class<Task>) type;
			builder.withComponent(new TaskTypeComponent(taskType));
		} else {
			// It's important to add the projection at the end because there is a tiny bug with ModelProjection specifically.
			//   The issue is the projection should really be ModelBufferElement but instead are "multi-component".
			//   Multi-components are a feature we don't really support which is having multiple components of the same type.
			//   The executor has some support specifically for ModelProjection.
			//   However, it lacks support for executing a rule for multiple ModelProjection after the fact,
			//   i.e. once the other inputs, say ConfigurableTag, matches.
			builder.withComponent(new MainProjectionComponent(type));
		}
		return builder.build();
	}

	private static <A extends Annotation> void findAnnotations(AnnotatedElement element, Class<A> annotationType, Set<A> found) {
		if (element instanceof Class) {
			Class<?> clazz = (Class<?>) element;

			// Recurse first in order to support top-down semantics for inherited, repeatable annotations.
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null && superclass != Object.class) {
				findAnnotations(superclass, annotationType, found);
			}

			// Search on interfaces
			for (Class<?> ifc : clazz.getInterfaces()) {
				if (ifc != Annotation.class) {
					findAnnotations(ifc, annotationType, found);
				}
			}
		}

		// Find annotations that are directly present or meta-present on directly present annotations.
		found.addAll(Arrays.asList(element.getDeclaredAnnotationsByType(annotationType)));

		// Find annotations that are indirectly present or meta-present on indirectly present annotations.
		found.addAll(Arrays.asList(element.getAnnotationsByType(annotationType)));
	}

	public static final class Builder {
		private final ModelRegistration.Builder delegate;

		private Builder(ModelRegistration.Builder delegate) {
			this.delegate = delegate;
		}

		public Builder ownedBy(ModelNode owner) {
			delegate.withComponent(new ParentComponent(owner));
			return this;
		}

		public Builder displayName(String displayName) {
			delegate.withComponent(new DisplayNameComponent(displayName));
			return this;
		}

		public <T extends ModelTag> Builder withTag(Class<T> tag) {
			delegate.withComponentTag(tag);
			return this;
		}

		public Builder withComponent(ModelComponent component) {
			delegate.withComponent(component);
			return this;
		}

		public ModelRegistration build() {
			return delegate.build();
		}
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Repeatable(Tags.class)
	public @interface Tag {
		Class<? extends ModelTag>[] value();
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	public @interface Tags {
		Tag[] value();
	}
}
