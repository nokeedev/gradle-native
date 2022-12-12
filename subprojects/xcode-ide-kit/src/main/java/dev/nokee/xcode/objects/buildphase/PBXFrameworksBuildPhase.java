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
import dev.nokee.xcode.project.CodeablePBXFrameworksBuildPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;

public interface PBXFrameworksBuildPhase extends PBXBuildPhase {
	// Xcode maps Optional/Required status via {@link PBXBuildFile#getSettings()}
	//   i.e. settings = {ATTRIBUTES = (Weak)}
	//   We can use utility methods to extract the "optional" (Weak) vs "required" (<nothing>) status.

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXFrameworksBuildPhase> {
		private final List<PBXBuildFile> files = new ArrayList<>();

		public Builder files(Iterable<? extends PBXBuildFile> files) {
			this.files.clear();
			stream(files).map(Objects::requireNonNull).forEach(this.files::add);
			return this;
		}

		@Override
		public PBXFrameworksBuildPhase build() {
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "PBXFrameworksBuildPhase");
			builder.put(CodeablePBXFrameworksBuildPhase.CodingKeys.files, ImmutableList.copyOf(files));

			return new CodeablePBXFrameworksBuildPhase(builder.build());
		}
	}
}
