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
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static java.util.stream.Collectors.joining;

public final class DimensionPropertyRegistrationFactory {
	private final ObjectFactory objectFactory;

	public DimensionPropertyRegistrationFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public Builder newAxisProperty(ModelPropertyIdentifier identifier) {
		return new Builder(identifier);
	}

	public <T> ModelRegistration newAxisProperty(CoordinateAxis<T> axis) {
		return newAxisProperty().axis(axis).build();
	}

	public Builder newAxisProperty() {
		return new Builder();
	}

	public final class Builder {
		private final ModelPropertyIdentifier identifier;
		private Class<Object> elementType;
		private CoordinateAxis<Object> axis;
		private Object defaultValues = ImmutableSet.of();
		private Consumer<Iterable<? extends Coordinate<Object>>> axisValidator;
		private boolean includeEmptyCoordinate = false;
		private List<Predicate<? super BuildVariantInternal>> filters = new ArrayList<>();

		private Builder() {
			this.identifier = null;
		}

		private Builder(ModelPropertyIdentifier identifier) {
			this.identifier = identifier;
		}

		@SuppressWarnings("unchecked")
		public Builder axis(CoordinateAxis<?> axis) {
			this.axis = (CoordinateAxis<Object>) axis;
			return this;
		}

		@SuppressWarnings("unchecked")
		public Builder elementType(Class<?> elementType) {
			this.elementType = (Class<Object>) elementType;
			return this;
		}

		public Builder validValues(Object... values) {
			this.axisValidator = new AssertSupportedValuesConsumer<>(ImmutableSet.copyOf(values));
			return this;
		}

		public <T> Builder validateUsing(Consumer<? super Iterable<Coordinate<T>>> axisValidator) {
			this.axisValidator = values -> axisValidator.accept(Cast.uncheckedCast("cannot get the type to match", values));
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

		@SuppressWarnings("unchecked")
		public ModelRegistration build() {
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

			val result = ModelRegistration.builder();

			if (identifier != null) {
				result.withComponent(new IdentifierComponent(identifier));
			}

			result
				.withComponent(tag(ModelPropertyTag.class))
				.withComponent(new ModelPropertyTypeComponent(set(of(elementType))))
				.withComponent(new GradlePropertyComponent(property))
				.withComponent(tag(VariantDimensionTag.class))
				.withComponent(new VariantDimensionAxisComponent(axis));

			if (filters != null && !filters.isEmpty()) {
				result.withComponent(new VariantDimensionAxisFilterComponent(filters));
			}

			if (axisValidator != null) {
				result.withComponent(new VariantDimensionAxisValidatorComponent(axisValidator));
			}

			if (includeEmptyCoordinate) {
				result.withComponent(tag(VariantDimensionAxisOptionalTag.class));
			}

			return result.build();
		}
	}

	// TODO: We register build variant
	public ModelRegistration buildVariants(ModelPropertyIdentifier identifier, Provider<Set<BuildVariant>> buildVariantProvider) {
		val property = objectFactory.setProperty(BuildVariant.class).convention(buildVariantProvider);
		property.finalizeValueOnRead();
		property.disallowChanges();
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(ModelPropertyTag.class))
			.withComponent(new ModelPropertyTypeComponent(set(of(BuildVariant.class))))
			.withComponent(new GradlePropertyComponent(property))
			.build();
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
