package dev.nokee.runtime.core;

import lombok.EqualsAndHashCode;
import org.gradle.util.GUtil;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultCoordinateAxis<T> implements CoordinateAxis<T> {
	private final Class<T> type;
	private final String name;

	public DefaultCoordinateAxis(Class<T> type) {
		this.type = requireNonNull(type);
		this.name = inferNameFromType(type);
	}

	public DefaultCoordinateAxis(Class<T> type, String name) {
		requireNonNull(type);
		requireNonNull(name);
		checkArgument(!name.isEmpty(), "coordinate axis name cannot be empty");
		checkArgument(name.chars().noneMatch(Character::isSpaceChar), "coordinate axis name cannot contains spaces");
		this.type = type;
		this.name = name;
	}

	private static String inferNameFromType(Class<?> type) {
		return GUtil.toWords(type.getSimpleName(), '-');
	}

	public Class<T> getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "axis <" + name + ">";
	}
}
