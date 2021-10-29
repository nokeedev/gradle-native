package dev.nokee.language.base.internal;

import dev.nokee.language.base.ConfigurableSourceSet;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

public final class SourceSetFactory {
	private final ObjectFactory objectFactory;

	public SourceSetFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public ConfigurableSourceSet sourceSet() {
		return new DefaultConfigurableSourceSet(new DefaultLanguageSourceSetStrategy(objectFactory));
	}

	public ConfigurableSourceSet bridgedSourceSet(SourceDirectorySet delegate) {
		return new DefaultConfigurableSourceSet(new BridgedLanguageSourceSetStrategy(delegate, objectFactory));
	}
}
