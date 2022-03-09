package dev.nokee.docs;

import org.gradle.util.GUtil;

public enum Dsl {
	GROOVY_DSL("groovy-dsl", "gradle"), KOTLIN_DSL("kotlin-dsl", "gradle.kts");

	private final String name;
	private final String extension;

	Dsl(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	public String getName() {
		return name;
	}

	public String getNameAsCamelCase() {
		return GUtil.toCamelCase(name);
	}

	public String getSettingsFileName() {
		return "settings." + extension;
	}
}
