/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.base.internal.util;

import com.google.common.collect.ImmutableList;
import dev.nokee.utils.ConfigureUtils;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.provider.PropertyInternal;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.io.File;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Functional utilities for Gradle properties.
 * As Gradle property classes are messy, we need to wrap the property type (i.e. {@link Property}, {@link SetProperty}, {@link ConfigurableFileCollection}).
 * The wrapping can also be used to adapt non-Gradle property such as setter or methods into property-like types.
 * See {@literal wrap(...)} methods to standard wrapper factory methods.
 */
public final class PropertyUtils {
	private PropertyUtils() {}

	@SuppressWarnings("UnstableApiUsage") // HasConfigurableValue is now stable
	public static <SELF, T extends HasConfigurableValue> BiConsumer<SELF, T> lockProperty() {
		return (self, value) -> {
			value.finalizeValueOnRead();
			value.disallowChanges();
		};
	}

	/**
	 * Sets a property value to the specified value.
	 * The value is purposefully an object allowing competing types to be used.
	 * Upon setting the value, invalid values will cause an exception.
	 *
	 * @param value  value to set, can be null
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that set the input property value to the specified value, never null
	 */
	public static <SELF, T> BiConsumer<SELF, SetAwareProperty<? extends T>> set(Object value) {
		return (self, property) -> property.set(value);
	}

	/**
	 * Sets a property value by mapping the property owner.
	 *
	 * @param mapper  mapper function, must not be null
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that set the input property value by mapping the owner, never null
	 */
	public static <SELF, T> BiConsumer<SELF, SetAwareProperty<? extends T>> set(Function<? super SELF, ? extends Object> mapper) {
		Objects.requireNonNull(mapper);
		return (self, property) -> property.set(mapper.apply(self));
	}

	public static <SELF, T> BiConsumer<SELF, Property<? extends T>> resetToConvention() {
		return (self, property) -> property.set(null);
	}

	/**
	 * Sets a property convention to the specified value.
	 *
	 * @param value  value to set as convention, can be null
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that set the input property convention value to the specified value, never null
	 */
	public static <SELF, T> BiConsumer<SELF, ConventionAwareProperty<? extends T>> convention(Object value) {
		return (self, property) -> property.convention(value);
	}

	/**
	 * Sets a property convention by mapping the property owner.
	 *
	 * @param mapper  mapper function, must not be null
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that set the input property convention value by mapping the owner, never null
	 */
	public static <SELF, T> BiConsumer<SELF, ConventionAwareProperty<? extends T>> convention(Function<? super SELF, ?> mapper) {
		Objects.requireNonNull(mapper);
		return (self, property) -> property.convention(mapper.apply(self));
	}

	/**
	 * Adds a value to a collection property.
	 *
	 * @param value  value to add to the property
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that add the specified value to the input property, never null
	 */
	public static <SELF, T> BiConsumer<SELF, CollectionAwareProperty<? extends T>> add(Object value) {
		return (self, property) -> property.add(value);
	}

	/**
	 * Adds the value of the mapping of the property owner to a collection property.
	 *
	 * @param mapper  mapper function, must not be null
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that add the mapping of the property owner to the input property, never null
	 */
	public static <SELF, T> BiConsumer<SELF, CollectionAwareProperty<? extends T>> add(Function<? super SELF, ?> mapper) {
		Objects.requireNonNull(mapper);
		return (self, property) -> property.add(mapper.apply(self));
	}

	/**
	 * Adds the specified values to a collection property.
	 *
	 * @param value  value to add to the property, must not be null
	 * @param values  values to add to the property, must not be null
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that add the specified values to the input property, never null
	 */
	public static <SELF, T> BiConsumer<SELF, CollectionAwareProperty<? extends T>> addAll(Object value, Object... values) {
		return (self, property) -> property.addAll(ImmutableList.builder().add(value).add(values).build());
	}

	/**
	 * Adds the specified values to a collection property.
	 *
	 * @param values  values to add to the property
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that add the specified values to the input property, never null
	 */
	public static <SELF, T> BiConsumer<SELF, CollectionAwareProperty<? extends T>> addAll(Iterable<?> values) {
		return (self, property) -> property.addAll(values);
	}

	/**
	 * Adds the values of the mapping of the property owner to a collection property.
	 *
	 * @param mapper  mapper function, must not be null
	 * @param <SELF>  property owner
	 * @param <T>  property type
	 * @return a composable action that add the mapping of the property owner to the input property, never null
	 */
	public static <SELF, T> BiConsumer<SELF, CollectionAwareProperty<? extends T>> addAll(Function<? super SELF, ? extends Object> mapper) {
		Objects.requireNonNull(mapper);
		return (self, property) -> {
			val values = mapper.apply(self);
			if (values instanceof Iterable) {
				property.addAll((Iterable<?>) values);
			} else {
				property.addAll(ImmutableList.of(values));
			}
		};
	}

	/**
	 * Adds file-values to a {@link FileCollection} property.
	 * See {@link Project#files(Object...)} for accepted types.
	 *
	 * @param path  a file path, required, must not be null
	 * @param paths  additional file paths, optional, must not be null
	 * @param <SELF>  the property owner type
	 * @return a consumer of both the owner and property, never null
	 */
	public static <SELF> BiConsumer<SELF, FileCollectionProperty> from(Object path, Object... paths) {
		return (self, files) -> files.from(path, paths);
	}

