package dev.nokee.runtime.nativebase;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

final class DefaultBuildType extends BuildType implements Serializable {
	private final String name;

	DefaultBuildType(String name) {
		this.name = requireNonNull(name);
	}

	@Override
	public String getName() {
		return name;
	}
}
