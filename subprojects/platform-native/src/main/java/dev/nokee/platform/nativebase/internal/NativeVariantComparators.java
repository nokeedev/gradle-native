package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;

import java.util.Comparator;

public class NativeVariantComparators {
	public static Comparator<VariantInternal> preferDebugBuildType() {
		return new SingleVariantDimensionComparator<>(BaseTargetBuildType.DIMENSION_TYPE, new PreferDebugBuildTypeComparator());
	}

	public static Comparator<VariantInternal> preferSharedBinaryLinkage() {
		return new SingleVariantDimensionComparator<>(DefaultBinaryLinkage.DIMENSION_TYPE, new PreferSharedBinaryLinkageComparator());
	}

	public static Comparator<VariantInternal> preferHostMachineArchitecture() {
		return new SingleVariantDimensionComparator<>(DefaultMachineArchitecture.DIMENSION_TYPE, new PreferHostMachineArchitectureComparator());
	}

	public static Comparator<VariantInternal> preferHostOperatingSystemFamily() {
		return new SingleVariantDimensionComparator<>(DefaultOperatingSystemFamily.DIMENSION_TYPE, new PreferHostOperatingSystemFamilyComparator());
	}
}
