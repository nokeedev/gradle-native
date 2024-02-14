/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.rules;

import com.google.common.collect.Streams;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.nativebase.internal.TargetedNativeComponentSpec;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
import org.gradle.api.Action;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public final class TargetedNativeComponentDimensionsRule implements Action<TargetedNativeComponentSpec> {
	private final ToolChainSelectorInternal toolChainSelector;

	public TargetedNativeComponentDimensionsRule(ToolChainSelectorInternal toolChainSelector) {
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	public void execute(TargetedNativeComponentSpec component) {
		// Ordering is important here
		new TargetMachinesPropertyRegistrationRule(toolChainSelector).execute(component);
		new TargetBuildTypesPropertyRegistrationRule().execute(component);
		new TargetLinkagesPropertyRegistrationRule().execute(component);
	}

	public static final class TargetBuildTypesPropertyRegistrationRule implements Action<TargetedNativeComponentSpec> {
		@Override
		public void execute(TargetedNativeComponentSpec component) {
			final DefaultVariantDimensions dimensions = (DefaultVariantDimensions) ((VariantAwareComponent<?>) component).getDimensions();
			val targetBuildTypes = dimensions.getDimensionFactory().newAxisProperty(BuildType.BUILD_TYPE_COORDINATE_AXIS).elementType(TargetBuildType.class).build();
			dimensions.getElements().add(targetBuildTypes);
			targetBuildTypes.getProperty().value(component.getTargetBuildTypes()).disallowChanges();

			component.getTargetBuildTypes().finalizeValueOnRead();
		}
	}

	public static final class TargetLinkagesPropertyRegistrationRule implements Action<TargetedNativeComponentSpec> {
		@Override
		public void execute(TargetedNativeComponentSpec component) {
			final DefaultVariantDimensions dimensions = (DefaultVariantDimensions) ((VariantAwareComponent<?>) component).getDimensions();
			val targetLinkages = dimensions.getDimensionFactory().newAxisProperty(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).elementType(TargetLinkage.class).build();
			dimensions.getElements().add(targetLinkages);
			targetLinkages.getProperty().value(component.getTargetLinkages()).disallowChanges();

			component.getTargetLinkages().finalizeValueOnRead();
		}
	}

	public static final class TargetMachinesPropertyRegistrationRule implements Action<TargetedNativeComponentSpec> {
		private final ToolChainSelectorInternal toolChainSelector;

		public TargetMachinesPropertyRegistrationRule(ToolChainSelectorInternal toolChainSelector) {
			this.toolChainSelector = toolChainSelector;
		}

		@Override
		public void execute(TargetedNativeComponentSpec component) {
			final DefaultVariantDimensions dimensions = (DefaultVariantDimensions) ((VariantAwareComponent<?>) component).getDimensions();
			val targetMachines = dimensions.getDimensionFactory().newAxisProperty(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
				.validateUsing((Iterable<Coordinate<TargetMachine>> it) -> assertTargetMachinesAreKnown(it, toolChainSelector))
				.build();
			dimensions.getElements().add(targetMachines);
			targetMachines.getProperty().value(component.getTargetMachines()).disallowChanges();

			component.getTargetMachines().finalizeValueOnRead();
		}

		private static void assertTargetMachinesAreKnown(Iterable<Coordinate<TargetMachine>> targetMachines, ToolChainSelectorInternal toolChainSelector) {
			List<TargetMachine> unknownTargetMachines = Streams.stream(targetMachines).filter(it -> !toolChainSelector.isKnown(it.getValue())).map(Coordinate::getValue).collect(Collectors.toList());
			if (!unknownTargetMachines.isEmpty()) {
				throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
			}
		}
	}
}
