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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;

import java.lang.reflect.Type;
import java.util.Objects;

import static dev.nokee.model.internal.type.ModelTypeUtils.toUndecoratedType;

@SuppressWarnings("unchecked")
public abstract class ModelComponentType<T> {
	public static final LoadingCache<Type, Bits> assignedComponentTypes = CacheBuilder.newBuilder()
		.build(new CacheLoader<Type, Bits>() {
			public Bits load(Type key) {
				return Bits.nthBit(typeIndex++);
			}
		});
	public static final LoadingCache<Type, Bits> assignedComponentTypeFamilies = CacheBuilder.newBuilder()
		.build(new CacheLoader<Type, Bits>() {
			public Bits load(Type key) {
				val visitor = new TypeVisitor();
				((ModelType<Object>) ModelType.of(key)).walkTypeHierarchy(visitor);
				return visitor.result;
			}
		});
	private static int typeIndex = 0;

	private static final class TypeVisitor implements ModelType.Visitor<Object> {
		private Bits result = Bits.empty();

		@Override
		public void visitType(ModelType<? super Object> type) {
			result = result.or(assignedComponentTypes.getUnchecked(type.getType()));
		}
	}

	public abstract boolean isSupertypeOf(ModelComponentType<?> componentType);

	public abstract Bits familyBits();

	@SuppressWarnings("unchecked")
	public static <T> ModelComponentType<? super T> ofInstance(T component) {
		Objects.requireNonNull(component);
		if (component instanceof ModelProjection) {
			return (ModelComponentType<? super T>) projectionOf(((ModelProjection) component).getType().getRawType());
		} else {
			return (ModelComponentType<? super T>) componentOf(toUndecoratedType(component.getClass()));
		}
	}

	public static <T> ModelComponentType<T> componentOf(Class<T> type) {
		Objects.requireNonNull(type);
		return new ComponentType<>(type);
	}

	public static <T> ModelComponentType<ModelProjection> projectionOf(Class<T> type) {
		Objects.requireNonNull(type);
		return new ProjectionType<>(type);
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class ComponentType<T> extends ModelComponentType<T> {
		Class<T> value;

		@Override
		public boolean isSupertypeOf(ModelComponentType<?> componentType) {
			if (componentType instanceof ComponentType) {
				return value.isAssignableFrom(((ComponentType<?>) componentType).getValue());
			}
			return false;
		}

		@Override
		public Bits familyBits() {
			return assignedComponentTypeFamilies.getUnchecked(value);
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class ProjectionType<T> extends ModelComponentType<ModelProjection> {
		Class<T> value;

		@Override
		public boolean isSupertypeOf(ModelComponentType<?> componentType) {
			if (componentType instanceof ProjectionType) {
				return value.isAssignableFrom(((ProjectionType<?>) componentType).getValue());
			}
			return false;
		}

		@Override
		public Bits familyBits() {
			return assignedComponentTypeFamilies.getUnchecked(value).or(assignedComponentTypes.getUnchecked(ModelProjection.class));
		}
	}
}
