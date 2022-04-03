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
package dev.nokee.xcode;

import dev.nokee.xcode.workspace.XCFileReference;

import java.io.File;

class XCFileReferenceResolver {
	private static final String FILE_REFERENCE_GROUP_LOCATION_TAG = "group:";
	private static final String FILE_REFERENCE_ABSOLUTE_LOCATION_TAG = "absolute:";
	private final File baseDirectory;

	public XCFileReferenceResolver(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public File resolve(XCFileReference fileReference) {
		if (isRelativeToWorkspace(fileReference)) {
			return new File(baseDirectory, withoutPrefix(fileReference.getLocation()));
		} else if (isAbsolute(fileReference)) {
			return new File(withoutPrefix(fileReference.getLocation()));
		}
		throw new IllegalArgumentException(String.format("Unknown Xcode workspace file reference '%s'.", fileReference));
	}

	private boolean isRelativeToWorkspace(XCFileReference fileReferenceLocation) {
		return fileReferenceLocation.getLocation().startsWith(FILE_REFERENCE_GROUP_LOCATION_TAG);
	}

	private String withoutPrefix(String fileReference) {
		return fileReference.substring(fileReference.indexOf(':') + 1);
	}

	private boolean isAbsolute(XCFileReference fileReferenceLocation) {
		return fileReferenceLocation.getLocation().startsWith(FILE_REFERENCE_ABSOLUTE_LOCATION_TAG);
	}
}
