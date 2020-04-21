package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.MachineArchitecture;
import dev.nokee.platform.nativebase.OperatingSystemFamily;
import dev.nokee.platform.nativebase.TargetMachine;
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;

public class NativePlatformFactory {
	public NativePlatformInternal create(TargetMachine targetMachine) {
		NativePlatformInternal result = new DefaultNativePlatform(platformNameFor(targetMachine));
		result.architecture(architecturePlatformNameFor(targetMachine.getArchitecture()));
		result.operatingSystem(operatingSystemPlatformNameFor(targetMachine.getOperatingSystemFamily()));
		return result;
	}

	private static String architecturePlatformNameFor(MachineArchitecture architecture) {
		return ((DefaultMachineArchitecture)architecture).getName();
	}

	private static String operatingSystemPlatformNameFor(OperatingSystemFamily operatingSystemFamily) {
		if (operatingSystemFamily.isMacOs()) {
			return "osx";
		}
		return ((DefaultOperatingSystemFamily)operatingSystemFamily).getName();
	}

	public static String platformNameFor(TargetMachine targetMachine) {
		DefaultTargetMachine targetMachineInternal = (DefaultTargetMachine)targetMachine;
		return targetMachineInternal.getOperatingSystemFamily().getName() + targetMachineInternal.getArchitecture().getName();
	}
}
