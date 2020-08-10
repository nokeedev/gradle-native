package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.runtime.base.internal.DefaultUsage;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.language.cpp.CppBinary;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ConfigurationUtilsEx {
	public static void configureAsIncoming(Configuration configuration) {
		configuration.setCanBeResolved(true);
		configuration.setCanBeConsumed(false);
	}

	public static void configureAsOutgoing(Configuration configuration) {
		configuration.setCanBeConsumed(true);
		configuration.setCanBeResolved(false);
	}

	public static void configureAsBucket(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(false);
	}

	public static Action<Configuration> asIncomingHeaderSearchPathFrom(DependencyBucket... fromBuckets) {
		return configuration -> {
			configureAsIncoming(configuration);
			configuration.setExtendsFrom(toConfigurations(fromBuckets));
			configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, new DefaultUsage(Usage.C_PLUS_PLUS_API));
		};
	}

	public static Action<Configuration> asIncomingSwiftModuleFrom(DependencyBucket... fromBuckets) {
		return configuration -> {
			configureAsIncoming(configuration);
			configuration.setExtendsFrom(toConfigurations(fromBuckets));
			configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, new DefaultUsage(Usage.SWIFT_API));
		};
	}

	public static Action<Configuration> asIncomingLinkLibrariesFrom(DependencyBucket... fromBuckets) {
		return configuration -> {
			configureAsIncoming(configuration);
			configuration.setExtendsFrom(toConfigurations(fromBuckets));
			configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, new DefaultUsage(Usage.NATIVE_LINK));
		};
	}

	public static Action<Configuration> asIncomingRuntimeLibrariesFrom(DependencyBucket... fromBuckets) {
		return configuration -> {
			configureAsIncoming(configuration);
			configuration.setExtendsFrom(toConfigurations(fromBuckets));
			configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, new DefaultUsage(Usage.NATIVE_RUNTIME));
		};
	}

	private static Iterable<Configuration> toConfigurations(DependencyBucket... buckets) {
		return Arrays.stream(buckets).map(DependencyBucket::getAsConfiguration).collect(Collectors.toList());
	}

	public static Action<Configuration> withAttributes(BuildVariant variant, ObjectFactory objects) {
		return configuration -> {
			val attributes = configuration.getAttributes();
			variant.getDimensions().forEach(it -> {
				if (it instanceof DefaultOperatingSystemFamily) {
					attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(OperatingSystemFamily.class, ((DefaultOperatingSystemFamily) it).getName()));
				} else if (it instanceof DefaultMachineArchitecture) {
					attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture.class, ((DefaultMachineArchitecture) it).getName()));
				} else if (it instanceof DefaultBinaryLinkage) {
					attributes.attribute(DefaultBinaryLinkage.LINKAGE_ATTRIBUTE, ((DefaultBinaryLinkage) it).getName());
				} else {
					throw new IllegalArgumentException(String.format("Unknown dimension variant '%s'", it.toString()));
				}
			});
		};
	}

	public static void configureAsGradleDebugCompatible(Configuration configuration) {
		configuration.attributes(attributes -> {
			attributes.attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, Boolean.TRUE);
			attributes.attribute(CppBinary.OPTIMIZED_ATTRIBUTE, Boolean.FALSE);
		});
	}
}
