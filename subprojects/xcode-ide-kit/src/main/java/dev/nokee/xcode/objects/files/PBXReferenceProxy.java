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

import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.project.CodeablePBXReferenceProxy;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

/**
 * A proxy for another object which might belong to another project contained in the same workspace of the document.
 */
public interface PBXReferenceProxy extends PBXReference, PBXBuildFile.FileReference, GroupChild {
	PBXContainerItemProxy getRemoteReference();

	String getFileType();

	@Override
	default void accept(PBXBuildFile.FileReference.Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	default void accept(GroupChild.Visitor visitor) {
		visitor.visit(this);
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXReferenceProxy>, LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder()
			.knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXReferenceProxy.CodingKeys.values());

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXReferenceProxy");
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeablePBXReferenceProxy.CodingKeys.name, name);
			return this;
		}

		public Builder path(String path) {
			builder.put(CodeablePBXReferenceProxy.CodingKeys.path, path);
			return this;
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			builder.put(CodeablePBXReferenceProxy.CodingKeys.sourceTree, sourceTree);
			return this;
		}

		public Builder remoteReference(PBXContainerItemProxy remoteReference) {
			builder.put(CodeablePBXReferenceProxy.CodingKeys.remoteRef, remoteReference);
			return this;
		}

		public Builder fileType(String fileType) {
			builder.put(CodeablePBXReferenceProxy.CodingKeys.fileType, fileType);
			return this;
		}

		@Override
		public PBXReferenceProxy build() {
			return new CodeablePBXReferenceProxy(builder.build());
		}
	}
}
