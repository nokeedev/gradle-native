package dev.nokee.model;

import org.gradle.api.plugins.ExtensionAware;

final class NokeeExtensionUtils {
	static NokeeExtension nokee(ExtensionAware target) {
		return (NokeeExtension) target.getExtensions().getByName("nokee");
	}
}
