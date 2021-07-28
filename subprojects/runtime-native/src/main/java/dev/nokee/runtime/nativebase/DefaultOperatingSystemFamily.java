package dev.nokee.runtime.nativebase;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

final class DefaultOperatingSystemFamily extends OperatingSystemFamily implements Serializable {
	private final String name;

	public DefaultOperatingSystemFamily(String name) {
		this.name = requireNonNull(name);
	}

	@Override
	public String getName() {
		return name;
	}
}
