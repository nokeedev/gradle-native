/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.type;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A type token to representing a resolved type.
 *
 * @param <T> the resolved type
 */
@EqualsAndHashCode
@SuppressWarnings("UnstableApiUsage")
public final class ModelType<T> {
	private static final ConcurrentHashMap<TypeToken<?>, Class<?>> rawTypes = new ConcurrentHashMap<>();
	private static Class<?> computeRawType(TypeToken<?> type) {
		return type.getRawType();
	}
	private static final ModelType<Object> UNTYPED = ModelType.of(Object.class);
	private static final Collection<ModelType<?>> OBJECT_TYPE = ImmutableList.of(of(Object.class));
	private final TypeToken<T> type;

	private ModelType(TypeToken<T> type) {
		this.type = type;
	}

	public Type getType() {
		return type.getType();
	}

	// TODO: It feels strange the method is about types but returns a class
	public Class<? super T> getRawType() {
		@SuppressWarnings("unchecked")
		final Class<? super T> result = (Class<? super T>) rawTypes.computeIfAbsent(type, ModelType::computeRawType);
		return result;
	}

	// TODO: It feels strange the method is about types but returns a class
	public Class<T> getConcreteType() {
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) rawTypes.computeIfAbsent(type, ModelType::computeRawType);
		return result;
	}

	public boolean isAssignableFrom(ModelType<?> type) {
		return this == type || this.getConcreteType().isAssignableFrom(type.getConcreteType());
	}

	public boolean isSubtypeOf(Type type) {
		return this.type.isSubtypeOf(type);
	}

	public boolean isSubtypeOf(ModelType<?> type) {
		return this.type.isSubtypeOf(type.type);
	}

	public boolean isSupertypeOf(Type type) {
		return this.type.isSupertypeOf(type);
	}

	public boolean isSupertypeOf(ModelType<?> type) {
		return this.type.isSupertypeOf(type.type);
	}

	public boolean isParameterized() {
		return this.type.getType() instanceof ParameterizedType;
	}

	public List<ModelType<?>> getTypeVariables() {
		if (isParameterized()) {
			Type[] typeArguments = ((ParameterizedType) this.type.getType()).getActualTypeArguments();
			val builder = ImmutableList.<ModelType<?>>builder();
			for (Type typeArgument : typeArguments) {
				builder.add(new ModelType<>(TypeToken.of(typeArgument)));
			}
			return builder.build();
		} else {
			return ImmutableList.of();
		}
	}

	@Override
	public String toString() {
		return (type.getRawType().isInterface() ? "interface" : "class") + " " + type.toString();
	}

	/**
	 * Creates a type representation for the specified class.
	 *
	 * @param type  a class
	 * @param <T>  the type
	 * @return a type representation of the specified class, never null.
	 */
	public static <T> ModelType<T> of(Class<T> type) {
		return new ModelType<>(TypeToken.of(type));
	}

	/**
	 * Creates a type representation for the specified type.
	 *
	 * @param type  a type
	 * @return a type representation of the specified type, never null.
	 */
	public static ModelType<?> of(Type type) {
		return new ModelType<>(TypeToken.of(type));
	}

	/**
	 * Creates a type representation for the specified instance.
	 *
	 * @param instance  a instance
	 * @param <T>  the type
	 * @return a type representation of the specified instance, never null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ModelType<T> typeOf(T instance) {
		return new ModelType<>(TypeToken.of((Class<T>) ModelTypeUtils.toUndecoratedType(instance.getClass())));
	}

	/**
	 * Creates a type representation for the specified type token.
	 *
	 * @param type  the type token
	 * @param <T>  the type
	 * @return a type representation of the specified type token, never null.
	 * @see TypeOf  for complex type
	 */
	public static <T> ModelType<T> of(TypeOf<T> type) {
		return new ModelType<>(type.type);
	}

	public static ModelType<Object> untyped() {
		return UNTYPED;
	}

	/**
	 * Returns the type representing the superclass of this type.
	 * If this type represents either the {@code Object} type, an interface, a primitive type, or void, then absent is returned.
	 *
	 * @return the type representing the superclass of this type if available, never null.
	 */
	public Optional<ModelType<? super T>> getSupertype() {
		return Optional.ofNullable(type.getRawType().getSuperclass())
			.map(type::getSupertype)
			.map(ModelType::new);
	}

	/**
	 * Determines the interfaces implemented by the class or interface represented by this object.
	 *
	 * @return a list of interfaces implemented by this type, never null.
	 */
	public List<ModelType<? super T>> getInterfaces() {
		return Arrays.stream(type.getRawType().getInterfaces())
			.map(this::cast)
			.map(type::getSupertype)
			.map(ModelType::new)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private Class<? super T> cast(Class<?> type) {
		return (Class<? super T>) type;
	}

	/**
	 * Visits all types in a type hierarchy in breadth-first order, super-classes first and then implemented interfaces.
	 *
	 * @param visitor  the visitor to call for each type in the hierarchy.
	 */
	public void walkTypeHierarchy(Visitor<? extends T> visitor) {
		val seenInterfaces = new HashSet<ModelType<?>>();
		val queue = new ArrayDeque<ModelType<? super T>>();
		queue.add(this);
		ModelType<? super T> walkingType;
		while ((walkingType = queue.poll()) != null) {
			if (OBJECT_TYPE.contains(walkingType)) {
				continue;
			}

			visitor.visitType(walkingType);

			walkingType.getSupertype().ifPresent(queue::add);
			walkingType.getInterfaces().stream().filter(seenInterfaces::add).forEach(queue::add);
		}
	}

	public interface Visitor<T> {
		void visitType(ModelType<? super T> type);
	}
}
