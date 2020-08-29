package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.BaseConsumableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencies;
import dev.nokee.platform.nativebase.internal.BaseTargetBuildType;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.NamedTargetBuildType;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Action;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;

/*abstract*/ class BaseNativeConsumableDependencyBucket extends BaseConsumableDependencyBucket {
	private final ObjectFactory objectFactory;

	protected BaseNativeConsumableDependencyBucket(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public void variant(BuildVariantInternal variant) {
		attributes(configureAttributes(variant));
	}

	private Action<AttributeContainer> configureAttributes(BuildVariantInternal variant) {
		return attributes -> {
			variant.getDimensions().forEach(it -> {
				if (it instanceof DefaultOperatingSystemFamily) {
					attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objectFactory.named(OperatingSystemFamily.class, ((DefaultOperatingSystemFamily) it).getName()));
				} else if (it instanceof DefaultMachineArchitecture) {
					attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objectFactory.named(MachineArchitecture.class, ((DefaultMachineArchitecture) it).getName()));
				} else if (it instanceof DefaultBinaryLinkage) {
					attributes.attribute(DefaultBinaryLinkage.LINKAGE_ATTRIBUTE, ((DefaultBinaryLinkage) it).getName());
				} else if (it instanceof NamedTargetBuildType) {
					attributes.attribute(BaseTargetBuildType.BUILD_TYPE_ATTRIBUTE, ((NamedTargetBuildType) it).getName());
				} else {
					throw new IllegalArgumentException(String.format("Unknown dimension variant '%s'", it.toString()));
				}
			});
		};
	}
}
