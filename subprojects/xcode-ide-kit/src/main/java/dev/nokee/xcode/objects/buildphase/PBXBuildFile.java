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
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.objects.Visitable;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXReferenceProxy;
import dev.nokee.xcode.objects.files.PBXVariantGroup;
import dev.nokee.xcode.objects.files.XCVersionGroup;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.project.CodeablePBXBuildFile;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import java.util.Map;
import java.util.Optional;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;

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

	Builder toBuilder();

	static PBXBuildFile ofFile(FileReference fileReference) {
		return PBXBuildFile.builder().fileRef(fileReference).build();
	}

	static PBXBuildFile ofProduct(XCSwiftPackageProductDependency productReference) {
		return PBXBuildFile.builder().productRef(productReference).build();
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXBuildFile>, LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder().knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXBuildFile.CodingKeys.values()));
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder;
			builder.put(KeyedCoders.ISA, "PBXBuildFile");
			builder.requires(key(CodeablePBXBuildFile.CodingKeys.fileRef).or(key(CodeablePBXBuildFile.CodingKeys.productRef)));
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder fileRef(FileReference fileRef) {
			builder.put(CodeablePBXBuildFile.CodingKeys.fileRef, fileRef);
			return this;
		}

		public Builder productRef(XCSwiftPackageProductDependency productRef) {
			builder.put(CodeablePBXBuildFile.CodingKeys.productRef, productRef);
			return this;
		}

		public Builder settings(Map<String, ?> settings) {
			builder.put(CodeablePBXBuildFile.CodingKeys.settings, ImmutableMap.copyOf(settings));
			return this;
		}

		@Override
		public PBXBuildFile build() {
			return new CodeablePBXBuildFile(builder.build());
		}
	}

	/**
	 * Represent a file reference for a {@link PBXBuildFile}.
	 */
	interface FileReference {
		@Visitable
		void accept(Visitor visitor);

		interface Visitor {
			void visit(PBXFileReference fileReference);
			void visit(PBXReferenceProxy referenceProxy);
			void visit(PBXVariantGroup variantGroup);
			void visit(XCVersionGroup versionGroup);
		}
	}
}
