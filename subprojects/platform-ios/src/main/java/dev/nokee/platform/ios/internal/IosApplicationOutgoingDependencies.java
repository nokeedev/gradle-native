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
package dev.nokee.platform.ios.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependencies;
import lombok.Getter;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class IosApplicationOutgoingDependencies implements NativeOutgoingDependencies {
	@Getter private final DirectoryProperty exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;

	@Inject
	public IosApplicationOutgoingDependencies(Configuration runtimeElements, ObjectFactory objects) {
		this.exportedHeaders = objects.directoryProperty();
		this.exportedSwiftModule = objects.fileProperty();
		this.exportedBinary = objects.property(Binary.class);

		runtimeElements.getOutgoing().artifact(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary));
	}

	private Provider<FileSystemLocation> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof SignedIosApplicationBundleInternal) {
			return ((SignedIosApplicationBundleInternal) binary).getApplicationBundleLocation();
		} else if (binary instanceof HasOutputFile) {
			return ((HasOutputFile) binary).getOutputFile().map(it -> it);
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
