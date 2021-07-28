package dev.nokee.runtime.nativebase;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

final class DefaultMachineArchitecture extends MachineArchitecture implements Serializable {
	private final String name;

	DefaultMachineArchitecture(String name) {
		this.name = requireNonNull(name);
	}

	@Override
	public String getName() {
		return name;
	}
}
