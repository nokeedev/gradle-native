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

import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.project.CodeablePBXFrameworksBuildPhase;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

public interface PBXFrameworksBuildPhase extends PBXBuildPhase {
	// Xcode maps Optional/Required status via {@link PBXBuildFile#getSettings()}
	//   i.e. settings = {ATTRIBUTES = (Weak)}
	//   We can use utility methods to extract the "optional" (Weak) vs "required" (<nothing>) status.

	@Override
	default <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}

	@Override
	Builder toBuilder();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXFrameworksBuildPhase>, BuildFileAwareBuilder<Builder>, LenientAwareBuilder<Builder>, PBXBuildPhase.Builder {
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder().knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXFrameworksBuildPhase.CodingKeys.values()));
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder;
			builder.put(KeyedCoders.ISA, "PBXFrameworksBuildPhase");
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		@Override
		public Builder file(PBXBuildFile file) {
			builder.add(CodeablePBXFrameworksBuildPhase.CodingKeys.files, file);
			return this;
		}

		@Override
		public Builder files(Iterable<? extends PBXBuildFile> files) {
			builder.put(CodeablePBXFrameworksBuildPhase.CodingKeys.files, files);
			return this;
		}

		@Override
		public PBXFrameworksBuildPhase build() {
			return new CodeablePBXFrameworksBuildPhase(builder.build());
		}
	}
}
