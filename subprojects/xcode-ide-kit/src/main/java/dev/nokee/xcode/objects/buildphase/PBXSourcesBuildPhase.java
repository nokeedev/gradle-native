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
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.project.CodeablePBXSourcesBuildPhase;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

/**
 * Lists the files to be compiled for the containing {@link PBXTarget}.
 *
 * <p>A target should contain at most one of this build phase.
 */
public interface PBXSourcesBuildPhase extends PBXBuildPhase {
	@Override
	Builder toBuilder();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXSourcesBuildPhase>, BuildFileAwareBuilder<Builder>, LenientAwareBuilder<Builder>, PBXBuildPhase.Builder {
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder());
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder;
			builder.put(KeyedCoders.ISA, "PBXSourcesBuildPhase");
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		@Override
		public Builder file(PBXBuildFile file) {
			builder.add(CodeablePBXSourcesBuildPhase.CodingKeys.files, file);
			return this;
		}

		@Override
		public Builder files(Iterable<? extends PBXBuildFile> files) {
			builder.put(CodeablePBXSourcesBuildPhase.CodingKeys.files, files);
			return this;
		}

		@Override
		public PBXSourcesBuildPhase build() {
			return new CodeablePBXSourcesBuildPhase(builder.build());
		}
	}
}
