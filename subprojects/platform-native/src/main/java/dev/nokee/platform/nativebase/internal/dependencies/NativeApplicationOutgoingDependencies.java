/*
 * Copyright 2020 the original author or authors.
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

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class NativeApplicationOutgoingDependencies implements NativeOutgoingDependencies {
	@Getter private final ConfigurableFileCollection exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter private final Configuration runtimeElements;

	@Inject
	public NativeApplicationOutgoingDependencies(DomainObjectIdentifierInternal ownerIdentifier, BuildVariantInternal buildVariant, DefaultNativeComponentDependencies dependencies, ConfigurationContainer configurationContainer, ObjectFactory objects) {
		this.objects = objects;
		this.exportedHeaders = objects.fileCollection();
		this.exportedSwiftModule = objects.fileProperty();
		this.exportedBinary = objects.property(Binary.class);

		ConfigurationUtils builder = objects.newInstance(ConfigurationUtils.class);
		val configurationRegistry = new ConfigurationBucketRegistryImpl(configurationContainer);
		val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of("runtimeElements"),
			ConsumableDependencyBucket.class, ownerIdentifier);
		this.runtimeElements = configurationRegistry.createIfAbsent(identifier.getConfigurationName(), ConfigurationBucketType.CONSUMABLE, builder.asOutgoingRuntimeLibrariesFrom(dependencies.getImplementation().getAsConfiguration(), dependencies.getRuntimeOnly().getAsConfiguration()).withVariant(buildVariant).withDescription(identifier.getDisplayName()));

		runtimeElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
	}

	private Provider<RegularFile> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof ExecutableBinaryInternal) {
			return ((ExecutableBinaryInternal) binary).getLinkTask().flatMap(LinkExecutable::getLinkedFile);
		} else if (binary instanceof HasOutputFile) {
			return ((HasOutputFile) binary).getOutputFile();
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
