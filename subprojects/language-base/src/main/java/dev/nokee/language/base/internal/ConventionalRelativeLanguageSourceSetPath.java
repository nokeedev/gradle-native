package dev.nokee.language.base.internal;

import lombok.val;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

public final class ConventionalRelativeLanguageSourceSetPath implements Callable<String> {
	private final String relativePath;

	private ConventionalRelativeLanguageSourceSetPath(String relativePath) {
		this.relativePath = relativePath;
	}

	public static ConventionalRelativeLanguageSourceSetPath of(LanguageSourceSetIdentifier<?> identifier) {
		return new ConventionalRelativeLanguageSourceSetPath(asConventionalRelativePath(componentOwnerName(identifier), sourceSetName(identifier)));
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

		public Builder fromIdentifier(LanguageSourceSetIdentifier<?> identifier) {
			withComponentName(componentOwnerName(identifier));
			withSourceSetName(sourceSetName(identifier));
			return this;
		}

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

	private static String sourceSetName(LanguageSourceSetIdentifier<?> identifier) {
		return identifier.getName().get();
	}

	private static String componentOwnerName(LanguageSourceSetIdentifier<?> identifier) {
		assert identifier.getOwnerIdentifier().getClass().getSimpleName().equals("ComponentIdentifier");
		try {
			val getNameMethod = identifier.getOwnerIdentifier().getClass().getMethod("getName");
			val componentName = getNameMethod.invoke(identifier.getOwnerIdentifier());
			val name = componentName.getClass().getMethod("get").invoke(componentName);
			return (String) name;
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
