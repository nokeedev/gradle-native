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
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
import org.gradle.api.Action;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public final class TargetMachinesPropertyRegistrationRule implements Action<Component> {
	private final ToolChainSelectorInternal toolChainSelector;

	public TargetMachinesPropertyRegistrationRule(ToolChainSelectorInternal toolChainSelector) {
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	public void execute(Component component) {
		if (component instanceof TargetMachineAwareComponent && component instanceof VariantAwareComponent) {
			final DefaultVariantDimensions dimensions = (DefaultVariantDimensions) ((VariantAwareComponent<?>) component).getDimensions();
			val targetMachines = dimensions.getDimensionFactory().newAxisProperty(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
				.validateUsing((Iterable<Coordinate<TargetMachine>> it) -> assertTargetMachinesAreKnown(it, toolChainSelector))
				.build();
			dimensions.getElements().add(targetMachines);
			targetMachines.getProperty().value(((TargetMachineAwareComponent) component).getTargetMachines()).disallowChanges();

			((TargetMachineAwareComponent) component).getTargetMachines().finalizeValueOnRead();
		}
	}

	private static void assertTargetMachinesAreKnown(Iterable<Coordinate<TargetMachine>> targetMachines, ToolChainSelectorInternal toolChainSelector) {
		List<TargetMachine> unknownTargetMachines = Streams.stream(targetMachines).filter(it -> !toolChainSelector.isKnown(it.getValue())).map(Coordinate::getValue).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
		}
	}
}
