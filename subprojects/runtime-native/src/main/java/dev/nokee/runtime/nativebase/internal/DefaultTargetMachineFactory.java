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
package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.*;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;

final class DefaultTargetMachineFactory implements TargetMachineFactory {
	private static final MachineArchitecture HOST_ARCH = MachineArchitecture.forName(System.getProperty("os.arch"));
	private static final OperatingSystemFamily WINDOWS = OperatingSystemFamily.forName(OperatingSystemFamily.WINDOWS);
	private static final OperatingSystemFamily LINUX = OperatingSystemFamily.forName(OperatingSystemFamily.LINUX);
	private static final OperatingSystemFamily MACOS = OperatingSystemFamily.forName(OperatingSystemFamily.MACOS);
	private static final OperatingSystemFamily FREE_BSD = OperatingSystemFamily.forName(OperatingSystemFamily.FREE_BSD);

	@Override
	public TargetMachineBuilder getWindows() {
		return new DefaultTargetMachineBuilder(WINDOWS);
	}

	@Override
	public TargetMachineBuilder getLinux() {
		return new DefaultTargetMachineBuilder(LINUX);
	}

	@Override
	public TargetMachineBuilder getMacOS() {
		return new DefaultTargetMachineBuilder(MACOS);
	}

	@Override
	public TargetMachineBuilder getFreeBSD() {
		return new DefaultTargetMachineBuilder(FREE_BSD);
	}

	// Internal, exposed for convenience.
	// Host is just a placeholder, not a target machine.
	public TargetMachine getHost() {
		return TargetMachines.host();
	}

	@Override
	public TargetMachineBuilder os(String name) {
		return new DefaultTargetMachineBuilder(OperatingSystemFamily.named(name));
	}

	private static final class DefaultTargetMachineBuilder extends AbstractTargetMachine implements TargetMachineBuilder, Named {
		private static final MachineArchitecture X86 = MachineArchitecture.forName(MachineArchitecture.X86);
		private static final MachineArchitecture X86_64 = MachineArchitecture.forName(MachineArchitecture.X86_64);

		public DefaultTargetMachineBuilder(OperatingSystemFamily operatingSystemFamily) {
			super(operatingSystemFamily, HOST_ARCH);
		}

		@Override
		public String getName() {
			return getOperatingSystemFamily().toString();
		}

		@Override
		public TargetMachine getX86() {
			return new DefaultTargetMachine(getOperatingSystemFamily(), X86);
		}

		@Override
		public TargetMachine getX86_64() {
			return new DefaultTargetMachine(getOperatingSystemFamily(), X86_64);
		}

		@Override
		public TargetMachine architecture(String name) {
			return new DefaultTargetMachine(getOperatingSystemFamily(), MachineArchitecture.named(name));
		}

		@Override
		public String toString() {
			return getOperatingSystemFamily().toString() + ":host";
		}
	}

	private static final class DefaultTargetMachine extends AbstractTargetMachine implements Named {
		DefaultTargetMachine(OperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
			super(operatingSystemFamily, architecture);
		}

		@Override
		public String toString() {
			return getOperatingSystemFamily().toString() + ":" + getArchitecture().toString();
		}

		@Override
		public String getName() {
			return getOperatingSystemFamily().toString() + StringUtils.capitalize(getArchitecture().toString());
		}
	}
}
