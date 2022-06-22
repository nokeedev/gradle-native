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

import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProperty;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.VariantDimensionBuilder;
import dev.nokee.platform.base.VariantDimensions;
import dev.nokee.runtime.core.CoordinateAxis;
import groovy.lang.Closure;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.provider.SetProperty;
import org.gradle.util.ConfigureUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import static dev.nokee.model.internal.type.GradlePropertyTypes.setProperty;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ModelBackedVariantDimensions implements VariantDimensions, ModelComponent {
	private final ModelNode owner;
	private final ModelRegistry registry;
	private final DimensionPropertyRegistrationFactory dimensionsPropertyFactory;

	public ModelBackedVariantDimensions(ModelNode owner, ModelRegistry registry, DimensionPropertyRegistrationFactory dimensionsPropertyFactory) {
		this.owner = owner;
		this.registry = registry;
		this.dimensionsPropertyFactory = dimensionsPropertyFactory;
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType) {
		val result = registry.register(ModelRegistration.builder()
			.withComponent(new ElementNameComponent(StringUtils.uncapitalize(axisType.getSimpleName())))
			.withComponent(new ParentComponent(owner))
			.mergeFrom(dimensionsPropertyFactory.newAxisProperty(CoordinateAxis.of(axisType)))
			.build());
		return ((ModelProperty<?>) result).asProperty(setProperty(of(axisType)));
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType, Action<? super VariantDimensionBuilder<T>> action) {
		Objects.requireNonNull(axisType);
		Objects.requireNonNull(action);
		val builder = ModelRegistration.builder()
			.withComponent(new ElementNameComponent(StringUtils.uncapitalize(axisType.getSimpleName())))
			.withComponent(new ParentComponent(owner));
		val axisBuilder = dimensionsPropertyFactory.newAxisProperty();
		axisBuilder.axis(CoordinateAxis.of(axisType));

		action.execute(new VariantDimensionBuilderAdapter<>(new VariantDimensionBuilderAdapter.Callback<T>() {
			@Override
			public <S> void accept(Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate) {
				axisBuilder.includeEmptyCoordinate();
				axisBuilder.filterVariant(new VariantDimensionAxisFilter<>(CoordinateAxis.of(axisType), otherAxisType, predicate));
			}
		}));

		val result = registry.register(builder.mergeFrom(axisBuilder.build()).build());
		return ((ModelProperty<?>) result).asProperty(setProperty(of(axisType)));
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType, @SuppressWarnings("rawtypes") Closure closure) {
		Objects.requireNonNull(closure);
		return newAxis(axisType, ConfigureUtil.configureUsing(closure));
	}
}
