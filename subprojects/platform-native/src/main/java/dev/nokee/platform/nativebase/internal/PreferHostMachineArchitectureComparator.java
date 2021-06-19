package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.MachineArchitecture;

import java.util.Comparator;

final class PreferHostMachineArchitectureComparator implements Comparator<MachineArchitecture> {
	private static final MachineArchitecture HOST = MachineArchitecture.forName(System.getProperty("os.arch"));

	@Override
	public int compare(MachineArchitecture lhs, MachineArchitecture rhs) {
		if (lhs.equals(HOST) && rhs.equals(HOST)) {
			return 0;
		} else if (lhs.equals(HOST)) {
			return -1;
		} else if (rhs.equals(HOST)) {
			return 1;
		}
		return 0;
	}
}
