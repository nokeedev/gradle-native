package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;

public class NativePlatformFactory {
	public static NativePlatformInternal create(TargetMachine targetMachine) {
		NativePlatformInternal result = new DefaultNativePlatform(platformNameFor(targetMachine));
		result.architecture(architecturePlatformNameFor(targetMachine.getArchitecture()));
		result.operatingSystem(operatingSystemPlatformNameFor(targetMachine.getOperatingSystemFamily()));
		return result;
	}

	private static String architecturePlatformNameFor(MachineArchitecture architecture) {
		return architecture.getCanonicalName();
	}

	private static String operatingSystemPlatformNameFor(OperatingSystemFamily operatingSystemFamily) {
		if (operatingSystemFamily.isMacOs()) {
			return "osx";
		}
		return operatingSystemFamily.getCanonicalName();
	}

	public static String platformNameFor(TargetMachine targetMachine) {
		return platformNameFor(targetMachine.getOperatingSystemFamily(), targetMachine.getArchitecture());
	}

	public static String platformNameFor(OperatingSystemFamily osFamily, MachineArchitecture architecture) {
		return osFamily.getCanonicalName() + architecture.getCanonicalName();
	}
}
