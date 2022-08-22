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
package dev.nokee.xcode.objects.targets;

import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;

import javax.annotation.Nullable;

/**
 * A proxy for another object which might belong to another project contained in the same workspace of the document.
 */
public final class PBXReferenceProxy extends PBXReference {
	private final PBXContainerItemProxy remoteReference;
	private final String fileType;

	public PBXReferenceProxy(@Nullable String name, @Nullable String path, PBXSourceTree sourceTree, PBXContainerItemProxy remoteReference, String fileType) {
		super(name, path, sourceTree);
		this.remoteReference = remoteReference;
		this.fileType = fileType;
	}

	public PBXContainerItemProxy getRemoteReference() {
		return remoteReference;
	}

	public String getFileType() {
		return fileType;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private String path;
		private PBXSourceTree sourceTree;
		private PBXContainerItemProxy remoteReference;
		private String fileType;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			this.sourceTree = sourceTree;
			return this;
		}

		public Builder remoteReference(PBXContainerItemProxy remoteReference) {
			this.remoteReference = remoteReference;
			return this;
		}

		public Builder fileType(String fileType) {
			this.fileType = fileType;
			return this;
		}

		public PBXReferenceProxy build() {
			return new PBXReferenceProxy(name, path, sourceTree, remoteReference, fileType);
		}
	}
}
