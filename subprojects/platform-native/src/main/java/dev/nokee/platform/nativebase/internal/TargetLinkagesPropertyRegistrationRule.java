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

import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetLinkage;
import lombok.val;

public final class TargetLinkagesPropertyRegistrationRule extends ModelActionWithInputs.ModelAction1<ModelProjection> {
	private final DimensionPropertyRegistrationFactory dimensions;
	private final ModelRegistry registry;

	public TargetLinkagesPropertyRegistrationRule(DimensionPropertyRegistrationFactory dimensions, ModelRegistry registry) {
		super(ModelComponentReference.ofProjection(ModelBackedTargetLinkageAwareComponentMixIn.class));
		this.dimensions = dimensions;
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection tag) {
		val targetLinkages = registry.register(ModelRegistration.builder().withComponent(new ElementNameComponent("targetLinkages")).withComponent(new ParentComponent(entity)).mergeFrom(dimensions.newAxisProperty()
			.elementType(TargetLinkage.class)
			.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
			.build()).build());
		entity.addComponent(new TargetLinkagesPropertyComponent(ModelNodes.of(targetLinkages)));
	}
}
