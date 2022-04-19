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

import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

public abstract class ModelComponentReference<T extends ModelComponent> {
	public abstract T get(ModelNode entity);

	public abstract Bits componentBits();

	public static <T extends ModelComponent> ModelComponentReference<T> of(Class<T> componentType) {
		return ofInstance(ModelComponentType.componentOf(componentType));
	}

	public static <T extends ModelComponent> ModelComponentReference<T> ofInstance(ModelComponentType<T> componentType) {
		return new OfInstanceReference<>(componentType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class OfInstanceReference<T extends ModelComponent> extends ModelComponentReference<T> implements ModelComponentReferenceInternal {
		private final ModelComponentType<T> componentType;

		private OfInstanceReference(ModelComponentType<T> componentType) {
			this.componentType = componentType;
		}

		@Override
		public boolean isSatisfiedBy(ModelComponentTypes componentTypes) {
			return componentTypes.contains(componentType);
		}

		@Override
		public boolean isSatisfiedBy(ModelComponentType<?> otherComponentType) {
			return componentType.equals(otherComponentType);
		}

		@Override
		public T get(ModelNode entity) {
			return entity.getComponent(componentType);
		}

		@Override
		public Bits componentBits() {
			return componentType.bits();
		}
	}

	public static <T extends ModelComponent> ModelComponentReference<T> ofAny(ModelComponentType<T> componentType) {
		return new OfAnyReference<>(componentType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class OfAnyReference<T extends ModelComponent> extends ModelComponentReference<T> implements ModelComponentReferenceInternal {
		private final ModelComponentType<T> componentType;

		private OfAnyReference(ModelComponentType<T> componentType) {
			this.componentType = componentType;
		}

		@Override
		public boolean isSatisfiedBy(ModelComponentTypes componentTypes) {
			return componentTypes.anyMatch(componentType::isSupertypeOf);
		}

		@Override
		public boolean isSatisfiedBy(ModelComponentType<?> otherComponentType) {
			return componentType.isSupertypeOf(otherComponentType);
		}

		@Override
		@SuppressWarnings("unchecked")
		public T get(ModelNode entity) {
			if (componentType.equals(ModelComponentType.componentOf(ModelProjection.class))) {
				return (T) entity.getComponents().filter(it -> componentType.isSupertypeOf(ModelComponentType.ofInstance(it))).reduce((a, b) -> b).orElseThrow(RuntimeException::new);
			} else {
				return (T) entity.getComponents().filter(it -> componentType.isSupertypeOf(ModelComponentType.ofInstance(it))).findFirst().orElseThrow(RuntimeException::new);
			}
		}

		@Override
		public Bits componentBits() {
			return componentType.bits();
		}
	}

	public static <T> ModelComponentReference<ModelProjection> ofProjection(Class<T> projectionType) {
		return new OfProjectionReference<>(projectionType);
	}

	public static <T> ModelComponentReference<ModelProjection> ofProjection(ModelType<T> projectionType) {
		return new OfProjectionReference<>(projectionType.getConcreteType());
	}

	private static final class OfProjectionReference<T> extends ModelComponentReference<ModelProjection> implements ModelComponentReferenceInternal {
		private final ModelComponentReference<ModelProjection> delegate;
		private final Class<T> projectionType;

		public OfProjectionReference(Class<T> projectionType) {
			this.delegate = new OfAnyReference<>(ModelComponentType.projectionOf(projectionType));
			this.projectionType = projectionType;
		}

		@Override
		public ModelProjection get(ModelNode entity) {
			return delegate.get(entity);
		}

		@Override
		public Bits componentBits() {
			return delegate.componentBits();
		}

		@Override
		public boolean isSatisfiedBy(ModelComponentTypes componentTypes) {
			return ((ModelComponentReferenceInternal) delegate).isSatisfiedBy(componentTypes);
		}

		@Override
		public boolean isSatisfiedBy(ModelComponentType<?> otherComponentType) {
			return ((ModelComponentReferenceInternal) delegate).isSatisfiedBy(otherComponentType);
		}
	}
}
