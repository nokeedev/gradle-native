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

import com.google.common.collect.MoreCollectors;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.ModelProperty;
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
import java.util.function.Predicate;

import static dev.nokee.model.internal.type.GradlePropertyTypes.setProperty;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.runtime.core.Coordinates.isAbsentCoordinate;

public final class ModelBackedVariantDimensions implements VariantDimensions {
	private final DomainObjectIdentifier owner;
	private final ModelRegistry registry;
	private final DimensionPropertyRegistrationFactory dimensionsPropertyFactory;

	public ModelBackedVariantDimensions(DomainObjectIdentifier owner, ModelRegistry registry, DimensionPropertyRegistrationFactory dimensionsPropertyFactory) {
		this.owner = owner;
		this.registry = registry;
		this.dimensionsPropertyFactory = dimensionsPropertyFactory;
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType) {
		val result = registry.register(dimensionsPropertyFactory.newAxisProperty(ModelPropertyIdentifier.of(owner, StringUtils.uncapitalize(axisType.getSimpleName())), CoordinateAxis.of(axisType)));
		return ((ModelProperty<?>) result).asProperty(setProperty(of(axisType)));
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType, Action<? super VariantDimensionBuilder> action) {
		Objects.requireNonNull(axisType);
		Objects.requireNonNull(action);
		val builder = dimensionsPropertyFactory.newAxisProperty(ModelPropertyIdentifier.of(owner, StringUtils.uncapitalize(axisType.getSimpleName())));
		builder.axis(CoordinateAxis.of(axisType));

		action.execute(new VariantDimensionBuilderAdapter(CoordinateAxis.of(axisType), builder));

		val result = registry.register(builder.build());
		return ((ModelProperty<?>) result).asProperty(setProperty(of(axisType)));
	}

	@Override
	public <T> SetProperty<T> newAxis(Class<T> axisType, @SuppressWarnings("rawtypes") Closure closure) {
		Objects.requireNonNull(closure);
		return newAxis(axisType, ConfigureUtil.configureUsing(closure));
	}

	private static final class VariantDimensionBuilderAdapter implements VariantDimensionBuilder {
		private final CoordinateAxis<?> axis;
		private final DimensionPropertyRegistrationFactory.Builder delegate;

		public <T> VariantDimensionBuilderAdapter(CoordinateAxis<T> axis, DimensionPropertyRegistrationFactory.Builder delegate) {
			this.axis = axis;
			this.delegate = delegate;
		}

		@Override
		public VariantDimensionBuilder onlyOn(Object otherAxisValue) {
			delegate.includeEmptyCoordinate();
			delegate.filterVariant(new OnlyOnPredicate(axis, otherAxisValue));
			return this;
		}

		@Override
		public VariantDimensionBuilder exceptOn(Object otherAxisValue) {
			delegate.includeEmptyCoordinate();
			delegate.filterVariant(new OnlyOnPredicate(axis, otherAxisValue).negate());
			return this;
		}
	}

	private static final class OnlyOnPredicate implements Predicate<BuildVariantInternal> {
		private final CoordinateAxis<?> axis;
		private final Object otherAxisValue;

		private OnlyOnPredicate(CoordinateAxis<?> axis, Object otherAxisValue) {
			this.axis = axis;
			this.otherAxisValue = otherAxisValue;
		}

		@Override
		public boolean test(BuildVariantInternal buildVariant) {
			val axisValue = buildVariant.getDimensions().stream().filter(c -> c.getAxis().equals(axis)).collect(MoreCollectors.onlyElement());
			return (!isAbsentCoordinate(axisValue) && !buildVariant.hasAxisOf(otherAxisValue)) || (isAbsentCoordinate(axisValue) && buildVariant.hasAxisOf(otherAxisValue));
		}
	}
}
