package dev.nokee.platform.base.internal;

import lombok.*;

@ToString
@EqualsAndHashCode
public final class BinaryName {
	private final String name;

	private BinaryName(String name) {
		this.name = name;
	}

	public String get() {
		return name;
	}

	public static BinaryName of(String name) {
		return new BinaryName(name);
	}
}
