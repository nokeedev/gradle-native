package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import lombok.*;
import org.gradle.api.Named;

import javax.annotation.Nonnull;

import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE) /** Use {@link DefaultMachineArchitecture#forName(String)} instead */
public abstract class DefaultMachineArchitecture implements MachineArchitecture, Named, Coordinate<MachineArchitecture> {
	@Getter @Nonnull private final String name;

	public static CoordinateAxis<MachineArchitecture> MACHINE_ARCHITECTURE_COORDINATE_AXIS = CoordinateAxis.of(MachineArchitecture.class);
	public static DefaultMachineArchitecture X86 = new KnownMachineArchitecture("x86", PointerSize.BIT32);
	public static DefaultMachineArchitecture X86_64 = new KnownMachineArchitecture("x86-64", PointerSize.BIT64);
	public static DefaultMachineArchitecture POWER_PC = new KnownMachineArchitecture("PowerPC", PointerSize.BIT32);
	public static DefaultMachineArchitecture POWER_PC_64 = new KnownMachineArchitecture("PowerPC64", PointerSize.BIT64);
	public static DefaultMachineArchitecture SPARC = new KnownMachineArchitecture("Sparc", PointerSize.BIT32);
	public static DefaultMachineArchitecture SPARC64 = new KnownMachineArchitecture("Sparc64", PointerSize.BIT64);
	public static DefaultMachineArchitecture ITANIUM = new KnownMachineArchitecture("Itanium", PointerSize.BIT64);
	public static DefaultMachineArchitecture PA_RISC = new KnownMachineArchitecture("PA-RISC", PointerSize.BIT64);
	public static DefaultMachineArchitecture HOST = forName(System.getProperty("os.arch"));

	private enum PointerSize {
		BIT32 {
			@Override
			boolean is32Bit() {
				return true;
			}

			@Override
			boolean is64Bit() {
				return false;
			}
		},
		BIT64 {
			@Override
			boolean is32Bit() {
				return false;
			}

			@Override
			boolean is64Bit() {
				return true;
			}
		};

		abstract boolean is32Bit();
		abstract boolean is64Bit();
	}

	public static DefaultMachineArchitecture forName(String name) {
		String archName = name.toLowerCase();
		if (asList("x86", "i386", "ia-32", "i686").contains(archName)) {
			return X86;
		} else if (asList("x86-64", "x86_64", "amd64", "x64").contains(archName)) {
			return X86_64;
		} else if (asList("ppc", "powerpc").contains(archName)) {
			return POWER_PC;
		} else if (asList("ppc64", "powerpc64").contains(archName)) {
			return POWER_PC_64;
		} else if (asList("sparc-v7", "sparc-v8", "sparc", "sparc32").contains(archName)) {
			return SPARC;
		} else if (asList("sparc-v9", "sparc64", "ultrasparc").contains(archName)) {
			return SPARC64;
		} else if (asList("ia-64", "ia64", "ia64n", "itanium").contains(archName)) {
			return ITANIUM;
		} else if (asList("pa-risc", "pa_risc").contains(archName)) {
			return PA_RISC;
		}

		// ARM is an umbrella for several different distinct architecture (A32, A64 and T32) which can be dynamically switched.
		//   It isn't a straight match between os.arch name and machine architecture.

		else {
			return new UnknownMachineArchitecture(archName); // unknown architecture, use as-is
		}
	}

	@Override
	public CoordinateAxis<MachineArchitecture> getAxis() {
		return MACHINE_ARCHITECTURE_COORDINATE_AXIS;
	}

	private static final class MachineArchitectureX86 extends DefaultMachineArchitecture {
		MachineArchitectureX86() {
			super("x86");
		}

		@Override
		public boolean is32Bit() {
			return true;
		}

		@Override
		public boolean is64Bit() {
			return false;
		}
	}

	private static final class KnownMachineArchitecture extends DefaultMachineArchitecture {
		private final PointerSize pointerSize;

		KnownMachineArchitecture(String canonicalName, PointerSize pointerSize) {
			super(canonicalName);
			this.pointerSize = pointerSize;
		}

		@Override
		public boolean is32Bit() {
			return pointerSize.is32Bit();
		}

		@Override
		public boolean is64Bit() {
			return pointerSize.is64Bit();
		}
	}

	public static final class UnknownMachineArchitecture extends DefaultMachineArchitecture {
		public UnknownMachineArchitecture(String name) {
			super(name);
		}

		@Override
		public boolean is32Bit() {
			throw new UnsupportedOperationException("Unknown architecture");
		}

		@Override
		public boolean is64Bit() {
			throw new UnsupportedOperationException("Unknown architecture");
		}
	}
}
