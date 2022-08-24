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
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Reference to a concrete file.
 */
@EqualsAndHashCode(callSuper = true)
public final class PBXFileReference extends PBXReference implements PBXContainerItemProxy.ContainerPortal, PBXBuildFile.FileReference, GroupChild {
	@Nullable private final String explicitFileType;
	@Nullable private final String lastKnownFileType;

	public PBXFileReference(String name, String path, PBXSourceTree sourceTree) {
		this(name, path, sourceTree, null);
	}

	// It seems the name can be null but not path which is a bit different from PBXGroup.
	public PBXFileReference(@Nullable String name, String path, PBXSourceTree sourceTree, @Nullable String defaultType) {
		super(name, path, sourceTree);

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
	}

	public Optional<String> getExplicitFileType() {
		return Optional.ofNullable(explicitFileType);
	}

	public Optional<String> getLastKnownFileType() {
		return Optional.ofNullable(lastKnownFileType);
	}

	@Override
	public String toString() {
		return String.format(
			"%s explicitFileType=%s",
			super.toString(),
			explicitFileType);
	}

	public static PBXFileReference ofAbsolutePath(File path) {
		return new PBXFileReference(path.getName(), path.getAbsolutePath(), PBXSourceTree.ABSOLUTE);
	}

	public static PBXFileReference ofAbsolutePath(Path path) {
		return new PBXFileReference(path.getFileName().toString(), path.toAbsolutePath().toString(), PBXSourceTree.ABSOLUTE);
	}

	public static PBXFileReference ofAbsolutePath(String path) {
		return new PBXFileReference(FilenameUtils.getName(path), path, PBXSourceTree.ABSOLUTE);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private String path;
		private PBXSourceTree sourceTree;

		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder path(String path) {
			this.path = Objects.requireNonNull(path);
			return this;
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			this.sourceTree = Objects.requireNonNull(sourceTree);
			return this;
		}

		public PBXFileReference build() {
			return new PBXFileReference(name, Objects.requireNonNull(path, "'path' must not be null"), Objects.requireNonNull(sourceTree, "'sourceTree' must not be null"));
		}
	}
}
