/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.model.internal.KnownModelObject;
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.nativebase.BuildType;

import java.util.Comparator;

import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS;
import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS;

public class NativeVariantComparators {
	public static Comparator<Variant> preferHostMachineArchitectureOnVariant() {
		return new SingleVariantDimensionComparator<>(ARCHITECTURE_COORDINATE_AXIS, new PreferHostMachineArchitectureComparator());
	}

	public static Comparator<Variant> preferHostOperatingSystemFamilyOnVariant() {
		return new SingleVariantDimensionComparator<>(OPERATING_SYSTEM_COORDINATE_AXIS, new PreferHostOperatingSystemFamilyComparator());
	}

	public static Comparator<KnownModelObject<? extends Variant>> preferDebugBuildType() {
		return new SingleVariantElementDimensionComparator<>(BuildType.BUILD_TYPE_COORDINATE_AXIS, new PreferDebugBuildTypeComparator());
	}

	public static Comparator<KnownModelObject<? extends Variant>> preferSharedBinaryLinkage() {
		return new SingleVariantElementDimensionComparator<>(BINARY_LINKAGE_COORDINATE_AXIS, new PreferSharedBinaryLinkageComparator());
	}

	public static Comparator<KnownModelObject<? extends Variant>> preferHostMachineArchitecture() {
		return new SingleVariantElementDimensionComparator<>(ARCHITECTURE_COORDINATE_AXIS, new PreferHostMachineArchitectureComparator());
	}

	public static Comparator<KnownModelObject<? extends Variant>> preferHostOperatingSystemFamily() {
		return new SingleVariantElementDimensionComparator<>(OPERATING_SYSTEM_COORDINATE_AXIS, new PreferHostOperatingSystemFamilyComparator());
	}
}
