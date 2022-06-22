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

import com.google.common.collect.Streams;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
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
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.val;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public final class TargetMachinesPropertyRegistrationRule extends ModelActionWithInputs.ModelAction1<ModelProjection> {
	private final DimensionPropertyRegistrationFactory dimensions;
	private final ModelRegistry registry;
	private final ToolChainSelectorInternal toolChainSelector;

	public TargetMachinesPropertyRegistrationRule(DimensionPropertyRegistrationFactory dimensions, ModelRegistry registry, ToolChainSelectorInternal toolChainSelector) {
		super(ModelComponentReference.ofProjection(ModelBackedTargetMachineAwareComponentMixIn.class));
		this.dimensions = dimensions;
		this.registry = registry;
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection tag) {
		val targetMachines = registry.register(ModelRegistration.builder().withComponent(new ElementNameComponent("targetMachines")).withComponent(new ParentComponent(entity)).mergeFrom(dimensions.newAxisProperty()
			.axis(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
			.defaultValue(TargetMachines.host())
			.validateUsing((Iterable<Coordinate<TargetMachine>> it) -> assertTargetMachinesAreKnown(it, toolChainSelector))
			.build()).build());
		entity.addComponent(new TargetMachinesPropertyComponent(ModelNodes.of(targetMachines)));
	}

	private static void assertTargetMachinesAreKnown(Iterable<Coordinate<TargetMachine>> targetMachines, ToolChainSelectorInternal toolChainSelector) {
		List<TargetMachine> unknownTargetMachines = Streams.stream(targetMachines).filter(it -> !toolChainSelector.isKnown(it.getValue())).map(Coordinate::getValue).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
		}
	}
}
