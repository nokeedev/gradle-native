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
		};

		public abstract TargetMachineBuilder apply(TargetMachineFactory factory);

		public abstract void assertOperatingSystem(TargetMachine machine);
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
		};

		public abstract TargetMachine apply(TargetMachineBuilder builder);

		public abstract void assertMachineArchitecture(TargetMachine machine);
	}

	static Stream<Arguments> configurers() {
		return Sets.cartesianProduct(copyOf(OperatingSystemFamilyConfigurer.values()), copyOf(MachineArchitectureConfigurer.values())).stream().map(it -> Arguments.of(it.get(0), it.get(1)));
	}
}
