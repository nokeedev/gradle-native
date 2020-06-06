package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.gradle.api.Named;

import javax.annotation.Nonnull;

import static java.util.Arrays.asList;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE) /** Use {@link DefaultMachineArchitecture#forName(String)} instead */
public abstract class DefaultMachineArchitecture implements MachineArchitecture, Named, Dimension {
	@Getter @Nonnull private final String name;

	public static DimensionType<DefaultMachineArchitecture> DIMENSION_TYPE = new DefaultDimensionType<>(DefaultMachineArchitecture.class);
	public static DefaultMachineArchitecture X86 = new MachineArchitectureX86();
	public static DefaultMachineArchitecture X86_64 = new MachineArchitectureX86_64();
	public static DefaultMachineArchitecture HOST = forName(System.getProperty("os.arch"));

	public static DefaultMachineArchitecture forName(String name) {
		String archName = name.toLowerCase();
		if (asList("x86", "i386", "ia-32", "i686").contains(archName)) {
			return X86;
		} else if (asList("x86-64", "x86_64", "amd64", "x64").contains(archName)) {
			return X86_64;
		} else {
			throw new UnsupportedOperationException("Unsupported architecture of name '" + archName + "'");
		}
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

		@Override
		public DimensionType<DefaultMachineArchitecture> getType() {
			return DIMENSION_TYPE;
		}
	}

	private static final class MachineArchitectureX86_64 extends DefaultMachineArchitecture {
		MachineArchitectureX86_64() {
			super("x86-64");
		}

		@Override
		public boolean is32Bit() {
			return false;
		}

		@Override
		public boolean is64Bit() {
			return true;
		}

		@Override
		public DimensionType<DefaultMachineArchitecture> getType() {
			return DIMENSION_TYPE;
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

		@Override
		public DimensionType<DefaultMachineArchitecture> getType() {
			return DIMENSION_TYPE;
		}
	}
}
