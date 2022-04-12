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
package dev.nokee.ide.xcode.internal.xcodeproj;

import com.google.common.base.MoreObjects;
import com.google.common.io.Files;

import java.util.Optional;

/**
 * Reference to a concrete file.
 */
public final class PBXFileReference extends PBXReference {
    private Optional<String> explicitFileType;
    private Optional<String> lastKnownFileType;

    public PBXFileReference(String name, String path, SourceTree sourceTree) {
    	this(name, path, sourceTree, Optional.empty());
	}

    public PBXFileReference(String name, String path, SourceTree sourceTree, Optional<String> defaultType) {
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
			explicitFileType = Optional.empty();
			lastKnownFileType = Optional.of(fileType);
		} else if (fileType != null) {
			explicitFileType = Optional.of(fileType);
			lastKnownFileType = Optional.empty();
		} else {
			explicitFileType = defaultType;
			lastKnownFileType = Optional.empty();
		}
    }

    public Optional<String> getExplicitFileType() {
        return explicitFileType;
    }

    public void setExplicitFileType(Optional<String> explicitFileType) {
        this.explicitFileType = explicitFileType;
    }

	public Optional<String> getLastKnownFileType() {
		return lastKnownFileType;
	}

	@Override
    public String isa() {
        return "PBXFileReference";
    }

    @Override
    public void serializeInto(XcodeprojSerializer s) {
        super.serializeInto(s);

        if (explicitFileType.isPresent()) {
            s.addField("explicitFileType", explicitFileType.get());
        }

        if (lastKnownFileType.isPresent()) {
            s.addField("lastKnownFileType", lastKnownFileType.get());
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s explicitFileType=%s",
            super.toString(),
            getExplicitFileType());
    }
}
