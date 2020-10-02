package dev.nokee.platform.base.internal;

import lombok.EqualsAndHashCode;
import org.gradle.util.GUtil;

@EqualsAndHashCode
public final class BaseName {
	private final String baseName;

	private BaseName(String baseName) {
		this.baseName = baseName;
	}

	public static BaseName of(String baseName) {
		return new BaseName(baseName);
	}

	public String getAsString() {
		return baseName;
	}

	public String getAsKebabCase() {
		return GUtil.toWords(baseName.replace("_", ""), '-');
	}

	public String getAsCamelCase() {
		return GUtil.toCamelCase(baseName.replace("_", ""));
	}

	public String getAsLowerCamelCase() {
		return GUtil.toLowerCamelCase(baseName.replace("_", ""));
	}

	@Override
	public String toString() {
		return baseName;
	}
}
