package dev.nokee.platform.base.internal;

import lombok.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class BinaryName {
	private final String name;

	private BinaryName(String name) {
		requireNonNull(name);
		checkArgument(!name.isEmpty());
		checkArgument(!Character.isUpperCase(name.charAt(0)));
		checkArgument(!name.contains(" "));
		this.name = name;
	}

	public String get() {
		return name;
	}

	public static BinaryName of(String name) {
		return new BinaryName(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
