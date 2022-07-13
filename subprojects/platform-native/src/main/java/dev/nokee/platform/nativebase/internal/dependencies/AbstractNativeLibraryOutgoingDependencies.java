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

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import lombok.Getter;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public abstract class AbstractNativeLibraryOutgoingDependencies {
	@Getter private final ConfigurableFileCollection exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;
	private final Configuration linkElements;
	private final Configuration runtimeElements;

	protected AbstractNativeLibraryOutgoingDependencies(Configuration linkElements, Configuration runtimeElements, ObjectFactory objects) {
		this.exportedHeaders = objects.fileCollection();
		this.exportedSwiftModule = objects.fileProperty();
		this.exportedBinary = objects.property(Binary.class);
		this.linkElements = linkElements;
		this.runtimeElements = runtimeElements;

		val linkArtifacts = objects.listProperty(PublishArtifact.class);
		linkArtifacts.addAll(getExportedBinary().flatMap(this::getOutgoingLinkLibrary));
		linkElements.getOutgoing().getArtifacts().addAllLater(linkArtifacts);

		val runtimeArtifacts = objects.listProperty(PublishArtifact.class);
		runtimeArtifacts.addAll(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
		runtimeElements.getOutgoing().getArtifacts().addAllLater(runtimeArtifacts);
	}

	public Configuration getLinkElements() {
		return linkElements;
	}

	public Configuration getRuntimeElements() {
		return runtimeElements;
	}

	private Provider<Iterable<PublishArtifact>> getOutgoingLinkLibrary(Binary binary) {
		if (binary instanceof SharedLibraryBinaryInternal) {
			if (((SharedLibraryBinaryInternal) binary).getTargetMachine().getOperatingSystemFamily().isWindows()) {
				return Providers.of(ImmutableList.of(new LazyPublishArtifact(((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(it -> ((LinkSharedLibraryTask) it).getImportLibrary()))));
			}
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile))));
		} else if (binary instanceof StaticLibraryBinary) {
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((StaticLibraryBinary) binary).getCreateTask().flatMap(CreateStaticLibrary::getOutputFile))));
		} else if (binary instanceof HasOutputFile) {
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((HasOutputFile) binary).getOutputFile())));
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}

	private Provider<Iterable<PublishArtifact>> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof SharedLibraryBinaryInternal) {
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((SharedLibraryBinaryInternal) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile))));
		} else if (binary instanceof StaticLibraryBinary) {
			return Providers.of(ImmutableList.of());
		} else if (binary instanceof HasOutputFile) {
			return Providers.of(ImmutableList.of(new LazyPublishArtifact(((HasOutputFile) binary).getOutputFile())));
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
