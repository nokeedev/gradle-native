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
package dev.nokee.xcode.objects.files;

import com.google.common.base.MoreObjects;
import com.google.common.io.Files;
import dev.nokee.xcode.objects.FileTypes;
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.project.CodeablePBXFileReference;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.xcode.project.DefaultKeyedObject.key;
import static java.util.Objects.requireNonNull;

/**
 * Reference to a concrete file.
 */
public interface PBXFileReference extends PBXReference, PBXContainerItemProxy.ContainerPortal, PBXBuildFile.FileReference, GroupChild {
	// It seems the name can be null but not path which is a bit different from PBXGroup.

	Optional<String> getExplicitFileType();

	Optional<String> getLastKnownFileType();

	@Override
	default void accept(PBXBuildFile.FileReference.Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	default void accept(GroupChild.Visitor visitor) {
		visitor.visit(this);
	}

	static PBXFileReference ofAbsolutePath(File path) {
		return PBXFileReference.builder().name(path.getName()).path(path.getAbsolutePath()).sourceTree(PBXSourceTree.ABSOLUTE).build();
	}

	static PBXFileReference ofAbsolutePath(Path path) {
		return PBXFileReference.builder().name(path.getFileName().toString()).path(path.toAbsolutePath().toString()).sourceTree(PBXSourceTree.ABSOLUTE).build();
	}

	static PBXFileReference ofAbsolutePath(String path) {
		return PBXFileReference.builder().name(FilenameUtils.getName(path)).path(path).sourceTree(PBXSourceTree.ABSOLUTE).build();
	}

	static PBXFileReference ofSourceRoot(String path) {
		return PBXFileReference.builder().name(FilenameUtils.getBaseName(path)).path(path).sourceTree(PBXSourceTree.SOURCE_ROOT).build();
	}

	static PBXFileReference ofGroup(String path) {
		path = FilenameUtils.normalizeNoEndSeparator(path);
		path = FilenameUtils.separatorsToUnix(path);
		return PBXFileReference.builder().name(FilenameUtils.getBaseName(path)).path(path).sourceTree(PBXSourceTree.GROUP).build();
	}

	static PBXFileReference ofBuiltProductsDir(String path) {
		path = FilenameUtils.normalizeNoEndSeparator(path);
		path = FilenameUtils.separatorsToUnix(path);
		return builder().name(FilenameUtils.getBaseName(path)).path(path).sourceTree(PBXSourceTree.BUILT_PRODUCTS_DIR).build();
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXFileReference>, LenientAwareBuilder<Builder> {
		private String name;
		private String path;
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder()
			.knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXFileReference.CodingKeys.values());

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXFileReference");
			builder.requires(key(CodeablePBXFileReference.CodingKeys.path));
			builder.requires(key(CodeablePBXFileReference.CodingKeys.sourceTree));

			builder.ifAbsent(CodeablePBXFileReference.CodingKeys.sourceTree, PBXSourceTree.GROUP);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			this.name = requireNonNull(name);
			builder.put(CodeablePBXFileReference.CodingKeys.name, name);
			return this;
		}

		public Builder path(String path) {
			path = requireNonNull(path).trim();
			checkArgument(!path.isEmpty());
			this.path = path;
			builder.put(CodeablePBXFileReference.CodingKeys.path, path);
			return this;
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			builder.put(CodeablePBXFileReference.CodingKeys.sourceTree, sourceTree);
			return this;
		}

		@Override
		public PBXFileReference build() {
			String defaultType = null;
			String explicitFileType;
			String lastKnownFileType;
			// PBXVariantGroups create file references where the name doesn't contain the file
			// extension.
			//
			// Try the path if it's present to check for an extension, then fall back to the name
			// if the path isn't present.
			String pathOrName = MoreObjects.firstNonNull(path, name);

			// this is necessary to prevent O(n^2) behavior in xcode project loading
			String fileType =
				FileTypes.FILE_EXTENSION_TO_IDENTIFIER.get(Files.getFileExtension(pathOrName));
			if (fileType != null && (FileTypes.EXPLICIT_FILE_TYPE_BROKEN_IDENTIFIERS.contains(fileType))
				|| FileTypes.MODIFIABLE_FILE_TYPE_IDENTIFIERS.contains(fileType)) {
				explicitFileType = null;
				lastKnownFileType = fileType;
			} else if (fileType != null) {
				explicitFileType = fileType;
				lastKnownFileType = null;
			} else {
				explicitFileType = defaultType;
				lastKnownFileType = null;
			}

			builder.putNullable(CodeablePBXFileReference.CodingKeys.explicitFileType, explicitFileType);
			builder.putNullable(CodeablePBXFileReference.CodingKeys.lastKnownFileType, lastKnownFileType);

			return new CodeablePBXFileReference(builder.build());
		}
	}
}
