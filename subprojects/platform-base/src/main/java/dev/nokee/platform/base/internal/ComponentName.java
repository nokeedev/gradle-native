package dev.nokee.platform.base.internal;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@ToString
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

	public String get() {
		return name;
	}
}
