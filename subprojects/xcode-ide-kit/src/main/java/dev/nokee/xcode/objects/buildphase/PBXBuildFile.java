/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.xcode.objects.buildphase;

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.project.CodeablePBXBuildFile;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.Map;
import java.util.Optional;

/**
 * File referenced by a build phase, unique to each build phase.
 *
 * <p>Contains a dictionary {@link #getSettings()} which holds additional information to be interpreted by
 * the particular phase referencing this object, e.g.:
 *
 * - {@link PBXSourcesBuildPhase } may read <code>{"COMPILER_FLAGS": "-foo"}</code> and interpret
 * that this file should be compiled with the additional flag {@code "-foo" }.
 */
public interface PBXBuildFile extends PBXProjectItem {
	Optional<FileReference> getFileRef();

	Optional<XCSwiftPackageProductDependency> getProductRef();

	Map<String, ?> getSettings();

	static PBXBuildFile ofFile(FileReference fileReference) {
		return PBXBuildFile.builder().fileRef(fileReference).build();
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder {
		private FileReference fileRef;
		private XCSwiftPackageProductDependency productRef;
		private ImmutableMap<String, Object> settings;

		public Builder fileRef(FileReference fileRef) {
			this.fileRef = fileRef;
			return this;
		}

		public Builder productRef(XCSwiftPackageProductDependency productRef) {
			this.productRef = productRef;
			return this;
		}

		public Builder settings(Map<String, ?> settings) {
			this.settings = ImmutableMap.copyOf(settings);
			return this;
		}

		public PBXBuildFile build() {
			// We can't assert either 'fileRef' and 'productRef' must not be null because it can happen...
			//   See global ID 'D8EC3E1B1E9BDA35006712EB' in Wikipedia Xcode project:
			//   https://raw.githubusercontent.com/wikimedia/wikipedia-ios/main/Wikipedia.xcodeproj/project.pbxproj

			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "PBXBuildFile");
			builder.put(CodeablePBXBuildFile.CodingKeys.fileRef, fileRef);
			builder.put(CodeablePBXBuildFile.CodingKeys.productRef, productRef);
			builder.put(CodeablePBXBuildFile.CodingKeys.settings, settings);

			return new CodeablePBXBuildFile(builder.build());
		}
	}

	/**
	 * Represent a file reference for a {@link PBXBuildFile}.
	 */
	interface FileReference {}
}
