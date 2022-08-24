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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;

public final class PBXFrameworksBuildPhase extends PBXBuildPhase {
	private PBXFrameworksBuildPhase(ImmutableList<PBXBuildFile> files) {
		super(files);
	}

	// Xcode maps Optional/Required status via {@link PBXBuildFile#getSettings()}
	//   i.e. settings = {ATTRIBUTES = (Weak)}
	//   We can use utility methods to extract the "optional" (Weak) vs "required" (<nothing>) status.

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final List<PBXBuildFile> files = new ArrayList<>();

		public Builder files(Iterable<? extends PBXBuildFile> files) {
			this.files.clear();
			stream(files).map(Objects::requireNonNull).forEach(this.files::add);
			return this;
		}

		public PBXFrameworksBuildPhase build() {
			return new PBXFrameworksBuildPhase(ImmutableList.copyOf(files));
		}
	}
}
