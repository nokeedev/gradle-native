package dev.nokee.platform.base.internal;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class ComponentName {
	private static final String MAIN_COMPONENT_NAME = "main";
	private final String name;

	private ComponentName(String name) {
		requireNonNull(name);
		checkArgument(!StringUtils.isEmpty(name));
		this.name = name;
	}

	public static ComponentName of(String name) {
		return new ComponentName(name);
	}

    public static ComponentName ofMain() {
		return new ComponentName(MAIN_COMPONENT_NAME);
    }

    public boolean isMain() {
		return name.equals(MAIN_COMPONENT_NAME);
	}

	public String get() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
