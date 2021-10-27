/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.runtime.base.internal.DefaultUsage;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.language.cpp.CppBinary;

import java.util.Arrays;
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS;

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

	public static ActionUtils.Action<Configuration> configureIncomingAttributes(BuildVariantInternal variant, ObjectFactory objects) {
		return configuration -> {
			val attributes = configuration.getAttributes();
			variant.getDimensions().forEach(it -> {
				if (it.getAxis().equals(OPERATING_SYSTEM_COORDINATE_AXIS)) {
					attributes.attribute(OPERATING_SYSTEM_ATTRIBUTE, (OperatingSystemFamily) it.getValue());
					attributes.attribute(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
						objects.named(org.gradle.nativeplatform.OperatingSystemFamily.class, ((OperatingSystemFamily) it.getValue()).getCanonicalName()));
				} else if (it.getAxis().equals(ARCHITECTURE_COORDINATE_AXIS)) {
					attributes.attribute(ARCHITECTURE_ATTRIBUTE, (MachineArchitecture) it.getValue());
					attributes.attribute(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
						objects.named(org.gradle.nativeplatform.MachineArchitecture.class, ((MachineArchitecture) it.getValue()).getCanonicalName()));
				} else if (it.getAxis().equals(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
					// Do not configure this dimension for incoming dependencies
				} else if (it.getAxis().equals(BuildType.BUILD_TYPE_COORDINATE_AXIS)) {
					attributes.attribute(BuildType.BUILD_TYPE_ATTRIBUTE, (BuildType) it.getValue());
				} else {
					throw new IllegalArgumentException(String.format("Unknown dimension variant '%s'", it.toString()));
				}
			});
		};
	}

	public static ActionUtils.Action<Configuration> configureOutgoingAttributes(BuildVariantInternal variant, ObjectFactory objects) {
		return configuration -> {
			val attributes = configuration.getAttributes();
			variant.getDimensions().forEach(it -> {
				if (it.getAxis().equals(OPERATING_SYSTEM_COORDINATE_AXIS)) {
					attributes.attribute(OPERATING_SYSTEM_ATTRIBUTE, (OperatingSystemFamily) it.getValue());
					attributes.attribute(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
						objects.named(org.gradle.nativeplatform.OperatingSystemFamily.class, ((OperatingSystemFamily) it.getValue()).getCanonicalName()));
				} else if (it.getAxis().equals(ARCHITECTURE_COORDINATE_AXIS)) {
					attributes.attribute(ARCHITECTURE_ATTRIBUTE, (MachineArchitecture) it.getValue());
					attributes.attribute(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
						objects.named(org.gradle.nativeplatform.MachineArchitecture.class, ((MachineArchitecture) it.getValue()).getCanonicalName()));
				} else if (it.getAxis().equals(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
					attributes.attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, (BinaryLinkage) it.getValue());
				} else if (it.getAxis().equals(BuildType.BUILD_TYPE_COORDINATE_AXIS)) {
					attributes.attribute(BuildType.BUILD_TYPE_ATTRIBUTE, (BuildType) it.getValue());
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
