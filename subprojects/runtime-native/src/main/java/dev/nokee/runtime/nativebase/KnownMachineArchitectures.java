package dev.nokee.runtime.nativebase;

import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static dev.nokee.runtime.nativebase.KnownMachineArchitectures.PointerSize.BIT32;
import static dev.nokee.runtime.nativebase.KnownMachineArchitectures.PointerSize.BIT64;
import static java.util.Arrays.asList;

final class KnownMachineArchitectures {
	private KnownMachineArchitectures() {}

	private static final List<KnownArchitecture> KNOWN_ARCHITECTURES = asList(
		new KnownArchitecture("x86", BIT32, "i386", "ia-32", "i686"),
		new KnownArchitecture("x86-64", BIT64, "x86_64", "amd64", "x64"),
		new KnownArchitecture("ia-64", BIT64, "ia64", "ia64n", "itanium"),
		new KnownArchitecture("ppc", BIT32, "powerpc"),
		new KnownArchitecture("ppc64", BIT64, "powerpc64"),
		new KnownArchitecture("sparc", BIT32, "sparc-v7", "sparc-v8", "sparc32"),
		new KnownArchitecture("sparc64", BIT64, "sparc-v9", "ultrasparc"),
		new KnownArchitecture("pa-risc", BIT64, "pa_risc")

		// ARM is an umbrella for several different distinct architecture (A32, A64 and T32) which can be dynamically switched.
		//   It isn't a straight match between os.arch name and machine architecture.
	);

	public static MachineArchitecture forName(String name) {
		for (KnownArchitecture knownArchitecture : KNOWN_ARCHITECTURES) {
			if (knownArchitecture.isAlias(name)) {
				return knownArchitecture;
			}
		}
		return new UnknownMachineArchitecture(name); // unknown architecture, use as-is
	}

	public static boolean is32BitArchitecture(String name) {
		return pointerSizeForName(name).is32Bit();
	}

	public static boolean is64BitArchitecture(String name) {
		return pointerSizeForName(name).is64Bit();
	}

	public static String canonical(String name) {
		for (KnownArchitecture knownArchitecture : KNOWN_ARCHITECTURES) {
			if (knownArchitecture.isAlias(name)) {
				return knownArchitecture.getCanonicalName();
			}
		}
		return name.toLowerCase(Locale.CANADA);
	}

	private static PointerSize pointerSizeForName(String name) {
		for (KnownArchitecture knownArchitecture : KNOWN_ARCHITECTURES) {
			if (knownArchitecture.isAlias(name)) {
				return knownArchitecture.pointerSize;
			}
		}
		throw new UnsupportedOperationException("Unknown architecture");
	}

	enum PointerSize {
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

	private static final class KnownArchitecture extends MachineArchitecture implements Serializable {
		private final String canonicalName;
		private final PointerSize pointerSize;
		private final Set<String> aliases;

		public KnownArchitecture(String canonicalName, PointerSize pointerSize, String... aliases) {
			this.canonicalName = canonicalName;
			this.pointerSize = pointerSize;
			this.aliases = ImmutableSet.copyOf(aliases);
		}

		@Override
		public String getName() {
			return canonicalName;
		}

		@Override
		public String getCanonicalName() {
			return canonicalName;
		}

		@Override
		public boolean is32Bit() {
			return pointerSize.is32Bit();
		}

		@Override
		public boolean is64Bit() {
			return pointerSize.is64Bit();
		}

		public boolean isAlias(String input) {
			return canonicalName.equalsIgnoreCase(input) || aliases.contains(input.toLowerCase(Locale.CANADA));
		}
	}

	private static final class UnknownMachineArchitecture extends MachineArchitecture implements Serializable {
		private final String name;

		private UnknownMachineArchitecture(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getCanonicalName() {
			return name.toLowerCase(Locale.CANADA);
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
