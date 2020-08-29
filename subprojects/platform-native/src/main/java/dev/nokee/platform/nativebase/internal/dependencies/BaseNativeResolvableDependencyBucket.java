package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.BaseResolvableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencies;
import dev.nokee.platform.nativebase.internal.BaseTargetBuildType;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.NamedTargetBuildType;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Action;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.language.cpp.CppBinary;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;

/*abstract*/ class BaseNativeResolvableDependencyBucket extends BaseResolvableDependencyBucket {
	private final ObjectFactory objectFactory;

	protected BaseNativeResolvableDependencyBucket(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public void variant(BuildVariantInternal variant) {
		attributes(configureAttributes(variant));
		if (!variant.hasAxisValue(BaseTargetBuildType.DIMENSION_TYPE)) {
			configureAsGradleDebugCompatible(getAttributes());
		}
	}

	private Action<AttributeContainer> configureAsGradleCompatibleBuildType(String buildTypeName) {
		return attributes -> {
			// TODO: Match Gradle debug when no build type are specified
			if (buildTypeName.equalsIgnoreCase("debug")) {
				configureAsGradleDebugCompatible(attributes);
			} else if (buildTypeName.equalsIgnoreCase("release")) {
				attributes.attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE);
				attributes.attribute(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.TRUE);
			}
		};
	}

	private void configureAsGradleDebugCompatible(AttributeContainer attributes) {
		attributes.attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE);
		attributes.attribute(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.FALSE);
	}

	private Action<AttributeContainer> configureAttributes(BuildVariantInternal variant) {
		return attributes -> {
			variant.getDimensions().forEach(it -> {
				if (it instanceof DefaultOperatingSystemFamily) {
					attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objectFactory.named(OperatingSystemFamily.class, ((DefaultOperatingSystemFamily) it).getName()));
				} else if (it instanceof DefaultMachineArchitecture) {
					attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objectFactory.named(MachineArchitecture.class, ((DefaultMachineArchitecture) it).getName()));
				} else if (it instanceof DefaultBinaryLinkage) {
					// Do not configure this dimension for incoming dependencies
				} else if (it instanceof NamedTargetBuildType) {
					attributes.attribute(NamedTargetBuildType.BUILD_TYPE_ATTRIBUTE, ((NamedTargetBuildType) it).getName());
					configureAsGradleCompatibleBuildType(((NamedTargetBuildType) it).getName()).execute(attributes);
				} else {
					throw new IllegalArgumentException(String.format("Unknown dimension variant '%s'", it.toString()));
				}
			});
		};
	}
}
