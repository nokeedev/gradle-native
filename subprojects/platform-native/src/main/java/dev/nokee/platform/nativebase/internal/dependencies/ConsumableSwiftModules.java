package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public final class ConsumableSwiftModules extends BaseNativeConsumableDependencyBucket {
	@Inject
	public ConsumableSwiftModules(ObjectFactory objectFactory) {
		super(objectFactory);
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.SWIFT_API));
	}

	public void swiftModule(Object notation) {
		getOutgoing().artifact(notation);
	}
}
