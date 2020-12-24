package dev.nokee.platform.base.internal;

import java.util.concurrent.Callable;

public final class ConventionalRelativeLanguageSourceSetPath implements Callable<String> {
	private final String relativePath;

	private ConventionalRelativeLanguageSourceSetPath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String get() {
		return relativePath;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String call() throws Exception {
		return relativePath;
	}

	public static final class Builder {
		private String componentName;
		private String sourceSetName;

		public Builder withComponentName(String componentName) {
			this.componentName = componentName;
			return this;
		}

		public Builder withSourceSetName(String sourceSetName) {
			this.sourceSetName = sourceSetName;
			return this;
		}

		public ConventionalRelativeLanguageSourceSetPath build() {
			assert componentName != null;
			assert sourceSetName != null;
			return new ConventionalRelativeLanguageSourceSetPath(asConventionalRelativePath(componentName, sourceSetName));
		}
	}

	private static String asConventionalRelativePath(String componentName, String sourceSetName) {
		return "src/" + componentName + "/" + sourceSetName;
	}
}
