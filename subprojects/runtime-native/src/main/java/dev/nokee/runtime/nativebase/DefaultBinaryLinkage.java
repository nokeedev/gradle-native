package dev.nokee.runtime.nativebase;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

final class DefaultBinaryLinkage extends BinaryLinkage implements Serializable {
	private final String name;

	DefaultBinaryLinkage(String name) {
		this.name = requireNonNull(name);
	}

	@Override
	public String getName() {
		return name;
	}
}
