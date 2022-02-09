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
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.utils.TransformerUtils;
import lombok.val;
import lombok.var;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.not;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static dev.nokee.runtime.core.Coordinates.absentCoordinate;
import static dev.nokee.utils.TransformerUtils.transformEach;
import static java.util.stream.Collectors.joining;

public final class DimensionPropertyRegistrationFactory {
	private final ObjectFactory objectFactory;

	public DimensionPropertyRegistrationFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
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
			this.supportedValues = ImmutableSet.copyOf(values);
			return this;
		}

		@SuppressWarnings("unchecked")
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

		@SuppressWarnings("unchecked")
		public ModelRegistration build() {
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

			return ModelRegistration.builder()
				.withComponent(path)
				.withComponent(identifier)
				.withComponent(ModelPropertyTag.instance())
				.withComponent(new ModelPropertyTypeComponent(set(of(elementType))))
				.withComponent(new GradlePropertyComponent(property))
				.withComponent(VariantDimensionTag.tag())
				.withComponent(new VariantDimensionAxisComponent(axis))
				.withComponent(new VariantDimensionAxisFilterComponent(filters))
				.withComponent(new VariantDimensionValuesComponent(property.map(toCoordinateSet())))
				.build();
		}

		private Transformer<CoordinateSet<Object>, Iterable<Object>> toCoordinateSet() {
			var axisValues = assertNonEmpty(axis.getDisplayName(), path.getParent().get().getName());

			if (supportedValues != null) {
				axisValues = axisValues.andThen(assertSupportedValues(supportedValues));
			} else if (axisValidator != null) {
				axisValues = axisValues.andThen(new PeekTransformer<>(it -> axisValidator.accept(it)));
			}

			var axisCoordinates = axisValues.andThen(transformEach(axis::create));

			if (includeEmptyCoordinate) {
				axisCoordinates = axisCoordinates.andThen(appended(absentCoordinate(axis)));
			}

			return axisCoordinates.andThen(CoordinateSet::of);
		}
	}

	public static <T> TransformerUtils.Transformer<Iterable<T>, Iterable<T>> appended(T element) {
		return new IterableAppendedAllTransformer<>(ImmutableList.of(element));
	}

	public static <T> TransformerUtils.Transformer<Iterable<T>, Iterable<T>> appendedAll(Iterable<T> suffix) {
		return new IterableAppendedAllTransformer<>(suffix);
	}

	public static final class IterableAppendedAllTransformer<T> implements TransformerUtils.Transformer<Iterable<T>, Iterable<T>> {
		private final Iterable<T> suffixElements;

		public IterableAppendedAllTransformer(Iterable<T> suffixElements) {
			this.suffixElements = suffixElements;
		}

		@Override
		public Iterable<T> transform(Iterable<T> values) {
			return Iterables.concat(values, suffixElements);
		}
	}

	// TODO: We register build variant
	public ModelRegistration buildVariants(ModelPropertyIdentifier identifier, Provider<Set<BuildVariant>> buildVariantProvider) {
		val property = objectFactory.setProperty(BuildVariant.class).convention(buildVariantProvider);
		property.finalizeValueOnRead();
		property.disallowChanges();
		return ModelRegistration.builder()
			.withComponent(toPath(identifier))
			.withComponent(identifier)
			.withComponent(ModelPropertyTag.instance())
			.withComponent(new ModelPropertyTypeComponent(set(of(BuildVariant.class))))
			.withComponent(new GradlePropertyComponent(property))
			.build();
	}

	public static <I extends Iterable<T>, T> TransformerUtils.Transformer<I, I> assertNonEmpty(String propertyName, String componentName) {
		return new PeekTransformer<>(new AssertNonEmpty<>(propertyName, componentName));
	}

	private static final class AssertNonEmpty<T> implements Consumer<Iterable<T>> {
		private final String propertyName;
		private final String componentName;

		private AssertNonEmpty(String propertyName, String componentName) {
			this.propertyName = propertyName;
			this.componentName = componentName;
		}

		@Override
		public void accept(Iterable<T> values) {
			if (Iterables.isEmpty(values)) {
				throw new IllegalArgumentException(String.format("A %s needs to be specified for component '%s'.", propertyName, componentName));
			}
		}
	}

	private static <I extends Iterable<T>, T> Transformer<I, I> assertSupportedValues(Set<T> supportedValues) {
		return new PeekTransformer<>(new AssertSupportedValuesConsumer<>(supportedValues));
	}

	private static final class AssertSupportedValuesConsumer<T> implements Consumer<Iterable<T>> {
		private final Set<T> supportedValues;

		private AssertSupportedValuesConsumer(Set<T> supportedValues) {
			this.supportedValues = supportedValues;
		}

		@Override
		public void accept(Iterable<T> values) {
			val unsupportedValues = Streams.stream(values).filter(not(supportedValues::contains)).collect(Collectors.toList());
			if (!unsupportedValues.isEmpty()) {
				throw new IllegalArgumentException("The following values are not supported:\n" + unsupportedValues.stream().map(it -> " * " + it).collect(joining("\n")));
			}
		}
	}
}
