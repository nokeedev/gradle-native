package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.dependencies.BaseConsumableDependencyBucket;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public final class ConsumableJvmRuntimeElements extends BaseConsumableDependencyBucket {
	@Inject
	public ConsumableJvmRuntimeElements(ObjectFactory objectFactory) {
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME));
		getAttributes().attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objectFactory.named(LibraryElements.class, LibraryElements.JAR));
	}

	public void artifact(Object notation) {
		getOutgoing().artifact(notation);
	}
}
