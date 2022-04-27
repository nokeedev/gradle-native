/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import lombok.val;

public final class TargetBuildTypesPropertyRegistrationRule extends ModelActionWithInputs.ModelAction2<ModelProjection, IdentifierComponent> {
	private final DimensionPropertyRegistrationFactory dimensions;
	private final ModelRegistry registry;

	public TargetBuildTypesPropertyRegistrationRule(DimensionPropertyRegistrationFactory dimensions, ModelRegistry registry) {
		super(ModelComponentReference.ofProjection(ModelBackedTargetBuildTypeAwareComponentMixIn.class), ModelComponentReference.of(IdentifierComponent.class));
		this.dimensions = dimensions;
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection tag, IdentifierComponent identifier) {
		val targetBuildTypes = registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier.get(), "targetBuildTypes"))
			.elementType(TargetBuildType.class)
			.axis(BuildType.BUILD_TYPE_COORDINATE_AXIS)
			.defaultValue(TargetBuildTypes.DEFAULT)
			.build());
		entity.addComponent(new TargetBuildTypesPropertyComponent(ModelNodes.of(targetBuildTypes)));
	}
}
