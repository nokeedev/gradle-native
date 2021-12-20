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
import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.core.DescendantNodes;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.CoordinateSpace;
import dev.nokee.utils.Cast;
import dev.nokee.utils.TransformerUtils;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.*;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.utils.TransformerUtils.toListTransformer;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class BuildVariants {
	private final Provider<List<CoordinateSet<?>>> dimensions;
	private final Provider<CoordinateSpace> finalSpace;
	private final SetProperty<BuildVariant> buildVariants;

	public BuildVariants(ModelNode entity, ProviderFactory providers, ObjectFactory objects) {
		Provider<List<DimensionPropertyRegistrationFactory.Dimension<?>>> dimensions = providers.provider(() -> {
			val nodes = entity.getComponent(ModelComponentType.componentOf(DescendantNodes.class)).getDirectDescendants().stream();
			val dimensionNodes = nodes.filter(it -> it.hasComponent(ModelComponentType.componentOf(DimensionPropertyRegistrationFactory.Dimension.class)));
			return dimensionNodes.map(it -> it.getComponent(ModelComponentType.componentOf(DimensionPropertyRegistrationFactory.Dimension.class))).map(it -> (DimensionPropertyRegistrationFactory.Dimension<?>) it).collect(Collectors.toList());
		});
		this.dimensions = dimensions.map(transformEach(it -> it.get())).map(toListTransformer());
		this.finalSpace = this.dimensions.map(CoordinateSpace::cartesianProduct);
		this.buildVariants = objects.setProperty(BuildVariant.class);

		buildVariants.convention(finalSpace.map(DefaultBuildVariant::fromSpace).map(buildVariants -> {
			val allFilters = dimensions.get().stream()
				.flatMap(it -> it.getFilters().stream())
				.collect(Collectors.toList());
			return buildVariants.stream().filter(buildVariant -> {
				return allFilters.stream()
					.noneMatch(it -> it.test(buildVariant));
			}).collect(ImmutableSet.toImmutableSet());
		}));
		buildVariants.finalizeValueOnRead();
		buildVariants.disallowChanges();
	}



	public Provider<List<CoordinateSet<?>>> dimensions() {
		return dimensions;
	}

	public Provider<Set<BuildVariant>> get() {
		return buildVariants;
	}
}
