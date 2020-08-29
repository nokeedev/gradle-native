package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.dependencies.BaseConsumableDependencyBucket;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public final class ConsumableJvmApiElements extends BaseConsumableDependencyBucket {
	@Inject
	public ConsumableJvmApiElements(ObjectFactory objectFactory) {
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_API));
		getAttributes().attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objectFactory.named(LibraryElements.class, LibraryElements.JAR));
	}

	public void artifact(Object notation) {
		getOutgoing().artifact(notation);
	}
}
