/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.nativebase.internal;

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
