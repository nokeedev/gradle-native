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

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.VariantDimensions;
import dev.nokee.runtime.core.CoordinateAxis;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.provider.SetProperty;

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
		return result.as(SetProperty.class).get();
	}
}