	/**
	 * Adds file-values mapped from the property owner to a {@link FileCollection}  property.
	 *
	 * @param mapper  a mapping function, must not be null
	 * @param <SELF>  the property owner type
	 * @return a consumer of both the owner and property, never null
	 */
	public static <SELF> BiConsumer<SELF, FileCollectionProperty> from(Function<? super SELF, ?> mapper) {
		Objects.requireNonNull(mapper);
		return (self, files) -> files.from(mapper.apply(self));
	}

	@SuppressWarnings("UnstableApiUsage")
	public static <T> Property<T> wrap(org.gradle.api.provider.Property<T> target) {
		return new Property<T>() {
			@Override
			public void convention(Object value) {
				if (value instanceof Provider) {
					target.convention((Provider<? extends T>) value);
				} else {
					target.convention((T) value);
				}
			}

			@Override
			public void set(Object value) {
				ConfigureUtils.setPropertyValue(target, value);
			}

			@Override
			public void finalizeValue() {
				target.finalizeValue();
			}

			@Override
			public void finalizeValueOnRead() {
				target.finalizeValueOnRead();
			}

			@Override
			public void disallowChanges() {
				target.disallowChanges();
			}
		};
	}

	@SuppressWarnings("UnstableApiUsage")
	public static <T> CollectionProperty<T> wrap(org.gradle.api.provider.HasMultipleValues<T> target) {
		return new CollectionProperty<T>() {
			@Override
			public void add(Object value) {
				if (value instanceof Provider) {
					target.add((Provider<? extends T>) value);
				} else {
					target.add((T) value);
				}
			}

			@Override
			public void addAll(Iterable<?> values) {
				values.forEach(value -> {
					if (value instanceof Provider) {
						target.addAll((Provider<? extends Iterable<? extends T>>) value);
					} else if (value instanceof Iterable) {
						target.addAll((Iterable<? extends T>) value);
					} else {
						add(value);
					}
				});
			}

			@Override
			public void convention(Object value) {
				if (value instanceof Provider) {
					target.convention((Provider<Iterable<T>>) value);
				} else {
					if (value != null && !(value instanceof Iterable)) {
						throw new IllegalArgumentException(String.format("Cannot set the value of a property of type %s using an instance of type %s.", ((PropertyInternal<?>) target).getType().getSimpleName(), value.getClass().getSimpleName()));
					}
					target.convention((Iterable<? extends T>) value);
				}
			}

			@Override
			public void set(Object value) {
				((PropertyInternal<?>) target).setFromAnyValue(value);
			}

			@Override
			public void finalizeValue() {
				target.finalizeValue();
			}

			@Override
			public void finalizeValueOnRead() {
				target.finalizeValueOnRead();
			}

			@Override
			public void disallowChanges() {
				target.disallowChanges();
			}
		};
	}

	@SuppressWarnings("UnstableApiUsage")
	public static FileCollectionProperty wrap(ConfigurableFileCollection target) {
		return new FileCollectionProperty() {
			@Override
			public void from(Object... paths) {
				target.from(paths);
			}

			@Override
			public void add(Object value) {
				target.from(value);
			}

			@Override
			public void addAll(Iterable<?> values) {
				target.from(values);
			}

			@Override
			public void set(Object value) {
				target.setFrom(value);
			}

			@Override
			public void finalizeValue() {
				target.finalizeValue();
			}

			@Override
			public void finalizeValueOnRead() {
				target.finalizeValueOnRead();
			}

			@Override
			public void disallowChanges() {
				target.disallowChanges();
			}
		};
	}

	@SuppressWarnings("UnstableApiUsage")
	public static <T> SetAwareProperty<T> wrap(Consumer<? super T> target) {
		return new SetAwareProperty<T>() {
			@Override
			public void set(Object value) {
				target.accept((T) value);
			}

			@Override
			public void finalizeValue() {
				throw new UnsupportedOperationException("Does not support finalizeValue");
			}

			@Override
			public void finalizeValueOnRead() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void disallowChanges() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Represents a Gradle property that contains several values such as {@link SetProperty} or {@link ConfigurableFileCollection}.
	 *
	 * @param <T>  collection element type
	 */
	@SuppressWarnings("UnstableApiUsage")
	public interface CollectionAwareProperty<T> extends HasConfigurableValue {
		void add(Object value);
		void addAll(Iterable<?> values);
	}

	public interface CollectionProperty<T> extends Property<Iterable<? extends T>>, CollectionAwareProperty<T> {}

	public interface FileCollectionProperty extends SetAwareProperty<Iterable<File>>, CollectionAwareProperty<File> {
		void from(Object... paths);
	}

	public interface Property<T> extends SetAwareProperty<T>, ConventionAwareProperty<T> {}

	/**
	 * Represents a Gradle property that can be set such as {@link org.gradle.api.provider.Property}, {@link SetProperty}, {@link ConfigurableFileCollection} or even a standard setter method.
	 * @param <T>  property type
	 */
	@SuppressWarnings("UnstableApiUsage")
	public interface SetAwareProperty<T> extends HasConfigurableValue {
		void set(Object value);
	}

	/**
	 * Represents a Gradle property that has a convention such as {@link org.gradle.api.provider.Property}, {@link SetProperty}, and {@link org.gradle.api.provider.ListProperty}.
	 * @param <T>  property type
	 */
	@SuppressWarnings("UnstableApiUsage")
	public interface ConventionAwareProperty<T> extends HasConfigurableValue {
		void convention(Object value);
	}
}
