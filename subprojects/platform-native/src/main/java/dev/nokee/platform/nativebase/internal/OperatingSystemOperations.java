package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import org.gradle.internal.os.OperatingSystem;

public class OperatingSystemOperations {
	private final OperatingSystem osInternal;

	public OperatingSystemOperations(OperatingSystem osInternal) {
		this.osInternal = osInternal;
	}
	public static OperatingSystemOperations of(OperatingSystemFamily osFamily) {
		if (osFamily.isWindows()) {
			return new OperatingSystemOperations(OperatingSystem.WINDOWS);
		} else if (osFamily.isMacOS()) {
			return new OperatingSystemOperations(OperatingSystem.MAC_OS);
		} else if (osFamily.isLinux()) {
			return new OperatingSystemOperations(OperatingSystem.LINUX);
		} else if (osFamily.isFreeBSD()) {
			return new OperatingSystemOperations(OperatingSystem.FREE_BSD);
		} else if (osFamily.isiOS()) {
			return new OperatingSystemOperations(OperatingSystem.MAC_OS);
		}
		// Assume *nix as default operating system
		return new OperatingSystemOperations(OperatingSystem.LINUX);
	}

	public String getSharedLibraryName(String libraryName) {
		return osInternal.getSharedLibraryName(libraryName);
	}

	public String getStaticLibraryName(String libraryName) {
		return osInternal.getStaticLibraryName(libraryName);
	}

	public String getExecutableName(String executableName) {
		return osInternal.getExecutableName(executableName);
	}
}
