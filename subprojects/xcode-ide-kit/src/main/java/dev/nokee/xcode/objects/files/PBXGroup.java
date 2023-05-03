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

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.project.CodeablePBXGroup;
import dev.nokee.xcode.project.CodeableXCVersionGroup;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import java.util.List;

import static com.google.common.collect.Streams.stream;
import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * A collection of files in Xcode's virtual filesystem hierarchy.
 */
public interface PBXGroup extends GroupChild {
	// It seems the name or path can be null but not both which is a bit different from PBXFileReference.
	//   Except for the mainGroup which both the name and path is null

	List<GroupChild> getChildren();

	Builder toBuilder();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXGroup>,  LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder().knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXGroup.CodingKeys.values()));
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder;
			builder.put(KeyedCoders.ISA, "PBXGroup");
			builder.requires(key(CodeablePBXGroup.CodingKeys.sourceTree));
			// mainGroup can have both null name and path

			// Default values
			builder.ifAbsent(CodeablePBXGroup.CodingKeys.sourceTree, PBXSourceTree.GROUP);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeablePBXGroup.CodingKeys.name, name);
			return this;
		}

		public Builder path(String path) {
			builder.put(CodeablePBXGroup.CodingKeys.path, path);
			return this;
		}

		public Builder child(GroupChild reference) {
			assertValid(reference);
			builder.add(CodeablePBXGroup.CodingKeys.children, reference);
			return this;
		}

		private void assertValid(GroupChild reference) {
			if (!reference.getName().isPresent() && !reference.getPath().isPresent()) {
				throw new NullPointerException("either 'name' or 'path' must not be null for non-main PBXGroup");
			}
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			builder.put(CodeablePBXGroup.CodingKeys.sourceTree, sourceTree);
			return this;
		}

		public Builder children(Iterable<? extends GroupChild> references) {
			builder.put(CodeablePBXGroup.CodingKeys.children, stream(references).peek(this::assertValid).collect(ImmutableList.toImmutableList()));
			return this;
		}

		@Override
		public PBXGroup build() {
			return new CodeablePBXGroup(builder.build());
		}
	}
}
