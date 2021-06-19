package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;

import java.util.Comparator;

import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS;
import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS;

public class NativeVariantComparators {
	public static Comparator<VariantInternal> preferDebugBuildType() {
		return new SingleVariantDimensionComparator<>(BuildType.BUILD_TYPE_COORDINATE_AXIS, new PreferDebugBuildTypeComparator());
	}

	public static Comparator<VariantInternal> preferSharedBinaryLinkage() {
		return new SingleVariantDimensionComparator<>(BINARY_LINKAGE_COORDINATE_AXIS, new PreferSharedBinaryLinkageComparator());
	}

	public static Comparator<VariantInternal> preferHostMachineArchitecture() {
		return new SingleVariantDimensionComparator<>(ARCHITECTURE_COORDINATE_AXIS, new PreferHostMachineArchitectureComparator());
	}

	public static Comparator<VariantInternal> preferHostOperatingSystemFamily() {
		return new SingleVariantDimensionComparator<>(DefaultOperatingSystemFamily.OPERATING_SYSTEM_FAMILY_COORDINATE_AXIS, new PreferHostOperatingSystemFamilyComparator());
	}
}
