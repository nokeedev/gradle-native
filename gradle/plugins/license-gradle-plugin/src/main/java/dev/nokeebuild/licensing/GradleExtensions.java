package dev.nokeebuild.licensing;

import org.gradle.api.Action;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

import java.util.Optional;

final class GradleExtensions {
	private final ExtensionContainer extensions;

	private GradleExtensions(ExtensionContainer extensions) {
		this.extensions = extensions;
	}

	public static GradleExtensions from(Object extensionAwareObject) {
		return new GradleExtensions(((ExtensionAware) extensionAwareObject).getExtensions());
	}

	public <T> void configure(String name, Action<? super T> action) {
		extensions.configure(name, action);
	}

	public <T> void configure(Class<T> type, Action<? super T> action) {
		extensions.configure(type, action);
	}

	public <T> void ifPresent(Class<T> type, Action<? super T> action) {
		Optional.ofNullable(extensions.findByType(type)).ifPresent(action::execute);
	}
}
