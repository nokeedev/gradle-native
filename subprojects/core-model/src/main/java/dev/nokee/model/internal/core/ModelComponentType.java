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

import com.google.common.base.Preconditions;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public abstract class ModelComponentType<T> {
	private static final ConcurrentHashMap<Type, Bits> assignedComponentTypes = new ConcurrentHashMap<>();
	public static Bits componentBits(Type type) {
		return assignedComponentTypes.computeIfAbsent(type, ModelComponentType::computeComponentBits);
	}
	private static Bits computeComponentBits(Type type) {
		return Bits.nthBit(typeIndex++);
	}

	private static final ConcurrentHashMap<Type, Bits> assignedComponentTypeFamilies = new ConcurrentHashMap<>();
	public static Bits componentFamilyBits(Type type) {
		return assignedComponentTypeFamilies.computeIfAbsent(type, ModelComponentType::computeComponentFamilyBits);
	}
	private static Bits computeComponentFamilyBits(Type type) {
		val modelType = ModelType.of(type);
		if (modelType.isSubtypeOf(ModelComponent.class)) {
			return componentBits(type);
		} else {
			val visitor = new TypeVisitor();
			((ModelType<Object>) modelType).walkTypeHierarchy(visitor);
			return visitor.result;
		}
	}
	private static int typeIndex = 0;

	private static final class TypeVisitor implements ModelType.Visitor<Object> {
		private Bits result = Bits.empty();

		@Override
		public void visitType(ModelType<? super Object> type) {
			result = result.or(componentBits(type.getType()));
			if (type.isParameterized()) {
				// This account for HasNativeCompileTask<CppCompileTask> && HasNativeCompileTask.
				//    However, it won't account for HasNativeCompileTask<? extends SourceCompile> matching HasNativeCompileTask<CppCompileTask>
				result = result.or(componentBits(type.getRawType()));
			}
		}
	}

	private static final ConcurrentHashMap<Type, ModelComponentType<?>> knownComponentTypes = new ConcurrentHashMap<>();

	public abstract boolean isSupertypeOf(ModelComponentType<?> componentType);

	public abstract Bits familyBits();

	public abstract Bits bits();

	@SuppressWarnings("unchecked")
	public static <T> ModelComponentType<? super T> ofInstance(T component) {
		Objects.requireNonNull(component);
		Preconditions.checkArgument(component instanceof ModelComponent);
		return (ModelComponentType<? super T>) ((ModelComponent) component).getComponentType();
	}


	public static <T> ModelComponentType<T> componentOf(Class<T> type) {
		Objects.requireNonNull(type);
		return (ModelComponentType<T>) knownComponentTypes.computeIfAbsent(type, t -> new ComponentType<>(type));
	}

	public static <T> ModelComponentType<ModelProjection> projectionOf(Class<T> type) {
		Objects.requireNonNull(type);
		return (ModelComponentType<ModelProjection>) knownComponentTypes.computeIfAbsent(type, t -> new ProjectionType<>(type));
	}

	private static int id = 0;
	private final int hashCode = id++;

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private static class ComponentType<T> extends ModelComponentType<T> {
		private final Class<T> value;

		private ComponentType(Class<T> value) {
			this.value = value;
		}

		@Override
		public boolean isSupertypeOf(ModelComponentType<?> componentType) {
			if (value.equals(ModelProjection.class) && componentType instanceof ProjectionType) {
				return true;
			} else if (componentType instanceof ComponentType) {
				return value.isAssignableFrom(((ComponentType<?>) componentType).value);
			}
			return false;
		}

		@Override
		public Bits familyBits() {
			return componentFamilyBits(value);
		}

		@Override
		public Bits bits() {
			return componentBits(value);
		}

		@Override
		public String toString() {
			return value.getName().substring(value.getName().lastIndexOf('.') + 1).replace('$', '.');
		}
	}

	private static class ProjectionType<T> extends ModelComponentType<ModelProjection> {
		private final Class<T> value;

		private ProjectionType(Class<T> value) {
			this.value = value;
		}

		@Override
		public boolean isSupertypeOf(ModelComponentType<?> componentType) {
			if (componentType instanceof ProjectionType) {
				return value.isAssignableFrom(((ProjectionType<?>) componentType).value);
			}
			return false;
		}

		@Override
		public Bits familyBits() {
			return componentFamilyBits(value).or(componentBits(ModelProjection.class));
		}

		@Override
		public Bits bits() {
			return componentBits(value).or(componentBits(ModelProjection.class));
		}

		@Override
		public String toString() {
			return "projection of " + value.getSimpleName();
		}
	}
}
