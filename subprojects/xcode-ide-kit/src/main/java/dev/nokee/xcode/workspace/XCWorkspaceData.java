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
package dev.nokee.xcode.workspace;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents the Xcode workspace data, typically located in {@literal contents.xcworkspacedata} under Xcode workspace.
 *
 * @see XCWorkspaceDataReader
 * @see XCWorkspaceDataWriter
 */
@EqualsAndHashCode
public final class XCWorkspaceData implements Serializable {
	private final ImmutableSet<XCFileReference> fileRefs;

	private XCWorkspaceData(ImmutableSet<XCFileReference> fileRefs) {
		this.fileRefs = fileRefs;
	}

	public Set<XCFileReference> getFileRefs() {
		return fileRefs;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final ImmutableSet.Builder<XCFileReference> fileReferences = ImmutableSet.builder();

		public Builder fileRef(XCFileReference fileReference) {
			fileReferences.add(fileReference);
			return this;
		}

		public XCWorkspaceData build() {
			return new XCWorkspaceData(fileReferences.build());
		}
	}
}
