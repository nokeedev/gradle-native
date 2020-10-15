package dev.nokee.language.base.internal;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class LanguageSourceSetName {
	private final String name;

	private LanguageSourceSetName(String name) {
		assert !Strings.isNullOrEmpty(name);
		this.name = name;
	}

	public static LanguageSourceSetName of(String name) {
		return new LanguageSourceSetName(name);
	}

	public String get() {
		return name;
	}
}
