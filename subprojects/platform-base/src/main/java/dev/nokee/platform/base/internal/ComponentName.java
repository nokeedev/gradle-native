package dev.nokee.platform.base.internal;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
public final class ComponentName {
	private final String name;

	private ComponentName(String name) {
		checkArgument(!Strings.isNullOrEmpty(name));
		this.name = name;
	}

	public static ComponentName of(String name) {
		return new ComponentName(name);
	}

	public boolean isMain() {
		return name.equals("main");
	}

	public String get() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
