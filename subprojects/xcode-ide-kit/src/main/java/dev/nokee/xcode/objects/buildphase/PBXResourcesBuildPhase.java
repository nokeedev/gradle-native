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

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.CodeablePBXResourcesBuildPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;

public interface PBXResourcesBuildPhase extends PBXBuildPhase {
	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXResourcesBuildPhase>, BuildFileAwareBuilder<Builder> {
		private final List<PBXBuildFile> files = new ArrayList<>();

		@Override
		public Builder file(PBXBuildFile file) {
			files.add(Objects.requireNonNull(file, "'file' must not be null"));
			return this;
		}

		@Override
		public Builder files(Iterable<? extends PBXBuildFile> files) {
			this.files.clear();
			stream(files).map(Objects::requireNonNull).forEach(this.files::add);
			return this;
		}

		@Override
		public PBXResourcesBuildPhase build() {
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "PBXResourcesBuildPhase");
			builder.put(CodeablePBXResourcesBuildPhase.CodingKeys.files, ImmutableList.copyOf(files));

			return new CodeablePBXResourcesBuildPhase(builder.build());
		}
	}
}
