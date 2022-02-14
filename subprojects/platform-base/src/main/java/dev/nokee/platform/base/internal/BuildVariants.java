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
import dev.nokee.model.internal.core.DescendantNodes;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.CoordinateSpace;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
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
			.map(transformEach(it -> it.getComponent(componentOf(VariantDimensionValuesComponent.class)).asProvider()))
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
