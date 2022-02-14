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
import dev.nokee.model.internal.core.DescendantNodes;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.CoordinateSpace;
import dev.nokee.utils.TransformerUtils;
import lombok.val;
import lombok.var;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.runtime.core.Coordinates.absentCoordinate;
import static dev.nokee.utils.Cast.uncheckedCastBecauseOfTypeErasure;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class BuildVariants {
	private final Provider<Iterable<CoordinateSet<?>>> dimensions;
	private final Provider<CoordinateSpace> finalSpace;
	private final SetProperty<BuildVariant> buildVariants;

	public BuildVariants(ModelNode entity, ProviderFactory providers, ObjectFactory objects) {
		Provider<List<ModelNode>> dimensions = providers.provider(() -> {
			val nodes = entity.getComponent(componentOf(DescendantNodes.class)).getDirectDescendants().stream();
			val dimensionNodes = nodes.filter(it -> it.hasComponent(componentOf(VariantDimensionTag.class)));
			return dimensionNodes.collect(Collectors.toList());
		});
		this.dimensions = dimensions
			.map(transformEach(new ToCoordinateSet(entity.getComponent(ModelPath.class).getName())))
			.flatMap(new ToProviderOfIterableTransformer<>(() -> uncheckedCastBecauseOfTypeErasure(objects.listProperty(CoordinateSet.class))));
		this.finalSpace = this.dimensions.map(CoordinateSpace::cartesianProduct);
		this.buildVariants = objects.setProperty(BuildVariant.class);

		buildVariants.convention(finalSpace.map(DefaultBuildVariant::fromSpace).map(buildVariants -> {
			val allFilters = dimensions.get().stream()
				.map(it -> it.getComponent(componentOf(VariantDimensionAxisFilterComponent.class)).get())
				.collect(Collectors.toList());
			return buildVariants.stream().filter(buildVariant -> {
				return allFilters.stream().allMatch(it -> it.test(buildVariant));
			}).collect(ImmutableSet.toImmutableSet());
		}));
		buildVariants.finalizeValueOnRead();
		buildVariants.disallowChanges();
	}

	public Provider<Iterable<CoordinateSet<?>>> dimensions() {
		return dimensions;
	}

	public Provider<Set<BuildVariant>> get() {
		return buildVariants;
	}

	private static final class ToCoordinateSet implements Transformer<Provider<CoordinateSet<?>>, ModelNode> {
		private final String componentName;

		public ToCoordinateSet(String componentName) {
			this.componentName = componentName;
		}

		@Override
		public Provider<CoordinateSet<?>> transform(ModelNode entity) {
			@SuppressWarnings("unchecked")
			val values = (Provider<Set<Object>>) entity.getComponent(GradlePropertyComponent.class).get();

			return values.map(asCoordinateSet(entity, componentName));
		}

		private Transformer<CoordinateSet<Object>, Iterable<Object>> asCoordinateSet(ModelNode entity, String componentName) {
			val axis = entity.getComponent(componentOf(VariantDimensionAxisComponent.class)).get();

			var axisValues = assertNonEmpty(axis.getDisplayName(), componentName);

			var axisCoordinates = axisValues.andThen(transformEach(axis::create));

			val axisValidator = entity.findComponent(componentOf(VariantDimensionAxisValidatorComponent.class))
				.map(VariantDimensionAxisValidatorComponent::get);
			if (axisValidator.isPresent()) {
				axisCoordinates = axisCoordinates.andThen(new PeekTransformer<>(it -> axisValidator.get().accept(it)));
			}

			if (entity.hasComponent(componentOf(VariantDimensionAxisOptionalTag.class))) {
				axisCoordinates = axisCoordinates.andThen(appended(absentCoordinate(axis)));
			}

			return axisCoordinates.andThen(CoordinateSet::of);
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
	}

	private static final class ToProviderOfIterableTransformer<T, C extends Provider<? extends Iterable<T>> & HasMultipleValues<T>> implements Transformer<Provider<? extends Iterable<T>>, Iterable<Provider<T>>> {
		private final Supplier<C> containerSupplier;

		public ToProviderOfIterableTransformer(Supplier<C> containerSupplier) {
			this.containerSupplier = containerSupplier;
		}

		@Override
		public Provider<? extends Iterable<T>> transform(Iterable<Provider<T>> providers) {
			final C container = containerSupplier.get();
			providers.forEach(((HasMultipleValues<T>) container)::add);
			return container;
		}
	}
}
