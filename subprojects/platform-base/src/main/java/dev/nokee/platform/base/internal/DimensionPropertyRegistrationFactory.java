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
package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;

public final class DimensionPropertyRegistrationFactory {
	private final ObjectFactory objectFactory;

	public DimensionPropertyRegistrationFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public <T> Builder<T> newAxisProperty(CoordinateAxis<T> axis) {
		return new Builder<>().axis(axis);
	}

	public static final class DimensionProperty<T> {
		private final SetProperty<T> property;
		private final CoordinateAxis<?> axis;
		private final Predicate<BuildVariantInternal> axisFilter;
		private final Consumer<Iterable<? extends Coordinate<Object>>> axisValidator;
		private final boolean includeEmptyCoordinate;

		public DimensionProperty(SetProperty<T> property, CoordinateAxis<?> axis, Predicate<BuildVariantInternal> axisFilter, Consumer<Iterable<? extends Coordinate<Object>>> axisValidator, boolean includeEmptyCoordinate) {
			assert property != null;
			assert axis != null;
			assert axisFilter != null;
			assert axisValidator != null;
			this.property = property;
			this.axis = axis;
			this.axisFilter = axisFilter;
			this.axisValidator = axisValidator;
			this.includeEmptyCoordinate = includeEmptyCoordinate;
		}

		public SetProperty<T> getProperty() {
			return property;
		}

		public CoordinateAxis<?> getAxis() {
			return axis;
		}

		public Predicate<BuildVariantInternal> getFilter() {
			return axisFilter;
		}

		public Consumer<Iterable<? extends Coordinate<Object>>> getValidator() {
			return axisValidator;
		}

		public boolean isOptional() {
			return includeEmptyCoordinate;
		}
	}

	public final class Builder<T> {
		private Class<Object> elementType;
		private CoordinateAxis<Object> axis;
		private Object defaultValues = ImmutableSet.of();
		private Consumer<Iterable<? extends Coordinate<Object>>> axisValidator;
		private boolean includeEmptyCoordinate = false;
		private List<Predicate<? super BuildVariantInternal>> filters = new ArrayList<>();

		private Builder() {}

		@SuppressWarnings("unchecked")
		public <U> Builder<U> axis(CoordinateAxis<U> axis) {
			this.axis = (CoordinateAxis<Object>) axis;
			return (Builder<U>) this;
		}

		@SuppressWarnings("unchecked")
		public <U> Builder<U> elementType(Class<U> elementType) {
			this.elementType = (Class<Object>) elementType;
			return (Builder<U>) this;
		}

		public Builder<T> validValues(Object... values) {
			this.axisValidator = new AssertSupportedValuesConsumer<>(ImmutableSet.copyOf(values));
			return this;
		}

		public <S> Builder<T> validateUsing(Consumer<? super Iterable<Coordinate<S>>> axisValidator) {
			this.axisValidator = values -> axisValidator.accept(Cast.uncheckedCast("cannot get the type to match", values));
			return this;
		}

		public Builder<T> includeEmptyCoordinate() {
			this.includeEmptyCoordinate = true;
			return this;
		}

		public Builder<T> filterVariant(Predicate<? super BuildVariantInternal> predicate) {
			filters.add(predicate);
			return this;
		}

		public Builder<T> defaultValue(Object value) {
			this.defaultValues = ImmutableSet.of(value);
			return this;
		}

		public Builder<T> defaultValues(Provider<? extends Iterable<? extends Object>> value) {
			this.defaultValues = value;
			return this;
		}

		@SuppressWarnings("unchecked")
		public DimensionProperty<T> build() {
			checkState(axis != null);

			if (elementType == null) {
				elementType = axis.getType();
			}

			val property = objectFactory.setProperty(elementType);
			property.finalizeValueOnRead();
			if (defaultValues instanceof Provider) {
				property.convention((Provider<? extends Iterable<?>>) defaultValues);
			} else {
				property.convention((Iterable<?>) defaultValues);
			}

			Predicate<BuildVariantInternal> axisFilter = __ -> true;
			if (filters != null && !filters.isEmpty()) {
				axisFilter = buildVariant -> filters.stream().anyMatch(it -> it.test(buildVariant));
			}

			if (axisValidator == null) {
				axisValidator = __ -> {};
			}

			return new DimensionProperty<>((SetProperty<T>) property, axis, axisFilter, axisValidator, includeEmptyCoordinate);
		}
	}

	private static final class AssertSupportedValuesConsumer<T> implements Consumer<Iterable<? extends Coordinate<T>>> {
		private final Set<T> supportedValues;

		private AssertSupportedValuesConsumer(Set<T> supportedValues) {
			this.supportedValues = supportedValues;
		}

		@Override
		public void accept(Iterable<? extends Coordinate<T>> values) {
			val unsupportedValues = Streams.stream(values).filter(it -> !supportedValues.contains(it.getValue())).collect(Collectors.toList());
			if (!unsupportedValues.isEmpty()) {
				throw new IllegalArgumentException("The following values are not supported:\n" + unsupportedValues.stream().map(it -> " * " + it).collect(joining("\n")));
			}
		}
	}
}
