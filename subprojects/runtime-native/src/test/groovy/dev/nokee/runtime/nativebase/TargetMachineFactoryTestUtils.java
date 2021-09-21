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
package dev.nokee.runtime.nativebase;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.copyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

final class TargetMachineFactoryTestUtils {
	enum OperatingSystemFamilyConfigurer {
		WINDOWS {
			@Override
			public TargetMachineBuilder apply(TargetMachineFactory factory) {
				return factory.getWindows();
			}

			@Override
			public void assertOperatingSystem(TargetMachine machine) {
				assertThat(machine.getOperatingSystemFamily(), equalTo(OperatingSystemFamily.forName(OperatingSystemFamily.WINDOWS)));
			}

			@Override
			public String getName() {
				return OperatingSystemFamily.WINDOWS;
			}
		},
		LINUX {
			@Override
			public TargetMachineBuilder apply(TargetMachineFactory factory) {
				return factory.getLinux();
			}

			@Override
			public void assertOperatingSystem(TargetMachine machine) {
				assertThat(machine.getOperatingSystemFamily(), equalTo(OperatingSystemFamily.forName(OperatingSystemFamily.LINUX)));
			}

			@Override
			public String getName() {
				return OperatingSystemFamily.LINUX;
			}
		},
		MACOS {
			@Override
			public TargetMachineBuilder apply(TargetMachineFactory factory) {
				return factory.getMacOS();
			}

			@Override
			public void assertOperatingSystem(TargetMachine machine) {
				assertThat(machine.getOperatingSystemFamily(), equalTo(OperatingSystemFamily.forName(OperatingSystemFamily.MACOS)));
			}

			@Override
			public String getName() {
				return OperatingSystemFamily.MACOS;
			}
		},
		FREE_BSD {
			@Override
			public TargetMachineBuilder apply(TargetMachineFactory factory) {
				return factory.getFreeBSD();
			}

			@Override
			public void assertOperatingSystem(TargetMachine machine) {
				assertThat(machine.getOperatingSystemFamily(), equalTo(OperatingSystemFamily.forName(OperatingSystemFamily.FREE_BSD)));
			}

			@Override
			public String getName() {
				return OperatingSystemFamily.FREE_BSD;
			}
		};

		public abstract TargetMachineBuilder apply(TargetMachineFactory factory);

		public abstract void assertOperatingSystem(TargetMachine machine);

		public abstract String getName();
	}

	enum MachineArchitectureConfigurer {
		X86 {
			@Override
			public TargetMachine apply(TargetMachineBuilder builder) {
				return builder.getX86();
			}

			@Override
			public void assertMachineArchitecture(TargetMachine machine) {
				assertThat(machine.getArchitecture(), equalTo(MachineArchitecture.forName(MachineArchitecture.X86)));
			}

			@Override
			public String getName() {
				return MachineArchitecture.X86;
			}
		},
		X86_64 {
			@Override
			public TargetMachine apply(TargetMachineBuilder builder) {
				return builder.getX86_64();
			}

			@Override
			public void assertMachineArchitecture(TargetMachine machine) {
				assertThat(machine.getArchitecture(), equalTo(MachineArchitecture.forName(MachineArchitecture.X86_64)));
			}

			@Override
			public String getName() {
				return MachineArchitecture.X86_64;
			}
		},
		HOST {
			@Override
			public TargetMachine apply(TargetMachineBuilder builder) {
				return builder;
			}

			@Override
			public void assertMachineArchitecture(TargetMachine machine) {
				assertThat(machine.getArchitecture(), equalTo(MachineArchitecture.forName(System.getProperty("os.arch"))));
			}

			@Override
			public String getName() {
				return MachineArchitecture.forName(System.getProperty("os.arch")).getCanonicalName();
			}
		};

		public abstract TargetMachine apply(TargetMachineBuilder builder);

		public abstract void assertMachineArchitecture(TargetMachine machine);

		public abstract String getName();
	}

	static Stream<Arguments> configurers() {
		return Sets.cartesianProduct(copyOf(OperatingSystemFamilyConfigurer.values()), copyOf(MachineArchitectureConfigurer.values())).stream().map(it -> Arguments.of(it.get(0), it.get(1)));
	}
}
