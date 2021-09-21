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

import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import org.gradle.internal.os.OperatingSystem;

public class OperatingSystemOperations {
	private final OperatingSystem osInternal;

	public OperatingSystemOperations(OperatingSystem osInternal) {
		this.osInternal = osInternal;
	}

	public static OperatingSystemOperations ofHost() {
		return of(OperatingSystemFamily.forName(System.getProperty("os.name")));
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
