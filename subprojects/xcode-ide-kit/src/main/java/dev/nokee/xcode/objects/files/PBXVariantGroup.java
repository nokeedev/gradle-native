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
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.project.CodeablePBXVariantGroup;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.List;

import static com.google.common.collect.Streams.stream;
import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Represents localized resources.
 */
public interface PBXVariantGroup extends PBXReference, GroupChild, PBXBuildFile.FileReference {
	List<GroupChild> getChildren();

	@Override
	default <R> R accept(PBXBuildFile.FileReference.Visitor<R> visitor) {
		return visitor.visit(this);
	}

	@Override
	default <R> R accept(GroupChild.Visitor<R> visitor) {
		return visitor.visit(this);
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXVariantGroup>, LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder()
			.knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXVariantGroup.CodingKeys.values());

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXVariantGroup");
			builder.requires(key(CodeablePBXVariantGroup.CodingKeys.sourceTree));
			// mainGroup can have both null name and path

			// Default values
			builder.ifAbsent(CodeablePBXVariantGroup.CodingKeys.sourceTree, PBXSourceTree.GROUP);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeablePBXVariantGroup.CodingKeys.name, name);
			return this;
		}

		public Builder path(String path) {
			builder.put(CodeablePBXVariantGroup.CodingKeys.path, path);
			return this;
		}

		public Builder child(GroupChild reference) {
			assertValid(reference);
			builder.add(CodeablePBXVariantGroup.CodingKeys.children, reference);
			return this;
		}

		private void assertValid(GroupChild reference) {
			if (!reference.getName().isPresent() && !reference.getPath().isPresent()) {
				throw new NullPointerException("either 'name' or 'path' must not be null for non-main PBXGroup");
			}
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			builder.put(CodeablePBXVariantGroup.CodingKeys.sourceTree, sourceTree);
			return this;
		}

		public Builder children(Iterable<? extends GroupChild> references) {
			builder.put(CodeablePBXVariantGroup.CodingKeys.children, stream(references).peek(this::assertValid).collect(ImmutableList.toImmutableList()));
			return this;
		}

		@Override
		public PBXVariantGroup build() {
			return new CodeablePBXVariantGroup(builder.build());
		}
	}
}
