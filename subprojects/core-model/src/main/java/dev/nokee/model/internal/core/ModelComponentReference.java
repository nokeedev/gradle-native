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

import lombok.EqualsAndHashCode;

public abstract class ModelComponentReference<T extends ModelComponent> {
	public abstract T get(ModelNode entity);

	public abstract Bits componentBits();

	public abstract ModelComponentType<T> getType();

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

		@Override
		public ModelComponentType<T> getType() {
			return componentType;
		}
	}
}
