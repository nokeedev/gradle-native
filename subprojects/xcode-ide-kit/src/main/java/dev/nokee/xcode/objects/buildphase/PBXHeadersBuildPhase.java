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
import dev.nokee.xcode.project.CodeablePBXHeadersBuildPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;

public interface PBXHeadersBuildPhase extends PBXBuildPhase {
	// Xcode maps Public/Private/Project headers via {@link PBXBuildFile#getSettings()}
	//   i.e. settings = {ATTRIBUTES = (Project, Public)}
	//   We can use utility methods to extract the "project" vs "public" vs "private" headers.

	static Builder builder() {
		return new Builder();
	}

	final class Builder {
		private final List<PBXBuildFile> files = new ArrayList<>();

		public Builder files(Iterable<? extends PBXBuildFile> files) {
			this.files.clear();
			stream(files).map(Objects::requireNonNull).forEach(this.files::add);
			return this;
		}

		public PBXHeadersBuildPhase build() {
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "PBXHeadersBuildPhase");
			builder.put(CodeablePBXHeadersBuildPhase.CodingKeys.files, ImmutableList.copyOf(files));

			return new CodeablePBXHeadersBuildPhase(builder.build());
		}
	}
}
