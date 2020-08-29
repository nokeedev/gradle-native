package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.ResolvableDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.FileCollection;

import javax.inject.Inject;

public final class ResolvableDependencies extends BaseResolvableDependencyBucket implements ResolvableDependencyBucket {
//	ResolvableDependencies(DependencyIdentifier<ResolvableDependencies> identifier, Configuration configuration) {
//		super(identifier, configuration);
//	}
	@Inject
	ResolvableDependencies() {}

	public FileCollection getFiles() {
		return configuration.getIncoming().getFiles();
	}

	public void attributes(Action<? super AttributeContainer> action) {
		configuration.attributes(action);
	}

	public AttributeContainer getAttributes() {
		return configuration.getAttributes();
	}

	public org.gradle.api.artifacts.ResolvableDependencies getIncoming() {
		return configuration.getIncoming();
	}
}
