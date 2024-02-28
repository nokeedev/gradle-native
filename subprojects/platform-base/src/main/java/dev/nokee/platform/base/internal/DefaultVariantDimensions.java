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
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantDimensionBuilder;
import dev.nokee.platform.base.VariantDimensions;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.CoordinateSpace;
import dev.nokee.utils.Cast;
import dev.nokee.utils.TransformerUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.runtime.core.Coordinates.absentCoordinate;
import static dev.nokee.util.ProviderOfIterableTransformer.toProviderOfIterable;
import static dev.nokee.utils.TransformerUtils.peek;
import static dev.nokee.utils.TransformerUtils.transformEach;

public /*final*/ abstract class DefaultVariantDimensions implements VariantDimensions {
	private final DimensionPropertyRegistrationFactory factory;
	private final Provider<Iterable<? extends CoordinateSet<?>>> dimensions;
	private final Provider<CoordinateSpace> finalSpace;
	private final SetProperty<BuildVariant> buildVariants;

	@Inject
	public DefaultVariantDimensions(DimensionPropertyRegistrationFactory factory, ObjectFactory objects) {
		this.factory = factory;
		final ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();

		this.dimensions = getElements()
			.map(transformEach(new ToCoordinateSet(identifier.getName())))
			.flatMap(toProviderOfIterable(objects::listProperty));
		this.finalSpace = this.dimensions.map(CoordinateSpace::cartesianProduct);
		this.buildVariants = objects.setProperty(BuildVariant.class);

		buildVariants.convention(finalSpace.map(DefaultBuildVariant::fromSpace).map(buildVariants -> {
			val allFilters = getElements().get().stream()
				.map(DimensionPropertyRegistrationFactory.DimensionProperty::getFilter)
				.collect(Collectors.toList());
			return buildVariants.stream().filter(buildVariant -> {
				return allFilters.stream().allMatch(it -> it.test(buildVariant));
			}).collect(ImmutableSet.toImmutableSet());
		}));
		buildVariants.finalizeValueOnRead();
		buildVariants.disallowChanges();
	}

	public DimensionPropertyRegistrationFactory getDimensionFactory() {
		return factory;
	}

	public abstract SetProperty<DimensionPropertyRegistrationFactory.DimensionProperty<?>> getElements();

	public SetProperty<BuildVariant> getBuildVariants() {
		return buildVariants;
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType) {
		DimensionPropertyRegistrationFactory.DimensionProperty<T> dimension = factory.newAxisProperty(CoordinateAxis.of(axisType)).build();
		getElements().add(dimension);
		return dimension.getProperty();
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType, Action<? super VariantDimensionBuilder<T>> action) {
		Objects.requireNonNull(axisType);
		Objects.requireNonNull(action);
		val axisBuilder = factory.newAxisProperty(CoordinateAxis.of(axisType));

		action.execute(new VariantDimensionBuilderAdapter<>(new VariantDimensionBuilderAdapter.Callback<T>() {
			@Override
			public <S> void accept(Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate) {
				axisBuilder.includeEmptyCoordinate();
				axisBuilder.filterVariant(new VariantDimensionAxisFilter<>(CoordinateAxis.of(axisType), otherAxisType, predicate));
			}
		}));

		DimensionPropertyRegistrationFactory.DimensionProperty<T> dimension = axisBuilder.build();
		getElements().add(dimension);
		return dimension.getProperty();
	}

	private static final class ToCoordinateSet implements Transformer<Provider<CoordinateSet<?>>, DimensionPropertyRegistrationFactory.DimensionProperty<?>> {
		private final String componentName;

		public ToCoordinateSet(ElementName componentName) {
			this.componentName = componentName.toString();
		}

		@Override
		public Provider<CoordinateSet<?>> transform(DimensionPropertyRegistrationFactory.DimensionProperty<?> dimension) {
			final Provider<? extends Set<Object>> values = Cast.uncheckedCastBecauseOfTypeErasure(dimension.getProperty());

			return values.map(asCoordinateSet(dimension, componentName));
		}

		private Transformer<CoordinateSet<Object>, Iterable<Object>> asCoordinateSet(DimensionPropertyRegistrationFactory.DimensionProperty<?> dimension, String componentName) {
			@SuppressWarnings("unchecked")
			final CoordinateAxis<Object> axis = (CoordinateAxis<Object>) dimension.getAxis();

			TransformerUtils.Transformer<Iterable<Object>, Iterable<Object>> axisValues = assertNonEmpty(axis.getDisplayName(), componentName);

			TransformerUtils.Transformer<Iterable<? extends Coordinate<Object>>, Iterable<Object>> axisCoordinates = axisValues.andThen(transformEach(axis::create));

			val axisValidator = dimension.getValidator();
			axisCoordinates = axisCoordinates.andThen(peek(it -> axisValidator.accept(it)));

			if (dimension.isOptional()) {
				axisCoordinates = axisCoordinates.andThen(prepended(absentCoordinate(axis)));
			}

			return axisCoordinates.andThen(CoordinateSet::of);
		}

		public static <T> TransformerUtils.Transformer<Iterable<T>, Iterable<? extends T>> prepended(T element) {
			return new ToCoordinateSet.IterablePrependedAllTransformer<>(ImmutableList.of(element));
		}

		public static <T> TransformerUtils.Transformer<Iterable<T>, Iterable<? extends T>> prependedAll(Iterable<T> prefix) {
			return new ToCoordinateSet.IterablePrependedAllTransformer<>(prefix);
		}

		public static final class IterablePrependedAllTransformer<T> implements TransformerUtils.Transformer<Iterable<T>, Iterable<? extends T>> {
			private final Iterable<T> prependElements;

			public IterablePrependedAllTransformer(Iterable<T> prependElements) {
				this.prependElements = prependElements;
			}

			@Override
			public Iterable<T> transform(Iterable<? extends T> values) {
				return Iterables.concat(prependElements, values);
			}
		}

		public static <I extends Iterable<T>, T> TransformerUtils.Transformer<I, I> assertNonEmpty(String propertyName, String componentName) {
			return peek(new ToCoordinateSet.AssertNonEmpty<>(propertyName, componentName));
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
	}
}
