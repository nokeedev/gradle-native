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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.IsModelProperty;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.Coordinates;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Predicates.not;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static java.util.stream.Collectors.joining;

public final class DimensionPropertyRegistrationFactory {
	private final ObjectFactory objectFactory;
	private final ModelLookup modelLookup;

	public DimensionPropertyRegistrationFactory(ObjectFactory objectFactory, ModelLookup modelLookup) {
		this.objectFactory = objectFactory;
		this.modelLookup = modelLookup;
	}

	// TODO: Can select default value?
	public <T> ModelRegistration newAxisProperty(ModelPropertyIdentifier identifier, CoordinateAxis<T> axis) {
		return newAxisProperty(identifier).axis(axis).build();
	}

	public Builder newAxisProperty(ModelPropertyIdentifier identifier) {
		return new Builder(identifier);
	}

	public final class Builder {
		private final ModelPropertyIdentifier identifier;
		private final ModelPath path;
		private Class<Object> elementType;
		private CoordinateAxis<Object> axis;
		private Set<Object> supportedValues;
		private Object defaultValues = ImmutableSet.of();
		private Consumer<? super Iterable<?>> axisValidator;
		private boolean includeEmptyCoordinate = false;
		private List<Predicate<? super BuildVariantInternal>> filters = new ArrayList<>();

		private Builder(ModelPropertyIdentifier identifier) {
			this.identifier = identifier;
			this.path = toPath(identifier);
		}

		public Builder axis(CoordinateAxis<?> axis) {
			this.axis = (CoordinateAxis<Object>) axis;
			return this;
		}

		public Builder elementType(Class<?> elementType) {
			this.elementType = (Class<Object>) elementType;
			return this;
		}

		public Builder validValues(Object... values) {
			this.supportedValues = ImmutableSet.copyOf(values);
			return this;
		}

		public <T> Builder validateUsing(Consumer<? super Iterable<T>> axisValidator) {
			this.axisValidator = (Consumer<? super Iterable<?>>) axisValidator;
			return this;
		}

		public Builder includeEmptyCoordinate() {
			this.includeEmptyCoordinate = true;
			return this;
		}

		public Builder filterVariant(Predicate<? super BuildVariantInternal> predicate) {
			filters.add(predicate);
			return this;
		}

		public Builder defaultValue(Object value) {
			this.defaultValues = ImmutableSet.of(value);
			return this;
		}

		public Builder defaultValues(Provider<? extends Iterable<? extends Object>> value) {
			this.defaultValues = value;
			return this;
		}

		public ModelRegistration build() {
			if (elementType == null) {
				elementType = axis.getType();
			}

			return ModelRegistration.builder()
				.withComponent(path)
				.withComponent(identifier)
				.withComponent(IsModelProperty.tag())
				.withComponent(createdUsing(ModelType.of(new TypeOf<SetProperty<?>>() {}), () -> {
					val result = objectFactory.setProperty(elementType);
					if (defaultValues instanceof Provider) {
						result.convention((Provider<? extends Iterable<?>>) defaultValues);
					} else {
						result.convention((Iterable<?>) defaultValues);
					}
					return result;
				}))
				.withComponent(new Dimension(axis, () -> {
					val property = ModelNodeUtils.get(modelLookup.get(path), SetProperty.class);
					property.finalizeValueOnRead();
					Provider<Iterable<Object>> valueProvider = property
						.map(assertNonEmpty(axis.getDisplayName(), path.getParent().get().getName()));

					if (supportedValues != null) {
						valueProvider = valueProvider.map(assertSupportedValues(supportedValues));
					} else if (axisValidator != null) {
						valueProvider = valueProvider.map(it -> {
							axisValidator.accept(it);
							return it;
						});
					}

					if (includeEmptyCoordinate) {
						return valueProvider
							.map(it -> Streams.concat(Streams.stream(it).map(a -> axis.create(a)), Stream.of(Coordinates.absentCoordinate(axis)))
								.collect(Coordinates.<Object>toCoordinateSet()))
							.get();
					} else {
						return valueProvider
							.map(it -> Streams.stream(it).map(a -> axis.create(a)).collect(Coordinates.<Object>toCoordinateSet()))
							.get();
					}
				}, filters))
				.build();
		}
	}

	// TODO: We register build variant
	public ModelRegistration buildVariants(ModelPropertyIdentifier identifier, Provider<Set<BuildVariant>> buildVariantProvider) {
		return ModelRegistration.builder()
			.withComponent(toPath(identifier))
			.withComponent(identifier)
			.withComponent(IsModelProperty.tag())
			.withComponent(createdUsing(ModelType.of(new TypeOf<SetProperty<BuildVariant>>() {}), () -> {
				val result = objectFactory.setProperty(BuildVariant.class).convention(buildVariantProvider);
				result.finalizeValueOnRead();
				result.disallowChanges();
				return result;
			}))
			.build();
	}

	public static <T> Transformer<Iterable<T>, Iterable<T>> assertNonEmpty(String propertyName, String componentName) {
		return values -> {
			if (Iterables.isEmpty(values)) {
				throw new IllegalArgumentException(String.format("A %s needs to be specified for component '%s'.", propertyName, componentName));
			}
			return values;
		};
	}

	private static <I extends Iterable<T>, T> Transformer<I, I> assertSupportedValues(Set<T> supportedValues) {
		return values -> {
			val unsupportedValues = Streams.stream(values).filter(not(supportedValues::contains)).collect(Collectors.toList());
			if (!unsupportedValues.isEmpty()) {
				throw new IllegalArgumentException("The following values are not supported:\n" + unsupportedValues.stream().map(it -> " * " + it).collect(joining("\n")));
			}
			return values;
		};
	}

	public static final class Dimension<T> {
		private final CoordinateAxis<T> axis;
		private final Supplier<CoordinateSet<T>> values;
		private final List<Predicate<BuildVariantInternal>> filters;

		public Dimension(CoordinateAxis<T> axis, Supplier<CoordinateSet<T>> values, List<Predicate<BuildVariantInternal>> filters) {
			this.axis = axis;
			this.values = values;
			this.filters = ImmutableList.copyOf(filters);
		}

		public CoordinateAxis<T> getAxis() {
			return axis;
		}

		public CoordinateSet<T> get() {
			return values.get();
		}

		public List<Predicate<BuildVariantInternal>> getFilters() {
			return filters;
		}
	}
}
