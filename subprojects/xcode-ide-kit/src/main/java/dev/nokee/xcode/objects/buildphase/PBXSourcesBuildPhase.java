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
import com.google.common.collect.Streams;
import dev.nokee.xcode.objects.targets.PBXTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;

/**
 * Lists the files to be compiled for the containing {@link PBXTarget}.
 *
 * A target should contain at most one of this build phase.
 */
public final class PBXSourcesBuildPhase extends PBXBuildPhase {

	private PBXSourcesBuildPhase(ImmutableList<PBXBuildFile> files) {
		super(files);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final List<PBXBuildFile> files = new ArrayList<>();

		public Builder file(PBXBuildFile file) {
			files.add(Objects.requireNonNull(file));
			return this;
		}

		public Builder files(Iterable<? extends PBXBuildFile> files) {
			this.files.clear();
			stream(files).map(Objects::requireNonNull).forEach(this.files::add);
			return this;
		}

		public PBXSourcesBuildPhase build() {
			return new PBXSourcesBuildPhase(ImmutableList.copyOf(files));
		}
	}
}
