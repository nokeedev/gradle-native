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

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.DefaultKnownDomainObject;
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;

public abstract class ModelComponentReference<T> {
	public abstract T get(ModelNode entity);

	public abstract Bits componentBits();

	public static <T extends ModelComponent> ModelComponentReference<T> of(Class<T> componentType) {
		return ofInstance(ModelComponentType.componentOf(componentType));
	}

	public static <T> ModelComponentReference<T> ofInstance(ModelComponentType<T> componentType) {
		return new OfInstanceReference<>(componentType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class OfInstanceReference<T> extends ModelComponentReference<T> implements ModelComponentReferenceInternal {
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

	public static <T> ModelComponentReference<T> ofAny(ModelComponentType<T> componentType) {
		return new OfAnyReference<>(componentType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class OfAnyReference<T> extends ModelComponentReference<T> implements ModelComponentReferenceInternal {
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

	public static <T> ModelProjectionReference<T> ofProjection(Class<T> projectionType) {
		return new OfProjectionReference<>(projectionType);
	}

	public static <T> ModelProjectionReference<T> ofProjection(ModelType<T> projectionType) {
		return new OfProjectionReference<>(projectionType.getConcreteType());
	}

	@EqualsAndHashCode(callSuper = false)
	public static abstract class ModelProjectionReference<T> extends ModelComponentReference<ModelProjection> {
		protected final ModelComponentReference<ModelProjection> delegate;
		private final Class<T> projectionType;

		protected ModelProjectionReference(Class<T> projectionType) {
			this.delegate = new OfAnyReference<>(ModelComponentType.projectionOf(projectionType));
			this.projectionType = projectionType;
		}

		public ModelComponentReference<NamedDomainObjectProvider<T>> asConfigurableProvider() {
			return new AsConfigurableProvider();
		}

		public ModelComponentReference<T> asDomainObject() {
			return new AsDomainObjectReference();
		}

		public ModelComponentReference<Provider<T>> asProvider() {
			return new AsProviderReference();
		}

		public ModelComponentReference<KnownDomainObject<T>> asKnownObject() {
			return new AsKnownObjectReference();
		}

		private final class AsConfigurableProvider extends ModelComponentReference<NamedDomainObjectProvider<T>> implements ModelComponentReferenceInternal {
			@Override
			public NamedDomainObjectProvider<T> get(ModelNode entity) {
				return entity.get(ModelElementFactory.class).createObject(entity, ModelType.of(projectionType)).asProvider();
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

		private final class AsDomainObjectReference extends ModelComponentReference<T> implements ModelComponentReferenceInternal {
			@Override
			public T get(ModelNode entity) {
				return ModelNodeUtils.get(entity, projectionType);
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

		private final class AsProviderReference extends ModelComponentReference<Provider<T>> implements ModelComponentReferenceInternal {
			@Override
			public Provider<T> get(ModelNode entity) {
				return entity.get(ModelElementFactory.class).createObject(entity, ModelType.of(projectionType)).asProvider();
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

		private final class AsKnownObjectReference extends ModelComponentReference<KnownDomainObject<T>> implements ModelComponentReferenceInternal {
			@Override
			public KnownDomainObject<T> get(ModelNode entity) {
				return DefaultKnownDomainObject.of(ModelType.of(projectionType), entity);
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

	private static final class OfProjectionReference<T> extends ModelProjectionReference<T> implements ModelComponentReferenceInternal {
		public OfProjectionReference(Class<T> projectionType) {
			super(projectionType);
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
