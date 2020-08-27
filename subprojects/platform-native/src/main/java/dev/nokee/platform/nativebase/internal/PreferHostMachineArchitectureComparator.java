package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;

import java.util.Comparator;

final class PreferHostMachineArchitectureComparator implements Comparator<DefaultMachineArchitecture> {
	@Override
	public int compare(DefaultMachineArchitecture lhs, DefaultMachineArchitecture rhs) {
		if (lhs.equals(DefaultMachineArchitecture.HOST) && rhs.equals(DefaultMachineArchitecture.HOST)) {
			return 0;
		} else if (lhs.equals(DefaultMachineArchitecture.HOST)) {
			return -1;
		} else if (rhs.equals(DefaultMachineArchitecture.HOST)) {
			return 1;
		}
		return 0;
	}
}
