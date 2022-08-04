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

import dev.nokee.runtime.nativebase.internal.NativeArtifactTypes;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public final class NativeLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	private final Configuration apiElements;

	public NativeLibraryOutgoingDependencies(Configuration apiElements, Configuration linkElements, Configuration runtimeElements, ObjectFactory objects) {
		super(linkElements, runtimeElements, objects);

		// TODO: Introduce compileOnlyApi which apiElements should extends from
		this.apiElements = apiElements;

		// See https://github.com/gradle/gradle/issues/15146 to learn more about splitting the implicit dependencies
		val apiArtifacts = objects.listProperty(PublishArtifact.class);
		apiArtifacts.addAll(getExportedHeaders().getElements().flatMap(this::getOutgoingApi));
		apiElements.getOutgoing().getArtifacts().addAllLater(apiArtifacts);
	}

	private Provider<Iterable<PublishArtifact>> getOutgoingApi(Set<FileSystemLocation> headers) {
		return ProviderUtils.supplied(() -> {
			return headers.stream().map(headerSearchPath -> {
				return new PublishArtifact() {
					@Override
					public String getName() {
						return headerSearchPath.getAsFile().getName();
					}

					@Override
					public String getExtension() {
						return FilenameUtils.getExtension(headerSearchPath.getAsFile().getName());
					}

					@Override
					public String getType() {
						return NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY;
					}

					@Nullable
					@Override
					public String getClassifier() {
						return null;
					}

					@Override
					public File getFile() {
						return headerSearchPath.getAsFile();
					}

					@Nullable
					@Override
					public Date getDate() {
						return new Date();
					}

					@Override
					public TaskDependency getBuildDependencies() {
						return getExportedHeaders().getBuildDependencies();
					}
				};
			}).collect(Collectors.toList());
		});
	}

	public Configuration getApiElements() {
		return apiElements;
	}
}
